package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.parser.type.Type;
import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

public record TypeTree(Type type, Kind kind, Span span) implements Tree {
    public static final Base BASE = new Base();

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }

    public interface Kind {}
    public static class Base implements Kind {
        private Base() {}
    }
    public record Pointer(TypeTree pointerType) implements Kind {}
    public record Array(TypeTree arrayType) implements Kind {}
    public record Struct(NameTree name) implements Kind {}
}
