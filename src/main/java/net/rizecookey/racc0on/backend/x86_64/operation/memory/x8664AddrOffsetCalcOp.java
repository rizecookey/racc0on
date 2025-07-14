package net.rizecookey.racc0on.backend.x86_64.operation.memory;

import net.rizecookey.racc0on.backend.memory.MemoryLayout;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.memory.x8664MemoryUtils;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.store.x8664MemoryStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.ConstIntNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.memory.ArrayMemberNode;
import net.rizecookey.racc0on.ir.node.operation.memory.StructMemberNode;

public class x8664AddrOffsetCalcOp implements x8664Op {
    private final int offsetFactor;
    private final Node result, base, offset;

    private StoreReference<x8664VarStore> resultRef, baseRef, offsetRef;

    public x8664AddrOffsetCalcOp(ArrayMemberNode result, Node base, Node offset) {
        this(result, base, offset, x8664MemoryUtils.createLayout(result.elementLayout()).size());
    }

    public x8664AddrOffsetCalcOp(StructMemberNode result, Node base) {
        this(result, base, new ConstIntNode(result.block(), structMemberOffset(result)), 1);
    }

    private static int structMemberOffset(StructMemberNode struct) {
        MemoryLayout.Compound layout = (MemoryLayout.Compound) x8664MemoryUtils.createLayout(struct.structLayout());

        return layout.members().get(struct.memberIndex()).start();
    }

    private x8664AddrOffsetCalcOp(Node result, Node base, Node offset, int offsetFactor) {
        this.result = result;
        this.base = base;
        this.offset = offset;
        this.offsetFactor = offsetFactor;

        resultRef = baseRef = offsetRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        resultRef = service.requestOutputStore(this, result);
        baseRef = service.requestInputStore(this, base);
        if (!(offset instanceof ConstIntNode)) {
            offsetRef = service.requestInputStore(this, offset);
        }
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664VarStore resultStore = storeSupplier.resolve(resultRef).orElseThrow();
        x8664VarStore baseStore = storeSupplier.resolve(baseRef).orElseThrow();
        Integer offsetConstant = offset instanceof ConstIntNode constIntNode ? constIntNode.value() : null;
        x8664Operand offsetOperand = offsetConstant != null
                ? new x8664Immediate(offsetConstant)
                : storeSupplier.resolve(offsetRef).orElseThrow();

        x8664VarStore tempResultStore = resultStore;
        if (resultStore instanceof x8664MemoryStore || resultStore.equals(baseStore)) {
            tempResultStore = x8664Register.MEMORY_ACCESS_RESERVE;
        }

        x8664Operand.Size offsetSize = x8664Operand.Size.fromValueType(offset.valueType());
        x8664Operand.Size pointerSize = x8664Operand.Size.fromValueType(result.valueType());
        generator.move(offsetSize, tempResultStore, offsetOperand);

        if (!offsetSize.equals(pointerSize)) {
            x8664InstrType signExtendInstr = switch (offsetSize) {
                case BYTE, WORD -> x8664InstrType.MOVSX;
                case DOUBLE_WORD -> x8664InstrType.MOVSXD;
                case QUAD_WORD -> throw new IllegalArgumentException("Cannot extend max size");
            };

            generator.write(signExtendInstr, pointerSize, offsetSize, tempResultStore, tempResultStore);
        }

        if (offsetFactor != 1 && (offsetConstant == null || offsetConstant != 0)) {
            generator.write(x8664InstrType.IMUL, pointerSize, tempResultStore, new x8664Immediate(offsetFactor));
        }

        generator.write(x8664InstrType.ADD, pointerSize, tempResultStore, baseStore);

        if (!tempResultStore.equals(resultStore)) {
            generator.move(pointerSize, resultStore, tempResultStore);
        }
    }
}
