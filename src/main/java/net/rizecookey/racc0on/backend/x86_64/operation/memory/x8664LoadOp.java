package net.rizecookey.racc0on.backend.x86_64.operation.memory;

import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.store.x8664MemoryStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.memory.LoadNode;

public class x8664LoadOp implements x8664Op {
    private final LoadNode out;
    private final Node address;
    private StoreReference<x8664VarStore> outRef, addressRef;

    public x8664LoadOp(LoadNode out, Node address) {
        this.out = out;
        this.address = address;

        outRef = addressRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        outRef = service.requestOutputStore(this, out);
        addressRef = service.requestInputStore(this, address);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664VarStore outStore = storeSupplier.resolve(outRef).orElseThrow();
        x8664VarStore addressStore = storeSupplier.resolve(addressRef).orElseThrow();

        x8664Register addressRegister = switch (addressStore) {
            case x8664StackStore _ -> {
                generator.move(x8664Operand.Size.fromValueType(address.valueType()), x8664Register.MEMORY_ACCESS_RESERVE, addressStore);
                yield x8664Register.MEMORY_ACCESS_RESERVE;
            }
            case x8664Register register -> register;
        };

        generator.move(x8664Operand.Size.fromValueType(out.valueType()), outStore, new x8664MemoryStore(addressRegister));
    }
}
