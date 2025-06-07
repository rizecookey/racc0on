package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.Arrays;
import java.util.List;

public class x8664OperationSequence implements x8664Op {
    private final List<x8664Op> operations;

    public x8664OperationSequence(List<x8664Op> operations) {
        this.operations = List.copyOf(operations);
    }

    public x8664OperationSequence(x8664Op... ops) {
        this(Arrays.asList(ops));
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
        operations.forEach(op -> op.makeStoreRequests(service));
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        operations.forEach(op -> op.write(generator, storeSupplier));
    }
}
