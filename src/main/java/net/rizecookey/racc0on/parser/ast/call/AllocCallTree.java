package net.rizecookey.racc0on.parser.ast.call;

import net.rizecookey.racc0on.lexer.keyword.AllocKeywordType;
import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionTree;
import net.rizecookey.racc0on.parser.symbol.Name;
import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

import java.util.List;

public record AllocCallTree(TypeTree type, Span span) implements CallTree {
    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }

    @Override
    public Name functionName() {
        return Name.forKeyword(AllocKeywordType.ALLOC);
    }

    @Override
    public List<ExpressionTree> arguments() {
        return List.of();
    }
}
