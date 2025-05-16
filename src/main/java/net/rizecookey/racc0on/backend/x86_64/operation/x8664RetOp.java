package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

public record x8664RetOp(x8664StoreLocation returnLocation) implements x8664Op {
    @Override
    public void write(x8664InstructionGenerator generator) {
        if (returnLocation != x8664Register.RAX) {
            generator.move(x8664Register.RAX, returnLocation);
        }

        generator.tearDownStack();
        generator.write(x8664InstrType.RET);
    }
}
