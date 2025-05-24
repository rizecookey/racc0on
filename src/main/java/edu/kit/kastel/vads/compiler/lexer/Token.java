package edu.kit.kastel.vads.compiler.lexer;

import edu.kit.kastel.vads.compiler.Span;

public sealed interface Token permits ErrorToken, Identifier, Keyword, NumberLiteral, BooleanLiteral, Operator, Separator {

    Span span();

    default boolean isTypeKeyword() {
        return false;
    }

    default boolean isKeyword(KeywordType keywordType) {
        return false;
    }

    default boolean isOperator(Operator.OperatorType operatorType) {
        return false;
    }

    default boolean isSeparator(Separator.SeparatorType separatorType) {
        return false;
    }

    String asString();
}
