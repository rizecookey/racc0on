package net.rizecookey.racc0on.parser.type;

public record StructType(String name) implements Type {
    @Override
    public String asString() {
        return "struct " + name();
    }
}
