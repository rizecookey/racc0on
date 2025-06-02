package edu.kit.kastel.vads.compiler.parser.ast.control;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.lexer.keyword.ControlKeywordType;
import edu.kit.kastel.vads.compiler.parser.ast.ControlTree;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;

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
