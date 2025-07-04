package net.rizecookey.racc0on.lexer;

import net.rizecookey.racc0on.utils.Span;

public record AmbiguousSymbol(SymbolType type, Span span) implements Token {
    public enum SymbolType {
        STAR("*"),
        MINUS("-"),
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
    public boolean isAmbiguous(SymbolType symbolType) {
        return type().equals(symbolType);
    }

    @Override
    public String asString() {
        return type.toString();
    }
}
