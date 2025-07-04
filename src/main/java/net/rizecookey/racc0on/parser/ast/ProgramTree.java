package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.utils.Position;
import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.parser.visitor.Visitor;

import java.util.List;

public record ProgramTree(List<StructDeclarationTree> structs, List<FunctionTree> functions) implements Tree {
    public ProgramTree {
        functions = List.copyOf(functions);
    }
    @Override
    public Span span() {
        if (functions.isEmpty()) {
            Position zero = new Position.SimplePosition(0, 0);
            return new Span.SimpleSpan(zero, zero);
        }

        var start = Span.min(functions.getFirst().span().start(), structs.getFirst().span().start());
        var end = Span.max(functions.getLast().span().end(), structs.getLast().span().end());
        return new Span.SimpleSpan(start, end);
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
