package net.rizecookey.racc0on.parser.type;

import java.util.Locale;

public enum BasicType implements Type {
    INT,
    BOOL;

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean matches(Type other) {
        if (other == Type.WILDCARD) {
            return true;
        }
        return equals(other);
    }

    @Override
    public boolean isSmallType() {
        return true;
    }
}
