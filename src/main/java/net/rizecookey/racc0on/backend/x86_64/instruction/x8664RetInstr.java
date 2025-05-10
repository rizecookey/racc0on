package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.x86_64.storage.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageLocation;
import net.rizecookey.racc0on.backend.x86_64.x8664ProcedureGenerator;

public class x8664RetInstr implements x8664Instr {
    private final x8664StorageLocation returnLoc;

    public x8664RetInstr(x8664StorageLocation returnLocation) {
        returnLoc = returnLocation;
    }

    @Override
    public void write(x8664ProcedureGenerator generator) {
        if (returnLoc != x8664Register.RAX) {
            new x8664MovInstr(x8664Register.RAX, returnLoc).write(generator);
        }

        generator.tearDownStack();
        generator.writeInstruction("ret");
    }
}
