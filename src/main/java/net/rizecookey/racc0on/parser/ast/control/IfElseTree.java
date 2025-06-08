package net.rizecookey.racc0on.parser.ast.control;

import net.rizecookey.racc0on.utils.Position;
import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.parser.ast.ControlTree;
import net.rizecookey.racc0on.parser.ast.ExpressionTree;
import net.rizecookey.racc0on.parser.ast.StatementTree;
import net.rizecookey.racc0on.parser.visitor.Visitor;
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
