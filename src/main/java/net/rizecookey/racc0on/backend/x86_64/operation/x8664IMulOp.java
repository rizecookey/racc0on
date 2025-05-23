package net.rizecookey.racc0on.backend.x86_64.operation;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;

public class x8664IMulOp extends x8664TwoOperandRmOrR32IOp {
    public x8664IMulOp(Operands.Binary<Node> operands) {
        super(x8664InstrType.IMUL, operands);
    }
}
