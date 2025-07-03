package net.rizecookey.racc0on.parser.ast.exp;

import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

public record ExpressionFieldTree(ExpressionTree struct, NameTree fieldName) implements ExpressionTree {
    @Override
    public Span span() {
        return struct.span().merge(fieldName.span());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
