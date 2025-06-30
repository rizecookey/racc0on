package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.Set;

public class x8664EnterOp implements x8664Op {
    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664Store> service) {

    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        generator.prepareStack();

        Set<x8664Store> writtenTo = generator.getWrittenTo();
        x8664Register.getRegisterSet().reversed().stream()
                .filter(x8664Register::isCalleeSaved)
                .filter(writtenTo::contains)
                .forEach(generator::push);
    }
}
