package net.rizecookey.racc0on.ir.node.constant;

import net.rizecookey.racc0on.ir.node.AbstractNode;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.ValueType;

public final class ConstIntNode extends AbstractNode implements ConstNode {
    private final int value;

    public ConstIntNode(Block block, int value) {
        super(block);
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstIntNode c) {
            return this.block() == c.block() && c.value == this.value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.value;
    }

    @Override
    protected String info() {
        return "[" + this.value + "]";
    }

    @Override
    public ValueType valueType() {
        return ValueType.INT;
    }
}
