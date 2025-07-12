package net.rizecookey.racc0on.backend.x86_64.operation.memory;

import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Label;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664MemoryLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.Node;

public class x8664ArrayBoundsCheckOp implements x8664Op {
    private static final x8664Label ABORT_LABEL = new x8664Label("$abort$");

    private final Node array, index;
    private StoreReference<x8664Store> arrayRef, indexRef;

    public x8664ArrayBoundsCheckOp(Node array, Node index) {
        this.array = array;
        this.index = index;

        arrayRef = indexRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664Store> service) {
        arrayRef = service.requestInputStore(this, array);
        indexRef = service.requestInputStore(this, index);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664Store arrayStore = storeSupplier.resolve(arrayRef).orElseThrow();
        x8664Operand.Size size = x8664Operand.Size.fromValueType(array.valueType());
        x8664Store indexStore = storeSupplier.resolve(indexRef).orElseThrow();

        x8664Register actualArrayStore = switch (arrayStore) {
            case x8664StackStore _ -> {
                generator.move(size, x8664Register.MEMORY_ACCESS_RESERVE, arrayStore);
                yield x8664Register.MEMORY_ACCESS_RESERVE;
            }
            case x8664Register register -> register;
        };
        generator.move(size, x8664Register.MEMORY_ACCESS_RESERVE, new x8664MemoryLocation(actualArrayStore, -4));
        generator.write(x8664InstrType.CMP, size, indexStore, x8664Register.MEMORY_ACCESS_RESERVE);
        generator.write(x8664InstrType.JGE, ABORT_LABEL);
        generator.write(x8664InstrType.CMP, size, indexStore, new x8664Immediate(0));
        generator.write(x8664InstrType.JL, ABORT_LABEL);
    }
}
