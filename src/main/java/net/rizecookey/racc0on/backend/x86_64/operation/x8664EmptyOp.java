package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

public class x8664EmptyOp implements x8664Op {
    public static final x8664EmptyOp INSTANCE = new x8664EmptyOp();

    private x8664EmptyOp() {}

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {

    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {

    }
}
