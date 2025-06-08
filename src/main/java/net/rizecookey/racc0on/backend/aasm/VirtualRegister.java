package net.rizecookey.racc0on.backend.aasm;

import net.rizecookey.racc0on.backend.regalloc.Register;

public record VirtualRegister(int id) implements Register {
    @Override
    public String toString() {
        return "%" + id();
    }
}
