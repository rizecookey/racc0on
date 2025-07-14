package net.rizecookey.racc0on.backend.x86_64.instruction;

import net.rizecookey.racc0on.backend.instruction.InstructionType;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public enum x8664InstrType implements InstructionType<x8664Instr, x8664VarStore> {
    ADD("add", operand(0)),
    SUB("sub", operand(0)),
    IMUL("imul", operand(0)),
    IDIV("idiv", specific(x8664Register.RAX), specific(x8664Register.RDX)),
    PUSH("push", specific(x8664Register.RSP)),
    POP("pop", specific(x8664Register.RSP), operand(0)),
    CDQ("cdq", specific(x8664Register.RAX), specific(x8664Register.RDX)),
    MOV("mov", operand(0)),
    MOVSX("movsx", operand(0)),
    MOVSXD("movsxd", operand(0)),
    RET("ret"),
    ENTER("enter", specific(x8664Register.RSP), specific(x8664Register.RBP)),
    LEAVE("leave", specific(x8664Register.RSP), specific(x8664Register.RBP)),
    JMP("jmp"),
    TEST("test"),
    JNZ("jnz"),
    JZ("jz"),
    JE("je"),
    JNE("jne"),
    JG("jg"),
    JGE("jge"),
    JL("jl"),
    JLE("jle"),
    SETZ("setz", operand(0)),
    SETE("sete", operand(0)),
    SETNE("setne", operand(0)),
    SETG("setg", operand(0)),
    SETGE("setge", operand(0)),
    SETL("setl", operand(0)),
    SETLE("setle", operand(0)),
    CMOVZ("cmovz", operand(0)),
    CMOVNZ("cmovnz", operand(0)),
    AND("and", operand(0)),
    OR("or", operand(0)),
    XOR("xor", operand(0)),
    CMP("cmp"),
    SAR("sar"),
    SAL("sal"),
    CALL("call"),
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
    public List<x8664VarStore> getOverridenStores(x8664Instr instruction) {
        return overriddenStoreGetters.stream()
                .map(getter -> getter.getOverridden(instruction))
                .filter(Objects::nonNull)
                .toList();
    }

    public static SpecificStoreGetter specific(x8664VarStore location) {
        return new SpecificStoreGetter(location);
    }

    public static OperandStoreGetter operand(int index) {
        return new OperandStoreGetter(index);
    }

    @FunctionalInterface
    public interface OverriddenStoreGetter {
        @Nullable
        x8664VarStore getOverridden(x8664Instr instr);
    }

    public record SpecificStoreGetter(x8664VarStore storeLocation) implements OverriddenStoreGetter {
        @Override
        public x8664VarStore getOverridden(x8664Instr instr) {
            return storeLocation;
        }
    }

    public record OperandStoreGetter(int operandIndex) implements OverriddenStoreGetter {
        @Override @Nullable
        public x8664VarStore getOverridden(x8664Instr instr) {
            if (!(instr.operands().get(operandIndex) instanceof x8664VarStore storeLocation)) {
                return null;
            }

            return storeLocation;
        }
    }
}
