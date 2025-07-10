package net.rizecookey.racc0on.ir.node.operation.memory;

import net.rizecookey.racc0on.ir.memory.MemoryType;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;

public final class StructMemberOffset extends Node {
    private final MemoryType.Compound layout;
    private final int memberIndex;

    public StructMemberOffset(Block block, MemoryType.Compound layout, int memberIndex) {
        super(block);
        this.layout = layout;
        this.memberIndex = memberIndex;
    }

    @Override
    public ValueType valueType() {
        return ValueType.INT;
    }

    public MemoryType.Compound structLayout() {
        return layout;
    }

    public int memberIndex() {
        return memberIndex;
    }
}
