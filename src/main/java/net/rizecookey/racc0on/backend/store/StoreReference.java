package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.backend.operand.store.VariableStore;

public interface StoreReference<U extends VariableStore> {
    record Null<U extends VariableStore>() implements StoreReference<U> {}
}
