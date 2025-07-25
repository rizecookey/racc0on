package net.rizecookey.racc0on.backend.x86_64.operand;

public record x8664Immediate(int value) implements x8664ValOperand {
    @Override
    public Id getId() {
        return new Id(String.valueOf(value()));
    }
}
