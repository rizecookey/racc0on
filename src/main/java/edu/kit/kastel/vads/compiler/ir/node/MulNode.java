package edu.kit.kastel.vads.compiler.ir.node;

public final class MulNode extends CommutativeBinaryOperationNode {
    public MulNode(Block block, Node left, Node right) {
        super(block, left, right);
    }
}
