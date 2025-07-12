package net.rizecookey.racc0on.backend.x86_64.operation.logic;

import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

public class x8664NotOp implements x8664Op {
    private final Node in, out;
    private StoreReference<x8664VarStore> inRef, outRef;

    public x8664NotOp(Node out, Node in) {
        this.in = in;
        this.out = out;

        inRef = outRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        inRef = service.requestInputStore(this, in);
        outRef = service.requestOutputStore(this, out);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664VarStore in = storeSupplier.resolve(inRef).orElseThrow();
        x8664VarStore out = storeSupplier.resolve(outRef).orElseThrow();
        generator.test(in, in);
        generator.write(x8664InstrType.SETZ, x8664Operand.Size.BYTE, out);
    }
}
