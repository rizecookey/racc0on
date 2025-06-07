package net.rizecookey.racc0on.backend.x86_64.operation.compare;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

public abstract class x8664CmpOp implements x8664Op {
    private final Node inLeft, inRight, out;
    private final x8664InstrType setInstr;
    private StoreReference<x8664Store> inLeftRef, inRightRef, outRef;

    public x8664CmpOp(x8664InstrType setInstr, Operands.Binary<Node> operands) {
        this.setInstr = setInstr;
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();
        this.out = operands.out();

        inLeftRef = inRightRef = outRef = new StoreReference.Null<>();
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
        inLeftRef = service.requestInputStore(this, inLeft);
        inRightRef = service.requestInputStore(this, inRight);
        outRef = service.requestOutputStore(this, out);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664Store inLeft = storeSupplier.resolve(inLeftRef).orElseThrow();
        x8664Store inRight = storeSupplier.resolve(inRightRef).orElseThrow();
        x8664Store out = storeSupplier.resolve(outRef).orElseThrow();

        x8664Store actualInRight = inRight;
        if (inLeft instanceof x8664StackStore && inRight instanceof x8664StackStore) {
            actualInRight = x8664Register.MEMORY_ACCESS_RESERVE;
            generator.move(actualInRight, inRight);
        }

        generator.write(x8664InstrType.CMP, inLeft, actualInRight);
        generator.write(setInstr, out);
    }
}
