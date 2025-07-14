package net.rizecookey.racc0on.backend.x86_64.operation.memory;

import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Label;
import net.rizecookey.racc0on.backend.x86_64.operand.store.x8664MemoryStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.ConstIntNode;
import net.rizecookey.racc0on.ir.node.Node;

public class x8664ArrayBoundsCheckOp implements x8664Op {
    private static final x8664Label ABORT_LABEL = new x8664Label("$abort$");

    private final Node array, index;
    private StoreReference<x8664VarStore> arrayRef, indexRef;

    public x8664ArrayBoundsCheckOp(Node array, Node index) {
        this.array = array;
        this.index = index;

        arrayRef = indexRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        arrayRef = service.requestInputStore(this, array);
        if (!(index instanceof ConstIntNode)) {
            indexRef = service.requestInputStore(this, index);
        }
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        Integer indexConstant = index instanceof ConstIntNode constIntNode ? constIntNode.value() : null;

        x8664Operand indexOperand;
        if (indexConstant != null) {
            if (indexConstant < 0) {
                generator.write(x8664InstrType.JMP, ABORT_LABEL);
                return;
            }
            indexOperand = new x8664Immediate(indexConstant);
        } else {
            indexOperand = storeSupplier.resolve(indexRef).orElseThrow();
        }

        x8664VarStore arrayStore = storeSupplier.resolve(arrayRef).orElseThrow();
        x8664Operand.Size arrayTypeSize = x8664Operand.Size.fromValueType(array.valueType());
        x8664Operand.Size indexTypeSize = x8664Operand.Size.fromValueType(index.valueType());

        x8664Register actualArrayStore = switch (arrayStore) {
            case x8664StackStore _ -> {
                generator.move(arrayTypeSize, x8664Register.MEMORY_ACCESS_RESERVE, arrayStore);
                yield x8664Register.MEMORY_ACCESS_RESERVE;
            }
            case x8664Register register -> register;
        };
        generator.move(indexTypeSize, x8664Register.MEMORY_ACCESS_RESERVE, new x8664MemoryStore(actualArrayStore, -4));
        generator.write(x8664InstrType.CMP, indexTypeSize, x8664Register.MEMORY_ACCESS_RESERVE, indexOperand);
        generator.write(x8664InstrType.JLE, ABORT_LABEL);

        if (indexConstant == null) {
            generator.write(x8664InstrType.CMP, indexTypeSize, indexOperand, new x8664Immediate(0));
            generator.write(x8664InstrType.JL, ABORT_LABEL);
        }
    }
}
