package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.x86_64.storage.x8664Operands;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageLocation;

public class x8664IMulInstr extends x8664TwoOperandRMInstr {
    public x8664IMulInstr(x8664Operands.Binary<x8664StorageLocation> operands) {
        super("imul", operands);
    }
}
