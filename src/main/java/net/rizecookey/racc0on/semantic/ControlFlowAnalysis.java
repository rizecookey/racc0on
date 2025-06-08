package net.rizecookey.racc0on.semantic;

import net.rizecookey.racc0on.utils.Position;
import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.parser.ast.StatementTree;
import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.control.ReturnTree;
import net.rizecookey.racc0on.parser.ast.control.WhileTree;
import net.rizecookey.racc0on.parser.visitor.NoOpVisitor;
import net.rizecookey.racc0on.parser.visitor.Unit;

/// Checks that:
///   - functions return
///   - continue or while statements only occur within loops
///   - step statements in for loops are not a declaration
class ControlFlowAnalysis implements NoOpVisitor<ControlFlowAnalysis.ControlFlowState> {

    static class ControlFlowState {
        boolean returns;
        boolean withinLoop;

        ControlFlowState(boolean returns, boolean withinLoop) {
            this.returns = returns;
            this.withinLoop = withinLoop;
        }
    }

    @Override
    public Unit visit(ReturnTree returnTree, ControlFlowState data) {
        data.returns = true;
        return NoOpVisitor.super.visit(returnTree, data);
    }

    @Override
    public Unit visit(ProgramTree programTree, ControlFlowState data) {
        programTree.topLevelTrees().forEach(t -> t.accept(this, data));
        return NoOpVisitor.super.visit(programTree, data);
    }

    @Override
    public Unit visit(FunctionTree functionTree, ControlFlowState data) {
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
    public Unit visit(BlockTree blockTree, ControlFlowState data) {
        for (StatementTree statement : blockTree.statements()) {
            ControlFlowState statementReturns = new ControlFlowState(false, data.withinLoop);
            statement.accept(this, statementReturns);
            if (statementReturns.returns) {
                data.returns = true;
                break;
            }
        }

        return NoOpVisitor.super.visit(blockTree, data);
    }

    @Override
    public Unit visit(IfElseTree ifElseTree, ControlFlowState data) {
        ControlFlowState thenBranchReturns = new ControlFlowState(false, data.withinLoop);
        ifElseTree.thenBranch().accept(this, thenBranchReturns);

        if (ifElseTree.elseBranch() == null) {
            return NoOpVisitor.super.visit(ifElseTree, data);
        }

        ControlFlowState elseBranchReturns = new ControlFlowState(false, data.withinLoop);
        ifElseTree.elseBranch().accept(this, elseBranchReturns);
        data.returns = data.returns || thenBranchReturns.returns && elseBranchReturns.returns;
        return NoOpVisitor.super.visit(ifElseTree, data);
    }

    @Override
    public Unit visit(LoopControlTree loopControlTree, ControlFlowState data) {
        if (!data.withinLoop) {
            throw new SemanticException(loopControlTree.span(), loopControlTree.type().keyword().keyword() + " outside of loop");
        }
        return NoOpVisitor.super.visit(loopControlTree, data);
    }

    @Override
    public Unit visit(WhileTree whileTree, ControlFlowState data) {
        ControlFlowState inner = new ControlFlowState(false, true);
        whileTree.body().accept(this, inner);

        return NoOpVisitor.super.visit(whileTree, data);
    }

    @Override
    public Unit visit(ForTree forTree, ControlFlowState data) {
        ControlFlowState inner = new ControlFlowState(false, true);
        forTree.body().accept(this, inner);

        if (forTree.step() instanceof DeclarationTree) {
            throw new SemanticException(forTree.step().span(), "step statement must not be a declaration");
        }

        return NoOpVisitor.super.visit(forTree, data);
    }
}
