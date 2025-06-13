package net.rizecookey.racc0on.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;

public class Graph<T> {
    private final SequencedSet<T> nodes;
    private final Map<T, SequencedSet<T>> edges;

    public Graph() {
        this(Map.of());
    }

    public Graph(Map<T, Collection<T>> nodeMapping) {
        nodes = new LinkedHashSet<>();
        edges = new HashMap<>();
        fillFromMap(nodeMapping);
    }

    private void fillFromMap(Map<T, Collection<T>> nodeMapping) {
        for (T node : nodeMapping.keySet()) {
            nodes.add(node);

            for (T neighbor : nodeMapping.get(node)) {
                nodes.add(neighbor);

                edges.computeIfAbsent(node, _ -> new LinkedHashSet<>()).add(neighbor);
                edges.computeIfAbsent(neighbor, _ -> new LinkedHashSet<>()).add(node);
            }
        }
    }

    public SequencedSet<T> getNodes() {
        return Collections.unmodifiableSequencedSet(nodes);
    }

    public void addNode(T node) {
        nodes.add(node);
        edges.putIfAbsent(node, new LinkedHashSet<>());
    }

    public void removeNode(T node) {
        nodes.remove(node);

        Set<T> neighbors = edges.containsKey(node) ? edges.get(node) : new LinkedHashSet<>();
        neighbors.forEach(neighbor -> edges.get(neighbor).remove(node));
        edges.remove(node);
    }

    public Set<T> getNeighbors(T node) {
        return Collections.unmodifiableSequencedSet(edges.containsKey(node) ? edges.get(node) : new LinkedHashSet<>());
    }

    public Map<T, Set<T>> getEdges() {
        return Collections.unmodifiableMap(edges);
    }

    public void addEdge(T node1, T node2) {
        if (!getNodes().contains(node1)) {
            addNode(node1);
        }

        if (!getNodes().contains(node2)) {
            addNode(node2);
        }

        edges.get(node1).add(node2);
        edges.get(node2).add(node1);
    }

    public void removeEdge(T node1, T node2) {
        if (!getNodes().contains(node1) || !getNodes().contains(node2)) {
            return;
        }

        edges.get(node1).remove(node2);
        edges.get(node2).remove(node1);
    }
}
