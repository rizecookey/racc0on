package net.rizecookey.racc0on.backend.instruction;

import java.util.List;

public interface InstructionGenerator<T extends Instruction<?, ?, ?>> {
    List<InstructionBlock<T>> generateInstructions();
}
