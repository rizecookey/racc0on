package net.rizecookey.racc0on.backend.x86_64.optimization;

import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstructionStream;

public abstract class x8664AsmOptimization {
    private final x8664InstructionStream stream;

    protected x8664AsmOptimization(x8664InstructionStream stream) {
        this.stream = stream;
    }

    protected x8664InstructionStream stream() {
        return stream;
    }

    public abstract void performOptimization(int currentIndex);
}
