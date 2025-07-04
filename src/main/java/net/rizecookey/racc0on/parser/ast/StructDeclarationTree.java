package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

import java.util.List;

public record StructDeclarationTree(NameTree name, List<FieldTree> fields, Span span) implements Tree {
    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
