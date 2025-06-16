package net.rizecookey.racc0on.backend.x86_64.optimization;

import net.rizecookey.racc0on.backend.instruction.InstructionBlock;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664Instr;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Label;

import java.util.List;
import java.util.ListIterator;

public class x8664JumpOptimization implements x8664InstrOptimization {
    @Override
    public void accept(List<InstructionBlock<x8664Instr>> instructionBlocks) {
        ListIterator<InstructionBlock<x8664Instr>> blockIterator = instructionBlocks.listIterator();
        while (blockIterator.hasNext()) {
            InstructionBlock<x8664Instr> block = blockIterator.next();

            x8664Instr last = block.instructions().getLast();
            if (!blockIterator.hasNext() || !(last instanceof x8664Instr.Unary(x8664InstrType type, x8664Label label, _))
                    || type != x8664InstrType.JMP) {
                continue;
            }

            if (instructionBlocks.get(blockIterator.nextIndex()).label().equals(label.value())) {
                block.instructions().removeLast();
            }
        }
    }
}
