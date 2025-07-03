package net.rizecookey.racc0on.lexer.keyword;

public enum AllocKeywordType implements KeywordType {
    ALLOC("alloc"),
    ALLOC_ARRAY("alloc_array"),
    ;

    private final String keyword;
    AllocKeywordType(String keyword) {
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
