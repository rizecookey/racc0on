package net.rizecookey.racc0on.lexer;

import net.rizecookey.racc0on.Span;

public record NumberLiteral(String value, int base, Span span) implements Token {
    @Override
    public String asString() {
        return value();
    }
}
