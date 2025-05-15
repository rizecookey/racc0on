package net.rizecookey.racc0on.backend.instruction;

import net.rizecookey.racc0on.backend.operand.stored.VariableStore;

import java.util.List;

public interface InstructionType<T extends Instruction<T, ?, U>, U extends VariableStore> {
    String getName();
    List<U> getOverridenStores(T instruction);
}
