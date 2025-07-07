package net.rizecookey.racc0on.parser.type;

import net.rizecookey.racc0on.ir.node.Node;

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
    public Node.ValueType toIrType() {
        return switch (this) {
            case INT -> Node.ValueType.INT;
            case BOOL -> Node.ValueType.BOOL;
        };
    }
}
