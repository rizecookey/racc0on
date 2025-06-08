package net.rizecookey.racc0on.lexer;

import net.rizecookey.racc0on.Span;

public record BooleanLiteral(boolean value, Span span) implements Token {
    @Override
    public String asString() {
        return Boolean.toString(value);
    }
}
