package net.rizecookey.racc0on.parser.symbol;

import net.rizecookey.racc0on.lexer.keyword.KeywordType;

record KeywordName(KeywordType type) implements Name {
    @Override
    public String asString() {
        return type().keyword();
    }
}
