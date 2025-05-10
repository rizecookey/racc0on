package net.rizecookey.racc0on.backend.x86_64;

public record x8664StackLocation(LocationId id, int offset) implements x8664StorageLocation {
    public x8664StackLocation(int offset) {
        this(new LocationId("[" + x8664Register.RSP.getId() + (offset != 0 ? "-" + offset : "") + "]"), offset);
    }

    @Override
    public String toString() {
        return id().qwordName();
    }

    @Override
    public LocationId getId() {
        return id();
    }
}
