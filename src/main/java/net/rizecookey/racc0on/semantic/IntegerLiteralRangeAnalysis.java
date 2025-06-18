package net.rizecookey.racc0on.semantic;

import net.rizecookey.racc0on.parser.ast.exp.IntLiteralTree;
import net.rizecookey.racc0on.parser.visitor.NoOpVisitor;
import net.rizecookey.racc0on.parser.visitor.RecursivePostorderVisitor;
import net.rizecookey.racc0on.parser.visitor.Unit;

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
