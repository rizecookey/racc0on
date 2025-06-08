package net.rizecookey.racc0on.backend.x86_64.operation.logic;

import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664TwoOperandRmMrOrMiOp;

public class x8664OrOp extends x8664TwoOperandRmMrOrMiOp {
    public x8664OrOp(Operands.Binary<Node> operands) {
        super(x8664InstrType.OR, operands);
    }
}
