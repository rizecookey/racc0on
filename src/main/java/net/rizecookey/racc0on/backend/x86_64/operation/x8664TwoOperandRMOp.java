package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operands;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

public class x8664TwoOperandRMOp implements x8664Op {
    private final x8664InstrType type;
    private final x8664StoreLocation out, inLeft, inRight;

    public x8664TwoOperandRMOp(x8664InstrType type, x8664Operands.Binary<x8664StoreLocation> operands) {
        this.type = type;
        this.out = operands.out();
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();
    }

    @Override
    public void write(x8664InstructionGenerator generator) {
        x8664StoreLocation target = out;

        if (out instanceof x8664StackLocation) {
            target = x8664Register.MEMORY_ACCESS_RESERVE;
        }
        new x8664TwoOperandRMOrMROp(type, new x8664Operands.Binary<>(target, inLeft, inRight)).write(generator);

        if (target != out) {
            generator.move(out, target);
        }
    }
}
