package net.rizecookey.racc0on.backend.x86_64.instruction;

public enum x8664InstrType {
    ADD("add"),
    SUB("sub"),
    IMUL("imul"),
    IDIV("idiv"),
    PUSH("push"),
    POP("pop"),
    CDQ("cdq"),
    MOV("mov"),
    RET("ret"),
    ENTER("enter"),
    LEAVE("leave");

    private final String name;
    x8664InstrType(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }
}
