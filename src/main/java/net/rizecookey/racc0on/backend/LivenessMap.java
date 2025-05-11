package net.rizecookey.racc0on.backend;

import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;

public class LivenessMap {
    private final Map<Node, SequencedSet<Node>> map;

    public LivenessMap() {
        this(Map.of());
    }

    public LivenessMap(Map<Node, SequencedSet<Node>> liveness) {
        map = new HashMap<>(liveness);
    }

    public SequencedSet<Node> getLiveAt(Node node) {
        return map.getOrDefault(node, new LinkedHashSet<>());
    }

    public boolean isLiveAt(Node at, Node other) {
        return getLiveAt(at).contains(other);
    }

    public void addLiveAt(Node at, Node other) {
        map.computeIfAbsent(at, _ -> new LinkedHashSet<>()).add(other);
    }

    public void addLiveAt(Node at, Collection<? extends Node> others) {
        map.computeIfAbsent(at, _ -> new LinkedHashSet<>()).addAll(others);
    }

    public void removeLiveAt(Node at, Node other) {
        map.computeIfAbsent(at, _ -> new LinkedHashSet<>()).remove(other);
    }

    public void removeLiveAt(Node at, Collection<? extends Node> others) {
        map.computeIfAbsent(at, _ -> new LinkedHashSet<>()).removeAll(others);
    }

    /**
     * Propagates all nodes that are live at from and are not to to being live
     * at to as well.
     *
     * @param from the node from which to propagate the liveness
     * @param to   the node whose liveness is to be set
     */
    public void propagateLiveness(Node from, Node to) {
        for (Node liveInFrom : getLiveAt(from)) {
            if (liveInFrom.equals(to)) {
                continue;
            }

            addLiveAt(to, liveInFrom);
        }
    }

    /**
     * Marks all inputs of a node as live.
     *
     * @param node the node for which to set all inputs as live
     */
    public void addInputs(Node node) {
        addLiveAt(node, NodeUtils.shortcutPredecessors(node).stream()
                .filter(NodeUtils::providesValue)
                .toList());
    }

    public static LivenessMap calculateFor(List<Node> program) {
        LivenessMap liveness = new LivenessMap();

        for (int i = program.size() - 1; i >= 0; i--) {
            Node node = program.get(i);

            liveness.addInputs(node);
            if (i > 0) {
                liveness.propagateLiveness(node, program.get(i - 1));
            }
        }

        return liveness;
    }
}
