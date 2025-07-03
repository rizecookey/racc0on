package net.rizecookey.racc0on.parser.visitor;

import net.rizecookey.racc0on.parser.ast.exp.ExpressionTree;
import net.rizecookey.racc0on.parser.ast.simp.AssignmentTree;
import net.rizecookey.racc0on.parser.ast.exp.BinaryOperationTree;
import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.exp.BoolLiteralTree;
import net.rizecookey.racc0on.parser.ast.call.BuiltinCallTree;
import net.rizecookey.racc0on.parser.ast.simp.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.exp.IdentExpressionTree;
import net.rizecookey.racc0on.parser.ast.LValueIdentTree;
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
import net.rizecookey.racc0on.parser.ast.StatementTree;
import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.parser.ast.control.WhileTree;
import net.rizecookey.racc0on.parser.ast.call.FunctionCallTree;

/// A visitor that traverses a tree in postorder
/// @param <T> a type for additional data
/// @param <R> a type for a return type
public class RecursivePostorderVisitor<T, R> implements Visitor<T, R> {
    private final Visitor<T, R> visitor;

    public RecursivePostorderVisitor(Visitor<T, R> visitor) {
        this.visitor = visitor;
    }

    @Override
    public R visit(AssignmentTree assignmentTree, T data) {
        R r = assignmentTree.lValue().accept(this, data);
        r = assignmentTree.expression().accept(this, accumulate(data, r));
        r = this.visitor.visit(assignmentTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(BinaryOperationTree binaryOperationTree, T data) {
        R r = binaryOperationTree.lhs().accept(this, data);
        r = binaryOperationTree.rhs().accept(this, accumulate(data, r));
        r = this.visitor.visit(binaryOperationTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(BlockTree blockTree, T data) {
        R r;
        T d = data;
        for (StatementTree statement : blockTree.statements()) {
            r = statement.accept(this, d);
            d = accumulate(d, r);
        }
        r = this.visitor.visit(blockTree, d);
        return r;
    }

    @Override
    public R visit(DeclarationTree declarationTree, T data) {
        R r = declarationTree.type().accept(this, data);
        r = declarationTree.name().accept(this, accumulate(data, r));
        if (declarationTree.initializer() != null) {
            r = declarationTree.initializer().accept(this, accumulate(data, r));
        }
        r = this.visitor.visit(declarationTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(FunctionTree functionTree, T data) {
        R r = functionTree.returnType().accept(this, data);
        r = functionTree.name().accept(this, accumulate(data, r));
        r = functionTree.body().accept(this, accumulate(data, r));
        r = this.visitor.visit(functionTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(IdentExpressionTree identExpressionTree, T data) {
        R r = identExpressionTree.name().accept(this, data);
        r = this.visitor.visit(identExpressionTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(IntLiteralTree intLiteralTree, T data) {
        return this.visitor.visit(intLiteralTree, data);
    }

    @Override
    public R visit(BoolLiteralTree boolLiteralTree, T data) {
        return this.visitor.visit(boolLiteralTree, data);
    }

    @Override
    public R visit(LValueIdentTree lValueIdentTree, T data) {
        R r = lValueIdentTree.name().accept(this, data);
        r = this.visitor.visit(lValueIdentTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(NameTree nameTree, T data) {
        return this.visitor.visit(nameTree, data);
    }

    @Override
    public R visit(UnaryOperationTree unaryOperationTree, T data) {
        R r = unaryOperationTree.expression().accept(this, data);
        r = this.visitor.visit(unaryOperationTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(ProgramTree programTree, T data) {
        R r;
        T d = data;
        for (FunctionTree tree : programTree.functions()) {
            r = tree.accept(this, d);
            d = accumulate(data, r);
        }
        r = this.visitor.visit(programTree, d);
        return r;
    }

    @Override
    public R visit(ReturnTree returnTree, T data) {
        R r = returnTree.expression().accept(this, data);
        r = this.visitor.visit(returnTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(TypeTree typeTree, T data) {
        return this.visitor.visit(typeTree, data);
    }

    @Override
    public R visit(LoopControlTree loopControlTree, T data) {
        return this.visitor.visit(loopControlTree, data);
    }

    @Override
    public R visit(IfElseTree ifElseTree, T data) {
        R r = ifElseTree.condition().accept(this, data);
        r = ifElseTree.thenBranch().accept(this, accumulate(data, r));
        if (ifElseTree.elseBranch() != null) {
            r = ifElseTree.elseBranch().accept(this, accumulate(data, r));
        }
        r = this.visitor.visit(ifElseTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(WhileTree whileTree, T data) {
        R r = whileTree.condition().accept(this, data);
        r = whileTree.body().accept(this, accumulate(data, r));
        r = this.visitor.visit(whileTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(ForTree forTree, T data) {
        R r = null;
        if (forTree.initializer() != null) {
            r = forTree.initializer().accept(this, data);
        }

        r = forTree.condition().accept(this, r != null ? accumulate(data, r) : data);
        r = forTree.body().accept(this, accumulate(data, r));
        if (forTree.step() != null) {
            r = forTree.step().accept(this, accumulate(data, r));
        }
        r = this.visitor.visit(forTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(TernaryExpressionTree ternaryExpressionTree, T data) {
        R r = ternaryExpressionTree.condition().accept(this, data);
        r = ternaryExpressionTree.ifBranch().accept(this, accumulate(data, r));
        r = ternaryExpressionTree.elseBranch().accept(this, accumulate(data, r));
        r = this.visitor.visit(ternaryExpressionTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(ParameterTree parameterTree, T data) {
        R r = parameterTree.type().accept(this, data);
        r = parameterTree.name().accept(this, accumulate(data, r));

        r = this.visitor.visit(parameterTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(FunctionCallTree functionCallTree, T data) {
        R r = functionCallTree.name().accept(this, data);
        for (ExpressionTree arg : functionCallTree.arguments()) {
            r = arg.accept(this, accumulate(data, r));
        }
        r = this.visitor.visit(functionCallTree, accumulate(data, r));
        return r;
    }

    @Override
    public R visit(BuiltinCallTree builtinCallTree, T data) {
        R r = null;
        for (ExpressionTree arg : builtinCallTree.arguments()) {
            r = arg.accept(this, r == null ? data : accumulate(data, r));
        }
        r = this.visitor.visit(builtinCallTree, r == null ? data : accumulate(data, r));
        return r;
    }

    protected T accumulate(T data, R value) {
        return data;
    }
}
