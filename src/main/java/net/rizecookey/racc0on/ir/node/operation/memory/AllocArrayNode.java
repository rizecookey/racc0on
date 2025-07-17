package net.rizecookey.racc0on.ir.node.operation.memory;

import net.rizecookey.racc0on.ir.memory.MemoryType;
import net.rizecookey.racc0on.ir.node.AbstractNode;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;

public final class AllocArrayNode extends AbstractNode {
    public static final int SIZE = 0;
    public static final int SIDE_EFFECT = 0;

    private final MemoryType type;

    public AllocArrayNode(Block block, MemoryType type, Node size, Node sideEffect) {
        super(block, size, sideEffect);
        this.type = type;
    }

    public MemoryType type() {
        return type;
    }

    @Override
    public ValueType valueType() {
        return ValueType.ARRAY;
    }
}
