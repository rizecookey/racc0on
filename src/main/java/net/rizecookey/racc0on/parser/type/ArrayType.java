package net.rizecookey.racc0on.parser.type;

import net.rizecookey.racc0on.ir.node.ValueType;

public record ArrayType<T extends Type>(T type) implements SmallType {
    @Override
    public String asString() {
        return type().asString() + "[]";
    }

    @Override
    public boolean matches(Type other) {
        if (other == Type.WILDCARD) {
            return true;
        }
        return other instanceof ArrayType<?>(Type otherInner) && type().matches(otherInner);
    }

    @Override
    public ValueType toIrType() {
        return ValueType.ARRAY;
    }
}
