package net.rizecookey.racc0on.backend.x86_64.operation.arithmetic;

import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.store.StoreConditions;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.ConstIntNode;
import net.rizecookey.racc0on.ir.node.Node;

public class x8664ShiftOp implements x8664Op {
    private final Direction direction;
    private final Node shiftee, shiftCount, out;

    private StoreReference<x8664VarStore> shifteeRef, shiftCountRef, outRef, backupRef;

    public x8664ShiftOp(Direction direction, Operands.Binary<Node> operands) {
        this.direction = direction;
        this.out = operands.out();
        this.shiftee = operands.inLeft();
        this.shiftCount = operands.inRight();

        shifteeRef = shiftCountRef = outRef = backupRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        StoreConditions<x8664VarStore> collidesWithRcx = StoreConditions.<x8664VarStore>builder()
                .collidesWith(x8664Register.RCX)
                .build();
        if (!(shiftee instanceof ConstIntNode)) {
            shifteeRef = service.requestInputStore(this, shiftee, collidesWithRcx);
        }

        if (!(shiftCount instanceof ConstIntNode)) {
            shiftCountRef = service.requestInputStore(this, shiftCount);
        }

        outRef = service.requestOutputStore(this, out, collidesWithRcx);

        backupRef = service.requestAdditional(this, collidesWithRcx);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664VarStore outStore = storeSupplier.resolve(outRef).orElseThrow();
        x8664Operand.Size size = x8664Operand.Size.fromValueType(out.valueType());
        x8664Operand shifteeStore = this.shiftee instanceof ConstIntNode c
                ? new x8664Immediate(c.value())
                : storeSupplier.resolve(shifteeRef).orElseThrow();
        x8664Operand shiftCountStore = this.shiftCount instanceof ConstIntNode c
                ? new x8664Immediate(c.value())
                : storeSupplier.resolve(shiftCountRef).orElseThrow();
        x8664VarStore backupStore = storeSupplier.resolve(backupRef).orElseThrow();

        boolean backupRcx = false;
        if (!shiftCountStore.equals(x8664Register.RCX)) {
            if (generator.getLiveStores().contains(x8664Register.RCX)) {
                backupRcx = true;
                generator.move(x8664Operand.Size.QUAD_WORD, backupStore, x8664Register.RCX);
            }
            generator.move(size, x8664Register.RCX, shiftCountStore);
        }

        if (!shifteeStore.equals(outStore)) {
            generator.move(size, outStore, shifteeStore);
        }

        generator.write(direction == Direction.LEFT ? x8664InstrType.SAL : x8664InstrType.SAR,
                size, x8664Operand.Size.BYTE, outStore, x8664Register.RCX);

        if (backupRcx) {
            generator.move(x8664Operand.Size.QUAD_WORD, x8664Register.RCX, backupStore);
        }
    }

    public enum Direction {
        LEFT, RIGHT
    }
}
