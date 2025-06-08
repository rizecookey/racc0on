package net.rizecookey.racc0on.parser.visitor;

import net.rizecookey.racc0on.parser.ast.AssignmentTree;
import net.rizecookey.racc0on.parser.ast.BinaryOperationTree;
import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.BoolLiteralTree;
import net.rizecookey.racc0on.parser.ast.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.IdentExpressionTree;
import net.rizecookey.racc0on.parser.ast.LValueIdentTree;
import net.rizecookey.racc0on.parser.ast.IntLiteralTree;
import net.rizecookey.racc0on.parser.ast.TernaryExpressionTree;
import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.ast.UnaryOperationTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.parser.ast.control.ReturnTree;
import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.parser.ast.control.WhileTree;

/// A visitor that does nothing and returns [Unit#INSTANCE] by default.
/// This can be used to implement operations only for specific tree types.
public interface NoOpVisitor<T> extends Visitor<T, Unit> {

    @Override
    default Unit visit(AssignmentTree assignmentTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(BinaryOperationTree binaryOperationTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(BlockTree blockTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(DeclarationTree declarationTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(FunctionTree functionTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(IdentExpressionTree identExpressionTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(IntLiteralTree intLiteralTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(BoolLiteralTree boolLiteralTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(LValueIdentTree lValueIdentTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(NameTree nameTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(UnaryOperationTree unaryOperationTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(ProgramTree programTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(ReturnTree returnTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(TypeTree typeTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(LoopControlTree loopControlTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(IfElseTree ifElseTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(WhileTree whileTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(ForTree forTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(TernaryExpressionTree ternaryExpressionTree, T data) {
        return Unit.INSTANCE;
    }
}
