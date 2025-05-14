package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.x86_64.storage.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageLocation;
import net.rizecookey.racc0on.backend.x86_64.x8664ProcedureGenerator;

public record x8664RetInstr(x8664StorageLocation returnLocation) implements x8664Instr {
    @Override
    public void write(x8664ProcedureGenerator generator) {
        if (!returnLocation.equals(x8664Register.RAX)) {
            new x8664MovInstr(x8664Register.RAX, returnLocation).write(generator);
        }

        generator.tearDownStack();
        generator.writeInstruction("ret");
    }
}
