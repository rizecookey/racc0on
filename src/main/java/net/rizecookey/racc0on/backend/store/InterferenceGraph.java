package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.backend.operand.stored.VariableStore;
import net.rizecookey.racc0on.backend.operation.Operation;
import net.rizecookey.racc0on.utils.Graph;
import net.rizecookey.racc0on.utils.Weighted;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class InterferenceGraph<T extends Operation<?, U>, U extends VariableStore> extends Graph<StoreReference<U>> {
    private final List<T> operations;
    private final LivenessMap<T, U> livenessMap;
    private final StoreRequests<T, U> requests;

    public InterferenceGraph(List<T> operations, LivenessMap<T, U> livenessMap, StoreRequests<T, U> requests) {
        this.operations = operations;
        this.livenessMap = livenessMap;
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

        for (var store : ordering) {
            Set<U> neighborsColors = getNeighbors(store).stream()
                    .filter(coloring::containsKey)
                    .map(coloring::get)
                    .collect(Collectors.toSet());

            U color = colorWithExisting(store, availableStores, neighborsColors);
            if (color == null) {
                color = newStoreProvider.get();
                availableStores.add(color);
            }

            coloring.put(store, color);
        }

        allocateAdditional(availableStores, newStoreProvider, coloring);

        return coloring;
    }

    private void allocateAdditional(List<U> initiallyAvailable, Supplier<U> newStoreProvider, Map<StoreReference<U>, U> coloring) {
        for (T operation : operations) {
            Set<U> forbiddenStores = livenessMap.getLiveAt(operation).stream()
                    .map(coloring::get)
                    .collect(Collectors.toCollection(HashSet::new));
            if (requests.requiresOutputStore(operation)) {
                forbiddenStores.add(coloring.get(requests.getOutputStore(operation)));
            }

            for (StoreReference<U> additionalStore : requests.getAdditionalStores(operation)) {
                U color = colorWithExisting(additionalStore, initiallyAvailable, forbiddenStores);
                if (color == null) {
                    color = newStoreProvider.get();
                    initiallyAvailable.add(color);
                }

                coloring.put(additionalStore, color);
                forbiddenStores.add(color);
            }
        }
    }

    /**
     * Color a reference with one of the available stores, not using any of the forbidden colors, if possible.
     *
     * @param store the store reference to color
     * @param availableStores the available stores for coloring
     * @param forbiddenColors colors that cannot be used for coloring
     *
     * @return a valid coloring for the provided reference, or {@code null} if not enough colors are available
     */
    private @Nullable U colorWithExisting(StoreReference<U> store, List<U> availableStores, Set<U> forbiddenColors) {
        StoreRequests.Conditions<U> conditions = requests.getConditions(store);
        for (U availableColor : availableStores) {
            if (forbiddenColors.contains(availableColor) || conditions.collisions().contains(availableColor)) {
                continue;
            }

            return availableColor;
        }

        return null;
    }

    public static <T extends Operation<?, U>, U extends VariableStore> InterferenceGraph<T, U> createFrom(List<T> operations, LivenessMap<T, U> livenessMap, StoreRequests<T, U> requests) {
        InterferenceGraph<T, U> graph = new InterferenceGraph<>(operations, livenessMap, requests);

        for (int i = operations.size() - 2; i >= 0; i--) {
            T operation = operations.get(i);
            if (!requests.requiresOutputStore(operation)) {
                continue;
            }
            var outStore = requests.getOutputStore(operation);
            graph.addNode(outStore);

            for (var live : livenessMap.getLiveAt(operations.get(i + 1))) {
                if (live.equals(outStore)) {
                    continue;
                }

                graph.addEdge(outStore, live);
            }
        }

        return graph;
    }
}
