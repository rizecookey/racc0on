package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.store.VariableStore;
import net.rizecookey.racc0on.backend.operation.Operation;

public interface StoreRequestService<T extends Operation<?, U>, U extends VariableStore> {
    default StoreReference<U> requestInputStore(T location, Node node) {
        return requestInputStore(location, node, StoreConditions.empty());
    }
    default StoreReference<U> requestInputStore(T location, Node node, StoreConditions<U> conditions) {
        return requestInputStore(location, node, (_, _) -> conditions);
    }
    StoreReference<U> requestInputStore(T location, Node node, StoreConditions.Supplier<T, U> conditionsSupplier);

    default StoreReference<U> requestOutputStore(T location, Node node) {
        return requestOutputStore(location, node, StoreConditions.empty());
    }
    default StoreReference<U> requestOutputStore(T location, Node node, StoreConditions<U> conditions) {
        return requestOutputStore(location, node, (_, _) -> conditions);
    }
    StoreReference<U> requestOutputStore(T location, Node node, StoreConditions.Supplier<T, U> conditionsSupplier);

    default StoreReference<U> requestAdditional(T location) {
        return requestAdditional(location, StoreConditions.empty());
    }
    default  StoreReference<U> requestAdditional(T location, StoreConditions<U> conditions) {
        return requestAdditional(location, (_, _) -> conditions);
    }
    StoreReference<U> requestAdditional(T location, StoreConditions.Supplier<T, U> conditionsSupplier);
}
