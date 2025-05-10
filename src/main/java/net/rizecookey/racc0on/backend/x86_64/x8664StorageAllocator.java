package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.InterferenceGraph;
import net.rizecookey.racc0on.backend.LivenessMap;
import net.rizecookey.racc0on.utils.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/* TODO: Deal with instructions requiring special registers */
public class x8664StorageAllocator {
    private final Map<Node, x8664StorageLocation> allocations = new HashMap<>();

    public record Allocation(Map<Node, x8664StorageLocation> allocations, int stackSize) {}

    public Allocation allocate(List<Node> sequentialProgram) {
        LivenessMap liveness = LivenessMap.calculateFor(sequentialProgram);
        InterferenceGraph interference = InterferenceGraph.createFrom(sequentialProgram, liveness);
        Map<Node, Integer> coloring = getColoring(interference, interference.getSimplicialEliminationOrdering());
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

        return new Allocation(Map.copyOf(allocations), Math.max(0, (maxColor - availableRegisters + 1) * 4));
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
