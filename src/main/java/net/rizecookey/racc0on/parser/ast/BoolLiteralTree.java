package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.Span;
import net.rizecookey.racc0on.parser.visitor.Visitor;

public record BoolLiteralTree(boolean value, Span span) implements ExpressionTree {
    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
