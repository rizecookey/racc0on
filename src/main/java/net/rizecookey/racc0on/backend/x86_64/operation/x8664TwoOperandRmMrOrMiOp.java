package net.rizecookey.racc0on.backend.x86_64.operation;

import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.function.Function;

public class x8664TwoOperandRmMrOrMiOp implements x8664Op {
    private final x8664InstrType type;
    private final Node out, inLeft, inRight;

    public x8664TwoOperandRmMrOrMiOp(x8664InstrType type, Operands.Binary<Node> operands) {
        this.type = type;
        this.out = operands.out();
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();
    }

    @Override
    public void write(x8664InstructionGenerator generator, Function<Node, x8664StoreLocation> storeSupplier) {
        x8664StoreLocation outOp = storeSupplier.apply(out);
        x8664Operand inLeftOp = inLeft instanceof ConstIntNode constNode
                ? new x8664Immediate(constNode.value())
                : storeSupplier.apply(inLeft);
        x8664Operand inRightOp = inRight instanceof ConstIntNode constNode
                ? new x8664Immediate(constNode.value())
                : storeSupplier.apply(inRight);

        if (outOp instanceof x8664StackLocation && (inLeftOp instanceof x8664StackLocation || inRightOp instanceof x8664StackLocation)) {
            generator.move(x8664Register.MEMORY_ACCESS_RESERVE, inLeftOp);
            generator.write(type, x8664Register.MEMORY_ACCESS_RESERVE, inRightOp);
            generator.move(outOp, x8664Register.MEMORY_ACCESS_RESERVE);
        } else {
            x8664Operand actualRight = inRightOp;
            if (!outOp.equals(inLeftOp)) {
                if (outOp.equals(inRightOp)) {
                    actualRight = x8664Register.MEMORY_ACCESS_RESERVE;
                    generator.move(x8664Register.MEMORY_ACCESS_RESERVE, inRightOp);
                }
                generator.move(outOp, inLeftOp);
            }

            generator.write(type, outOp, actualRight);
        }
    }
}
