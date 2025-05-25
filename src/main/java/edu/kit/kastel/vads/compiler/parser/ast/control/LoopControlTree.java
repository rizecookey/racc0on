package edu.kit.kastel.vads.compiler.parser.ast.control;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.lexer.keyword.KeywordType;
import edu.kit.kastel.vads.compiler.parser.ast.ControlTree;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;

public record LoopControlTree(KeywordType keywordType, Span span) implements ControlTree {
    @Override
    public Span span() {
        return span;
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
