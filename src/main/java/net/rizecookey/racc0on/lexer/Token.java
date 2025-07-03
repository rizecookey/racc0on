package net.rizecookey.racc0on.lexer;

import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.lexer.keyword.KeywordType;

public sealed interface Token permits BooleanLiteral, ErrorToken, Identifier, Keyword, NumberLiteral, Operator, AmbiguousSymbol, Separator {

    Span span();

    default boolean isBasicTypeKeyword() {
        return false;
    }

    default boolean isComposedTypeKeyword() {
        return false;
    }

    default boolean isControlKeyword() {
        return false;
    }

    default boolean isBuiltinFunctionKeyword() {
        return false;
    }

    default boolean isKeyword(KeywordType keywordType) {
        return false;
    }

    default boolean isOperator(OperatorType operatorType) {
        return false;
    }

    default boolean isSeparator(Separator.SeparatorType separatorType) {
        return false;
    }

    default boolean isAmbiguous(AmbiguousSymbol.SymbolType symbolType) {
        return false;
    }

    String asString();
}
