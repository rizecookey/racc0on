package edu.kit.kastel.vads.compiler.lexer.keyword;

public enum BoolLiteralKeywordType implements KeywordType {
    TRUE("true"),
    FALSE("false"),
    ;

    private final String keyword;
    BoolLiteralKeywordType(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String keyword() {
        return keyword;
    }

    @Override
    public String toString() {
        return keyword();
    }
}
