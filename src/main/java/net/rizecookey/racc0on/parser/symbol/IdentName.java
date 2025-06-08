package net.rizecookey.racc0on.parser.symbol;

record IdentName(String identifier) implements Name {
    @Override
    public String asString() {
        return identifier();
    }
}
