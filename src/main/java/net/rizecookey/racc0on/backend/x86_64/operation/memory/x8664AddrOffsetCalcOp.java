package net.rizecookey.racc0on.backend.x86_64.operation.memory;

import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.memory.x8664MemoryUtils;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.store.x8664MemoryStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664ValOperand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.constant.ConstIntNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.memory.ArrayMemberNode;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class x8664AddrOffsetCalcOp implements x8664Op {
    private final int offsetFactor;
    private final Node result, base, offset;

    private StoreReference<x8664VarStore> resultRef, baseRef, offsetRef;

    public x8664AddrOffsetCalcOp(ArrayMemberNode result, Node base, Node offset) {
        this(result, base, offset, x8664MemoryUtils.createLayout(result.elementLayout()).size());
    }

    public x8664AddrOffsetCalcOp(Node result, Node base, int offset) {
        this(result, base, new ConstIntNode(result.block(), offset), 1);
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
        x8664ValOperand offsetOperand = offsetConstant != null
                ? new x8664Immediate(offsetConstant)
                : storeSupplier.resolve(offsetRef).orElseThrow();

        x8664Register tempResultStore = switch (resultStore) {
            case x8664Register register -> register;
            case x8664StackStore _ -> x8664Register.MEMORY_ACCESS_RESERVE;
        };

        x8664Operand.Size pointerSize = x8664Operand.Size.fromValueType(result.valueType());
        x8664Operand.Size offsetSize = x8664Operand.Size.fromValueType(offset.valueType());

        x8664Register leaBase = baseStore instanceof x8664Register register ? register : null;
        x8664Operand.Size leaScalar = sizeFromInt(offsetFactor);
        x8664Register leaIndex = leaScalar != null && offsetOperand instanceof x8664Register register ? register : null;
        int leaOffset = offsetConstant != null ? offsetConstant * offsetFactor : 0;
        if (leaIndex == null && offsetConstant == null) {
            // offset is not in a register
            if (tempResultStore.equals(baseStore)) {
                tempResultStore = x8664Register.MEMORY_ACCESS_RESERVE;
            }
            generator.move(offsetSize, tempResultStore, offsetOperand);
            if (leaScalar == null) {
                generator.write(x8664InstrType.IMUL, pointerSize, tempResultStore, new x8664Immediate(offsetFactor));
                leaScalar = x8664Operand.Size.BYTE;
            }
            leaIndex = tempResultStore;
        }
        if (leaBase != null || leaIndex != null || leaOffset != 0) {
            generator.write(x8664InstrType.LEA, pointerSize, tempResultStore,
                    new x8664MemoryStore(leaBase, leaIndex, Objects.requireNonNullElse(leaScalar, x8664Operand.Size.DOUBLE_WORD), leaOffset));
        }
        if (leaBase == null) {
            generator.write(x8664InstrType.ADD, pointerSize, tempResultStore, baseStore);
        }

        if (!tempResultStore.equals(resultStore)) {
            generator.move(pointerSize, resultStore, tempResultStore);
        }
    }

    private static x8664Operand.@Nullable Size sizeFromInt(int value) {
        return switch (value) {
            case 1 -> x8664Operand.Size.BYTE;
            case 2 -> x8664Operand.Size.WORD;
            case 4 -> x8664Operand.Size.DOUBLE_WORD;
            case 8 -> x8664Operand.Size.QUAD_WORD;
            default -> null;
        };
    }
}
