package net.rizecookey.racc0on.backend.x86_64.operand.stored;

import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;

public record x8664StackStore(Id id, int rbpRelativeOffset) implements x8664Store {
    public x8664StackStore(int rbpRelativeOffset) {
        this(stackId(rbpRelativeOffset), rbpRelativeOffset);
    }

    private static Id stackId(int rbpRelativeOffset) {
        String common = "[" + x8664Register.RBP.getId().qwordName() + "-" + rbpRelativeOffset + "]";
        return new x8664Operand.Id(common);
    }

    @Override
    public String toString() {
        return id().qwordName();
    }

    @Override
    public Id getId() {
        return id();
    }
}
