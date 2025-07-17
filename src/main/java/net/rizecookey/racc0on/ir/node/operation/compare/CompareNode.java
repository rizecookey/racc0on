package net.rizecookey.racc0on.ir.node.operation.compare;

import net.rizecookey.racc0on.ir.node.operation.BinaryOperationNode;

public sealed interface CompareNode extends BinaryOperationNode permits EqNode, GreaterNode, GreaterOrEqNode, LessNode, LessOrEqNode, NotEqNode {
}
