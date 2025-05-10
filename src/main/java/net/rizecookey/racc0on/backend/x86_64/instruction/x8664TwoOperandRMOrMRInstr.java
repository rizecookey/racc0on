package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.x86_64.storage.x8664Operands;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StackLocation;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageLocation;
import net.rizecookey.racc0on.backend.x86_64.x8664ProcedureGenerator;

public class x8664TwoOperandRMOrMRInstr implements x8664Instr {
    private final String name;
    private final x8664StorageLocation out, inLeft, inRight;

    public x8664TwoOperandRMOrMRInstr(String name, x8664Operands.Binary<x8664StorageLocation> operands) {
        this.name = name;
        this.out = operands.out();
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();
    }

    @Override
    public void write(x8664ProcedureGenerator generator) {
        if (out instanceof x8664StackLocation && (inLeft instanceof x8664StackLocation || inRight instanceof x8664StackLocation)) {
            new x8664MovInstr(x8664Register.RBP, inLeft).write(generator);
            generator.writeInstruction(name, x8664Register.RBP, inRight);
            new x8664MovInstr(out, x8664Register.RBP).write(generator);
        } else {
            x8664StorageLocation actualRight = inRight;
            if (out != inLeft) {
                if (out == inRight) {
                    new x8664MovInstr(x8664Register.RBP, inRight).write(generator);
                    actualRight = x8664Register.RBP;
                }
                new x8664MovInstr(out, inLeft).write(generator);
            }

            generator.writeInstruction(name, out, actualRight);
        }
    }
}
