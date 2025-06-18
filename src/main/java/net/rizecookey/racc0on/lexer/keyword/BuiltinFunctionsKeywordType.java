package net.rizecookey.racc0on.lexer.keyword;

public enum BuiltinFunctionsKeywordType implements KeywordType {
    PRINT("print"),
    READ("read"),
    FLUSH("flush"),
    ;

    private final String keyword;
    BuiltinFunctionsKeywordType(String keyword) {
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
