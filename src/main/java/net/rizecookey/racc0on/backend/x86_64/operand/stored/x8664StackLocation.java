package net.rizecookey.racc0on.backend.x86_64.operand.stored;

public record x8664StackLocation(Id id, int offset) implements x8664StoreLocation {
    public x8664StackLocation(int offset) {
        this(Id.stack(offset), offset);
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
