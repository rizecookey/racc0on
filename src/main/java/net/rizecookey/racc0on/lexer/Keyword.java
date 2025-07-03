package net.rizecookey.racc0on.lexer;

import net.rizecookey.racc0on.lexer.keyword.BuiltinFunctionsKeywordType;
import net.rizecookey.racc0on.lexer.keyword.ComposedTypeKeywordType;
import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.lexer.keyword.ControlKeywordType;
import net.rizecookey.racc0on.lexer.keyword.KeywordType;
import net.rizecookey.racc0on.lexer.keyword.BasicTypeKeywordType;

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
    public boolean isBasicTypeKeyword() {
        return type instanceof BasicTypeKeywordType;
    }

    @Override
    public boolean isComposedTypeKeyword() {
        return type instanceof ComposedTypeKeywordType;
    }

    @Override
    public boolean isControlKeyword() {
        return type instanceof ControlKeywordType;
    }

    @Override
    public boolean isBuiltinFunctionKeyword() {
        return type instanceof BuiltinFunctionsKeywordType;
    }
}
