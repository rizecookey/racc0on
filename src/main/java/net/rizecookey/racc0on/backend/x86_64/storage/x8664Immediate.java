package net.rizecookey.racc0on.backend.x86_64.storage;

public record x8664Immediate(String value) implements x8664Operand {
    @Override
    public OperandId getId() {
        return new OperandId(value);
    }
}
