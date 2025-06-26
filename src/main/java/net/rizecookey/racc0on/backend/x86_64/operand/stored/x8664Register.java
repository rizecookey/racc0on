package net.rizecookey.racc0on.backend.x86_64.operand.stored;

import java.util.List;

public enum x8664Register implements x8664Store {
    R12(reg64("r12"), SaveType.CALLEE_SAVED),
    R13(reg64("r13"), SaveType.CALLEE_SAVED),
    R14(reg64("r14"), SaveType.CALLEE_SAVED),
    R15(reg64("r15"), SaveType.CALLEE_SAVED),

    RBX(reg16("bx"), SaveType.CALLEE_SAVED),
    RCX(reg16("cx"), SaveType.CALLER_SAVED),
    RSI(reg16("si"), SaveType.CALLER_SAVED),
    RDI(reg16("di"), SaveType.CALLER_SAVED),

    R8(reg64("r8"), SaveType.CALLER_SAVED),
    R9(reg64("r9"), SaveType.CALLER_SAVED),
    R10(reg64("r10"), SaveType.CALLER_SAVED),

    RAX(reg16("ax"), SaveType.CALLER_SAVED), // move down to make allocation prefer other registers
    RDX(reg16("dx"), SaveType.CALLER_SAVED),

    R11(reg64("r11"), Usage.MEMORY_ACCESS_RESERVE, SaveType.CALLER_SAVED),

    RSP(reg16("sp"), Usage.STACK_POINTER, SaveType.NONE),
    RBP(reg16("bp"), Usage.BASE_POINTER, SaveType.NONE);

    public static final List<x8664Register> ARGUMENT_REGISTERS = List.of(
            RDI,
            RSI,
            RDX,
            RCX,
            R8,
            R9
    );
    public static final x8664Register MEMORY_ACCESS_RESERVE = R11;

    private final Id id;
    private final Usage usage;
    private final SaveType saveType;
    x8664Register(Id id, SaveType saveType) {
        this(id, Usage.GENERAL, saveType);
    }

    x8664Register(Id id, Usage usage, SaveType saveType) {
        this.id = id;
        this.usage = usage;
        this.saveType = saveType;
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

    public SaveType getSaveType() {
        return saveType;
    }

    public boolean isGeneralPurpose() {
        return usage == Usage.GENERAL;
    }

    public boolean isCalleeSaved() {
        return saveType == SaveType.CALLEE_SAVED;
    }

    public boolean isCallerSaved() {
        return saveType == SaveType.CALLER_SAVED;
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

    public enum SaveType {
        CALLER_SAVED, CALLEE_SAVED, NONE
    }
}
