package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.Span;
import net.rizecookey.racc0on.parser.visitor.Visitor;

import java.util.List;

public record BlockTree(List<StatementTree> statements, Span span) implements StatementTree {

    public BlockTree {
        statements = List.copyOf(statements);
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
