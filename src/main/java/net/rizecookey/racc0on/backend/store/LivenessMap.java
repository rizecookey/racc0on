package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.backend.operand.stored.VariableStore;
import net.rizecookey.racc0on.backend.operation.Operation;
import net.rizecookey.racc0on.backend.operation.OperationBlock;
import net.rizecookey.racc0on.backend.operation.OperationSchedule;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;

public class LivenessMap<T extends Operation<?, U>, U extends VariableStore> {
    private final Map<T, SequencedSet<StoreReference<U>>> map;

    public LivenessMap() {
        this(Map.of());
    }

    public LivenessMap(Map<T, SequencedSet<StoreReference<U>>> liveness) {
        map = new HashMap<>(liveness);
    }

    public SequencedSet<StoreReference<U>> getLiveAt(T at) {
        return map.containsKey(at) ? map.get(at) : new LinkedHashSet<>();
    }

    public boolean isLiveAt(T at, StoreReference<U> other) {
        return getLiveAt(at).contains(other);
    }

    public boolean addLiveAt(T at, StoreReference<U> other) {
        return map.computeIfAbsent(at, _ -> new LinkedHashSet<>()).add(other);
    }

    public boolean addLiveAt(T at, Collection<? extends StoreReference<U>> others) {
        return map.computeIfAbsent(at, _ -> new LinkedHashSet<>()).addAll(others);
    }

    public boolean removeLiveAt(T at, StoreReference<U> other) {
        return map.computeIfAbsent(at, _ -> new LinkedHashSet<>()).remove(other);
    }

    public boolean removeLiveAt(T at, Collection<? extends StoreReference<U>> others) {
        return map.computeIfAbsent(at, _ -> new LinkedHashSet<>()).removeAll(others);
    }

    /**
     * Propagates all variables that are live at from and are not an output of to to being live
     * at to as well.
     *
     * @param from the operation from which to propagate the liveness
     * @param to   the operation whose liveness is to be set
     */
    public boolean propagateLiveness(T from, T to, StoreRequests<T, U> requests) {
        boolean changed = false;
        for (StoreReference<U> liveInFrom : getLiveAt(from)) {
            if (liveInFrom.equals(requests.getOutputStore(to))) {
                continue;
            }

            changed |= addLiveAt(to, liveInFrom);
        }

        return changed;
    }

    public static <T extends Operation<?, U>, U extends VariableStore> LivenessMap<T, U> calculateFor(OperationSchedule<T> schedule, StoreRequests<T, U> requests) {
        LivenessMap<T, U> liveness = new LivenessMap<>();

        boolean changed;
        do {
            changed = false;
            for (OperationBlock<T> block : List.copyOf(schedule.blocks().values()).reversed()) {
                for (int i = block.operations().size() - 1; i >= 0; i--) {
                    T op = block.operations().get(i);
                    changed |= liveness.addLiveAt(op, requests.getInputStores(op));

                    if (i < block.operations().size() - 1) {
                        changed |= liveness.propagateLiveness(block.operations().get(i + 1), op, requests);
                    }

                    for (var target : op.targetLabels()) {
                        T opTarget = schedule.blocks().get(target).operations().getFirst();
                        changed |= liveness.propagateLiveness(opTarget, op, requests);
                    }
                }
            }
        } while (changed);

        return liveness;
    }
}
