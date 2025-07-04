package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

public record FieldDeclarationTree(TypeTree type, NameTree name) implements Tree {
    @Override
    public Span span() {
        return type.span().merge(name.span());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
