package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.BitwiseAndNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.BitwiseOrNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.BitwiseXorNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.EqNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.GreaterNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.GreaterOrEqNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.LessNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.LessOrEqNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.NotEqNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.ShiftLeftNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.ShiftRightNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.SubNode;
import edu.kit.kastel.vads.compiler.ir.optimize.Optimizer;
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo;
import edu.kit.kastel.vads.compiler.ir.util.DebugInfoHelper;
import edu.kit.kastel.vads.compiler.lexer.Operator;
import edu.kit.kastel.vads.compiler.lexer.OperatorType;
import edu.kit.kastel.vads.compiler.parser.ast.AssignmentTree;
import edu.kit.kastel.vads.compiler.parser.ast.BinaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.BoolLiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.DeclarationTree;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IdentExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueIdentTree;
import edu.kit.kastel.vads.compiler.parser.ast.IntLiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.TernaryExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.ForTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.IfElseTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.LoopControlTree;
import edu.kit.kastel.vads.compiler.parser.ast.NameTree;
import edu.kit.kastel.vads.compiler.parser.ast.UnaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.ReturnTree;
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.Tree;
import edu.kit.kastel.vads.compiler.parser.ast.TypeTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.WhileTree;
import edu.kit.kastel.vads.compiler.parser.symbol.Name;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.BinaryOperator;

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

        private void pushSpan(Tree tree) {
            this.debugStack.push(DebugInfoHelper.getDebugInfo());
            DebugInfoHelper.setDebugInfo(new DebugInfo.SourceInfo(tree.span()));
        }

        private void popSpan() {
            DebugInfoHelper.setDebugInfo(this.debugStack.pop());
        }

        private @Nullable BinaryOperator<Node> desugarer(SsaTranslation data, Operator op) {
            OperatorType type = op.type();
            if (!(type instanceof OperatorType.Assignment binType)) {
                throw new IllegalArgumentException("not an assignment operator " + op);
            }
            return switch (binType) {
                case OperatorType.Assignment.MINUS -> data.constructor.forBinaryOp(SubNode::new);
                case OperatorType.Assignment.PLUS -> data.constructor.forBinaryOp(AddNode::new);
                case OperatorType.Assignment.MUL -> data.constructor.forBinaryOp(MulNode::new);
                case OperatorType.Assignment.DIV -> (lhs, rhs) -> projResultDivMod(data, data.constructor.newBinarySideEffectOp(DivNode::new, lhs, rhs));
                case OperatorType.Assignment.MOD -> (lhs, rhs) -> projResultDivMod(data, data.constructor.newBinarySideEffectOp(ModNode::new, lhs, rhs));

                case OperatorType.Assignment.BITWISE_AND -> data.constructor.forBinaryOp(BitwiseAndNode::new);
                case OperatorType.Assignment.BITWISE_XOR -> data.constructor.forBinaryOp(BitwiseXorNode::new);
                case OperatorType.Assignment.BITWISE_OR -> data.constructor.forBinaryOp(BitwiseOrNode::new);

                case OperatorType.Assignment.SHIFT_LEFT -> data.constructor.forBinaryOp(ShiftLeftNode::new);
                case OperatorType.Assignment.SHIFT_RIGHT -> data.constructor.forBinaryOp(ShiftRightNode::new);

                case OperatorType.Assignment.DEFAULT -> null;
            };
        }

        @Override
        public Optional<Node> visit(AssignmentTree assignmentTree, SsaTranslation data) {
            pushSpan(assignmentTree);
            BinaryOperator<Node> desugar = desugarer(data, assignmentTree.operator());

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
                case MINUS -> data.constructor.newBinaryOp(SubNode::new, lhs, rhs);
                case PLUS -> data.constructor.newBinaryOp(AddNode::new, lhs, rhs);
                case MUL -> data.constructor.newBinaryOp(MulNode::new, lhs, rhs);
                case DIV -> projResultDivMod(data, data.constructor.newBinarySideEffectOp(DivNode::new, lhs, rhs));
                case MOD -> projResultDivMod(data, data.constructor.newBinarySideEffectOp(ModNode::new, lhs, rhs));
                case BITWISE_AND -> data.constructor.newBinaryOp(BitwiseAndNode::new, lhs, rhs);
                case BITWISE_XOR -> data.constructor.newBinaryOp(BitwiseXorNode::new, lhs, rhs);
                case BITWISE_OR -> data.constructor.newBinaryOp(BitwiseOrNode::new, lhs, rhs);
                case SHIFT_LEFT -> data.constructor.newBinaryOp(ShiftLeftNode::new, lhs, rhs);
                case SHIFT_RIGHT -> data.constructor.newBinaryOp(ShiftRightNode::new, lhs, rhs);
                case LESS_THAN -> data.constructor.newBinaryOp(LessNode::new, lhs, rhs);
                case LESS_OR_EQUAL -> data.constructor.newBinaryOp(LessOrEqNode::new, lhs, rhs);
                case GREATER_THAN -> data.constructor.newBinaryOp(GreaterNode::new, lhs, rhs);
                case GREATER_OR_EQUAL -> data.constructor.newBinaryOp(GreaterOrEqNode::new, lhs, rhs);
                case EQUAL -> data.constructor.newBinaryOp(EqNode::new, lhs, rhs);
                case NOT_EQUAL -> data.constructor.newBinaryOp(NotEqNode::new, lhs, rhs);

                case AND, OR -> throw new UnsupportedOperationException();
            };
            popSpan();
            return Optional.of(res);
        }

        @Override
        public Optional<Node> visit(BlockTree blockTree, SsaTranslation data) {
            pushSpan(blockTree);
            for (StatementTree statement : blockTree.statements()) {
                statement.accept(this, data);
                // skip everything after a return in a block
                if (statement instanceof ReturnTree) {
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
            // TODO
            throw new UnsupportedOperationException();
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
            Node res = data.constructor.newBinaryOp(SubNode::new, data.constructor.newConstInt(0), node);
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
        public Optional<Node> visit(LoopControlTree loopControlTree, SsaTranslation data) {
            // TODO
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Node> visit(IfElseTree ifElseTree, SsaTranslation data) {
            // TODO
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Node> visit(WhileTree whileTree, SsaTranslation data) {
            // TODO
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Node> visit(ForTree forTree, SsaTranslation data) {
            // TODO
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Node> visit(TernaryExpressionTree ternaryExpressionTree, SsaTranslation data) {
            // TODO
            throw new UnsupportedOperationException();
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


}
