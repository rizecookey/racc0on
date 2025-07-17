package net.rizecookey.racc0on.ir.node.operation.arithmetic;

import net.rizecookey.racc0on.ir.node.Node;

public sealed interface ArithmeticNode extends Node permits AddNode, DivNode, ModNode, MulNode, ShiftLeftNode, ShiftRightNode, SubNode {
}
