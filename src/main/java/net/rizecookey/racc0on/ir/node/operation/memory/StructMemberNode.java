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
    public boolean equals(Object obj) {
        if (!(obj instanceof StructMemberNode s)) {
            return false;
        }
        return block() == s.block() && predecessor(STRUCT) == s.predecessor(STRUCT) && memberIndex() == s.memberIndex();
    }

    @Override
    public int hashCode() {
        int h = block().hashCode() * 31;
        h += (predecessorHash(this, STRUCT) * 31 + memberIndex()) ^ this.getClass().hashCode();
        return h;
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
