package net.rizecookey.racc0on.lexer;

import net.rizecookey.racc0on.utils.Span;

public record PointerSymbol(PointerSymbolType type, Span span) implements Token {
    public enum PointerSymbolType {
        STAR("*"),
        ;

        private final String value;
        PointerSymbolType(String value) {
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
