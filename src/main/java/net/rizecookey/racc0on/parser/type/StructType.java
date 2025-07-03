package net.rizecookey.racc0on.parser.type;

import net.rizecookey.racc0on.lexer.Identifier;

public record StructType(Identifier name) implements Type {
    @Override
    public String asString() {
        return "struct " + name();
    }
}
