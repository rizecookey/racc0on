package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.instruction.InstructionType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public enum x8664InstrType implements InstructionType<x8664Instr, x8664Store> {
    ADD("add", operand(0)),
    SUB("sub", operand(0)),
    IMUL("imul", operand(0)),
    IDIV("idiv", specific(x8664Register.RAX), specific(x8664Register.RDX)),
    PUSH("push", specific(x8664Register.RSP)),
    POP("pop", specific(x8664Register.RSP), operand(0)),
    CDQ("cdq", specific(x8664Register.RAX), specific(x8664Register.RDX)),
    MOV("mov", operand(0)),
    RET("ret"),
    ENTER("enter", specific(x8664Register.RSP), specific(x8664Register.RBP)),
    LEAVE("leave", specific(x8664Register.RSP), specific(x8664Register.RBP)),
    JMP("jmp")
    ;

    private final String name;
    private final List<OverriddenStoreGetter> overriddenStoreGetters;
    x8664InstrType(String name, OverriddenStoreGetter... modifiedStores) {
        this.name = name;
        this.overriddenStoreGetters = List.of(modifiedStores);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<x8664Store> getOverridenStores(x8664Instr instruction) {
        return overriddenStoreGetters.stream()
                .map(getter -> getter.getOverridden(instruction))
                .filter(Objects::nonNull)
                .toList();
    }

    public static SpecificStoreGetter specific(x8664Store location) {
        return new SpecificStoreGetter(location);
    }

    public static OperandStoreGetter operand(int index) {
        return new OperandStoreGetter(index);
    }

    @FunctionalInterface
    public interface OverriddenStoreGetter {
        @Nullable
        x8664Store getOverridden(x8664Instr instr);
    }

    public record SpecificStoreGetter(x8664Store storeLocation) implements OverriddenStoreGetter {
        @Override
        public x8664Store getOverridden(x8664Instr instr) {
            return storeLocation;
        }
    }

    public record OperandStoreGetter(int operandIndex) implements OverriddenStoreGetter {
        @Override @Nullable
        public x8664Store getOverridden(x8664Instr instr) {
            if (!(instr.operands().get(operandIndex) instanceof x8664Store storeLocation)) {
                return null;
            }

            return storeLocation;
        }
    }
}
