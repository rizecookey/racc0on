package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.x86_64.storage.x8664Operands;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageLocation;

public class x8664SubInstr extends x8664TwoOperandRMOrMRInstr {
    public x8664SubInstr(x8664Operands.Binary<x8664StorageLocation> operands) {
        super("sub", operands);
    }
}
