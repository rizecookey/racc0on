package net.rizecookey.racc0on.parser.visitor;

import net.rizecookey.racc0on.parser.ast.FieldTree;
import net.rizecookey.racc0on.parser.ast.StructTree;
import net.rizecookey.racc0on.parser.ast.call.AllocArrayCallTree;
import net.rizecookey.racc0on.parser.ast.call.AllocCallTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionFieldTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionDereferenceTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueDereferenceTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueFieldTree;
import net.rizecookey.racc0on.parser.ast.simp.AssignmentTree;
import net.rizecookey.racc0on.parser.ast.exp.BinaryOperationTree;
import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.exp.BoolLiteralTree;
import net.rizecookey.racc0on.parser.ast.call.BuiltinCallTree;
import net.rizecookey.racc0on.parser.ast.simp.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.exp.IdentExpressionTree;
import net.rizecookey.racc0on.parser.ast.ParameterTree;
import net.rizecookey.racc0on.parser.ast.exp.TernaryExpressionTree;
import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueIdentTree;
import net.rizecookey.racc0on.parser.ast.exp.IntLiteralTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.ast.exp.UnaryOperationTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.parser.ast.control.ReturnTree;
import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.parser.ast.control.WhileTree;
import net.rizecookey.racc0on.parser.ast.call.FunctionCallTree;

public interface Visitor<T, R> {

    R visit(AssignmentTree assignmentTree, T data);

    R visit(BinaryOperationTree binaryOperationTree, T data);

    R visit(BlockTree blockTree, T data);

    R visit(DeclarationTree declarationTree, T data);

    R visit(FunctionTree functionTree, T data);

    R visit(IdentExpressionTree identExpressionTree, T data);

    R visit(IntLiteralTree intLiteralTree, T data);

    R visit(BoolLiteralTree boolLiteralTree, T data);

    R visit(LValueIdentTree lValueIdentTree, T data);

    R visit(NameTree nameTree, T data);

    R visit(UnaryOperationTree unaryOperationTree, T data);

    R visit(ProgramTree programTree, T data);

    R visit(ReturnTree returnTree, T data);

    R visit(TypeTree typeTree, T data);

    R visit(LoopControlTree loopControlTree, T data);

    R visit(IfElseTree ifElseTree, T data);

    R visit(WhileTree whileTree, T data);

    R visit(ForTree forTree, T data);

    R visit(TernaryExpressionTree ternaryExpressionTree, T data);

    R visit(ParameterTree parameterTree, T data);

    R visit(FunctionCallTree functionCallTree, T data);

    R visit(BuiltinCallTree builtinCallTree, T data);

    R visit(StructTree structTree, T data);

    R visit(FieldTree fieldTree, T data);

    R visit(ExpressionArrayAccessTree expressionArrayAccessTree, T data);

    R visit(ExpressionDereferenceTree expressionDereferenceTree, T data);

    R visit(ExpressionFieldTree expressionFieldTree, T data);

    R visit(LValueArrayAccessTree lValueArrayAccessTree, T data);

    R visit(LValueDereferenceTree lValueDereferenceTree, T data);

    R visit(LValueFieldTree lValueFieldTree, T data);

    R visit(AllocCallTree allocCallTree, T data);

    R visit(AllocArrayCallTree allocArrayCallTree, T data);
}
