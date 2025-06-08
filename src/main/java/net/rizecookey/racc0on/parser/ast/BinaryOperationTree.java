package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.Span;
import net.rizecookey.racc0on.lexer.OperatorType;
import net.rizecookey.racc0on.parser.visitor.Visitor;

public record BinaryOperationTree(
    ExpressionTree lhs, ExpressionTree rhs, OperatorType.Binary operatorType
) implements ExpressionTree {
    @Override
    public Span span() {
        return lhs().span().merge(rhs().span());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
