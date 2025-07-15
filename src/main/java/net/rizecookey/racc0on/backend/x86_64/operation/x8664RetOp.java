package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.Node;

import java.util.Set;

public class x8664RetOp implements x8664Op {
    private final Node returnValue;
    private StoreReference<x8664VarStore> inRef;

    public x8664RetOp(Node returnValue) {
        this.returnValue = returnValue;
        inRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        inRef = service.requestInputStore(this, returnValue);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664VarStore returnLocation = storeSupplier.resolve(inRef).orElseThrow();
        if (returnLocation != x8664Register.RAX) {
            generator.move(x8664Operand.Size.QUAD_WORD, x8664Register.RAX, returnLocation);
        }

        Set<x8664VarStore> writtenTo = generator.getWrittenTo();
        x8664Register.getRegisterSet().stream()
                .filter(x8664Register::isCalleeSaved)
                .filter(writtenTo::contains)
                .forEach(generator::pop);

        generator.tearDownStack();
        generator.write(x8664InstrType.RET);
    }
}
