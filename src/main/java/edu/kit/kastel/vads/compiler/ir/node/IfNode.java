package edu.kit.kastel.vads.compiler.ir.node;

public final class IfNode extends Node {
    public static final int CONDITION = 0;

    public IfNode(Block block, Node condition) {
        super(block, condition);
    }
}
