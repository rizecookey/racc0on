package net.rizecookey.racc0on.parser.type;

public sealed interface Type permits ArrayType, BasicType, PointerType, StructType, Type.Wildcard {
    Type WILDCARD = new Wildcard();

    String asString();
    boolean matches(Type other);

    final class Wildcard implements Type {
        private Wildcard() {}

        @Override
        public String asString() {
            return "?";
        }

        @Override
        public boolean matches(Type other) {
            return true;
        }
    }
}
