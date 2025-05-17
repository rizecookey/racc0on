package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.backend.operand.stored.VariableStore;
import net.rizecookey.racc0on.backend.operation.Operation;
import net.rizecookey.racc0on.utils.Graph;
import net.rizecookey.racc0on.utils.Weighted;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

public class InterferenceGraph<T extends Operation<?, U>, U extends VariableStore> extends Graph<StoreReference<U>> {

    public List<StoreReference<U>> getSimplicialEliminationOrdering() {
        PriorityQueue<Weighted<StoreReference<U>>> priorityQueue =
                new PriorityQueue<>(Comparator.<Weighted<StoreReference<U>>>comparingInt(Weighted::weight).reversed());
        getNodes().forEach(store -> priorityQueue.add(new Weighted<>(store, 0)));

        List<StoreReference<U>> ordering = new ArrayList<>();
        while (!priorityQueue.isEmpty()) {
            var store = priorityQueue.poll();
            ordering.add(store.value());

            for (var other : priorityQueue.stream().toList()) {
                if (!getNeighbors(store.value()).contains(other.value())) {
                    continue;
                }

                priorityQueue.remove(other);
                priorityQueue.add(new Weighted<>(other.value(), other.weight() + 1));
            }
        }

        return ordering;
    }

    public Map<StoreReference<U>, Integer> createColoring() {
        List<StoreReference<U>> ordering = getSimplicialEliminationOrdering();
        Map<StoreReference<U>, Integer> coloring = new HashMap<>();

        for (var store : ordering) {
            Set<Integer> neighborsColors = getNeighbors(store).stream()
                    .filter(coloring::containsKey)
                    .map(coloring::get)
                    .collect(Collectors.toSet());

            int color;
            for (color = 0; neighborsColors.contains(color); color++) {}
            coloring.put(store, color);
        }

        return coloring;
    }

    public static <T extends Operation<?, U>, U extends VariableStore> InterferenceGraph<T, U> createFrom(List<T> sequentialProgram, LivenessMap<T, U> livenessMap, StoreRequests<T, U> requests) {
        InterferenceGraph<T, U> graph = new InterferenceGraph<>();

        for (int i = sequentialProgram.size() - 2; i >= 0; i--) {
            T operation = sequentialProgram.get(i);
            if (!requests.requiresOutputStore(operation)) {
                continue;
            }
            var outStore = requests.getOutputStore(operation);
            graph.addNode(outStore);

            for (var alive : livenessMap.getLiveAt(sequentialProgram.get(i + 1))) {
                if (alive.equals(outStore)) {
                    continue;
                }

                graph.addEdge(outStore, alive);
            }
        }

        return graph;
    }
}
