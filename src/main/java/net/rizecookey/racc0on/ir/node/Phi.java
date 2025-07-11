package net.rizecookey.racc0on.ir.node;

import net.rizecookey.racc0on.utils.Memoized;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Phi extends Node {
    private final Memoized<ValueType> valueType;

    public Phi(Block block) {
        super(block);
        this.valueType = Memoized.memoize(this::calculateValueType);
    }

    public void appendOperand(Node node) {
        addPredecessor(node);
        valueType.clear();
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
        valueType.clear();
    }

    private ValueType calculateValueType() {
        Set<Node> ignoredPredecessors = new HashSet<>();
        ignoredPredecessors.add(this);
        return calculateValueType(ignoredPredecessors);
    }

    private ValueType calculateValueType(Set<Node> ignoredPredecessors) {
        for (Node predecessor : predecessors()) {
            if (!ignoredPredecessors.add(predecessor)) {
                continue;
            }

            if (predecessor instanceof Phi phi) {
                return phi.calculateValueType(ignoredPredecessors);
            } else {
                return predecessor.valueType();
            }
        }

        return ValueType.NONE;
    }

    @Override
    public ValueType valueType() {
        return valueType.get();
    }
}
