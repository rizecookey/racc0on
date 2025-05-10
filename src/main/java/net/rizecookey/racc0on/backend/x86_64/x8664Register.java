package net.rizecookey.racc0on.backend.x86_64;

import java.util.List;

import static net.rizecookey.racc0on.backend.x86_64.x8664StorageLocation.reg16;
import static net.rizecookey.racc0on.backend.x86_64.x8664StorageLocation.reg64;

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
    R15(reg64("r15")),

    RSP(reg16("sp"), Usage.STACK_POINTER),
    RBP(reg16("bp"), Usage.MEMORY_ACCESS_RESERVE);

    private final LocationId id;
    private final Usage usage;
    x8664Register(LocationId id) {
        this(id, Usage.GENERAL);
    }

    x8664Register(LocationId id, Usage usage) {
        this.id = id;
        this.usage = usage;
    }

    public LocationId getId() {
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
        GENERAL, STACK_POINTER, MEMORY_ACCESS_RESERVE
    }
}
