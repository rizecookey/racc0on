package net.rizecookey.racc0on.backend;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.utils.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class InterferenceGraph extends Graph<Node> {
    private record Weighted<T>(T value, int weight) {}

    public List<Node> getSimplicialEliminationOrdering() {
        PriorityQueue<Weighted<Node>> priorityQueue = new PriorityQueue<>(Comparator.<Weighted<Node>>comparingInt(Weighted::weight).reversed());
        getNodes().forEach(node -> priorityQueue.add(new Weighted<>(node, 0)));

        List<Node> ordering = new ArrayList<>();
        while (!priorityQueue.isEmpty()) {
            Weighted<Node> node = priorityQueue.poll();
            ordering.add(node.value());

            for (Weighted<Node> other : priorityQueue.stream().toList()) {
                if (!getNeighbors(node.value()).contains(other.value())) {
                    continue;
                }

                priorityQueue.remove(other);
                priorityQueue.add(new Weighted<>(other.value(), other.weight() + 1));
            }
        }

        return ordering;
    }

    public static InterferenceGraph createFrom(List<Node> sequentialProgram, LivenessMap livenessMap) {
        InterferenceGraph graph = new InterferenceGraph();

        for (int i = sequentialProgram.size() - 2; i >= 0; i--) {
            Node node = sequentialProgram.get(i);
            if (!NodeUtils.providesValue(node)) {
                continue;
            }
            graph.addNode(node);

            for (Node alive : livenessMap.getLiveAt(sequentialProgram.get(i + 1))) {
                if (alive.equals(node) || !NodeUtils.providesValue(alive)) {
                    continue;
                }

                graph.addEdge(node, alive);
            }
        }

        return graph;
    }
}
