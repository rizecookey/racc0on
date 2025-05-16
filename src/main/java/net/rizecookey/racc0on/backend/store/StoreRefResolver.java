package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.backend.operand.stored.VariableStore;

import java.util.Optional;

@FunctionalInterface
public interface StoreRefResolver<U extends VariableStore> {
    Optional<U> resolve(StoreReference<U> reference);
}
