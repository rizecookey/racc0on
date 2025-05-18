package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.backend.operand.stored.VariableStore;
import net.rizecookey.racc0on.backend.operation.Operation;

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

    public void addLiveAt(T at, StoreReference<U> other) {
        map.computeIfAbsent(at, _ -> new LinkedHashSet<>()).add(other);
    }

    public void addLiveAt(T at, Collection<? extends StoreReference<U>> others) {
        map.computeIfAbsent(at, _ -> new LinkedHashSet<>()).addAll(others);
    }

    public void removeLiveAt(T at, StoreReference<U> other) {
        map.computeIfAbsent(at, _ -> new LinkedHashSet<>()).remove(other);
    }

    public void removeLiveAt(T at, Collection<? extends StoreReference<U>> others) {
        map.computeIfAbsent(at, _ -> new LinkedHashSet<>()).removeAll(others);
    }

    /**
     * Propagates all variables that are live at from and are not an output of to to being live
     * at to as well.
     *
     * @param from the operation from which to propagate the liveness
     * @param to   the operation whose liveness is to be set
     */
    public void propagateLiveness(T from, T to, StoreRequests<T, U> requests) {
        for (StoreReference<U> liveInFrom : getLiveAt(from)) {
            if (liveInFrom.equals(requests.getOutputStore(to))) {
                continue;
            }

            addLiveAt(to, liveInFrom);
        }
    }

    public static <T extends Operation<?, U>, U extends VariableStore> LivenessMap<T, U> calculateFor(List<T> program, StoreRequests<T, U> requests) {
        LivenessMap<T, U> liveness = new LivenessMap<>();

        for (int i = program.size() - 1; i >= 0; i--) {
            T op = program.get(i);

            liveness.addLiveAt(op, requests.getInputStores(op));

            if (i > 0) {
                liveness.propagateLiveness(op, program.get(i - 1), requests);
            }
        }

        return liveness;
    }
}
