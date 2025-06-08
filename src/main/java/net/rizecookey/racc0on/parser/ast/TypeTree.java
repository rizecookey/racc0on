package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.parser.type.Type;
import net.rizecookey.racc0on.parser.visitor.Visitor;

public record TypeTree(Type type, Span span) implements Tree {
    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
