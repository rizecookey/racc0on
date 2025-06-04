package edu.kit.kastel.vads.compiler.ir.node;

public final class Phi extends Node {
    public Phi(Block block) {
        super(block);
    }

    public void appendOperand(Node node) {
        addPredecessor(node);
    }

    public void replaceBy(Node replacement) {
        for (Node user : graph().successors(this)) {
            if (user.equals(this)) {
                continue;
            }

            int predecessorIndex = user.predecessors().indexOf(this);
            user.setPredecessor(predecessorIndex, replacement);
        }

        for (Node predecessor : predecessors()) {
            graph().removeSuccessor(predecessor, this);
        }
    }
}
