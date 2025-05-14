package net.rizecookey.racc0on.backend;

import java.util.List;

public interface InstructionGenerator<T extends Instruction> {
    List<T> generateInstructions();
}
