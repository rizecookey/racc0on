package net.rizecookey.racc0on.backend.x86_64.operand;

import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;

public record x8664MemoryLocation(Id id, x8664Register base, int offset) implements x8664Operand {
    public x8664MemoryLocation(x8664Register base, int offset) {
        this(id(base, offset), base, offset);
    }

    public x8664MemoryLocation(x8664Register base) {
        this(base, 0);
    }

    private static Id id(x8664Register base, int offset) {
        String offsetString = "";
        if (offset > 0) {
            offsetString += "+" + offset;
        } else if (offset < 0) {
            offsetString += "-" + -offset;
        }
        return new x8664Operand.Id("[" + base.getId().qwordName() + offsetString + "]");
    }

    @Override
    public Id getId() {
        return id();
    }
}
