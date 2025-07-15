package net.rizecookey.racc0on.backend.x86_64.operation.compare;

import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.ir.node.Node;

public class x8664NotEqOp extends x8664CmpOp {
    public x8664NotEqOp(Operands.Binary<Node> operands) {
        super(x8664InstrType.SETNE, operands);
    }
}
