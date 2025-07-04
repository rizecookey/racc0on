package net.rizecookey.racc0on.parser.visitor;

import net.rizecookey.racc0on.parser.ast.FieldDeclarationTree;
import net.rizecookey.racc0on.parser.ast.StructDeclarationTree;
import net.rizecookey.racc0on.parser.ast.call.AllocArrayCallTree;
import net.rizecookey.racc0on.parser.ast.call.AllocCallTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpDereferenceTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpFieldAccessTree;
import net.rizecookey.racc0on.parser.ast.exp.PointerLiteralTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueDereferenceTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueFieldAccessTree;
import net.rizecookey.racc0on.parser.ast.simp.AssignmentTree;
import net.rizecookey.racc0on.parser.ast.exp.BinaryOperationTree;
import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.exp.BoolLiteralTree;
import net.rizecookey.racc0on.parser.ast.call.BuiltinCallTree;
import net.rizecookey.racc0on.parser.ast.simp.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.exp.IdentExpressionTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueIdentTree;
import net.rizecookey.racc0on.parser.ast.exp.IntLiteralTree;
import net.rizecookey.racc0on.parser.ast.ParameterTree;
import net.rizecookey.racc0on.parser.ast.exp.TernaryExpressionTree;
import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.ast.exp.UnaryOperationTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.parser.ast.control.ReturnTree;
import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.parser.ast.control.WhileTree;
import net.rizecookey.racc0on.parser.ast.call.FunctionCallTree;

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
    
    @Override
    default Unit visit(ParameterTree parameterTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(FunctionCallTree functionCallTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(BuiltinCallTree builtinCallTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(StructDeclarationTree structDeclarationTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(FieldDeclarationTree fieldDeclarationTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(ExpArrayAccessTree expArrayAccessTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(ExpDereferenceTree expDereferenceTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(ExpFieldAccessTree expFieldAccessTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(LValueArrayAccessTree lValueArrayAccessTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(LValueDereferenceTree lValueDereferenceTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(LValueFieldAccessTree lValueFieldAccessTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(AllocCallTree allocCallTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(AllocArrayCallTree allocArrayCallTree, T data) {
        return Unit.INSTANCE;
    }

    @Override
    default Unit visit(PointerLiteralTree pointerLiteralTree, T data) {
        return Unit.INSTANCE;
    }
}
