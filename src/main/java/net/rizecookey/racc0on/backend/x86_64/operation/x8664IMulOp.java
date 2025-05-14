package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operands;

public class x8664IMulOp extends x8664TwoOperandRMOp {
    public x8664IMulOp(x8664Operands.Binary<x8664StoreLocation> operands) {
        super(x8664InstrType.IMUL, operands);
    }
}
