package net.rizecookey.racc0on.ir.node.constant;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;

public final class ConstBoolNode extends Node implements ConstNode {
    private final boolean value;

    public ConstBoolNode(Block block, boolean value) {
        super(block);
        this.value = value;
    }

    public boolean value() {
        return value;
    }

    @Override
    public int hashCode() {
        return this.value ? 1 : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstBoolNode other) {
            return this.block() == other.block() && this.value() == other.value();
        }

        return false;
    }

    @Override
    protected String info() {
        return "[" + value() + "]";
    }

    @Override
    public ValueType valueType() {
        return ValueType.BOOL;
    }
}
