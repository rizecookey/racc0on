package net.rizecookey.racc0on.backend.x86_64.operation.memory;

import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Label;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.Node;

public class x8664AddressNullCheckOp implements x8664Op {
    private static final x8664Label SEGFAULT_LABEL = new x8664Label("$segfault$");

    private final Node address;

    private StoreReference<x8664VarStore> addressRef;

    public x8664AddressNullCheckOp(Node address) {
        this.address = address;

        addressRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        addressRef = service.requestInputStore(this, address);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664VarStore addressStore = storeSupplier.resolve(addressRef).orElseThrow();
        generator.test(x8664Operand.Size.fromValueType(address.valueType()), addressStore, addressStore);
        generator.write(x8664InstrType.JZ, SEGFAULT_LABEL);
    }
}
