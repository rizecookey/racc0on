package net.rizecookey.racc0on.ir.node.operation;

import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.ArithmeticNode;
import net.rizecookey.racc0on.ir.node.operation.compare.CompareNode;
import net.rizecookey.racc0on.ir.node.operation.logic.BitLogicNode;

public sealed interface BinaryOperationNode extends Node permits AbstractBinaryOperationNode, ArithmeticNode, CompareNode, BitLogicNode {
    int LEFT = 0;
    int RIGHT = 1;
}
