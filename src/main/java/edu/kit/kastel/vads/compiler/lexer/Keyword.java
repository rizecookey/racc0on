package edu.kit.kastel.vads.compiler.lexer;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.lexer.keyword.ControlKeywordType;
import edu.kit.kastel.vads.compiler.lexer.keyword.KeywordType;
import edu.kit.kastel.vads.compiler.lexer.keyword.TypeKeywordType;

public record Keyword(KeywordType type, Span span) implements Token {
    @Override
    public boolean isKeyword(KeywordType keywordType) {
        return type().equals(keywordType);
    }

    @Override
    public String asString() {
        return type().keyword();
    }

    @Override
    public boolean isTypeKeyword() {
        return type instanceof TypeKeywordType;
    }

    @Override
    public boolean isControlKeyword() {
        return type instanceof ControlKeywordType;
    }
}
