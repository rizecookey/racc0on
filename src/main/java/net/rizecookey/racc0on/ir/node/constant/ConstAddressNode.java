package net.rizecookey.racc0on.ir.node.constant;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;

public final class ConstAddressNode extends Node implements ConstNode {
    private final long address;

    public ConstAddressNode(Block block, long address) {
        super(block);
        this.address = address;
    }

    public long address() {
        return address;
    }

    @Override
    public ValueType valueType() {
        return ValueType.POINTER;
    }
}
