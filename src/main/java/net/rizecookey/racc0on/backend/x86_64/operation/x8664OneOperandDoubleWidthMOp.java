package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operands;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.List;
import java.util.Set;

public class x8664OneOperandDoubleWidthMOp implements x8664Op {
    private static final Set<x8664Register> SELF_TAINTED = Set.of(x8664Register.RDX, x8664Register.RAX);

    private final x8664InstrType type;
    private final List<x8664Register> tainted;
    private final x8664Register inData, outData;
    private final x8664StoreLocation out, inLeft, inRight;

    public x8664OneOperandDoubleWidthMOp(x8664InstrType type, List<x8664Register> tainted,
                                         x8664Register inData, x8664Register outData,
                                         x8664Operands.Binary<x8664StoreLocation> operands) {
        this.type = type;
        this.tainted = tainted;
        this.inData = inData;
        this.outData = outData;
        this.out = operands.out();
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();
    }

    private void pushTainted(x8664InstructionGenerator generator) {
        for (x8664Register tainted : tainted.reversed()) {
            if (tainted == out) {
                continue;
            }

            push(tainted, generator);
        }
    }

    private void popTainted(x8664InstructionGenerator generator) {
        for (x8664Register tainted : tainted) {
            if (tainted == out) {
                continue;
            }

            pop(tainted, generator);
        }
    }

    @Override
    public void write(x8664InstructionGenerator generator) {
        pushTainted(generator);

        x8664StoreLocation realRight = inRight;
        if (realRight instanceof x8664Register inRightRegister && SELF_TAINTED.contains(inRightRegister)) {
            realRight = x8664Register.MEMORY_ACCESS_RESERVE;
            move(realRight, inRight, generator);
        }

        if (inLeft != inData) {
            move(inData, inLeft, generator);
        }

        generator.write(x8664InstrType.CDQ);
        generator.write(type, realRight);
        if (out != outData) {
            move(out, outData, generator);
        }

        popTainted(generator);
    }
}
