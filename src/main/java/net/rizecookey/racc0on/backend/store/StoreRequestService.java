package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.stored.VariableStore;
import net.rizecookey.racc0on.backend.operation.Operation;

public interface StoreRequestService<T extends Operation<?, U>, U extends VariableStore> {
    default StoreReference<U> requestInputStore(T location, Node node) {
        return requestInputStore(location, node, StoreConditions.empty());
    }
    StoreReference<U> requestInputStore(T location, Node node, StoreConditions<U> conditions);

    default StoreReference<U> requestOutputStore(T location, Node node) {
        return requestOutputStore(location, node, StoreConditions.empty());
    }
    StoreReference<U> requestOutputStore(T location, Node node, StoreConditions<U> conditions);

    default StoreReference<U> requestAdditional(T location) {
        return requestAdditional(location, StoreConditions.empty());
    }
    StoreReference<U> requestAdditional(T location, StoreConditions<U> conditions);

    StoreReference<U> resolveOutputIfAllocated(T location, Node node);

}
