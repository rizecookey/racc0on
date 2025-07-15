package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.Phi;

public class x8664PhiMoveOp implements x8664Op {
    private final Phi phi;
    private final Node value;
    private StoreReference<x8664VarStore> inRef, outRef;

    public x8664PhiMoveOp(Phi phi, Node value) {
        this.phi = phi;
        this.value = value;
        inRef = outRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        inRef = service.requestInputStore(this, value);
        outRef = service.requestOutputStore(this, phi);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664VarStore in = storeSupplier.resolve(inRef).orElseThrow();
        x8664VarStore out = storeSupplier.resolve(outRef).orElseThrow();
        generator.move(x8664Operand.Size.fromValueType(phi.valueType()), out, in);
    }
}
