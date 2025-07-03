package net.rizecookey.racc0on.lexer.keyword;

public enum PointerLiteralKeywordType implements KeywordType {
    NULL("NULL"),
    ;

    private final String keyword;
    PointerLiteralKeywordType(String keyword) {
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
