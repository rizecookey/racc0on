package net.rizecookey.racc0on.lexer;

import net.rizecookey.racc0on.utils.Span;

public record AmbiguousSymbol(SymbolType type, Span span) implements Token {
    public enum SymbolType {
        STAR("*"),
        ;

        private final String value;
        SymbolType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Override
    public String asString() {
        return type.toString();
    }
}
