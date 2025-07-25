package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.store.StoreConditions;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate64;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.constant.ConstAddressNode;
import net.rizecookey.racc0on.ir.node.constant.ConstBoolNode;
import net.rizecookey.racc0on.ir.node.constant.ConstIntNode;
import net.rizecookey.racc0on.ir.node.Node;

import java.util.Optional;

public class x8664LoadConstPhantomOp implements x8664Op {
    private final Node out;
    private final long value;
    private StoreReference<x8664VarStore> outWeakRef;

    public x8664LoadConstPhantomOp(ConstIntNode node) {
        this(node, node.value());
    }

    public x8664LoadConstPhantomOp(ConstBoolNode node) {
        this(node, node.value() ? 1 : 0);
    }

    public x8664LoadConstPhantomOp(ConstAddressNode node) {
        this(node, node.address());
    }

    private x8664LoadConstPhantomOp(Node node, long value) {
        this.out = node;
        this.value = value;
        outWeakRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        outWeakRef = service.requestOutputStore(this, out, StoreConditions.noAllocation());
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        Optional<x8664VarStore> outOp = storeSupplier.resolve(outWeakRef);
        if (outOp.isEmpty()) {
            return;
        }

        x8664Operand immediate = value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE
                ? new x8664Immediate((int) value)
                : new x8664Immediate64(value);
        generator.move(x8664Operand.Size.fromValueType(out.valueType()), outOp.get(), immediate);
    }
}
