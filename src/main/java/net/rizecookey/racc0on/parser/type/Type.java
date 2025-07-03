package net.rizecookey.racc0on.parser.type;

public sealed interface Type permits ArrayType, BasicType, PointerType, StructType {
    String asString();
}
