package net.rizecookey.racc0on.ir;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.operation.binary.DivNode;
import net.rizecookey.racc0on.ir.node.operation.binary.ModNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.optimize.Optimizer;
import net.rizecookey.racc0on.ir.util.DebugInfo;
import net.rizecookey.racc0on.ir.util.DebugInfoHelper;
import net.rizecookey.racc0on.lexer.OperatorType;
import net.rizecookey.racc0on.parser.ast.AssignmentTree;
import net.rizecookey.racc0on.parser.ast.BinaryOperationTree;
import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.BoolLiteralTree;
import net.rizecookey.racc0on.parser.ast.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.IdentExpressionTree;
import net.rizecookey.racc0on.parser.ast.LValueIdentTree;
import net.rizecookey.racc0on.parser.ast.IntLiteralTree;
import net.rizecookey.racc0on.parser.ast.SimpleStatementTree;
import net.rizecookey.racc0on.parser.ast.TernaryExpressionTree;
import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.ast.UnaryOperationTree;
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
                        (lhs, rhs) -> projResultDivMod(data, data.constructor.newDiv(lhs, rhs));
                case OperatorType.Assignment.MOD ->
                        (lhs, rhs) -> projResultDivMod(data, data.constructor.newMod(lhs, rhs));

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
            pushSpan(binaryOperationTree);
            Node lhs = binaryOperationTree.lhs().accept(this, data).orElseThrow();
            Node rhs = binaryOperationTree.rhs().accept(this, data).orElseThrow();
            Node res = switch (binaryOperationTree.operatorType()) {
                case MINUS -> data.constructor.newSub(lhs, rhs);
                case PLUS -> data.constructor.newAdd(lhs, rhs);
                case MUL -> data.constructor.newMul(lhs, rhs);
                case DIV -> projResultDivMod(data, data.constructor.newDiv(lhs, rhs));
                case MOD -> projResultDivMod(data, data.constructor.newMod(lhs, rhs));
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
                case AND -> data.constructor.newAnd(lhs, rhs);
                case OR -> data.constructor.newOr(lhs, rhs);
            };
            popSpan();
            return Optional.of(res);
        }

        @Override
        public Optional<Node> visit(BlockTree blockTree, SsaTranslation data) {
            pushSpan(blockTree);
            for (StatementTree statement : blockTree.statements()) {
                statement.accept(this, data);
                // skip everything after a return, continue or break in a block
                if (statement instanceof ReturnTree || statement instanceof LoopControlTree) {
                    break;
                }
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
            Node trueJump = data.constructor.hasUnconditionalJump() ? null : data.constructor.newJump();
            Node falseJump = falseProj;
            if (ifElseTree.elseBranch() != null) {
                Block falseBranch = data.constructor.newBlock(falseProj);
                data.constructor.sealBlock(falseBranch);
                ifElseTree.elseBranch().accept(this, data);
                falseJump = data.constructor.hasUnconditionalJump() ? null : data.constructor.newJump();
            }

            Block followingBlock = data.constructor.newBlock(Stream.of(trueJump, falseJump).filter(Objects::nonNull).toList());
            data.constructor.sealBlock(followingBlock);

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

            Block followingBlock = data.constructor.newBlock(ifFalse);

            Block bodyBlock = data.constructor.newBlock(ifTrue);
            data.constructor.sealBlock(bodyBlock);
            transformerStack.push(new LoopInfo(conditionBlock, followingBlock, forTree.step()));
            forTree.body().accept(this, data);
            transformerStack.pop();
            if (!data.constructor.hasUnconditionalJump()) {
                Node jumpToStep = data.constructor.newJump();
                Block stepBlock = data.constructor.newBlock(jumpToStep);
                data.constructor.sealBlock(stepBlock);
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

            Node jump = data.constructor.newJump();
            Node target = switch (loopControlTree.type()) {
                case CONTINUE -> {
                    Block stepBlock = data.constructor.newBlock(jump);
                    data.constructor.sealBlock(stepBlock);
                    if (loop.step() != null) {
                        loop.step().accept(this, data);
                    }
                    jump = data.constructor.newJump();
                    yield loop.continueTarget();
                }
                case BREAK -> loop.breakTarget();
            };
            target.addPredecessor(jump);

            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(TernaryExpressionTree ternaryExpressionTree, SsaTranslation data) {
            pushSpan(ternaryExpressionTree);

            Node condition = ternaryExpressionTree.condition().accept(this, data).orElseThrow();
            Node ifTrue = ternaryExpressionTree.ifBranch().accept(this, data).orElseThrow();
            Node ifFalse = ternaryExpressionTree.elseBranch().accept(this, data).orElseThrow();
            Node res = data.constructor.newTernary(condition, ifTrue, ifFalse);

            popSpan();
            return Optional.of(res);
        }

        private Node projResultDivMod(SsaTranslation data, Node divMod) {
            // make sure we actually have a div or a mod, as optimizations could
            // have changed it to something else already
            if (!(divMod instanceof DivNode || divMod instanceof ModNode)) {
                return divMod;
            }
            Node projSideEffect = data.constructor.newSideEffectProj(divMod);
            data.constructor.writeCurrentSideEffect(projSideEffect);
            return data.constructor.newResultProj(divMod);
        }
    }

    private record LoopInfo(Node continueTarget, Node breakTarget, @Nullable SimpleStatementTree step) implements NoOpVisitor<SsaTranslation> {}
}
