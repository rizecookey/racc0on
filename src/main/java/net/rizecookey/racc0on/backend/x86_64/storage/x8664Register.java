package net.rizecookey.racc0on.backend.x86_64.storage;

import java.util.List;

public enum x8664Register implements x8664StorageLocation {
    RAX(reg16("ax")),
    RBX(reg16("bx")),
    RCX(reg16("cx")),
    RDX(reg16("dx")),
    RSI(reg16("si")),
    RDI(reg16("di")),

    R8(reg64("r8")),
    R9(reg64("r9")),
    R10(reg64("r10")),
    R11(reg64("r11")),
    R12(reg64("r12")),
    R13(reg64("r13")),
    R14(reg64("r14")),
    R15(reg64("r15"), Usage.MEMORY_ACCESS_RESERVE),

    RSP(reg16("sp"), Usage.STACK_POINTER),
    RBP(reg16("bp"), Usage.BASE_POINTER);

    public static final x8664Register MEMORY_ACCESS_RESERVE = R15;

    private final OperandId id;
    private final Usage usage;
    x8664Register(OperandId id) {
        this(id, Usage.GENERAL);
    }

    x8664Register(OperandId id, Usage usage) {
        this.id = id;
        this.usage = usage;
    }

    static OperandId reg16(String coreId) {
        return new OperandId("r" + coreId, "e" + coreId, coreId);
    }

    static OperandId reg64(String coreName) {
        return new OperandId(coreName, coreName + "d", coreName + "w");
    }

    public OperandId getId() {
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
