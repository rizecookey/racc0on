package net.rizecookey.racc0on.lexer.keyword;

public enum ReservedKeywordType implements KeywordType {
    STRUCT("struct"),
    ASSERT("assert"),
    NULL("NULL"),
    PRINT("print"),
    READ("read"),
    ALLOC("alloc"),
    ALLOC_ARRAY("alloc_array"),
    VOID("void"),
    CHAR("char"),
    STRING("string"),
    ;

    private final String keyword;
    ReservedKeywordType(String keyword) {
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
