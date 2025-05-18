package net.rizecookey.racc0on.backend.x86_64.storage;

public record x8664StackLocation(OperandId id, int rbpRelativeOffset) implements x8664StorageLocation {
    public x8664StackLocation(int rbpRelativeOffset) {
        this(stackId(rbpRelativeOffset), rbpRelativeOffset);
    }

    public static OperandId stackId(int rbpRelativeOffset) {
        String common = "[" + x8664Register.RBP.getId().qwordName() + "-" + rbpRelativeOffset + "]";
        return new OperandId(common);
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
