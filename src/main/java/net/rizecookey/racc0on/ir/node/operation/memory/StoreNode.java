package net.rizecookey.racc0on.ir.node.operation.memory;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;

public final class StoreNode extends Node {
    public static final int VALUE = 0;
    public static final int ADDRESS = 1;
    public static final int SIDE_EFFECT = 2;

    public StoreNode(Block block, Node value, Node address, Node sideEffect) {
        super(block, value, address, sideEffect);
    }
}
