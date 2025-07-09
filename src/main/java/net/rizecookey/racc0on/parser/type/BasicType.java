package net.rizecookey.racc0on.parser.type;

import net.rizecookey.racc0on.ir.node.ValueType;

import java.util.Locale;

public enum BasicType implements SmallType {
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
    public ValueType toIrType() {
        return switch (this) {
            case INT -> ValueType.INT;
            case BOOL -> ValueType.BOOL;
        };
    }
}
