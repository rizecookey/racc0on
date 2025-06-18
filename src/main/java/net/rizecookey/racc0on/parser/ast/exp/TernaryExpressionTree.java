package net.rizecookey.racc0on.parser.ast.exp;

import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.parser.visitor.Visitor;

public record TernaryExpressionTree(ExpressionTree condition, ExpressionTree ifBranch, ExpressionTree elseBranch) implements ExpressionTree {
    @Override
    public Span span() {
        return condition.span().merge(elseBranch.span());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
