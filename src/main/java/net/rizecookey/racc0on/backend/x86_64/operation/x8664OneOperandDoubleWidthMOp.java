package net.rizecookey.racc0on.backend.x86_64.operation;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class x8664OneOperandDoubleWidthMOp implements x8664Op {
    private static final Set<x8664Register> SELF_TAINTED = Set.of(x8664Register.RDX, x8664Register.RAX);

    private final x8664InstrType type;
    private final List<x8664Register> tainted;
    private final x8664Register inData, outData;
    private final Node out, inLeft, inRight;

    public x8664OneOperandDoubleWidthMOp(x8664InstrType type, List<x8664Register> tainted,
                                         x8664Register inData, x8664Register outData,
                                         Operands.Binary<Node> operands) {
        this.type = type;
        this.tainted = tainted;
        this.inData = inData;
        this.outData = outData;
        this.out = operands.out();
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();
    }

    private void pushTainted(x8664InstructionGenerator generator, x8664StoreLocation outOp) {
        for (x8664Register tainted : tainted.reversed()) {
            if (tainted.equals(outOp)) {
                continue;
            }

            generator.push(tainted);
        }
    }

    private void popTainted(x8664InstructionGenerator generator, x8664StoreLocation outOp) {
        for (x8664Register tainted : tainted) {
            if (tainted.equals(outOp)) {
                continue;
            }

            generator.pop(tainted);
        }
    }

    @Override
    public void write(x8664InstructionGenerator generator, Function<Node, x8664StoreLocation> storeSupplier) {
        x8664StoreLocation outOp = storeSupplier.apply(out);
        x8664StoreLocation inLeftOp = storeSupplier.apply(inLeft);
        x8664StoreLocation inRightOp = storeSupplier.apply(inRight);
        pushTainted(generator, outOp);

        x8664StoreLocation realRight = inRightOp;
        if (realRight instanceof x8664Register inRightRegister && SELF_TAINTED.contains(inRightRegister)) {
            realRight = x8664Register.MEMORY_ACCESS_RESERVE;
            generator.move(realRight, inRightOp);
        }

        if (!inLeftOp.equals(inData)) {
            generator.move(inData, inLeftOp);
        }

        generator.write(x8664InstrType.CDQ);
        generator.write(type, realRight);
        if (!outOp.equals(outData)) {
            generator.move(outOp, outData);
        }

        popTainted(generator, outOp);
    }
}
