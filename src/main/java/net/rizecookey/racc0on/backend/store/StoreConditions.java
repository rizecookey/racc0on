package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.backend.operand.stored.VariableStore;
import net.rizecookey.racc0on.backend.operation.Operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public sealed interface StoreConditions<U extends VariableStore> permits StoreConditions.AllocationRules, StoreConditions.NoAllocation {
    static <U extends VariableStore> Builder<U> builder() {
        return new Builder<>();
    }

    static <U extends VariableStore> StoreConditions<U> empty() {
        return new AllocationRules<>(List.of(), false, List.of());
    }

    static <U extends VariableStore> StoreConditions<U> noAllocation() {
        return new NoAllocation<>();
    }

    List<U> targetedStores();
    boolean targetsOptional();
    List<U> collisions();
    boolean shouldAllocate();

    default StoreConditions<U> merge(StoreConditions<U> other) {
        List<U> targeted = new ArrayList<>(targetedStores());
        targeted.addAll(other.targetedStores());
        List<U> collisions = new ArrayList<>(collisions());
        collisions.addAll(other.collisions());
        return new AllocationRules<>(List.copyOf(targeted), other.targetsOptional() && targetsOptional(),
                List.copyOf(collisions));
    }

    record AllocationRules<U extends VariableStore>(List<U> targetedStores, boolean targetsOptional, List<U> collisions)
            implements StoreConditions<U> {
        @Override
        public boolean shouldAllocate() {
            return true;
        }
    }

    final class NoAllocation<U extends VariableStore> implements StoreConditions<U> {

        @Override
        public List<U> targetedStores() {
            return List.of();
        }

        @Override
        public boolean targetsOptional() {
            return false;
        }

        @Override
        public List<U> collisions() {
            return List.of();
        }

        @Override
        public boolean shouldAllocate() {
            return false;
        }

        @Override
        public StoreConditions<U> merge(StoreConditions<U> other) {
            if (!other.shouldAllocate()) {
                return this;
            }
            return StoreConditions.super.merge(other);
        }
    }

    class Builder<U extends VariableStore> {
        private final List<U> targetedStores;
        private boolean targetsOptional;
        private final List<U> collidesWith;

        private Builder() {
            targetedStores = new ArrayList<>();
            targetsOptional = false;
            collidesWith = new ArrayList<>();
        }

        public Builder<U> targets(U store) {
            targetedStores.add(store);
            return this;
        }

        public Builder<U> targets(Collection<? extends U> stores) {
            targetedStores.addAll(stores);
            return this;
        }

        public Builder<U> targetsOptional(boolean optional) {
            this.targetsOptional = optional;
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
            return new StoreConditions.AllocationRules<>(List.copyOf(targetedStores), targetsOptional,
                    List.copyOf(collidesWith));
        }
    }

    @FunctionalInterface
    interface Supplier<T extends Operation<?, U>, U extends VariableStore> {
        StoreConditions<U> supply(StoreReference<U> store, LivenessMap<T, U> liveness);
    }
}
