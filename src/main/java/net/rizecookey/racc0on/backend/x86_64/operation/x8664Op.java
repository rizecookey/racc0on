package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.operation.Operation;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.List;

public interface x8664Op extends Operation<x8664Operand, x8664Store>, x8664OpLike {
    void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service);

    void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier);

    @Override
    default List<x8664Op> asOperations() {
        return List.of(this);
    }
}
