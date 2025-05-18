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
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class InterferenceGraph<T extends Operation<?, U>, U extends VariableStore> extends Graph<StoreReference<U>> {
    private final StoreRequests<T, U> requests;

    public InterferenceGraph(StoreRequests<T, U> requests) {
        this.requests = requests;
    }

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

    public Map<StoreReference<U>, U> createColoring(List<U> initiallyAvailable, Supplier<U> newStoreProvider) {
        List<StoreReference<U>> ordering = getSimplicialEliminationOrdering();
        Map<StoreReference<U>, U> coloring = new HashMap<>();
        List<U> availableStores = new ArrayList<>(initiallyAvailable);

        outer:
        for (var store : ordering) {
            Set<U> neighborsColors = getNeighbors(store).stream()
                    .filter(coloring::containsKey)
                    .map(coloring::get)
                    .collect(Collectors.toSet());

            StoreRequests.Conditions<U> conditions = requests.getConditions(store);
            for (U availableColor : availableStores) {
                if (!neighborsColors.contains(availableColor) && !conditions.collisions().contains(availableColor)) {
                    coloring.put(store, availableColor);
                    continue outer;
                }
            }

            U newColor = newStoreProvider.get();
            coloring.put(store, newColor);
            availableStores.add(newColor);
        }

        return coloring;
    }

    public static <T extends Operation<?, U>, U extends VariableStore> InterferenceGraph<T, U> createFrom(List<T> sequentialProgram, LivenessMap<T, U> livenessMap, StoreRequests<T, U> requests) {
        InterferenceGraph<T, U> graph = new InterferenceGraph<>(requests);

        for (int i = sequentialProgram.size() - 2; i >= 0; i--) {
            T operation = sequentialProgram.get(i);
            if (!requests.requiresOutputStore(operation)) {
                continue;
            }
            var outStore = requests.getOutputStore(operation);
            graph.addNode(outStore);

            for (var live : livenessMap.getLiveAt(sequentialProgram.get(i + 1))) {
                if (live.equals(outStore)) {
                    continue;
                }

                graph.addEdge(outStore, live);
            }
        }

        return graph;
    }
}
