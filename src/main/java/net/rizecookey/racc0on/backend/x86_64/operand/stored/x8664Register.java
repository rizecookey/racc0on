package net.rizecookey.racc0on.backend.x86_64.operand.stored;

import java.util.List;

public enum x8664Register implements x8664StoreLocation {
    RBX(reg16("bx")),
    RCX(reg16("cx")),
    RSI(reg16("si")),
    RDI(reg16("di")),

    R8(reg64("r8")),
    R9(reg64("r9")),
    R10(reg64("r10")),
    R11(reg64("r11")),
    R12(reg64("r12")),
    R13(reg64("r13")),
    R14(reg64("r14")),

    RAX(reg16("ax")), // move down to make allocation prefer other registers
    RDX(reg16("dx")),

    R15(reg64("r15"), Usage.MEMORY_ACCESS_RESERVE),

    RSP(reg16("sp"), Usage.STACK_POINTER),
    RBP(reg16("bp"), Usage.BASE_POINTER);

    public static final x8664Register MEMORY_ACCESS_RESERVE = R15;

    private final Id id;
    private final Usage usage;
    x8664Register(Id id) {
        this(id, Usage.GENERAL);
    }

    x8664Register(Id id, Usage usage) {
        this.id = id;
        this.usage = usage;
    }

    static Id reg16(String coreId) {
        return new Id("r" + coreId, "e" + coreId, coreId,
                coreId.contains("x") ? coreId.replace("x", "l") : coreId + "l");
    }

    static Id reg64(String coreName) {
        return new Id(coreName, coreName + "d", coreName + "w", coreName + "b");
    }

    public Id getId() {
        return id;
    }

    public Usage getUsage() {
        return usage;
    }

    public boolean isGeneralPurpose() {
        return usage == Usage.GENERAL;
    }

    public static List<x8664Register> getRegisterSet() {
        return List.of(values());
    }

    @Override
    public String toString() {
        return id.qwordName();
    }

    public enum Usage {
        GENERAL, STACK_POINTER, BASE_POINTER, MEMORY_ACCESS_RESERVE
    }
}
