package net.rizecookey.racc0on.lexer.keyword;

public enum ComposedTypeKeywordType implements KeywordType {
    STRUCT("struct"),
    ;

    private final String keyword;
    ComposedTypeKeywordType(String keyword) {
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
