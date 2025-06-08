package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.Span;
import net.rizecookey.racc0on.parser.visitor.Visitor;

public record FunctionTree(TypeTree returnType, NameTree name, BlockTree body) implements Tree {
    @Override
    public Span span() {
        return new Span.SimpleSpan(returnType().span().start(), body().span().end());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
