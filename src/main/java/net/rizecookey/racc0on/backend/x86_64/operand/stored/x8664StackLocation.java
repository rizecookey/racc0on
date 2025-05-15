package net.rizecookey.racc0on.backend.x86_64.operand.stored;

import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;

public record x8664StackLocation(Id id, int offset) implements x8664StoreLocation {
    public x8664StackLocation(int offset) {
        this(stackId(offset), offset);
    }

    private static Id stackId(int offset) {
        int relativeToBasePointer = offset * 4 + 8;
        String common = "[" + x8664Register.RBP.getId().qwordName() + "-" + relativeToBasePointer + "]";
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
