package net.rizecookey.racc0on.ir.node.operation;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;

public final class TernaryNode extends Node {
    public static final int CONDITION = 0;
    public static final int IF_TRUE = 1;
    public static final int IF_FALSE = 2;

    public TernaryNode(Block block, Node condition, Node ifTrue, Node ifFalse) {
        super(block, condition, ifTrue, ifFalse);
    }
}
