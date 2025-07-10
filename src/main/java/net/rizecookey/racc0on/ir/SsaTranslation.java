package net.rizecookey.racc0on.ir;

import net.rizecookey.racc0on.ir.memory.MemoryType;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.Phi;
import net.rizecookey.racc0on.ir.optimize.Optimizer;
import net.rizecookey.racc0on.ir.util.DebugInfo;
import net.rizecookey.racc0on.ir.util.DebugInfoHelper;
import net.rizecookey.racc0on.ir.util.NodeSupport;
import net.rizecookey.racc0on.lexer.OperatorType;
import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.FieldDeclarationTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.ast.ParameterTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.parser.ast.StatementTree;
import net.rizecookey.racc0on.parser.ast.StructDeclarationTree;
import net.rizecookey.racc0on.parser.ast.Tree;
import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.parser.ast.call.AllocArrayCallTree;
import net.rizecookey.racc0on.parser.ast.call.AllocCallTree;
import net.rizecookey.racc0on.parser.ast.call.BuiltinCallTree;
import net.rizecookey.racc0on.parser.ast.call.FunctionCallTree;
import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.control.ReturnTree;
import net.rizecookey.racc0on.parser.ast.control.WhileTree;
import net.rizecookey.racc0on.parser.ast.exp.BinaryOperationTree;
import net.rizecookey.racc0on.parser.ast.exp.BoolLiteralTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpDereferenceTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpFieldAccessTree;
import net.rizecookey.racc0on.parser.ast.exp.IdentExpressionTree;
import net.rizecookey.racc0on.parser.ast.exp.IntLiteralTree;
import net.rizecookey.racc0on.parser.ast.exp.PointerLiteralTree;
import net.rizecookey.racc0on.parser.ast.exp.TernaryExpressionTree;
import net.rizecookey.racc0on.parser.ast.exp.UnaryOperationTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueDereferenceTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueFieldAccessTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueIdentTree;
import net.rizecookey.racc0on.parser.ast.simp.AssignmentTree;
import net.rizecookey.racc0on.parser.ast.simp.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.simp.SimpleStatementTree;
import net.rizecookey.racc0on.parser.symbol.Name;
import net.rizecookey.racc0on.parser.type.ArrayType;
import net.rizecookey.racc0on.parser.type.PointerType;
import net.rizecookey.racc0on.parser.type.SmallType;
import net.rizecookey.racc0on.parser.type.StructType;
import net.rizecookey.racc0on.parser.type.Type;
import net.rizecookey.racc0on.parser.visitor.NoOpVisitor;
import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.semantic.SemanticInformation;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/// SSA translation as described in
/// [`Simple and Efficient Construction of Static Single Assignment Form`](https://compilers.cs.uni-saarland.de/papers/bbhlmz13cc.pdf).
///
/// This implementation also tracks side effect edges that can be used to avoid reordering of operations that cannot be
/// reordered.
///
/// We recommend to read the paper to better understand the mechanics implemented here.
public class SsaTranslation {
    private final FunctionTree function;
    private final GraphConstructor constructor;
    private final SemanticInformation semanticInfo;

    public SsaTranslation(FunctionTree function, Optimizer optimizer, SemanticInformation semanticInfo) {
        this.function = function;
        this.constructor = new GraphConstructor(optimizer, function.name().name().asString());
        this.semanticInfo = semanticInfo;
    }

    public IrGraph translate() {
        var visitor = new SsaTranslationVisitor();
        this.function.accept(visitor, this);
        return this.constructor.graph();
    }

    private void writeVariable(Name variable, Block block, Node value) {
        this.constructor.writeVariable(variable, block, value);
    }

    private Node readVariable(Name variable, Block block) {
        return this.constructor.readVariable(variable, block);
    }

    private Block currentBlock() {
        return this.constructor.currentBlock();
    }

    private static class SsaTranslationVisitor implements Visitor<SsaTranslation, Optional<Node>> {

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private static final Optional<Node> NOT_AN_EXPRESSION = Optional.empty();

        private final Deque<DebugInfo> debugStack = new ArrayDeque<>();
        private final Deque<LoopInfo> transformerStack = new ArrayDeque<>();

        private CancellationRange currentCancellation = CancellationRange.NONE;

