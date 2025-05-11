package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.x86_64.x8664ProcedureGenerator;

public class x8664EmptyInstr implements x8664Instr {
    public static final x8664EmptyInstr INSTANCE = new x8664EmptyInstr();

    private x8664EmptyInstr() {}

    @Override
    public void write(x8664ProcedureGenerator generator) {
    }
}
