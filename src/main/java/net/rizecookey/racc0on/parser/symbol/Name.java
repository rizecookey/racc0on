package net.rizecookey.racc0on.parser.symbol;

import net.rizecookey.racc0on.lexer.Identifier;
import net.rizecookey.racc0on.lexer.Keyword;
import net.rizecookey.racc0on.lexer.keyword.KeywordType;

public sealed interface Name permits IdentName, KeywordName {

    static Name forKeyword(Keyword keyword) {
        return new KeywordName(keyword.type());
    }

    static Name forKeyword(KeywordType type) {
        return new KeywordName(type);
    }

    static Name forIdentifier(Identifier identifier) {
        return new IdentName(identifier.value());
    }

    String asString();
}
