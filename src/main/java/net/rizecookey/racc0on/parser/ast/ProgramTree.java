package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.utils.Position;
import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.parser.visitor.Visitor;

import java.util.List;

public record ProgramTree(List<FunctionTree> functions) implements Tree {
    public ProgramTree {
        functions = List.copyOf(functions);
    }
    @Override
    public Span span() {
        if (functions.isEmpty()) {
            Position zero = new Position.SimplePosition(0, 0);
            return new Span.SimpleSpan(zero, zero);
        }

        var first = functions.getFirst();
        var last = functions.getLast();
        return new Span.SimpleSpan(first.span().start(), last.span().end());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
