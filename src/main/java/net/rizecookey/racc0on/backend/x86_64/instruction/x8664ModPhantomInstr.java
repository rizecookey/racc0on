package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.x86_64.storage.x8664Operands;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageLocation;

import java.util.List;

public class x8664ModPhantomInstr extends x8664OneOperandDoubleWidthRMInstr {
    public x8664ModPhantomInstr(x8664Operands.Binary<x8664StorageLocation> operands) {
        super("idiv", List.of(x8664Register.RDX, x8664Register.RAX), x8664Register.RAX, x8664Register.RDX, operands);
    }
}
