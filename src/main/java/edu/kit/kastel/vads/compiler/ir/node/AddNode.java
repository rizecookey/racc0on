package edu.kit.kastel.vads.compiler.ir.node;

public final class AddNode extends CommutativeBinaryOperationNode {
    public AddNode(Block block, Node left, Node right) {
        super(block, left, right);
    }
}