        private MemoryType toMemoryType(Type type, SsaTranslation data) {
            return switch (type) {
                case SmallType small -> new MemoryType.Value(small.toIrType());
                case StructType struct -> {
                    StructDeclarationTree declaration = data.semanticInfo.structs().get(struct.name());
                    if (declaration == null) {
                        throw new IllegalStateException("unknown struct " + struct.name().asString());
                    }

                    yield new MemoryType.Compound(declaration.fields().stream()
                            .map(field -> toMemoryType(field.type().type(), data))
                            .toList());
                }
                case Type.Wildcard _ -> throw new IllegalStateException("Illegal type");
            };
        }

        private StructDeclarationTree getStructDeclaration(Name structName, SsaTranslation data) {
            StructDeclarationTree declaration = data.semanticInfo.structs().get(structName);

            if (declaration == null) {
                throw new IllegalStateException("Unknown struct " + structName.asString());
            }

            return declaration;
        }

        private FunctionTree getFunctionDeclaration(Name functionName, SsaTranslation data) {
            FunctionTree declaration = data.semanticInfo.functions().get(functionName);

            if (declaration == null) {
                throw new IllegalStateException("Unknown function " + functionName.asString());
            }

            return declaration;
        }

        private enum CancellationRange {
            NONE, LOOP, RETURN
        }

        private void resetInstructionCancellation(CancellationRange range) {
            if (range.ordinal() >= currentCancellation.ordinal()) {
                currentCancellation = CancellationRange.NONE;
            }
        }

        private void resetInstructionCancellation() {
            resetInstructionCancellation(CancellationRange.values()[CancellationRange.values().length - 1]);
        }

        private void cancelFurtherInstructions(CancellationRange range) {
            if (range.ordinal() > currentCancellation.ordinal()) {
                currentCancellation = range;
            }
        }

        private boolean instructionsCancelled() {
            return currentCancellation != CancellationRange.NONE;
        }

        private CancellationRange instructionCancellationRange() {
            return currentCancellation;
        }

        private void pushSpan(Tree tree) {
            this.debugStack.push(DebugInfoHelper.getDebugInfo());
            DebugInfoHelper.setDebugInfo(new DebugInfo.SourceInfo(tree.span()));
        }

        private void popSpan() {
            DebugInfoHelper.setDebugInfo(this.debugStack.pop());
        }

        private @Nullable BinaryOperator<Node> desugarer(SsaTranslation data, OperatorType type) {
            if (!(type instanceof OperatorType.Assignment binType)) {
                throw new IllegalArgumentException("not an assignment operator " + type);
            }
            return switch (binType) {
                case OperatorType.Assignment.MINUS -> data.constructor::newSub;
                case OperatorType.Assignment.PLUS -> data.constructor::newAdd;
                case OperatorType.Assignment.MUL -> data.constructor::newMul;
                case OperatorType.Assignment.DIV ->
                        (lhs, rhs) -> projResultSideEffectCause(data, data.constructor.newDiv(lhs, rhs));
                case OperatorType.Assignment.MOD ->
                        (lhs, rhs) -> projResultSideEffectCause(data, data.constructor.newMod(lhs, rhs));

                case OperatorType.Assignment.BITWISE_AND -> data.constructor::newBitwiseAnd;
                case OperatorType.Assignment.BITWISE_XOR -> data.constructor::newBitwiseXor;
                case OperatorType.Assignment.BITWISE_OR -> data.constructor::newBitwiseOr;

                case OperatorType.Assignment.SHIFT_LEFT -> data.constructor::newShiftLeft;
                case OperatorType.Assignment.SHIFT_RIGHT -> data.constructor::newShiftRight;

                case OperatorType.Assignment.DEFAULT -> null;
            };
        }

