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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstAddressNode c) {
            return this.block() == c.block() && c.address() == this.address();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) this.address();
    }

    public long address() {
        return address;
    }

    @Override
    public ValueType valueType() {
        return ValueType.POINTER;
    }
}
