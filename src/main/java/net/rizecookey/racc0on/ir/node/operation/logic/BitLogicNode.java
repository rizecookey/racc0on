package net.rizecookey.racc0on.ir.node.operation.logic;

import net.rizecookey.racc0on.ir.node.operation.BinaryOperationNode;

public sealed interface BitLogicNode extends BinaryOperationNode permits BitwiseAndNode, BitwiseOrNode, BitwiseXorNode {
}
