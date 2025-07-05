package net.rizecookey.racc0on.parser.type;

public record ArrayType<T extends Type>(T type) implements Type {
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
    public boolean isSmallType() {
        return true;
    }
}
