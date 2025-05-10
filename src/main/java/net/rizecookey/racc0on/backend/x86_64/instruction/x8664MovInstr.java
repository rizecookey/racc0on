package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.x86_64.storage.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StackLocation;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageLocation;
import net.rizecookey.racc0on.backend.x86_64.x8664ProcedureGenerator;

public class x8664MovInstr implements x8664Instr {
    private final x8664StorageLocation to;
    private final x8664Operand from;

    public x8664MovInstr(x8664StorageLocation to, x8664Operand from) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void write(x8664ProcedureGenerator generator) {
        if (to instanceof x8664StackLocation && from instanceof x8664StackLocation) {
            new x8664MovInstr(x8664Register.RBP, from).write(generator);
            new x8664MovInstr(to, x8664Register.RBP).write(generator);
            return;
        }

        generator.writeInstruction("mov", to, from);
    }
}
