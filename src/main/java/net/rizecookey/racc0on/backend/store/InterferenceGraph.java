package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.backend.operand.store.VariableStore;
import net.rizecookey.racc0on.backend.operation.Operation;
import net.rizecookey.racc0on.backend.operation.OperationBlock;
import net.rizecookey.racc0on.backend.operation.OperationSchedule;
import net.rizecookey.racc0on.utils.Graph;
import net.rizecookey.racc0on.utils.Weighted;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InterferenceGraph<T extends Operation<?, U>, U extends VariableStore> extends Graph<StoreReference<U>> {
    private final List<T> operations;
    private final LivenessMap<T, U> livenessMap;
    private final StoreRequests<T, U> requests;

    public InterferenceGraph(List<T> operations, LivenessMap<T, U> livenessMap, StoreRequests<T, U> requests) {
        this.operations = operations;
        this.livenessMap = livenessMap;
        this.requests = requests;
    }

    public List<StoreReference<U>> getSimplicialEliminationOrdering(Map<StoreReference<U>, StoreConditions<U>> conditions) {
        Map<StoreReference<U>, Integer> currentWeight = new HashMap<>();
        PriorityQueue<Weighted<StoreReference<U>>> priorityQueue =
                new PriorityQueue<>(Comparator.<Weighted<StoreReference<U>>>comparingInt(Weighted::weight).reversed());
        List<StoreReference<U>> ordering = new ArrayList<>(this.getNodes().stream()
                .filter(store -> !conditions.get(store).targetedStores().isEmpty())
                .toList());
        this.getNodes().stream()
                .filter(store -> conditions.get(store).targetedStores().isEmpty())
                .map(store -> new Weighted<>(store, 0))
                .forEach(value -> {
                    currentWeight.put(value.value(), value.weight());
                    priorityQueue.add(value);
                });

        while (!priorityQueue.isEmpty()) {
            var store = priorityQueue.poll();
            if (!currentWeight.get(store.value()).equals(store.weight())) {
                continue;
            }
            ordering.add(store.value());

            for (var other : priorityQueue.stream().toList()) {
                if (!getNeighbors(store.value()).contains(other.value())
                        || !currentWeight.getOrDefault(other.value(), -1).equals(other.weight())) {
                    continue;
                }

                var newValue = new Weighted<>(other.value(), other.weight() + 1);
                currentWeight.put(newValue.value(), newValue.weight());
                priorityQueue.add(newValue);
            }
        }

        return ordering;
    }

    public Map<StoreReference<U>, U> createColoring(List<U> initiallyAvailable, Supplier<U> newStoreProvider) {
        Map<StoreReference<U>, StoreConditions<U>> conditions = getNodes().stream()
                .map(ref -> Map.entry(ref, requests.getConditionSupplier(ref).supply(ref, livenessMap)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<StoreReference<U>> ordering = getSimplicialEliminationOrdering(conditions);
        Map<StoreReference<U>, U> coloring = new HashMap<>();
        List<U> availableStores = new ArrayList<>(initiallyAvailable);

        for (var store : ordering) {
            Set<U> neighborsColors = getNeighbors(store).stream()
                    .filter(coloring::containsKey)
                    .map(coloring::get)
                    .collect(Collectors.toSet());

            StoreConditions<U> storeConditions = conditions.get(store);
            if (!storeConditions.shouldAllocate()) {
                continue;
            }
            U color = colorWithExisting(conditions.get(store), availableStores, neighborsColors).orElseGet(() -> {
                U newColor = newStoreProvider.get();
                availableStores.add(newColor);
                return newColor;
            });

            coloring.put(store, color);
        }

        allocateAdditional(availableStores, newStoreProvider, coloring);

        return coloring;
    }

    private void allocateAdditional(List<U> initiallyAvailable, Supplier<U> newStoreProvider,
                                    Map<StoreReference<U>, U> coloring) {
        for (T operation : operations) {
            Set<U> forbiddenStores = livenessMap.getLiveAt(operation).stream()
                    .map(coloring::get)
                    .collect(Collectors.toCollection(HashSet::new));
            if (requests.requiresOutputStore(operation)) {
                forbiddenStores.add(coloring.get(requests.getOutputStore(operation)));
            }

            for (StoreReference<U> additionalStore : requests.getAdditionalStores(operation)) {
                StoreConditions<U> storeConditions = requests.getConditionSupplier(additionalStore)
                        .supply(additionalStore, livenessMap);
                if (!storeConditions.shouldAllocate()) {
                    continue;
                }
                U color = colorWithExisting(storeConditions, initiallyAvailable, forbiddenStores).orElseGet(() -> {
                    U newColor = newStoreProvider.get();
                    initiallyAvailable.add(newColor);
                    return newColor;
                });

                coloring.put(additionalStore, color);
                forbiddenStores.add(color);
            }
        }
    }

    /**
     * Color a reference with one of the available stores, not using any of the forbidden colors, if possible.
     *
     * @param conditions the conditions to adhere to when coloring
     * @param availableStores the available stores for coloring
     * @param forbiddenColors colors that cannot be used for coloring
     *
     * @return a valid coloring for the provided reference (wrapped in an {@link Optional}, or empty if not enough
     * colors are available
     */
    private Optional<U> colorWithExisting(StoreConditions<U> conditions, List<U> availableStores,
                                          Set<U> forbiddenColors) {
        if (!conditions.targetedStores().isEmpty() && conditions.targetsOptional()) {
            return Optional.of(conditions.targetedStores().stream()
                    .filter(color -> !forbiddenColors.contains(color))
                    .filter(color -> !conditions.collisions().contains(color))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("All targeted stores are reserved")));
        }

        return Stream.concat(conditions.targetedStores().stream(), availableStores.stream())
                .filter(color -> !forbiddenColors.contains(color))
                .filter(color -> !conditions.collisions().contains(color))
                .findFirst();
    }

    public static <T extends Operation<?, U>, U extends VariableStore> InterferenceGraph<T, U> createFrom(OperationSchedule<T> schedule, LivenessMap<T, U> livenessMap, StoreRequests<T, U> requests) {
        InterferenceGraph<T, U> graph = new InterferenceGraph<>(schedule.blocks().values().stream()
                .flatMap(coll -> coll.operations().stream()).toList(), livenessMap, requests);

        for (OperationBlock<T> block : schedule.blocks().values()) {
            for (int i = 0; i < block.operations().size(); i++) {
                T operation = block.operations().get(i);
                if (!requests.requiresOutputStore(operation)) {
                    continue;
                }

                var outStore = requests.getOutputStore(operation);
                graph.addNode(outStore);

                List<T> successors = new ArrayList<>();
                if (i < block.operations().size() - 1) {
                    successors.add(block.operations().get(i + 1));
                }
                operation.targetLabels().stream()
                        .map(label -> schedule.blocks().get(label).operations().getFirst())
                        .forEach(successors::add);
                for (var successor : successors) {
                    for (var live : livenessMap.getLiveAt(successor)) {
                        if (live.equals(outStore)) {
                            continue;
                        }

                        graph.addEdge(outStore, live);
                    }
                }
            }
        }

        return graph;
    }
}
