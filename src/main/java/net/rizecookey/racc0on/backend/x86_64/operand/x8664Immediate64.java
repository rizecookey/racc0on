package net.rizecookey.racc0on.backend.x86_64.operand;

public record x8664Immediate64(long value) implements x8664Operand {
    @Override
    public Id getId() {
        return new Id(String.valueOf(value()));
    }
}
