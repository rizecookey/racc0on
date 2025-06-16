package net.rizecookey.racc0on.backend.x86_64.optimization;

import net.rizecookey.racc0on.backend.instruction.InstructionBlock;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664Instr;

import java.util.List;
import java.util.function.Consumer;

@FunctionalInterface
public interface x8664InstrOptimization extends Consumer<List<InstructionBlock<x8664Instr>>> {
}
