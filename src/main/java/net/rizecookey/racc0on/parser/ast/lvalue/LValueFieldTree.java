package net.rizecookey.racc0on.parser.ast.lvalue;

import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

public record LValueFieldTree(LValueTree struct, NameTree fieldName) implements LValueTree {
    @Override
    public Span span() {
        return struct.span().merge(fieldName.span());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
