package edu.kit.kastel.vads.compiler.semantic;

import edu.kit.kastel.vads.compiler.parser.ast.IntLiteralTree;
import edu.kit.kastel.vads.compiler.parser.visitor.NoOpVisitor;
import edu.kit.kastel.vads.compiler.parser.visitor.RecursivePostorderVisitor;
import edu.kit.kastel.vads.compiler.parser.visitor.Unit;

public class IntegerLiteralRangeAnalysis extends RecursivePostorderVisitor<Namespace<Void>, Unit> {

    public IntegerLiteralRangeAnalysis() {
        super(new NoOpVisitor<>() {
            @Override
            public Unit visit(IntLiteralTree intLiteralTree, Namespace<Void> data) {
                intLiteralTree.parseValue()
                        .orElseThrow(
                                () -> new SemanticException(intLiteralTree.span(), "invalid integer literal " + intLiteralTree.value())
                        );
                return NoOpVisitor.super.visit(intLiteralTree, data);
            }
        });
    }
}
