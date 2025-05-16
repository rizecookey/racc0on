package net.rizecookey.racc0on.backend.x86_64.operation;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.function.Function;

public class x8664TwoOperandRmOrR32IOp implements x8664Op {
    private final x8664InstrType type;
    private final Node out, inLeft, inRight;

    public x8664TwoOperandRmOrR32IOp(x8664InstrType type, Operands.Binary<Node> operands) {
        this.type = type;
        this.out = operands.out();
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();
    }

    @Override
    public void write(x8664InstructionGenerator generator, Function<Node, x8664StoreLocation> storeSupplier) {
        x8664StoreLocation outOp = storeSupplier.apply(out);
        x8664StoreLocation target;

        if (outOp instanceof x8664StackLocation) {
            target = x8664Register.MEMORY_ACCESS_RESERVE;
        } else {
            target = outOp;
        }

        new x8664TwoOperandRmMrOrMiOp(type, new Operands.Binary<>(out, inLeft, inRight)).write(generator, node -> {
            if (node.equals(out)) {
                return target;
            }

            return storeSupplier.apply(node);
        });

        if (!target.equals(outOp)) {
            generator.move(outOp, target);
        }
    }
}
