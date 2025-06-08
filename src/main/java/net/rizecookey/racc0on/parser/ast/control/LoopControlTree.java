package net.rizecookey.racc0on.parser.ast.control;

import net.rizecookey.racc0on.Span;
import net.rizecookey.racc0on.lexer.keyword.ControlKeywordType;
import net.rizecookey.racc0on.parser.ast.ControlTree;
import net.rizecookey.racc0on.parser.visitor.Visitor;

public record LoopControlTree(Type type, Span span) implements ControlTree {
    @Override
    public Span span() {
        return span;
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }

    public enum Type {
        CONTINUE(ControlKeywordType.CONTINUE),
        BREAK(ControlKeywordType.BREAK),
        ;

        private final ControlKeywordType keyword;
        Type(ControlKeywordType keyword) {
            this.keyword = keyword;
        }

        public ControlKeywordType keyword() {
            return keyword;
        }
    }
}
