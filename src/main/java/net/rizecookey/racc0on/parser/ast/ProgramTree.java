package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

import java.util.List;

public record ProgramTree(List<StructDeclarationTree> structs, List<FunctionTree> functions, Span span) implements Tree {
    public ProgramTree {
        functions = List.copyOf(functions);
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
