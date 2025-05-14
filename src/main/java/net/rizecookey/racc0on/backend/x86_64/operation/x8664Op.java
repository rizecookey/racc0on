package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

public interface x8664Op {
    void write(x8664InstructionGenerator generator);

    default void move(x8664StoreLocation to, x8664Operand from, x8664InstructionGenerator generator) {
        new x8664MovOp(to, from).write(generator);
    }

    default void push(x8664Operand operand, x8664InstructionGenerator generator) {
        generator.write(x8664InstrType.PUSH, x8664Operand.Size.QUAD_WORD, operand);
    }

    default void pop(x8664Operand operand, x8664InstructionGenerator generator) {
        generator.write(x8664InstrType.POP, x8664Operand.Size.QUAD_WORD, operand);
    }
}
