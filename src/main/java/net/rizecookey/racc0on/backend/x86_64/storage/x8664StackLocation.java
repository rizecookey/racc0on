package net.rizecookey.racc0on.backend.x86_64.storage;

public record x8664StackLocation(OperandId id, int offset) implements x8664StorageLocation {
    public x8664StackLocation(int offset) {
        this(new OperandId("[" + x8664Register.RSP.getId() + (offset != 0 ? "-" + offset : "") + "]"), offset);
    }

    @Override
    public String toString() {
        return id().qwordName();
    }

    @Override
    public OperandId getId() {
        return id();
    }
}
