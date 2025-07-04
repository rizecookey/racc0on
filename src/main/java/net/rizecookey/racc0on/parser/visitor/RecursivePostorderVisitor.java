package net.rizecookey.racc0on.parser.visitor;

import net.rizecookey.racc0on.parser.ast.FieldTree;
import net.rizecookey.racc0on.parser.ast.StructDeclarationTree;
import net.rizecookey.racc0on.parser.ast.call.AllocArrayCallTree;
import net.rizecookey.racc0on.parser.ast.call.AllocCallTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionDereferenceTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionFieldTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionTree;
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
        T d = accumulate(data, r);
        r = assignmentTree.expression().accept(this, d);
        d = accumulate(d, r);
        r = this.visitor.visit(assignmentTree, d);
        return r;
    }

    @Override
    public R visit(BinaryOperationTree binaryOperationTree, T data) {
        R r = binaryOperationTree.lhs().accept(this, data);
        T d = accumulate(data, r);
        r = binaryOperationTree.rhs().accept(this, d);
        d = accumulate(d, r);
        r = this.visitor.visit(binaryOperationTree, d);
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
        T d = accumulate(data, r);
        r = declarationTree.name().accept(this, d);
        d = accumulate(d, r);
        if (declarationTree.initializer() != null) {
            r = declarationTree.initializer().accept(this, d);
            d = accumulate(d, r);
        }
        r = this.visitor.visit(declarationTree, d);
        return r;
    }

    @Override
    public R visit(FunctionTree functionTree, T data) {
        R r = functionTree.returnType().accept(this, data);
        T d = accumulate(data, r);
        r = functionTree.name().accept(this, d);
        d = accumulate(d, r);
        r = functionTree.body().accept(this, d);
        d = accumulate(d, r);
        r = this.visitor.visit(functionTree, d);
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
        for (StructDeclarationTree tree : programTree.structs()) {
            r = tree.accept(this, d);
            d = accumulate(data, r);
        }
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
        T d = accumulate(data, r);
        r = ifElseTree.thenBranch().accept(this, d);
        d = accumulate(d, r);
        if (ifElseTree.elseBranch() != null) {
            r = ifElseTree.elseBranch().accept(this, d);
            d = accumulate(d, r);
        }
        r = this.visitor.visit(ifElseTree, d);
        return r;
    }

    @Override
    public R visit(WhileTree whileTree, T data) {
        R r = whileTree.condition().accept(this, data);
        T d = accumulate(data, r);
        r = whileTree.body().accept(this, d);
        d = accumulate(d, r);
        r = this.visitor.visit(whileTree, d);
        return r;
    }

    @Override
    public R visit(ForTree forTree, T data) {
        R r;
        T d = data;
        if (forTree.initializer() != null) {
            r = forTree.initializer().accept(this, d);
            d = accumulate(d, r);
        }

        r = forTree.condition().accept(this, d);
        d = accumulate(d, r);
        r = forTree.body().accept(this, d);
        d = accumulate(d, r);
        if (forTree.step() != null) {
            r = forTree.step().accept(this, d);
            d = accumulate(d, r);
        }
        r = this.visitor.visit(forTree, d);
        return r;
    }

    @Override
    public R visit(TernaryExpressionTree ternaryExpressionTree, T data) {
        R r = ternaryExpressionTree.condition().accept(this, data);
        T d = accumulate(data, r);
        r = ternaryExpressionTree.ifBranch().accept(this, d);
        d = accumulate(d, r);
        r = ternaryExpressionTree.elseBranch().accept(this, d);
        d = accumulate(d, r);
        r = this.visitor.visit(ternaryExpressionTree, d);
        return r;
    }

    @Override
    public R visit(ParameterTree parameterTree, T data) {
        R r = parameterTree.type().accept(this, data);
        T d = accumulate(data, r);
        r = parameterTree.name().accept(this, d);
        d = accumulate(d, r);

        r = this.visitor.visit(parameterTree, d);
        return r;
    }

    @Override
    public R visit(FunctionCallTree functionCallTree, T data) {
        R r = functionCallTree.name().accept(this, data);
        T d = accumulate(data, r);
        for (ExpressionTree arg : functionCallTree.arguments()) {
            r = arg.accept(this, d);
            d = accumulate(d, r);
        }
        r = this.visitor.visit(functionCallTree, d);
        return r;
    }

    @Override
    public R visit(BuiltinCallTree builtinCallTree, T data) {
        R r;
        T d = data;
        for (ExpressionTree arg : builtinCallTree.arguments()) {
            r = arg.accept(this, d);
            d = accumulate(d, r);
        }
        r = this.visitor.visit(builtinCallTree, d);
        return r;
    }

    @Override
    public R visit(StructDeclarationTree structDeclarationTree, T data) {
        R r;
        T d = data;
        for (FieldTree field : structDeclarationTree.fields()) {
            r = field.accept(this, d);
            d = accumulate(d, r);
        }

        return this.visitor.visit(structDeclarationTree, d);
    }

    @Override
    public R visit(FieldTree fieldTree, T data) {
        R r = fieldTree.type().accept(this, data);
        T d = accumulate(data, r);
        r = fieldTree.name().accept(this, d);
        d = accumulate(d, r);

        return this.visitor.visit(fieldTree, d);
    }

    @Override
    public R visit(ExpressionArrayAccessTree expressionArrayAccessTree, T data) {
        R r = expressionArrayAccessTree.array().accept(this, data);
        T d = accumulate(data, r);
        r = expressionArrayAccessTree.index().accept(this, d);
        d = accumulate(d, r);

        return this.visitor.visit(expressionArrayAccessTree, d);
    }

    @Override
    public R visit(ExpressionDereferenceTree expressionDereferenceTree, T data) {
        R r = expressionDereferenceTree.pointer().accept(this, data);

        return this.visitor.visit(expressionDereferenceTree, accumulate(data, r));
    }

    @Override
    public R visit(ExpressionFieldTree expressionFieldTree, T data) {
        R r = expressionFieldTree.struct().accept(this, data);
        T d = accumulate(data, r);
        r = expressionFieldTree.fieldName().accept(this, d);
        d = accumulate(d, r);

        return this.visitor.visit(expressionFieldTree, d);
    }

    @Override
    public R visit(LValueArrayAccessTree lValueArrayAccessTree, T data) {
        R r = lValueArrayAccessTree.array().accept(this, data);
        T d = accumulate(data, r);
        r = lValueArrayAccessTree.index().accept(this, d);
        d = accumulate(d, r);

        return this.visitor.visit(lValueArrayAccessTree, d);
    }

    @Override
    public R visit(LValueDereferenceTree lValueDereferenceTree, T data) {
        R r = lValueDereferenceTree.pointer().accept(this, data);

        return this.visitor.visit(lValueDereferenceTree, accumulate(data, r));
    }

    @Override
    public R visit(LValueFieldTree lValueFieldTree, T data) {
        R r = lValueFieldTree.struct().accept(this, data);
        T d = accumulate(data, r);
        r = lValueFieldTree.fieldName().accept(this, d);
        d = accumulate(d, r);

        return this.visitor.visit(lValueFieldTree, d);
    }

    @Override
    public R visit(AllocCallTree allocCallTree, T data) {
        R r = allocCallTree.type().accept(this, data);

        return this.visitor.visit(allocCallTree, accumulate(data, r));
    }

    @Override
    public R visit(AllocArrayCallTree allocArrayCallTree, T data) {
        R r = allocArrayCallTree.type().accept(this, data);
        T d = accumulate(data, r);
        r = allocArrayCallTree.elementCount().accept(this, d);

        return this.visitor.visit(allocArrayCallTree, accumulate(d, r));
    }

    protected T accumulate(T data, R value) {
        return data;
    }
}