        @Override
        public Optional<Node> visit(AssignmentTree assignmentTree, SsaTranslation data) {
            pushSpan(assignmentTree);
            BinaryOperator<Node> desugar = desugarer(data, assignmentTree.type());

            Node rhs = assignmentTree.expression().accept(this, data).orElseThrow();
            switch (assignmentTree.lValue()) {
                case LValueIdentTree(var name) -> {
                    if (desugar != null) {
                        rhs = desugar.apply(data.readVariable(name.name(), data.currentBlock()), rhs);
                    }
                    data.writeVariable(name.name(), data.currentBlock(), rhs);
                }
                case LValueArrayAccessTree arrayAccess -> {
                    ArrayType<?> type = (ArrayType<?>) data.semanticInfo.accessTypes().get(arrayAccess.array());
                    Node address = createArrayAccessAddressCalculation(arrayAccess.array(), type, arrayAccess.index(), data);
                    projResultSideEffectCause(data, data.constructor.newStore(rhs, address));
                }
                case LValueDereferenceTree dereference -> {
                    Node address = dereference.pointer().accept(this, data).orElseThrow();
                    projResultSideEffectCause(data, data.constructor.newStore(rhs, address));
                }
                case LValueFieldAccessTree fieldAccess -> {
                    StructType structType = (StructType) data.semanticInfo.accessTypes().get(fieldAccess.struct());
                    int memberIndex = getMemberIndex(getStructDeclaration(structType.name(), data),
                            fieldAccess.fieldName().name());
                    Node address = createFieldAccessAddressCalculation(fieldAccess.struct(), structType, memberIndex, data);
                    projResultSideEffectCause(data, data.constructor.newStore(rhs, address));
                }
            }
            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(BinaryOperationTree binaryOperationTree, SsaTranslation data) {
            OperatorType.Binary type = binaryOperationTree.operatorType();
            if (type == OperatorType.Binary.AND) {
                return Optional.of(transformAndToTernary(data, binaryOperationTree));
            } else if (type == OperatorType.Binary.OR) {
                return Optional.of(transformOrToTernary(data, binaryOperationTree));
            }

            pushSpan(binaryOperationTree);

            Node lhs = binaryOperationTree.lhs().accept(this, data).orElseThrow();
            Node rhs = binaryOperationTree.rhs().accept(this, data).orElseThrow();
            Node res = switch (binaryOperationTree.operatorType()) {
                case MINUS -> data.constructor.newSub(lhs, rhs);
                case PLUS -> data.constructor.newAdd(lhs, rhs);
                case MUL -> data.constructor.newMul(lhs, rhs);
                case DIV -> projResultSideEffectCause(data, data.constructor.newDiv(lhs, rhs));
                case MOD -> projResultSideEffectCause(data, data.constructor.newMod(lhs, rhs));
                case BITWISE_AND -> data.constructor.newBitwiseAnd(lhs, rhs);
                case BITWISE_XOR -> data.constructor.newBitwiseXor(lhs, rhs);
                case BITWISE_OR -> data.constructor.newBitwiseOr(lhs, rhs);
                case SHIFT_LEFT -> data.constructor.newShiftLeft(lhs, rhs);
                case SHIFT_RIGHT -> data.constructor.newShiftRight(lhs, rhs);
                case LESS_THAN -> data.constructor.newLessThan(lhs, rhs);
                case LESS_OR_EQUAL -> data.constructor.newLessOrEqual(lhs, rhs);
                case GREATER_THAN -> data.constructor.newGreaterThan(lhs, rhs);
                case GREATER_OR_EQUAL -> data.constructor.newGreaterOrEqual(lhs, rhs);
                case EQUAL -> data.constructor.newEqual(lhs, rhs);
                case NOT_EQUAL -> data.constructor.newNotEqual(lhs, rhs);
                default -> throw new IllegalStateException("Unexpected value: " + binaryOperationTree.operatorType());
            };
            popSpan();
            return Optional.of(res);
        }

        private Node transformOrToTernary(SsaTranslation data, BinaryOperationTree tree) {
            if (tree.operatorType() != OperatorType.Binary.OR) {
                throw new IllegalArgumentException("Expected AND binary operation tree");
            }
            TernaryExpressionTree ternary = new TernaryExpressionTree(tree.lhs(), new BoolLiteralTree(true, tree.span()),
                    tree.rhs());

            return ternary.accept(this, data).orElseThrow();
        }

        private Node transformAndToTernary(SsaTranslation data, BinaryOperationTree tree) {
            if (tree.operatorType() != OperatorType.Binary.AND) {
                throw new IllegalArgumentException("Expected OR binary operation tree");
            }
            TernaryExpressionTree ternary = new TernaryExpressionTree(tree.lhs(), tree.rhs(),
                    new BoolLiteralTree(false, tree.span()));

            return ternary.accept(this, data).orElseThrow();
        }

        @Override
        public Optional<Node> visit(BlockTree blockTree, SsaTranslation data) {
            pushSpan(blockTree);
            for (StatementTree statement : blockTree.statements()) {
                // skip everything after a return, continue or break in a block
                if (instructionsCancelled()) {
                    break;
                }
                statement.accept(this, data);
            }
            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(DeclarationTree declarationTree, SsaTranslation data) {
            pushSpan(declarationTree);
            if (declarationTree.initializer() != null) {
                Node rhs = declarationTree.initializer().accept(this, data).orElseThrow();
                data.writeVariable(declarationTree.name().name(), data.currentBlock(), rhs);
            }
            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(FunctionTree functionTree, SsaTranslation data) {
            pushSpan(functionTree);
            Node start = data.constructor.newStart();
            data.constructor.writeCurrentSideEffect(data.constructor.newSideEffectProj(start));
            functionTree.parameters().forEach(p -> p.accept(this, data));
            functionTree.body().accept(this, data);
            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(IdentExpressionTree identExpressionTree, SsaTranslation data) {
            pushSpan(identExpressionTree);
            Node value = data.readVariable(identExpressionTree.name().name(), data.currentBlock());
            popSpan();
            return Optional.of(value);
        }

        @Override
        public Optional<Node> visit(IntLiteralTree intLiteralTree, SsaTranslation data) {
            pushSpan(intLiteralTree);
            Node node = data.constructor.newConstInt((int) intLiteralTree.parseValue().orElseThrow());
            popSpan();
            return Optional.of(node);
        }

        @Override
        public Optional<Node> visit(BoolLiteralTree boolLiteralTree, SsaTranslation data) {
            pushSpan(boolLiteralTree);
            Node node = data.constructor.newConstBool(boolLiteralTree.value());
            popSpan();
            return Optional.of(node);
        }

        @Override
        public Optional<Node> visit(LValueIdentTree lValueIdentTree, SsaTranslation data) {
            return Optional.of(data.readVariable(lValueIdentTree.name().name(), data.currentBlock()));
        }

        @Override
        public Optional<Node> visit(NameTree nameTree, SsaTranslation data) {
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(UnaryOperationTree unaryOperationTree, SsaTranslation data) {
            pushSpan(unaryOperationTree);
            Node node = unaryOperationTree.expression().accept(this, data).orElseThrow();
            Node res = switch (unaryOperationTree.type()) {
                case NEGATION -> data.constructor.newSub(data.constructor.newConstInt(0), node);
                case NOT -> data.constructor.newNot(node);
                case BITWISE_NOT -> data.constructor.newBitwiseXor(data.constructor.newConstInt(0xFFFFFFFF), node);
            };
            popSpan();
            return Optional.of(res);
        }

        @Override
        public Optional<Node> visit(ProgramTree programTree, SsaTranslation data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Node> visit(ReturnTree returnTree, SsaTranslation data) {
            pushSpan(returnTree);
            Node node = returnTree.expression().accept(this, data).orElseThrow();
            Node ret = data.constructor.newReturn(node);
            data.constructor.graph().endBlock().addPredecessor(ret);
            cancelFurtherInstructions(CancellationRange.RETURN);
            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(TypeTree typeTree, SsaTranslation data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Node> visit(IfElseTree ifElseTree, SsaTranslation data) {
            pushSpan(ifElseTree);

            Node condition = ifElseTree.condition().accept(this, data).orElseThrow();
            Node ifNode = data.constructor.newIf(condition);
            Node trueProj = data.constructor.newIfTrueProj(ifNode);
            Node falseProj = data.constructor.newIfFalseProj(ifNode);
            Block trueBranch = data.constructor.newBlock(trueProj);
            data.constructor.sealBlock(trueBranch);
            ifElseTree.thenBranch().accept(this, data);
            CancellationRange trueBranchCancellationRange = instructionCancellationRange();
            resetInstructionCancellation();
            Node trueJump = data.constructor.hasUnconditionalExit() ? null : data.constructor.newJump();

            Block falseBranch = data.constructor.newBlock(falseProj);
            data.constructor.sealBlock(falseBranch);
            CancellationRange falseBranchCancellationRange = CancellationRange.NONE;
            if (ifElseTree.elseBranch() != null) {
                ifElseTree.elseBranch().accept(this, data);
                falseBranchCancellationRange = instructionCancellationRange();
                resetInstructionCancellation();
            }
            Node falseJump = data.constructor.hasUnconditionalExit() ? null : data.constructor.newJump();

            if (trueJump != null || falseJump != null) {
                Block followingBlock = data.constructor.newBlock(Stream.of(trueJump, falseJump).filter(Objects::nonNull).toList());
                data.constructor.sealBlock(followingBlock);
            }

            cancelFurtherInstructions(CancellationRange.values()[Math.min(
                    trueBranchCancellationRange.ordinal(),
                    falseBranchCancellationRange.ordinal()
            )]);

            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(WhileTree whileTree, SsaTranslation data) {
            ForTree transformed = new ForTree(null, whileTree.condition(), whileTree.body(), null, whileTree.start());
            return visit(transformed, data);
        }

        @Override
        public Optional<Node> visit(ForTree forTree, SsaTranslation data) {
            pushSpan(forTree);

            if (forTree.initializer() != null) {
                forTree.initializer().accept(this, data);
            }
            Node jumpFromPrevious = data.constructor.newJump();

            Block conditionBlock = data.constructor.newBlock(jumpFromPrevious);
            Node ifNode = data.constructor.newIf(forTree.condition().accept(this, data).orElseThrow());
            Node ifTrue = data.constructor.newIfTrueProj(ifNode);
            Node ifFalse = data.constructor.newIfFalseProj(ifNode);

            Block exitBlock = data.constructor.newBlock(ifFalse);
            data.constructor.sealBlock(exitBlock);
            Node jumpToFollowing = data.constructor.newJump();

            Block followingBlock = data.constructor.newBlock(jumpToFollowing);

            Block bodyBlock = data.constructor.newBlock(ifTrue);
            data.constructor.sealBlock(bodyBlock);
            transformerStack.push(new LoopInfo(conditionBlock, followingBlock, forTree.step()));
            forTree.body().accept(this, data);
            resetInstructionCancellation();
            transformerStack.pop();
            if (!data.constructor.hasUnconditionalExit()) {
                if (forTree.step() != null) {
                    forTree.step().accept(this, data);
                }
                Node jumpToCondition = data.constructor.newJump();
                conditionBlock.addPredecessor(jumpToCondition);
            }

            data.constructor.sealBlock(conditionBlock);

            data.constructor.setCurrentBlock(followingBlock);
            data.constructor.sealBlock(followingBlock);

            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(LoopControlTree loopControlTree, SsaTranslation data) {
            pushSpan(loopControlTree);
            if (transformerStack.isEmpty()) {
                throw new IllegalStateException("no corresponding loop statement for " + loopControlTree.type().keyword());
            }
            LoopInfo loop = transformerStack.peek();

            Node target = switch (loopControlTree.type()) {
                case CONTINUE -> {
                    if (loop.step() != null) {
                        loop.step().accept(this, data);
                    }
                    yield loop.continueTarget();
                }
                case BREAK -> loop.breakTarget();
            };
            Node jump = data.constructor.newJump();
            target.addPredecessor(jump);

            cancelFurtherInstructions(CancellationRange.LOOP);

            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(TernaryExpressionTree ternaryExpressionTree, SsaTranslation data) {
            pushSpan(ternaryExpressionTree);

            Node condition = ternaryExpressionTree.condition().accept(this, data).orElseThrow();
            Node ifNode = data.constructor.newIf(condition);
            Node ifTrueJump = data.constructor.newIfTrueProj(ifNode);
            Node ifFalseJump = data.constructor.newIfFalseProj(ifNode);

            Block ifTrueBlock = data.constructor.newBlock(ifTrueJump);
            data.constructor.sealBlock(ifTrueBlock);
            Node ifTrue = ternaryExpressionTree.ifBranch().accept(this, data).orElseThrow();
            Node ifTrueExitJump = data.constructor.newJump();

            Block ifFalseBlock = data.constructor.newBlock(ifFalseJump);
            data.constructor.sealBlock(ifFalseBlock);
            Node ifFalse = ternaryExpressionTree.elseBranch().accept(this, data).orElseThrow();
            Node ifFalseExitJump = data.constructor.newJump();

            Block followingBlock = data.constructor.newBlock(ifTrueExitJump, ifFalseExitJump);
            data.constructor.sealBlock(followingBlock);

            Phi res = data.constructor.newPhi();
            res.appendOperand(ifTrue);
            res.appendOperand(ifFalse);

            popSpan();
            return Optional.of(res);
        }

        @Override
        public Optional<Node> visit(ParameterTree parameterTree, SsaTranslation data) {
            pushSpan(parameterTree);
            SmallType type = (SmallType) parameterTree.type().type();
            Node parameter = data.constructor.newParameter(parameterTree.index(), type.toIrType());
            data.writeVariable(parameterTree.name().name(), data.currentBlock(), parameter);
            popSpan();
            return Optional.of(parameter);
        }

        @Override
        public Optional<Node> visit(FunctionCallTree functionCallTree, SsaTranslation data) {
            pushSpan(functionCallTree);
            Node[] args = functionCallTree.arguments().stream()
                    .map(arg -> arg.accept(this, data).orElseThrow())
                    .toArray(Node[]::new);
            Name name = functionCallTree.name().name();
            SmallType returnType = (SmallType) getFunctionDeclaration(name, data).returnType().type();
            Node result = projResultSideEffectCause(
                    data,
                    data.constructor.newCall(functionCallTree.name().name().asString(), returnType.toIrType(), args)
            );
            popSpan();
            return Optional.of(result);
        }

        @Override
        public Optional<Node> visit(BuiltinCallTree builtinCallTree, SsaTranslation data) {
            pushSpan(builtinCallTree);
            Node[] args = builtinCallTree.arguments().stream()
                    .map(arg -> arg.accept(this, data).orElseThrow())
                    .toArray(Node[]::new);
            Node result = projResultSideEffectCause(
                    data,
                    data.constructor.newBuiltinCall(builtinCallTree.type().keyword(), args)
            );
            popSpan();
            return Optional.of(result);
        }

        @Override
        public Optional<Node> visit(StructDeclarationTree structDeclarationTree, SsaTranslation data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Node> visit(FieldDeclarationTree fieldDeclarationTree, SsaTranslation data) {
            throw new UnsupportedOperationException();
        }

        private Node createArrayAccessAddressCalculation(Tree arrayTree, ArrayType<? extends Type> arrayType, Tree indexTree, SsaTranslation data) {
            Node array = arrayTree.accept(this, data).orElseThrow();
            MemoryType arrayMemoryType = toMemoryType(arrayType, data);

            Node index = indexTree.accept(this, data).orElseThrow();
            Node offset = data.constructor.newArrayMemberOffset(arrayMemoryType, index);
            return data.constructor.newAdd(array, offset);
        }

        private Optional<Node> visitArrayAccess(Tree arrayTree, Tree indexTree, SsaTranslation data) {
            pushSpan(arrayTree);
            ArrayType<?> type = (ArrayType<?>) data.semanticInfo.accessTypes().get(arrayTree);
            Node address = createArrayAccessAddressCalculation(arrayTree, type, indexTree, data);
            if (!(type.type() instanceof SmallType smallType)) {
                return Optional.of(address);
            }

            Node load = projResultSideEffectCause(data, data.constructor.newLoad(address, smallType.toIrType()));
            popSpan();
            return Optional.of(load);
        }

        private Optional<Node> visitDereference(Tree pointerTree, SsaTranslation data) {
            pushSpan(pointerTree);
            Node pointer = pointerTree.accept(this, data).orElseThrow();
            PointerType<?> type = (PointerType<?>) data.semanticInfo.accessTypes().get(pointerTree);

            if (!(type.type() instanceof SmallType smallType)) {
                return Optional.of(pointer);
            }

            Node load = projResultSideEffectCause(data, data.constructor.newLoad(pointer, smallType.toIrType()));
            popSpan();
            return Optional.of(load);
        }

        private int getMemberIndex(StructDeclarationTree struct, Name field) {
            for (int i = 0; i < struct.fields().size(); i++) {
                if (struct.fields().get(i).name().name().equals(field)) {
                    return i;
                }
            }

            throw new IllegalStateException("Struct " + struct.name().name().asString() + " does not have a field called " + field.asString());
        }

        private Node createFieldAccessAddressCalculation(Tree structTree, StructType structType, int memberIndex, SsaTranslation data) {
            Node struct = structTree.accept(this, data).orElseThrow();
            MemoryType.Compound memoryType = (MemoryType.Compound) toMemoryType(structType, data);

            Node offset = data.constructor.newStructMemberOffset(memoryType, memberIndex);
            return data.constructor.newAdd(struct, offset);
        }

        private Optional<Node> visitFieldAccess(Tree structTree, Name fieldName, SsaTranslation data) {
            pushSpan(structTree);
            StructType structType = (StructType) data.semanticInfo.accessTypes().get(structTree);
            StructDeclarationTree declaration = getStructDeclaration(structType.name(), data);
            int memberIndex = getMemberIndex(declaration, fieldName);
            Node address = createFieldAccessAddressCalculation(structTree, structType, memberIndex, data);

            if (!(declaration.fields().get(memberIndex).type().type() instanceof SmallType smallType)) {
                return Optional.of(address);
            }
            Node result = projResultSideEffectCause(data, data.constructor.newLoad(address, smallType.toIrType()));

            popSpan();
            return Optional.of(result);
        }

        @Override
        public Optional<Node> visit(ExpArrayAccessTree expArrayAccessTree, SsaTranslation data) {
            return visitArrayAccess(expArrayAccessTree.array(), expArrayAccessTree.index(), data);
        }

        @Override
        public Optional<Node> visit(ExpDereferenceTree expDereferenceTree, SsaTranslation data) {
            return visitDereference(expDereferenceTree.pointer(), data);
        }

        @Override
        public Optional<Node> visit(ExpFieldAccessTree expFieldAccessTree, SsaTranslation data) {
            return visitFieldAccess(expFieldAccessTree.struct(), expFieldAccessTree.fieldName().name(), data);
        }

        @Override
        public Optional<Node> visit(LValueArrayAccessTree lValueArrayAccessTree, SsaTranslation data) {
            return visitArrayAccess(lValueArrayAccessTree.array(), lValueArrayAccessTree.index(), data);
        }

        @Override
        public Optional<Node> visit(LValueDereferenceTree lValueDereferenceTree, SsaTranslation data) {
            return visitDereference(lValueDereferenceTree.pointer(), data);
        }

        @Override
        public Optional<Node> visit(LValueFieldAccessTree lValueFieldAccessTree, SsaTranslation data) {
            return visitFieldAccess(lValueFieldAccessTree.struct(), lValueFieldAccessTree.fieldName().name(), data);
        }

        @Override
        public Optional<Node> visit(AllocCallTree allocCallTree, SsaTranslation data) {
            pushSpan(allocCallTree);
            Node result = projResultSideEffectCause(data, data.constructor.newAlloc(toMemoryType(allocCallTree.type().type(), data)));
            popSpan();

            return Optional.of(result);
        }

        @Override
        public Optional<Node> visit(AllocArrayCallTree allocArrayCallTree, SsaTranslation data) {
            pushSpan(allocArrayCallTree);
            Node size = allocArrayCallTree.elementCount().accept(this, data).orElseThrow();
            MemoryType memoryType = toMemoryType(allocArrayCallTree.type().type(), data);
            Node result = projResultSideEffectCause(data, data.constructor.newAllocArray(memoryType, size));
            popSpan();

            return Optional.of(result);
        }

        @Override
        public Optional<Node> visit(PointerLiteralTree pointerLiteralTree, SsaTranslation data) {
            pushSpan(pointerLiteralTree);
            Node result = data.constructor.newConstInt(0);
            popSpan();
            return Optional.of(result);
        }

        private Node projResultSideEffectCause(SsaTranslation data, Node sideEffectCause) {
            // make sure we actually have a side effect cause, as optimizations could
            // have changed it to something else already
            if (!NodeSupport.causesSideEffect(sideEffectCause)) {
                return sideEffectCause;
            }
            Node projSideEffect = data.constructor.newSideEffectProj(sideEffectCause);
            data.constructor.writeCurrentSideEffect(projSideEffect);
            return data.constructor.newResultProj(sideEffectCause);
        }
    }

    private record LoopInfo(Node continueTarget, Node breakTarget, @Nullable SimpleStatementTree step) implements NoOpVisitor<SsaTranslation> {}
}
