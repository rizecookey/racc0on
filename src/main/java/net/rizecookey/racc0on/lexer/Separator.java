package net.rizecookey.racc0on.lexer;

import net.rizecookey.racc0on.utils.Span;

public record Separator(SeparatorType type, Span span) implements Token {

    @Override
    public boolean isSeparator(SeparatorType separatorType) {
        return type() == separatorType;
    }

    @Override
    public String asString() {
        return type().toString();
    }

    public enum SeparatorType {
        PAREN_OPEN("("),
        PAREN_CLOSE(")"),
        BRACE_OPEN("{"),
        BRACE_CLOSE("}"),
        BRACKET_OPEN("["),
        BRACKET_CLOSE("]"),
        COMMA(","),
        SEMICOLON(";"),
        ;

        private final String value;

        SeparatorType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
