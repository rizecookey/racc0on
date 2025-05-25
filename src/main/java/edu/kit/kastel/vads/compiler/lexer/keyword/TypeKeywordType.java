package edu.kit.kastel.vads.compiler.lexer.keyword;

import edu.kit.kastel.vads.compiler.parser.type.BasicType;
import edu.kit.kastel.vads.compiler.parser.type.Type;

public enum TypeKeywordType implements KeywordType {
    INT("int", BasicType.INT),
    BOOL("bool", BasicType.BOOL),
    ;

    private final String keyword;
    private final Type type;
    TypeKeywordType(String keyword, Type type) {
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
