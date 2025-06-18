package net.rizecookey.racc0on.parser.visitor;

import net.rizecookey.racc0on.parser.ast.simp.AssignmentTree;
import net.rizecookey.racc0on.parser.ast.exp.BinaryOperationTree;
import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.exp.BoolLiteralTree;
import net.rizecookey.racc0on.parser.ast.simp.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.exp.IdentExpressionTree;
import net.rizecookey.racc0on.parser.ast.ParameterTree;
import net.rizecookey.racc0on.parser.ast.exp.TernaryExpressionTree;
import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.LValueIdentTree;
import net.rizecookey.racc0on.parser.ast.exp.IntLiteralTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.ast.exp.UnaryOperationTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.parser.ast.control.ReturnTree;
import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.parser.ast.control.WhileTree;

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
}
