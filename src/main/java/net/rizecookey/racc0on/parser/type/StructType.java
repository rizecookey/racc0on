package net.rizecookey.racc0on.parser.type;

import net.rizecookey.racc0on.parser.symbol.Name;

public record StructType(Name name) implements Type {
    @Override
    public String asString() {
        return "struct " + name().asString();
    }

    @Override
    public boolean matches(Type other) {
        if (other == Type.WILDCARD) {
            return true;
        }
        return other instanceof StructType(Name otherName) && name().equals(otherName);
    }

    @Override
    public boolean isSmallType() {
        return false;
    }
}
