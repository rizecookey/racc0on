package net.rizecookey.racc0on.backend.instruction;

import net.rizecookey.racc0on.backend.operand.Operand;
import net.rizecookey.racc0on.backend.operand.stored.VariableStore;

import java.util.List;

public interface Instruction<T extends Instruction<T, U, V>, U extends Operand, V extends VariableStore> {
    InstructionType<T, V> type();
    List<? extends U> operands();
    String toAssembly();
}
