package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

public record x8664MovOp(x8664Store to, x8664Operand from) implements x8664Op {
    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664Operand actualFrom = from;
        if (to instanceof x8664StackStore && from instanceof x8664StackStore) {
            actualFrom = x8664Register.MEMORY_ACCESS_RESERVE;
            generator.write(x8664InstrType.MOV, x8664Register.MEMORY_ACCESS_RESERVE, from);
        }

        generator.write(x8664InstrType.MOV, to, actualFrom);
    }
}
