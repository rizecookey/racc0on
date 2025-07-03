package net.rizecookey.racc0on.parser.ast.lvalue;

import net.rizecookey.racc0on.parser.ast.exp.ExpressionTree;
import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

public record LValueArrayAccessTree(LValueTree array, ExpressionTree index, Span span) implements LValueTree {
    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
