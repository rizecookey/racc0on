package net.rizecookey.racc0on.ir;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Phi;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.optimize.Optimizer;
import net.rizecookey.racc0on.ir.util.DebugInfo;
import net.rizecookey.racc0on.ir.util.DebugInfoHelper;
import net.rizecookey.racc0on.ir.util.NodeSupport;
import net.rizecookey.racc0on.lexer.OperatorType;
import net.rizecookey.racc0on.parser.ast.simp.AssignmentTree;
import net.rizecookey.racc0on.parser.ast.exp.BinaryOperationTree;
import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.exp.BoolLiteralTree;
import net.rizecookey.racc0on.parser.ast.call.BuiltinCallTree;
import net.rizecookey.racc0on.parser.ast.simp.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.exp.IdentExpressionTree;
import net.rizecookey.racc0on.parser.ast.LValueIdentTree;
import net.rizecookey.racc0on.parser.ast.exp.IntLiteralTree;
import net.rizecookey.racc0on.parser.ast.ParameterTree;
import net.rizecookey.racc0on.parser.ast.call.FunctionCallTree;
import net.rizecookey.racc0on.parser.ast.simp.SimpleStatementTree;
import net.rizecookey.racc0on.parser.ast.exp.TernaryExpressionTree;
import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.ast.exp.UnaryOperationTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.parser.ast.control.ReturnTree;
import net.rizecookey.racc0on.parser.ast.StatementTree;
import net.rizecookey.racc0on.parser.ast.Tree;
import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.parser.ast.control.WhileTree;
import net.rizecookey.racc0on.parser.symbol.Name;
import net.rizecookey.racc0on.parser.visitor.NoOpVisitor;
import net.rizecookey.racc0on.parser.visitor.Visitor;
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

    public SsaTranslation(FunctionTree function, Optimizer optimizer) {
        this.function = function;
        this.constructor = new GraphConstructor(optimizer, function.name().name().asString());
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

            switch (assignmentTree.lValue()) {
                case LValueIdentTree(var name) -> {
                    Node rhs = assignmentTree.expression().accept(this, data).orElseThrow();
                    if (desugar != null) {
                        rhs = desugar.apply(data.readVariable(name.name(), data.currentBlock()), rhs);
                    }
                    data.writeVariable(name.name(), data.currentBlock(), rhs);
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
            return NOT_AN_EXPRESSION;
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
            Node parameter = data.constructor.newParameter(parameterTree.index());
            data.writeVariable(parameterTree.name().name(), data.currentBlock(), parameter);
            return Optional.of(parameter);
        }

        @Override
        public Optional<Node> visit(FunctionCallTree functionCallTree, SsaTranslation data) {
            Node[] args = functionCallTree.arguments().stream()
                    .map(arg -> arg.accept(this, data).orElseThrow())
                    .toArray(Node[]::new);
            Node result = projResultSideEffectCause(
                    data,
                    data.constructor.newCall(functionCallTree.name().name().asString(), args)
            );
            return Optional.of(result);
        }

        @Override
        public Optional<Node> visit(BuiltinCallTree builtinCallTree, SsaTranslation data) {
            Node[] args = builtinCallTree.arguments().stream()
                    .map(arg -> arg.accept(this, data).orElseThrow())
                    .toArray(Node[]::new);
            Node result = projResultSideEffectCause(
                    data,
                    data.constructor.newBuiltinCall(builtinCallTree.type(), args)
            );
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
