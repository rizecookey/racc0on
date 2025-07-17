package net.rizecookey.racc0on.ir.node.operation.arithmetic;

import net.rizecookey.racc0on.ir.node.operation.BinaryOperationNode;

public sealed interface ArithmeticNode extends BinaryOperationNode permits AddNode, DivNode, ModNode, MulNode, ShiftLeftNode, ShiftRightNode, SubNode {
}
