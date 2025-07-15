package net.rizecookey.racc0on.backend.x86_64.operation.arithmetic;

import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664TwoOperandRmMrOrMiOp;
import net.rizecookey.racc0on.ir.node.Node;

public class x8664AddOp extends x8664TwoOperandRmMrOrMiOp {
    public x8664AddOp(Operands.Binary<Node> operands) {
        super(x8664InstrType.ADD, operands);
    }
}
