package net.rizecookey.racc0on.backend.x86_64.operation.compare;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;

public class x8664GreaterOp extends x8664CmpOp {
    public x8664GreaterOp(Operands.Binary<Node> operands) {
        super(x8664InstrType.SETG, operands);
    }
}
