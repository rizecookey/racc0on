package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.parser.symbol.Name;
import net.rizecookey.racc0on.parser.visitor.Visitor;

public record NameTree(Name name, Span span) implements Tree {
    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
