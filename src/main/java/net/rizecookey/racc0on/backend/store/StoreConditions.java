package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.backend.operand.stored.VariableStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record StoreConditions<U extends VariableStore>(List<U> targetedStores, List<U> collisions) {
    public static <U extends VariableStore> Builder<U> builder() {
        return new Builder<>();
    }

    public static <U extends VariableStore> StoreConditions<U> empty() {
        return new StoreConditions<>(List.of(), List.of());
    }

    public StoreConditions<U> merge(StoreConditions<U> other) {
        List<U> targeted = new ArrayList<>(targetedStores());
        targeted.addAll(other.targetedStores());
        List<U> collisions = new ArrayList<>(collisions());
        collisions.addAll(other.collisions());
        return new StoreConditions<>(List.copyOf(targeted), List.copyOf(collisions));
    }

    public static class Builder<U extends VariableStore> {
        private final List<U> targetedStores;
        private final List<U> collidesWith;

        private Builder() {
            targetedStores = new ArrayList<>();
            collidesWith = new ArrayList<>();
        }

        // TODO potentially add optional targets for optimizations
        public Builder<U> targets(U store) {
            targetedStores.add(store);
            return this;
        }

        public Builder<U> targets(Collection<? extends U> stores) {
            targetedStores.addAll(stores);
            return this;
        }

        public Builder<U> collidesWith(U store) {
            collidesWith.add(store);
            return this;
        }

        public Builder<U> collidesWith(Collection<? extends U> stores) {
            collidesWith.addAll(stores);
            return this;
        }

        public StoreConditions<U> build() {
            return new StoreConditions<>(List.copyOf(targetedStores), List.copyOf(collidesWith));
        }
    }
}
