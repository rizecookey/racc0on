package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.stored.VariableStore;
import net.rizecookey.racc0on.backend.operation.Operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface StoreRequestService<T extends Operation<?, U>, U extends VariableStore> {
    default StoreReference<U> requestInputStore(T location, Node node) {
        return requestInputStore(location, node, Conditions.empty());
    }
    StoreReference<U> requestInputStore(T location, Node node, Conditions<U> conditions);

    default StoreReference<U> requestOutputStore(T location, Node node) {
        return requestOutputStore(location, node, Conditions.empty());
    }
    StoreReference<U> requestOutputStore(T location, Node node, Conditions<U> conditions);

    default StoreReference<U> requestAdditional(T location) {
        return requestAdditional(location, Conditions.empty());
    }
    StoreReference<U> requestAdditional(T location, Conditions<U> conditions);

    StoreReference<U> resolveOutputIfAllocated(T location, Node node);

    class ConditionBuilder<U extends VariableStore> {
        private final List<U> preferredStores;
        private final List<U> collidesWith;

        private ConditionBuilder() {
            preferredStores = new ArrayList<>();
            collidesWith = new ArrayList<>();
        }

        public ConditionBuilder<U> prefers(U store) {
            preferredStores.add(store);
            return this;
        }

        public ConditionBuilder<U> prefers(Collection<? extends U> stores) {
            preferredStores.addAll(stores);
            return this;
        }

        public ConditionBuilder<U> collidesWith(U store) {
            collidesWith.add(store);
            return this;
        }

        public ConditionBuilder<U> collidesWith(Collection<? extends U> stores) {
            collidesWith.addAll(stores);
            return this;
        }

        public Conditions<U> build() {
            return new Conditions<>(List.copyOf(preferredStores), List.copyOf(collidesWith));
        }
    }

    record Conditions<U extends VariableStore>(List<U> preferredLocations, List<U> collisions) {
        public static <U extends VariableStore> ConditionBuilder<U> builder() {
            return new ConditionBuilder<>();
        }

        public static <U extends VariableStore> Conditions<U> empty() {
            return new Conditions<>(List.of(), List.of());
        }

        public Conditions<U> merge(Conditions<U> other) {
            List<U> preferredLocations = new ArrayList<>(preferredLocations());
            preferredLocations.addAll(other.preferredLocations());
            List<U> collisions = new ArrayList<>(collisions());
            collisions.addAll(other.collisions());
            return new Conditions<>(List.copyOf(preferredLocations), List.copyOf(collisions));
        }
    }
}
