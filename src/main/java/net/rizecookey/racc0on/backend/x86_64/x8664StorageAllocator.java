package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.backend.regalloc.RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.LivenessMap;
import net.rizecookey.racc0on.backend.NodeUtils;
import net.rizecookey.racc0on.utils.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

/* TODO: Deal with instructions requiring special registers */
public class x8664StorageAllocator implements RegisterAllocator {
    private final Map<Node, Register> allocations = new HashMap<>();

    @Override
    public Map<Node, Register> allocateRegisters(IrGraph graph) {
        LivenessMap liveness = LivenessMap.calculateFor(graph);
        Graph<Node> interference = createInterferenceGraph(graph, liveness);
        Map<Node, Integer> coloring = getColoring(interference, getSimplicialEliminationOrdering(interference));
        int maxColor = coloring.values().stream().max(Integer::compareTo).orElseThrow();

        List<x8664StorageLocation> availableLocations = new ArrayList<>(x8664Register.getRegisterSet()
                .stream()
                .filter(x8664Register::isGeneralPurpose)
                .toList());
        int availableRegisters = availableLocations.size();
        for (int i = availableRegisters; i <= maxColor; i++) {
            availableLocations.add(new x8664StackLocation(i * 4));
        }

        for (Node node : coloring.keySet()) {
            allocations.put(node, availableLocations.get(coloring.get(node)));
        }

        return Map.copyOf(allocations);
    }

    private record Weighted<T>(T value, int weight) {}

    private static Graph<Node> createInterferenceGraph(IrGraph program, LivenessMap liveness) {
        Graph<Node> graph = new Graph<>();

        for (Node node : NodeUtils.traverseBackwards(program)) {
            if (!NodeUtils.providesValue(node)) {
                continue;
            }

            for (Node alive : liveness.getLiveAt(node)) {
                graph.addEdge(node, alive);
            }
        }

        return graph;
    }

    private static List<Node> getSimplicialEliminationOrdering(Graph<Node> interferenceGraph) {
        PriorityQueue<Weighted<Node>> priorityQueue = new PriorityQueue<>(Comparator.<Weighted<Node>>comparingInt(Weighted::weight).reversed());
        interferenceGraph.getNodes().forEach(node -> priorityQueue.add(new Weighted<>(node, 0)));

        List<Node> ordering = new ArrayList<>();
        while (!priorityQueue.isEmpty()) {
            Weighted<Node> node = priorityQueue.poll();
            ordering.add(node.value());

            for (Weighted<Node> other : priorityQueue.stream().toList()) {
                if (!interferenceGraph.getNeighbors(node.value()).contains(other.value())) {
                    continue;
                }

                priorityQueue.remove(other);
                priorityQueue.add(new Weighted<>(other.value(), other.weight() + 1));
            }
        }

        return ordering;
    }

    private static Map<Node, Integer> getColoring(Graph<Node> interferenceGraph, List<Node> simplicialEliminationOrdering) {
        Map<Node, Integer> coloring = new HashMap<>();

        for (Node node : simplicialEliminationOrdering) {
            Set<Integer> neighborsColors = interferenceGraph.getNeighbors(node).stream()
                    .filter(coloring::containsKey)
                    .map(coloring::get)
                    .collect(Collectors.toSet());

            int color;
            for (color = 0; neighborsColors.contains(color); color++) {}
            coloring.put(node, color);
        }

        return coloring;
    }
}
