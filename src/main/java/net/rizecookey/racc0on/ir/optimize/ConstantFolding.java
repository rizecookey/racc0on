package net.rizecookey.racc0on.ir.optimize;

import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.constant.ConstAddressNode;
import net.rizecookey.racc0on.ir.node.constant.ConstBoolNode;
import net.rizecookey.racc0on.ir.node.constant.ConstIntNode;
import net.rizecookey.racc0on.ir.node.constant.ConstNode;
import net.rizecookey.racc0on.ir.node.operation.BinaryOperationNode;
import net.rizecookey.racc0on.ir.node.operation.UnaryOperationNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.AddNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.ArithmeticNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.DivNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.ModNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.MulNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.ShiftLeftNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.ShiftRightNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.SubNode;
import net.rizecookey.racc0on.ir.node.operation.compare.CompareNode;
import net.rizecookey.racc0on.ir.node.operation.compare.EqNode;
import net.rizecookey.racc0on.ir.node.operation.compare.GreaterNode;
import net.rizecookey.racc0on.ir.node.operation.compare.GreaterOrEqNode;
import net.rizecookey.racc0on.ir.node.operation.compare.LessNode;
import net.rizecookey.racc0on.ir.node.operation.compare.LessOrEqNode;
import net.rizecookey.racc0on.ir.node.operation.compare.NotEqNode;
import net.rizecookey.racc0on.ir.node.operation.logic.BitLogicNode;
import net.rizecookey.racc0on.ir.node.operation.logic.BitwiseAndNode;
import net.rizecookey.racc0on.ir.node.operation.logic.BitwiseOrNode;
import net.rizecookey.racc0on.ir.node.operation.logic.BitwiseXorNode;
import net.rizecookey.racc0on.ir.node.operation.logic.NotNode;

import java.util.HashMap;
import java.util.Map;

public class ConstantFolding implements Optimizer {
    private final Map<Node, Node> folded = new HashMap<>();

    @Override
    public Node transform(Node node) {
        if (folded.containsKey(node)) {
            return folded.get(node);
        }

        Node result = node;

        if (node.predecessors().stream().allMatch(pred -> pred instanceof ConstNode)) {
            if (node instanceof BinaryOperationNode binaryOperation) {
                result = optimizeBinary(binaryOperation);
            } else if (node instanceof UnaryOperationNode unaryOperation) {
                result = optimizeUnary(unaryOperation);
            }
        }

        if (result != node) {
            folded.put(node, result);
        }

        return result;
    }

    private Node optimizeUnary(UnaryOperationNode unaryOperation) {
        ConstNode in = (ConstNode) unaryOperation.predecessor(UnaryOperationNode.IN);
        return switch (unaryOperation) {
            case NotNode _ -> new ConstBoolNode(in.block(), asLong(in) == 0);
        };
    }

    private Node optimizeBinary(BinaryOperationNode binaryOperation) {
        ConstNode left = (ConstNode) binaryOperation.predecessor(BinaryOperationNode.LEFT);
        ConstNode right = (ConstNode) binaryOperation.predecessor(BinaryOperationNode.RIGHT);
        return switch (binaryOperation) {
            case ArithmeticNode arithmeticNode -> optimizeBinaryArithmetic(arithmeticNode, (ConstIntNode) left, (ConstIntNode) right);
            case CompareNode compareNode -> optimizeBinaryCompare(compareNode, left, right);
            case BitLogicNode bitLogicNode -> optimizeBinaryBitLogic(bitLogicNode, (ConstIntNode) left, (ConstIntNode) right);
        };
    }

    private Node optimizeBinaryArithmetic(ArithmeticNode arithmeticOperation, ConstIntNode left, ConstIntNode right) {
        int result = switch (arithmeticOperation) {
            case AddNode _ -> left.value() + right.value();
            case DivNode _ -> left.value() / right.value();
            case ModNode _ -> left.value() % right.value();
            case MulNode _ -> left.value() * right.value();
            case ShiftLeftNode _ -> left.value() << right.value();
            case ShiftRightNode _ -> left.value() >> right.value();
            case SubNode _ -> left.value() - right.value();
        };

        return new ConstIntNode(left.block(), result);
    }

    private Node optimizeBinaryCompare(CompareNode compareNode, ConstNode left, ConstNode right) {
        long leftValue = asLong(left);
        long rightValue = asLong(right);
        int compareResult = Long.compare(leftValue, rightValue);

        boolean result = switch (compareNode) {
            case EqNode _ -> compareResult == 0;
            case NotEqNode _ -> compareResult != 0;
            case GreaterNode _ -> compareResult > 0;
            case GreaterOrEqNode _ -> compareResult >= 0;
            case LessNode _ -> compareResult < 0;
            case LessOrEqNode _ -> compareResult <= 0;
        };

        return new ConstBoolNode(left.block(), result);
    }

    private long asLong(ConstNode constNode) {
        return switch (constNode) {
            case ConstIntNode constInt -> constInt.value();
            case ConstBoolNode constBool -> constBool.value() ? 1 : 0;
            case ConstAddressNode constAddress -> constAddress.address();
        };
    }

    private Node optimizeBinaryBitLogic(BitLogicNode bitLogicNode, ConstIntNode left, ConstIntNode right) {
        int result = switch (bitLogicNode) {
            case BitwiseAndNode _ -> left.value() & right.value();
            case BitwiseOrNode _ -> left.value() | right.value();
            case BitwiseXorNode _ -> left.value() ^ right.value();
        };

        return new ConstIntNode(left.block(), result);
    }
}
