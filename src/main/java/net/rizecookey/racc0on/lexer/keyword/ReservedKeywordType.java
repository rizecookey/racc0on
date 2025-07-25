package net.rizecookey.racc0on.lexer.keyword;

public enum ReservedKeywordType implements KeywordType {
    ASSERT("assert"),
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
