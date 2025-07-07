package net.rizecookey.racc0on.ir.node;

import java.util.List;

public final class Phi extends Node {
    public Phi(Block block) {
        super(block);
    }

    public void appendOperand(Node node) {
        addPredecessor(node);
    }

    public void replaceBy(Node replacement) {
        for (Node user : List.copyOf(graph().successors(this))) {
            if (user.equals(this)) {
                continue;
            }
            /* Remove all users from their predecessor's successors.
               This is done to prevent mutating issues in sets. */
            for (Node predecessor : user.predecessors()) {
                graph().removeSuccessor(predecessor, user);
            }
            int predecessorIndex = user.predecessors().indexOf(this);
            user.setPredecessor(predecessorIndex, replacement);
            /* Re-add user to successors of all predecessors. */
            for (Node predecessor : user.predecessors()) {
                graph().registerSuccessor(predecessor, user);
            }
        }
    }

    @Override
    public ValueType valueType() {
        for (Node operand : predecessors()) {
            if (!(operand instanceof Phi)) {
                return operand.valueType();
            }
        }

        return super.valueType();
    }
}
