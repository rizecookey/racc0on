package net.rizecookey.racc0on.ir.node;

public final class ParameterNode extends Node {
    private final int index;
    private final ValueType valueType;

    public ParameterNode(Block block, int index, ValueType valueType, Node startNode) {
        super(block, startNode);
        this.index = index;
        this.valueType = valueType;
    }

    public int index() {
        return index;
    }

    @Override
    public ValueType valueType() {
        return valueType;
    }
}
