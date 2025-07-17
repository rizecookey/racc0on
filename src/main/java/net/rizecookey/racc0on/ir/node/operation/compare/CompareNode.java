package net.rizecookey.racc0on.ir.node.operation.compare;

import net.rizecookey.racc0on.ir.node.Node;

public sealed interface CompareNode extends Node permits EqNode, GreaterNode, GreaterOrEqNode, LessNode, LessOrEqNode, NotEqNode {
}
