package edu.kit.kastel.vads.compiler.semantic;

import edu.kit.kastel.vads.compiler.Position;
import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.IfElseTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.ReturnTree;
import edu.kit.kastel.vads.compiler.parser.visitor.NoOpVisitor;
import edu.kit.kastel.vads.compiler.parser.visitor.Unit;

/// Checks that functions return.
/// Currently only works for straight-line code.
class ReturnAnalysis implements NoOpVisitor<ReturnAnalysis.ReturnState> {

    static class ReturnState {
        boolean returns = false;
    }

    @Override
    public Unit visit(ReturnTree returnTree, ReturnState data) {
        data.returns = true;
        return NoOpVisitor.super.visit(returnTree, data);
    }

    @Override
    public Unit visit(ProgramTree programTree, ReturnState data) {
        programTree.topLevelTrees().forEach(t -> t.accept(this, data));
        return NoOpVisitor.super.visit(programTree, data);
    }

    @Override
    public Unit visit(FunctionTree functionTree, ReturnState data) {
        functionTree.body().accept(this, data);
        if (!data.returns) {
            Position end = functionTree.span().end();
            throw new SemanticException(new Span.SimpleSpan(new Position.SimplePosition(end.line(), end.column() - 1), end),
                    "function " + functionTree.name().name().asString() + " does not return");
        }
        data.returns = false;
        return NoOpVisitor.super.visit(functionTree, data);
    }

    @Override
    public Unit visit(BlockTree blockTree, ReturnState data) {
        for (StatementTree statement : blockTree.statements()) {
            ReturnState statementReturns = new ReturnState();
            statement.accept(this, statementReturns);
            if (statementReturns.returns) {
                data.returns = true;
                break;
            }
        }

        return NoOpVisitor.super.visit(blockTree, data);
    }

    @Override
    public Unit visit(IfElseTree ifElseTree, ReturnState data) {
        if (ifElseTree.elseBranch() == null) {
            return NoOpVisitor.super.visit(ifElseTree, data);
        }

        ReturnState thenBranchReturns = new ReturnState();
        ReturnState elseBranchReturns = new ReturnState();
        ifElseTree.thenBranch().accept(this, thenBranchReturns);
        ifElseTree.elseBranch().accept(this, elseBranchReturns);
        data.returns = data.returns || thenBranchReturns.returns && elseBranchReturns.returns;
        return NoOpVisitor.super.visit(ifElseTree, data);
    }
}
