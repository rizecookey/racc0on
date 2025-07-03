package net.rizecookey.racc0on.parser.ast.call;

import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionTree;
import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

public record AllocArrayCallTree(TypeTree type, ExpressionTree elementCount, Span span) implements ExpressionTree {
    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
