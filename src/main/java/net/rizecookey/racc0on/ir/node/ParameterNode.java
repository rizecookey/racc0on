package net.rizecookey.racc0on.ir.node;

public final class ParameterNode extends Node {
    private final int index;

    public ParameterNode(Block block, int index, Node startNode) {
        super(block, startNode);
        this.index = index;
    }

    public int index() {
        return index;
    }
}
