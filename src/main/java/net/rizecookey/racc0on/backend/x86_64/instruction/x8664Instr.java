package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.instruction.Instruction;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Label;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public sealed interface x8664Instr extends Instruction<x8664Instr, x8664Operand, x8664Store> permits x8664Instr.NoOperand, x8664Instr.Unary, x8664Instr.Binary {
    @Override
    x8664InstrType type();
    List<x8664Operand.Size> sizes();

    @Override
    default String toAssembly() {
        StringBuilder result = new StringBuilder(type().getName());
        List<x8664Operand.Size> sizes = sizes();
        if (sizes.isEmpty()) {
            sizes = Collections.nCopies(operands().size(), x8664Operand.Size.DOUBLE_WORD);
        }

        List<String> operandStrings = new ArrayList<>();
        for (int i = 0; i < operands().size(); i++) {
            x8664Operand operand = operands().get(i);
            x8664Operand.Size size = sizes.get(i);
            boolean declareType = !(operand instanceof x8664Register) && !(operand instanceof x8664Label);
            operandStrings.add((declareType ? size.getPrefix() + " " : "") + operand.getId().getName(size));
        }

        return result.append(!operands().isEmpty() ? " ": "")
                .append(String.join(", ", operandStrings))
                .toString();
    }

    record NoOperand(x8664InstrType type) implements x8664Instr {
        @Override
        public List<? extends x8664Operand> operands() {
            return List.of();
        }

        @Override
        public List<x8664Operand.Size> sizes() {
            return List.of();
        }
    }

    record Unary(x8664InstrType type, x8664Operand operand, x8664Operand.Size size) implements x8664Instr {
        public Unary(x8664InstrType type, x8664Operand operand) {
            this(type, operand, x8664Operand.Size.DOUBLE_WORD);
        }

        @Override
        public List<? extends x8664Operand> operands() {
            return List.of(operand);
        }

        @Override
        public List<x8664Operand.Size> sizes() {
            return List.of(size);
        }
    }

    record Binary(x8664InstrType type, x8664Operand first, x8664Operand second, x8664Operand.Size firstSize, x8664Operand.Size secondSize) implements x8664Instr {
        public Binary(x8664InstrType type, x8664Operand first, x8664Operand second) {
            this(type, first, second, x8664Operand.Size.DOUBLE_WORD);
        }

        public Binary(x8664InstrType type, x8664Operand first, x8664Operand second, x8664Operand.Size size) {
            this(type, first, second, size, size);
        }

        @Override
        public List<? extends x8664Operand> operands() {
            return List.of(first, second);
        }

        @Override
        public List<x8664Operand.Size> sizes() {
            return List.of(firstSize, secondSize);
        }
    }
}
