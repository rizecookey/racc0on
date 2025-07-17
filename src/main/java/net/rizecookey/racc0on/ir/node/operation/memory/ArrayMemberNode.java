package net.rizecookey.racc0on.ir.node.operation.memory;

import net.rizecookey.racc0on.ir.memory.MemoryType;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;

public final class ArrayMemberNode extends Node {
    public static final int ARRAY = 0;
    public static final int INDEX = 1;

    private final MemoryType elementLayout;

    public ArrayMemberNode(Block block, Node array, MemoryType elementLayout, Node index) {
        super(block, array, index);
        this.elementLayout = elementLayout;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ArrayMemberNode a)) {
            return false;
        }
        return block() == a.block() && predecessor(ARRAY) == a.predecessor(ARRAY) && predecessor(INDEX) == a.predecessor(INDEX);
    }

    @Override
    public int hashCode() {
        int h = block().hashCode() * 31;
        h += (predecessorHash(this, ARRAY) * 31 + predecessorHash(this, INDEX)) ^ this.getClass().hashCode();
        return h;
    }

    @Override
    public ValueType valueType() {
        return ValueType.POINTER;
    }

    public MemoryType elementLayout() {
        return elementLayout;
    }
}
