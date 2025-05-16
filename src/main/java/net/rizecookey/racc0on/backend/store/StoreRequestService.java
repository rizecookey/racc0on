package net.rizecookey.racc0on.backend.store;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.stored.VariableStore;
import net.rizecookey.racc0on.backend.operation.Operation;

import java.util.List;

public interface StoreRequestService<T extends Operation<?, U>, U extends VariableStore> {
    default StoreReference<U> requestInputStore(T location, Node node) {
        return requestInputStore(location, node, List.of());
    }
    StoreReference<U> requestInputStore(T location, Node node, List<? extends Condition<T, U>> conditions);

    default StoreReference<U> requestOutputStore(T location, Node node) {
        return requestOutputStore(location, node, List.of());
    }
    StoreReference<U> requestOutputStore(T location, Node node, List<? extends Condition<T, U>> conditions);

    default StoreReference<U> requestAdditional(T location) {
        return requestAdditional(location, List.of());
    }
    StoreReference<U> requestAdditional(T location, List<? extends Condition<T, U>> conditions);

    StoreReference<U> resolveOutputIfAllocated(T location, Node node);

    sealed interface Condition<T extends Operation<?, U>, U extends VariableStore> permits PreferredLocation {}
    record PreferredLocation<T extends Operation<?, U>, U extends VariableStore>(T location) implements Condition<T, U> {}
}
