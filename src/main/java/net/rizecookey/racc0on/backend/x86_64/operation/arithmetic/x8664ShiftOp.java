package net.rizecookey.racc0on.backend.x86_64.operation.arithmetic;

import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
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

    private StoreReference<x8664Store> shifteeRef, shiftCountRef, outRef, backupRef;

    public x8664ShiftOp(Direction direction, Operands.Binary<Node> operands) {
        this.direction = direction;
        this.out = operands.out();
        this.shiftee = operands.inLeft();
        this.shiftCount = operands.inRight();

        shifteeRef = shiftCountRef = outRef = backupRef = new StoreReference.Null<>();
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
        if (!(shiftee instanceof ConstIntNode)) {
            shifteeRef = service.requestInputStore(this, shiftee, StoreRequestService.Conditions.<x8664Store>builder()
                    .collidesWith(x8664Register.RCX)
                    .build());
        }

        if (!(shiftCount instanceof ConstIntNode)) {
            shiftCountRef = service.requestInputStore(this, shiftCount);
        }

        outRef = service.requestOutputStore(this, out, StoreRequestService.Conditions.<x8664Store>builder()
                .collidesWith(x8664Register.RCX)
                .build());

        backupRef = service.requestAdditional(this, StoreRequestService.Conditions.<x8664Store>builder()
                .collidesWith(x8664Register.RCX)
                .build());
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664Store out = storeSupplier.resolve(outRef).orElseThrow();
        x8664Operand shiftee = this.shiftee instanceof ConstIntNode c
                ? new x8664Immediate(c.value())
                : storeSupplier.resolve(shifteeRef).orElseThrow();
        x8664Operand shiftCount = this.shiftCount instanceof ConstIntNode c
                ? new x8664Immediate(c.value())
                : storeSupplier.resolve(shiftCountRef).orElseThrow();

        // TODO eliminate this if possible
        x8664Store backup = storeSupplier.resolve(backupRef).orElseThrow();

        // TODO check for liveness before backing up to potentially save moves
        if (!shiftCount.equals(x8664Register.RCX)) {
            generator.move(backup, x8664Register.RCX);
            generator.move(x8664Register.RCX, shiftCount);
        }

        if (!shiftee.equals(out)) {
            generator.move(out, shiftee);
        }

        generator.write(direction == Direction.LEFT ? x8664InstrType.SAL : x8664InstrType.SAR,
                x8664Operand.Size.DOUBLE_WORD, x8664Operand.Size.BYTE, out, x8664Register.RCX);

        if (!shiftCount.equals(x8664Register.RCX)) {
            generator.move(x8664Register.RCX, backup);
        }
    }

    public enum Direction {
        LEFT, RIGHT
    }
}
