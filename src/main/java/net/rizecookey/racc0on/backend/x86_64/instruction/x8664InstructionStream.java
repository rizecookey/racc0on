package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.instruction.InstructionStream;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;

import java.util.List;

public class x8664InstructionStream extends InstructionStream<x8664Instr, x8664StoreLocation> {
    public x8664InstructionStream() {
        super(x8664StoreLocation.class);
    }

    public x8664InstructionStream(List<x8664Instr> instructions) {
        super(instructions, x8664StoreLocation.class);
    }
}
