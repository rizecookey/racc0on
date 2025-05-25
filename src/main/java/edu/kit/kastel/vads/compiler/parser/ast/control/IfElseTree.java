package edu.kit.kastel.vads.compiler.parser.ast.control;

import edu.kit.kastel.vads.compiler.Position;
import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.parser.ast.ControlTree;
import edu.kit.kastel.vads.compiler.parser.ast.ExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import org.jspecify.annotations.Nullable;

public record IfElseTree(ExpressionTree condition, StatementTree thenBranch, @Nullable StatementTree elseBranch,
                         Position start) implements ControlTree {
    @Override
    public Span span() {
        return new Span.SimpleSpan(start(), elseBranch() != null ? elseBranch().span().end() : thenBranch().span().end());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
