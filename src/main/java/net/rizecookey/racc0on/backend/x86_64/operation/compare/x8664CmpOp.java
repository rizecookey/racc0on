package net.rizecookey.racc0on.backend.x86_64.operation.compare;

import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.store.StoreConditions;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.Node;

public abstract class x8664CmpOp implements x8664Op {
    private final Node inLeft, inRight, out;
    private final x8664InstrType setInstr;
    private StoreReference<x8664VarStore> inLeftRef, inRightRef, outRef;

    public x8664CmpOp(x8664InstrType setInstr, Operands.Binary<Node> operands) {
        this.setInstr = setInstr;
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();
        this.out = operands.out();

        inLeftRef = inRightRef = outRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        inLeftRef = service.requestInputStore(this, inLeft);
        inRightRef = service.requestInputStore(this, inRight);
        /* do not allocate a store if this comparison is only used for a conditional jump */
        outRef = service.requestOutputStore(this, out, StoreConditions.noAllocation());
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664VarStore inLeftStore = storeSupplier.resolve(inLeftRef).orElseThrow();
        x8664VarStore inRightStore = storeSupplier.resolve(inRightRef).orElseThrow();
        x8664VarStore outStore = storeSupplier.resolve(outRef).orElse(null);
        x8664Operand.Size size = x8664Operand.Size.fromValueType(out.valueType());

        x8664VarStore actualInRight = inRightStore;
        if (inLeftStore instanceof x8664StackStore && inRightStore instanceof x8664StackStore) {
            actualInRight = x8664Register.MEMORY_ACCESS_RESERVE;
            generator.move(size, actualInRight, inRightStore);
        }

        generator.write(x8664InstrType.CMP, size, inLeftStore, actualInRight);
        if (outStore != null) {
            generator.write(setInstr, x8664Operand.Size.BYTE, outStore);
        }
    }
}
