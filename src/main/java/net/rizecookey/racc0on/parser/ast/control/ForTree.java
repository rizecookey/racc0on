package net.rizecookey.racc0on.parser.ast.control;

import net.rizecookey.racc0on.utils.Position;
import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionTree;
import net.rizecookey.racc0on.parser.ast.simp.SimpleStatementTree;
import net.rizecookey.racc0on.parser.ast.StatementTree;
import net.rizecookey.racc0on.parser.visitor.Visitor;
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
