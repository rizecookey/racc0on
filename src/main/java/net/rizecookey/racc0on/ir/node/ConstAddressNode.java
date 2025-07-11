package net.rizecookey.racc0on.ir.node;

public final class ConstAddressNode extends Node {
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
