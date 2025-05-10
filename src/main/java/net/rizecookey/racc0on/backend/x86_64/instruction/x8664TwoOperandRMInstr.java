package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.x86_64.storage.x8664Operands;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StackLocation;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageLocation;
import net.rizecookey.racc0on.backend.x86_64.x8664ProcedureGenerator;

public class x8664TwoOperandRMInstr implements x8664Instr {
    private final String name;
    private final x8664StorageLocation out, inLeft, inRight;

    public x8664TwoOperandRMInstr(String name, x8664Operands.Binary<x8664StorageLocation> operands) {
        this.name = name;
        this.out = operands.out();
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();
    }

    @Override
    public void write(x8664ProcedureGenerator generator) {
        x8664StorageLocation target = out;

        if (out instanceof x8664StackLocation) {
            target = x8664Register.RBP;
        }
        new x8664TwoOperandRMOrMRInstr(name, new x8664Operands.Binary<>(target, inLeft, inRight)).write(generator);

        if (target != out) {
            new x8664MovInstr(out, target).write(generator);
        }
    }
}
