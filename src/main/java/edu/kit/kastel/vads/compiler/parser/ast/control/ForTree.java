package edu.kit.kastel.vads.compiler.parser.ast.control;

import edu.kit.kastel.vads.compiler.Position;
import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.parser.ast.ControlTree;
import edu.kit.kastel.vads.compiler.parser.ast.ExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.SimpleStatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import org.jspecify.annotations.Nullable;

public record ForTree(@Nullable SimpleStatementTree initializer, ExpressionTree condition, StatementTree body,
                      @Nullable SimpleStatementTree step, Position start) implements ControlTree {
    @Override
    public Span span() {
        return new Span.SimpleSpan(start, body.span().end());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
