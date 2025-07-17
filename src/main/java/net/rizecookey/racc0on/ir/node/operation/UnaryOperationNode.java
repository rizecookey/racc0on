package net.rizecookey.racc0on.ir.node.operation;

import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.logic.NotNode;

public sealed interface UnaryOperationNode extends Node permits AbstractUnaryOperationNode, NotNode {
    int IN = 0;
}
