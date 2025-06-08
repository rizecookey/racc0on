package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.Span;
import net.rizecookey.racc0on.lexer.OperatorType;
import net.rizecookey.racc0on.parser.visitor.Visitor;

public record UnaryOperationTree(OperatorType.Unary type, ExpressionTree expression, Span minusPos) implements ExpressionTree {
    @Override
    public Span span() {
        return minusPos().merge(expression().span());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
