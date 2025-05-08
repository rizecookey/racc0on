package net.rizecookey.racc0on.backend.x86_64;

import java.util.List;

public enum x8664Register implements x8664StorageLocation {
    RAX("rax"),
    RBX("rbx"),
    RCX("rcx"),
    RDX("rdx"),
    RSI("rsi"),
    RDI("rdi"),
    R8("r8"),
    R9("r9"),
    R10("r10"),
    R11("r11"),
    R12("r12"),
    R13("r13"),
    R14("r14"),
    R15("r15"),

    RSP("rsp", Usage.STACK_POINTER),
    RBP("rbp", Usage.BASE_POINTER);

    private final String id;
    private final Usage usage;
    x8664Register(String id) {
        this.id = id;
        this.usage = Usage.GENERAL;
    }

    x8664Register(String id, Usage usage) {
        this.id = id;
        this.usage = usage;
    }

    public String getId() {
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
        return id;
    }

    public enum Usage {
        GENERAL, STACK_POINTER, BASE_POINTER
    }
}
