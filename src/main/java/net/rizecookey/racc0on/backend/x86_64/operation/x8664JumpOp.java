package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Label;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.List;

public class x8664JumpOp implements x8664Op {
    private final String target;

    public x8664JumpOp(String target) {
        this.target = target;
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
    }

    @Override
    public List<String> targetLabels() {
        return List.of(target);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        generator.write(x8664InstrType.JMP, new x8664Label(target));
    }
}
