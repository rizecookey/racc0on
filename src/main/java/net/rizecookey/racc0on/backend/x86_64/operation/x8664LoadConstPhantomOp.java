package net.rizecookey.racc0on.backend.x86_64.operation;

import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.Optional;

public class x8664LoadConstPhantomOp implements x8664Op {
    private final ConstIntNode out;
    private StoreReference<x8664StoreLocation> outWeakRef;

    public x8664LoadConstPhantomOp(ConstIntNode node) {
        out = node;
        outWeakRef = new StoreReference.Null<>();
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664StoreLocation> service) {
        outWeakRef = service.resolveIfAllocated(out);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        Optional<x8664StoreLocation> outOp = storeSupplier.resolve(outWeakRef);
        if (outOp.isEmpty()) {
            return;
        }

        generator.move(outOp.get(), new x8664Immediate(out.value()));
    }
}
