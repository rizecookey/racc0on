package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664ValOperand;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.constant.ConstIntNode;
import net.rizecookey.racc0on.ir.node.Node;

public abstract class x8664TwoOperandRmOrR32IOp implements x8664Op {
    private final x8664InstrType type;
    private final Node out, inLeft, inRight;
    private StoreReference<x8664VarStore> outRef, inLeftRef, inRightRef;

    public x8664TwoOperandRmOrR32IOp(x8664InstrType type, Operands.Binary<Node> operands) {
        this.type = type;
        this.out = operands.out();
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();

        inLeftRef = inRightRef = outRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        outRef = service.requestOutputStore(this, out);
        if (!(inLeft instanceof ConstIntNode)) {
            inLeftRef = service.requestInputStore(this, inLeft);
        }

        if (!(inRight instanceof ConstIntNode)) {
            inRightRef = service.requestInputStore(this, inRight);
        }
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664VarStore outOp = storeSupplier.resolve(outRef).orElseThrow();
        x8664Operand.Size size = x8664Operand.Size.fromValueType(out.valueType());
        x8664VarStore target;

        if (outOp instanceof x8664StackStore) {
            target = x8664Register.MEMORY_ACCESS_RESERVE;
        } else {
            target = outOp;
        }

        x8664ValOperand inLeftOp = inLeft instanceof ConstIntNode constNode
                ? new x8664Immediate(constNode.value())
                : storeSupplier.resolve(inLeftRef).orElseThrow();
        x8664ValOperand inRightOp = inRight instanceof ConstIntNode constNode
                ? new x8664Immediate(constNode.value())
                : storeSupplier.resolve(inRightRef).orElseThrow();

        x8664TwoOperandRmMrOrMiOp.write(generator, type, size, target, inLeftOp, inRightOp);

        if (!target.equals(outOp)) {
            generator.move(size, outOp, target);
        }
    }
}
