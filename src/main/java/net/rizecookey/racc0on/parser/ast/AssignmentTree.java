package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.lexer.OperatorType;
import net.rizecookey.racc0on.parser.visitor.Visitor;

public record AssignmentTree(LValueTree lValue, ExpressionTree expression, OperatorType.Assignment type) implements SimpleStatementTree {
    @Override
    public Span span() {
        return lValue().span().merge(expression().span());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
