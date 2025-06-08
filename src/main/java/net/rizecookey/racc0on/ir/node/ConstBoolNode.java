package net.rizecookey.racc0on.ir.node;

public final class ConstBoolNode extends Node {
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
}
