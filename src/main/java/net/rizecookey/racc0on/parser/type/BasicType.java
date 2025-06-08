package net.rizecookey.racc0on.parser.type;

import java.util.Locale;

public enum BasicType implements Type {
    INT,
    BOOL;

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
