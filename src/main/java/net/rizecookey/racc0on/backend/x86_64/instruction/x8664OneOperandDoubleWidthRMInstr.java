package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.x86_64.storage.x8664Operands;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageLocation;
import net.rizecookey.racc0on.backend.x86_64.x8664ProcedureGenerator;

import java.util.List;
import java.util.Set;

public class x8664OneOperandDoubleWidthRMInstr implements x8664Instr {
    private static final Set<x8664Register> SELF_TAINTED = Set.of(x8664Register.RDX, x8664Register.RAX);

    private final String name;
    private final List<x8664Register> tainted;
    private final x8664Register inData, outData;
    private final x8664StorageLocation out, inLeft, inRight;

    public x8664OneOperandDoubleWidthRMInstr(String name, List<x8664Register> tainted,
                                             x8664Register inData, x8664Register outData,
                                             x8664Operands.Binary<x8664StorageLocation> operands) {
        this.name = name;
        this.tainted = tainted;
        this.inData = inData;
        this.outData = outData;
        this.out = operands.out();
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();
    }

    @Override
    public void write(x8664ProcedureGenerator generator) {
        pushTainted(generator);

        x8664StorageLocation realRight = inRight;
        if (realRight instanceof x8664Register inRightRegister && SELF_TAINTED.contains(inRightRegister)) {
            realRight = x8664Register.MEMORY_ACCESS_RESERVE;
            new x8664MovInstr(realRight, inRight).write(generator);
        }

        if (inLeft != inData) {
            new x8664MovInstr(inData, inLeft).write(generator);
        }

        generator.writeInstruction("cdq");
        generator.writeInstruction(name, realRight);
        if (out != outData) {
            new x8664MovInstr(out, outData).write(generator);
        }

        popTainted(generator);
    }

    private void pushTainted(x8664ProcedureGenerator generator) {
        for (x8664Register tainted : tainted.reversed()) {
            if (tainted == out) {
                continue;
            }
            new x8664PushInstr(tainted).write(generator);
        }
    }

    private void popTainted(x8664ProcedureGenerator generator) {
        for (x8664Register tainted : tainted) {
            if (tainted == out) {
                continue;
            }
            new x8664PopInstr(tainted).write(generator);
        }
    }
}
