package net.rizecookey.racc0on.backend.x86_64;

public record x8664StackLocation(int offset) implements x8664StorageLocation {
    @Override
    public String toString() {
        return "[" + x8664Register.RSP.getId() + (offset != 0 ? "-" + offset : "") + "]";
    }
}
