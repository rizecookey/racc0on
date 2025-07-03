package net.rizecookey.racc0on.parser.type;

public record ArrayType<T extends Type>(T type) implements Type {
    @Override
    public String asString() {
        return type().asString() + "[]";
    }
}
