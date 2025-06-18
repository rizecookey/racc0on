package net.rizecookey.racc0on.parser.ast.simp;

import net.rizecookey.racc0on.parser.ast.exp.ExpressionTree;
import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.parser.visitor.Visitor;
import org.jspecify.annotations.Nullable;

public record DeclarationTree(TypeTree type, NameTree name, @Nullable ExpressionTree initializer) implements SimpleStatementTree {
    @Override
    public Span span() {
        if (initializer() != null) {
            return type().span().merge(initializer().span());
        }
        return type().span().merge(name().span());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
