package net.rizecookey.racc0on.ir.node.operation.memory;

import net.rizecookey.racc0on.ir.memory.MemoryType;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;

public final class StructMemberNode extends Node {
    public static final int STRUCT = 0;

    private final MemoryType.Compound layout;
    private final int memberIndex;

    public StructMemberNode(Block block, Node struct, MemoryType.Compound layout, int memberIndex) {
        super(block, struct);
        this.layout = layout;
        this.memberIndex = memberIndex;
    }

    @Override
    public ValueType valueType() {
        return ValueType.POINTER;
    }

    public MemoryType.Compound structLayout() {
        return layout;
    }

    public int memberIndex() {
        return memberIndex;
    }
}
