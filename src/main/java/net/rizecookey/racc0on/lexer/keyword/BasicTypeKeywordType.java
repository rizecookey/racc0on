package net.rizecookey.racc0on.lexer.keyword;

import net.rizecookey.racc0on.parser.type.BasicType;
import net.rizecookey.racc0on.parser.type.Type;

public enum BasicTypeKeywordType implements KeywordType {
    INT("int", BasicType.INT),
    BOOL("bool", BasicType.BOOL),
    ;

    private final String keyword;
    private final Type type;
    BasicTypeKeywordType(String keyword, Type type) {
        this.keyword = keyword;
        this.type = type;
    }

    @Override
    public String keyword() {
        return keyword;
    }

    public Type type() {
        return type;
    }

    @Override
    public String toString() {
        return keyword();
    }
}
