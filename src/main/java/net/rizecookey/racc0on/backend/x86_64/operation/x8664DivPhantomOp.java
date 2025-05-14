package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operands;

import java.util.List;

public class x8664DivPhantomOp extends x8664OneOperandDoubleWidthMOp {
    public x8664DivPhantomOp(x8664Operands.Binary<x8664StoreLocation> operands) {
        super(x8664InstrType.IDIV, List.of(x8664Register.RDX, x8664Register.RAX), x8664Register.RAX, x8664Register.RAX, operands);
    }
}
