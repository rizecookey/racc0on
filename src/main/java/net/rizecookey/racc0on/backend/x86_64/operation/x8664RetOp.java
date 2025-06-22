package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

public class x8664RetOp implements x8664Op {
    private final Node returnValue;
    private StoreReference<x8664Store> inRef;

    public x8664RetOp(Node returnValue) {
        this.returnValue = returnValue;
        inRef = new StoreReference.Null<>();
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
        inRef = service.requestInputStore(this, returnValue);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664Store returnLocation = storeSupplier.resolve(inRef).orElseThrow();
        if (returnLocation != x8664Register.RAX) {
            generator.move(x8664Register.RAX, returnLocation);
        }

        x8664Register.getRegisterSet().stream()
                .filter(x8664Register::isCalleeSaved)
                .forEach(generator::pop); // TODO see x8664EnterOp.java

        generator.tearDownStack();
        generator.write(x8664InstrType.RET);
    }
}
