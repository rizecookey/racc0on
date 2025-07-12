package net.rizecookey.racc0on.backend.x86_64.operand;

public record x8664Immediate(String value) implements x8664Operand {
    public x8664Immediate(long value) {
        this(String.valueOf(value));
    }
    @Override
    public Id getId() {
        return new Id(value);
    }
}
