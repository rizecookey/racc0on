package net.rizecookey.racc0on.parser.ast.call;

import net.rizecookey.racc0on.lexer.Keyword;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionTree;
import net.rizecookey.racc0on.parser.symbol.Name;
import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

import java.util.List;

public record BuiltinCallTree(Keyword keyword, List<ExpressionTree> arguments, Span span) implements CallTree {

    @Override
    public Name functionName() {
        return Name.forKeyword(keyword());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
