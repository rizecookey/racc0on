package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operands;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

public class x8664TwoOperandRMOrMROp implements x8664Op {
    private final x8664InstrType type;
    private final x8664StoreLocation out, inLeft, inRight;

    public x8664TwoOperandRMOrMROp(x8664InstrType type, x8664Operands.Binary<x8664StoreLocation> operands) {
        this.type = type;
        this.out = operands.out();
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();
    }

    @Override
    public void write(x8664InstructionGenerator generator) {
        if (out instanceof x8664StackLocation && (inLeft instanceof x8664StackLocation || inRight instanceof x8664StackLocation)) {
            move(x8664Register.MEMORY_ACCESS_RESERVE, inLeft, generator);
            generator.write(type, x8664Register.MEMORY_ACCESS_RESERVE, inRight);
            move(out, x8664Register.MEMORY_ACCESS_RESERVE, generator);
        } else {
            x8664StoreLocation actualRight = inRight;
            if (out != inLeft) {
                if (out == inRight) {
                    actualRight = x8664Register.MEMORY_ACCESS_RESERVE;
                    move(actualRight, inRight, generator);
                }
                move(out, inLeft, generator);
            }

            generator.write(type, out, actualRight);
        }
    }
}
