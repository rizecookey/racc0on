package net.rizecookey.racc0on.lexer;

import net.rizecookey.racc0on.utils.Span;

public record Identifier(String value, Span span) implements Token {
    @Override
    public String asString() {
        return value();
    }
}
