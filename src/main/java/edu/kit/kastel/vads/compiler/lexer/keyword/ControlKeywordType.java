package edu.kit.kastel.vads.compiler.lexer.keyword;

public enum ControlKeywordType implements KeywordType {
    IF("if"),
    ELSE("else"),
    WHILE("while"),
    FOR("for"),
    CONTINUE("continue"),
    BREAK("break"),
    RETURN("return"),
    ;

    private final String keyword;
    ControlKeywordType(String keyword) {
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
