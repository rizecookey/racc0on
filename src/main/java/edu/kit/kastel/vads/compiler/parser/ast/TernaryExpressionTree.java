package edu.kit.kastel.vads.compiler.parser.ast;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;

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
