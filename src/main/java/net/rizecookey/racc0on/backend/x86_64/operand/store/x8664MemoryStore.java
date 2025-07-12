package net.rizecookey.racc0on.backend.x86_64.operand.store;

import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;

public sealed class x8664MemoryStore implements x8664Store permits x8664StackStore {
    private final x8664Operand.Id id;
    private final x8664Register base;
    private final int offset;

    public x8664MemoryStore(x8664Register base, int offset) {
        this.id = id(base, offset);
        this.base = base;
        this.offset = offset;
    }

    public x8664MemoryStore(x8664Register base) {
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

    public x8664Register base() {
        return base;
    }

    public int offset() {
        return offset;
    }

    @Override
    public Id getId() {
        return id;
    }
}
