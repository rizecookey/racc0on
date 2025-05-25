package edu.kit.kastel.vads.compiler.parser.symbol;

import edu.kit.kastel.vads.compiler.lexer.keyword.KeywordType;

record KeywordName(KeywordType type) implements Name {
    @Override
    public String asString() {
        return type().keyword();
    }
}
