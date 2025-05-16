package net.rizecookey.racc0on.backend.store;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.stored.VariableStore;
import net.rizecookey.racc0on.backend.operation.Operation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;

public class StoreRequests<T extends Operation<?, U>, U extends VariableStore> implements StoreRequestService<T, U> {
    private final Map<T, SequencedSet<StoreReference<U>>> inputStores;
    private final Map<T, StoreReference<U>> outputStores;
    private final Map<T, SequencedSet<StoreReference<U>>> additionalStores;
    private final Map<StoreReference<U>, Set<Condition<T, U>>> storeConditions;

    private final Map<StoreReference<U>, T> pendingStores;

    private int additionalStoreId;

    public StoreRequests() {
        inputStores = new HashMap<>();
        outputStores = new HashMap<>();
        additionalStores = new HashMap<>();
        storeConditions = new HashMap<>();

        pendingStores = new HashMap<>();

        additionalStoreId = 0;
    }

    private record RegularStore<U extends VariableStore>(Node node) implements StoreReference<U> {}
    private record AdditionalStore<U extends VariableStore>(int id) implements StoreReference<U> {}

    @Override
    public StoreReference<U> requestInputStore(T location, Node node, List<? extends Condition<T, U>> conditions) {
        RegularStore<U> nodeStore = new RegularStore<>(node);
        inputStores.computeIfAbsent(location, _ -> new LinkedHashSet<>()).add(nodeStore);
        storeConditions.computeIfAbsent(nodeStore, _ -> new HashSet<>()).addAll(conditions);

        if (pendingStores.containsKey(nodeStore)) {
            T pendingLoc = pendingStores.get(nodeStore);
            pendingStores.remove(nodeStore);
            outputStores.put(pendingLoc, nodeStore);
        }

        return nodeStore;
    }

    @Override
    public StoreReference<U> requestOutputStore(T location, Node node, List<? extends Condition<T, U>> conditions) {
        RegularStore<U> nodeStore = new RegularStore<>(node);
        outputStores.put(location, new RegularStore<>(node));
        storeConditions.computeIfAbsent(nodeStore, _ -> new HashSet<>()).addAll(conditions);

        return nodeStore;
    }

    @Override
    public StoreReference<U> requestAdditional(T location, List<? extends Condition<T, U>> conditions) {
        AdditionalStore<U> additionalStore = new AdditionalStore<>(additionalStoreId++);
        additionalStores.computeIfAbsent(location, _ -> new LinkedHashSet<>()).add(additionalStore);
        storeConditions.computeIfAbsent(additionalStore, _ -> new HashSet<>()).addAll(conditions);

        return additionalStore;
    }

    @Override
    public StoreReference<U> resolveOutputIfAllocated(T location, Node node) {
        RegularStore<U> nodeStore = new RegularStore<>(node);
        pendingStores.put(nodeStore, location);
        return nodeStore;
    }

    public SequencedSet<StoreReference<U>> getInputStores(T location) {
        return inputStores.containsKey(location) ? inputStores.get(location) : new LinkedHashSet<>();
    }

    public boolean requiresOutputStore(T location) {
        return outputStores.containsKey(location);
    }

    public StoreReference<U> getOutputStore(T location) {
        return outputStores.get(location);
    }

    public SequencedSet<StoreReference<U>> getAdditionalStores(T location) {
        return additionalStores.getOrDefault(location, new LinkedHashSet<>());
    }
}
