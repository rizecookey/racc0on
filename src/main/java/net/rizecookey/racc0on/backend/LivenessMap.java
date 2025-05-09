package net.rizecookey.racc0on.backend;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
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

    public void propagateLiveness(Node from, Node to) {
        for (Node liveInFrom : getLiveAt(from)) {
            if (liveInFrom.equals(to)) {
                continue;
            }

            addLiveAt(to, liveInFrom);
        }
    }

    public void propagateLiveness(Node from, Collection<? extends Node> tos) {
        for (Node to : tos) {
            propagateLiveness(from, to);
        }
    }

    public static LivenessMap calculateFor(IrGraph program) {
        LivenessMap liveness = new LivenessMap();

        for (Node node : NodeUtils.traverseBackwards(program)) {
            List<? extends Node> valueInputs = node.predecessors()
                    .stream()
                    .filter(NodeUtils::providesValue)
                    .toList();

            if (NodeUtils.providesValue(node)) {
                liveness.addLiveAt(node, valueInputs);
            }

            liveness.propagateLiveness(node, node.predecessors());
        }

        return liveness;
    }
}
