package net.rizecookey.racc0on.backend.x86_64.operation;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

public class x8664PhiMoveOp implements x8664Op {
    private final Phi phi;
    private final Node value;
    private StoreReference<x8664Store> inRef, outRef;

    public x8664PhiMoveOp(Phi phi, Node value) {
        this.phi = phi;
        this.value = value;
        inRef = outRef = new StoreReference.Null<>();
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
        inRef = service.requestInputStore(this, value);
        outRef = service.requestOutputStore(this, phi);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664Store in = storeSupplier.resolve(inRef).orElseThrow();
        x8664Store out = storeSupplier.resolve(outRef).orElseThrow();
        generator.move(out, in);
    }
}
