package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageLocation;
import net.rizecookey.racc0on.backend.x86_64.x8664ProcedureGenerator;

public record x8664PopInstr(x8664StorageLocation location) implements x8664Instr {
    @Override
    public void write(x8664ProcedureGenerator generator) {
        generator.writeInstruction0("pop", location.getId().qwordName());
    }
}
