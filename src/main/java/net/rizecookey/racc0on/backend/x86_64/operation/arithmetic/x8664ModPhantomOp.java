package net.rizecookey.racc0on.backend.x86_64.operation.arithmetic;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664OneOperandDoubleWidthMOp;

import java.util.List;

public class x8664ModPhantomOp extends x8664OneOperandDoubleWidthMOp {
    public x8664ModPhantomOp(Operands.Binary<Node> operands) {
        super(x8664InstrType.IDIV, List.of(x8664Register.RDX, x8664Register.RAX), x8664Register.RAX, x8664Register.RDX, operands);
    }
}
