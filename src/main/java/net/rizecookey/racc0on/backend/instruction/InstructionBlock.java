package net.rizecookey.racc0on.backend.instruction;

import java.util.List;

public record InstructionBlock<T extends Instruction<?, ?, ?>>(String label, List<T> instructions) {
}
