package net.rizecookey.racc0on.ir.node.operation.memory;

import net.rizecookey.racc0on.ir.memory.MemoryType;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;

public final class ArrayMemberOffset extends Node {
    public static final int INDEX = 0;

    private final MemoryType elementLayout;

    public ArrayMemberOffset(Block block, MemoryType elementLayout, Node index) {
        super(block, index);
        this.elementLayout = elementLayout;
    }

    @Override
    public ValueType valueType() {
        return ValueType.INT;
    }

    public MemoryType elementLayout() {
        return elementLayout;
    }
}
