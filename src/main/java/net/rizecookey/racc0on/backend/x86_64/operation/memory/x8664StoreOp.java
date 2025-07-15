package net.rizecookey.racc0on.backend.x86_64.operation.memory;

import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.store.x8664MemoryStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664ValOperand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.constant.ConstIntNode;
import net.rizecookey.racc0on.ir.node.Node;

public class x8664StoreOp implements x8664Op {
    private final Node value, address;

    private StoreReference<x8664VarStore> valueRef, addressRef;

    public x8664StoreOp(Node value, Node address) {
        this.value = value;
        this.address = address;

        valueRef = addressRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        if (!(value instanceof ConstIntNode)) {
            valueRef = service.requestInputStore(this, value);
        }
        addressRef = service.requestInputStore(this, address);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664ValOperand valueOperand = value instanceof ConstIntNode constIntNode
                ? new x8664Immediate(constIntNode.value())
                : storeSupplier.resolve(valueRef).orElseThrow();
        x8664VarStore addressStore = storeSupplier.resolve(addressRef).orElseThrow();

        x8664Register actualAddressStore = switch (addressStore) {
            case x8664StackStore _ -> {
                generator.move(x8664Operand.Size.fromValueType(address.valueType()), x8664Register.MEMORY_ACCESS_RESERVE, addressStore);
                yield x8664Register.MEMORY_ACCESS_RESERVE;
            }
            case x8664Register register -> register;
        };

        generator.move(x8664Operand.Size.fromValueType(value.valueType()), new x8664MemoryStore(actualAddressStore), valueOperand);
    }
}
