package net.rizecookey.racc0on.parser.ast.exp;

import net.rizecookey.racc0on.lexer.keyword.PointerLiteralKeywordType;
import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

public record PointerLiteralTree(PointerLiteralKeywordType type, Span span) implements ExpressionTree {
    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
