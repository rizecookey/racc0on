package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.instruction.Instruction;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Label;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import org.jspecify.annotations.Nullable;

import java.util.List;

public sealed interface x8664Instr extends Instruction<x8664Instr, x8664Operand, x8664Store> permits x8664Instr.NoOperand, x8664Instr.Unary, x8664Instr.Binary {
    @Override
    x8664InstrType type();
    x8664Operand.@Nullable Size size();

    @Override
    default String toAssembly() {
        StringBuilder result = new StringBuilder(type().getName());
        x8664Operand.Size size = size();
        if (size == null) {
            size = x8664Operand.Size.DOUBLE_WORD;
        }
        List<? extends x8664Operand> operands = operands().stream().filter(operand -> !(operand instanceof x8664Label)).toList();
        boolean typeImplicitlyGiven = operands.isEmpty() || operands.stream().anyMatch(operand -> operand instanceof x8664Register);
        if (!typeImplicitlyGiven) {
            result.append(" ").append(size.getPrefix());
        }

        x8664Operand.Size finalSize = size;
        return result.append(!operands().isEmpty() ? " ": "")
                .append(String.join(", ", operands()
                        .stream()
                        .map(operand -> operand.getId().getName(finalSize))
                        .toList()))
                .toString();
    }

    record NoOperand(x8664InstrType type) implements x8664Instr {
        @Override
        public List<? extends x8664Operand> operands() {
            return List.of();
        }

        @Override
        public x8664Operand.@Nullable Size size() {
            return null;
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
    }

    record Binary(x8664InstrType type, x8664Operand first, x8664Operand second, x8664Operand.Size size) implements x8664Instr {
        public Binary(x8664InstrType type, x8664Operand first, x8664Operand second) {
            this(type, first, second, x8664Operand.Size.DOUBLE_WORD);
        }

        @Override
        public List<? extends x8664Operand> operands() {
            return List.of(first, second);
        }
    }
}
