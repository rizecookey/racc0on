package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.parser.visitor.Visitor;

public record LValueIdentTree(NameTree name) implements LValueTree {
    @Override
    public Span span() {
        return name().span();
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
