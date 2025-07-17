package net.rizecookey.racc0on.ir.node.operation.memory;

import net.rizecookey.racc0on.ir.memory.MemoryType;
import net.rizecookey.racc0on.ir.node.AbstractNode;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;

public final class AllocNode extends AbstractNode {
    public static int SIDE_EFFECT = 0;

    private final MemoryType type;

    public AllocNode(Block block, MemoryType type, Node sideEffect) {
        super(block, sideEffect);
        this.type = type;
    }

    public MemoryType type() {
        return type;
    }

    @Override
    public ValueType valueType() {
        return ValueType.POINTER;
    }
}
