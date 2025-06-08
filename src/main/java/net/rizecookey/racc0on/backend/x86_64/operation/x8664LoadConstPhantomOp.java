package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.ir.node.ConstBoolNode;
import net.rizecookey.racc0on.ir.node.ConstIntNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.Optional;

public class x8664LoadConstPhantomOp implements x8664Op {
    private final Node out;
    private final int value;
    private StoreReference<x8664Store> outWeakRef;

    public x8664LoadConstPhantomOp(ConstIntNode node) {
        this(node, node.value());
    }

    public x8664LoadConstPhantomOp(ConstBoolNode node) {
        this(node, node.value() ? 1 : 0);
    }

    private x8664LoadConstPhantomOp(Node node, int value) {
        this.out = node;
        this.value = value;
        outWeakRef = new StoreReference.Null<>();
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
        outWeakRef = service.resolveOutputIfAllocated(this, out);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        Optional<x8664Store> outOp = storeSupplier.resolve(outWeakRef);
        if (outOp.isEmpty()) {
            return;
        }

        generator.move(outOp.get(), new x8664Immediate(value));
    }
}
