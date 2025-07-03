package net.rizecookey.racc0on.semantic;

import net.rizecookey.racc0on.parser.ast.FieldTree;
import net.rizecookey.racc0on.parser.ast.ParameterTree;
import net.rizecookey.racc0on.parser.ast.StructTree;
import net.rizecookey.racc0on.parser.ast.call.AllocArrayCallTree;
import net.rizecookey.racc0on.parser.ast.call.AllocCallTree;
import net.rizecookey.racc0on.parser.ast.call.BuiltinCallTree;
import net.rizecookey.racc0on.parser.ast.call.FunctionCallTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionDereferenceTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionFieldTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpressionTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueDereferenceTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueFieldTree;
import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.parser.ast.simp.AssignmentTree;
import net.rizecookey.racc0on.parser.ast.exp.BinaryOperationTree;
import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.exp.BoolLiteralTree;
import net.rizecookey.racc0on.parser.ast.simp.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.exp.IdentExpressionTree;
import net.rizecookey.racc0on.parser.ast.exp.IntLiteralTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueIdentTree;
import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.parser.ast.StatementTree;
import net.rizecookey.racc0on.parser.ast.exp.TernaryExpressionTree;
import net.rizecookey.racc0on.parser.ast.Tree;
import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.parser.ast.exp.UnaryOperationTree;
import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.control.ReturnTree;
import net.rizecookey.racc0on.parser.ast.control.WhileTree;
import net.rizecookey.racc0on.parser.type.BasicType;
import net.rizecookey.racc0on.parser.type.Type;
import net.rizecookey.racc0on.parser.visitor.Visitor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

class TypeAnalysis implements Visitor<TypeAnalysis.FunctionInfo, Optional<Type>> {
    private Namespace<FunctionTree> functionNamespace = new Namespace<>();

    static class FunctionInfo {
        @Nullable FunctionTree function;
        Namespace<Type> namespace;

        FunctionInfo(@Nullable FunctionTree function) {
            this.function = function;
            this.namespace = new Namespace<>();
        }
    }

    @Override
    public Optional<Type> visit(AssignmentTree assignmentTree, FunctionInfo info) {
        Type type = expectTypeSame(info, assignmentTree.lValue(), assignmentTree.expression());
        type = switch (assignmentTree.type()) {
            case MINUS, PLUS, MUL, DIV, MOD, BITWISE_AND, BITWISE_OR, BITWISE_XOR, SHIFT_LEFT, SHIFT_RIGHT
                    -> expectType(assignmentTree.span(), BasicType.INT, type);
            case DEFAULT -> type;
        };
        return Optional.of(type);
    }

    @Override
    public Optional<Type> visit(BinaryOperationTree binaryOperationTree, FunctionInfo info) {
        Type operandsType = expectTypeSame(info, binaryOperationTree.lhs(), binaryOperationTree.rhs());

        Type resultType = switch (binaryOperationTree.operatorType()) {
            case PLUS, MINUS, MUL, DIV, MOD, BITWISE_AND, BITWISE_OR, BITWISE_XOR,
                 SHIFT_LEFT, SHIFT_RIGHT -> {
                expectType(binaryOperationTree.span(), BasicType.INT, operandsType);
                yield BasicType.INT;
            }
            case LESS_THAN, LESS_OR_EQUAL, GREATER_THAN, GREATER_OR_EQUAL -> {
                expectType(binaryOperationTree.span(), BasicType.INT, operandsType);
                yield BasicType.BOOL;
            }
            case AND, OR -> {
                expectType(binaryOperationTree.span(), BasicType.BOOL, operandsType);
                yield BasicType.BOOL;
            }
            case EQUAL, NOT_EQUAL -> BasicType.BOOL;
        };
        return Optional.of(resultType);
    }

    @Override
    public Optional<Type> visit(BlockTree blockTree, FunctionInfo info) {
        for (StatementTree statement : blockTree.statements()) {
            statement.accept(this, info);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(DeclarationTree declarationTree, FunctionInfo info) {
        Type type = declarationTree.initializer() != null
                ? expectTypeSame(info, declarationTree.type(), declarationTree.initializer())
                : declarationTree.type().accept(this, info).orElseThrow();
        info.namespace.put(declarationTree.name().name(), type);
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(FunctionTree functionTree, FunctionInfo info) {
        functionTree.parameters().forEach(p -> info.namespace.put(p.name().name(), p.type().type()));
        functionTree.body().accept(this, info);
        return functionTree.returnType().accept(this, info);
    }

    @Override
    public Optional<Type> visit(IdentExpressionTree identExpressionTree, FunctionInfo info) {
        Type type = info.namespace.get(identExpressionTree.name());
        return type != null ? Optional.of(type) : Optional.empty();
    }

    @Override
    public Optional<Type> visit(IntLiteralTree intLiteralTree, FunctionInfo info) {
        return Optional.of(BasicType.INT);
    }

    @Override
    public Optional<Type> visit(BoolLiteralTree boolLiteralTree, FunctionInfo info) {
        return Optional.of(BasicType.BOOL);
    }

    @Override
    public Optional<Type> visit(LValueIdentTree lValueIdentTree, FunctionInfo info) {
        Type type = info.namespace.get(lValueIdentTree.name());
        return type != null ? Optional.of(type) : Optional.empty();
    }

    @Override
    public Optional<Type> visit(NameTree nameTree, FunctionInfo info) {
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(UnaryOperationTree unaryOperationTree, FunctionInfo info) {
        Type type = switch (unaryOperationTree.type()) {
            case NEGATION, BITWISE_NOT -> expectType(info, BasicType.INT, unaryOperationTree.expression());
            case NOT -> expectType(info, BasicType.BOOL, unaryOperationTree.expression());
        };

        return Optional.of(type);
    }

    @Override
    public Optional<Type> visit(ProgramTree programTree, FunctionInfo info) {
        boolean mainFound = false;
        for (FunctionTree function : programTree.functions()) {
            if (function.name().name().asString().equals("main")) {
                expectType(function.returnType().span(), BasicType.INT, function.returnType().accept(this, info).orElseThrow());
                if (!function.parameters().isEmpty()) {
                    throw new SemanticException(function.parameters().getFirst().span().merge(function.parameters().getLast().span()),
                            "main function should not have any parameters");
                }
                mainFound = true;
            }

            if (functionNamespace.get(function.name()) != null) {
                throw new SemanticException(function.name().span(), "a function with the name "
                        + function.name().name().asString() + " already exists");
            }

            functionNamespace.put(function.name().name(), function);
        }

        if (!mainFound) {
            Span span = new Span.SimpleSpan(programTree.span().end(), programTree.span().end());
            throw new SemanticException(span, "missing main function");
        }
        functionNamespace = new Namespace<>();
        programTree.functions().forEach(t -> functionNamespace.put(t.name().name(), t));
        programTree.functions().forEach(t -> t.accept(this, new FunctionInfo(t)));
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(ReturnTree returnTree, FunctionInfo info) {
        if (info.function == null) {
            throw new IllegalStateException("No current function found");
        }
        expectType(info, info.function.returnType().type(), returnTree.expression());
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(TypeTree typeTree, FunctionInfo info) {
        return Optional.of(typeTree.type());
    }

    @Override
    public Optional<Type> visit(LoopControlTree loopControlTree, FunctionInfo info) {
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(IfElseTree ifElseTree, FunctionInfo info) {
        expectType(info, BasicType.BOOL, ifElseTree.condition());
        ifElseTree.thenBranch().accept(this, info);
        if (ifElseTree.elseBranch() != null) {
            ifElseTree.elseBranch().accept(this, info);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(WhileTree whileTree, FunctionInfo info) {
        expectType(info, BasicType.BOOL, whileTree.condition());
        whileTree.body().accept(this, info);
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(ForTree forTree, FunctionInfo info) {
        if (forTree.initializer() != null) {
            forTree.initializer().accept(this, info);
        }
        expectType(info, BasicType.BOOL, forTree.condition());
        forTree.body().accept(this, info);
        if (forTree.step() != null) {
            forTree.step().accept(this, info);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(TernaryExpressionTree ternaryExpressionTree, FunctionInfo info) {
        expectType(info, BasicType.BOOL, ternaryExpressionTree.condition());
        return Optional.of(expectTypeSame(info, ternaryExpressionTree.ifBranch(), ternaryExpressionTree.elseBranch()));
    }

    @Override
    public Optional<Type> visit(ParameterTree parameterTree, FunctionInfo info) {
        return parameterTree.type().accept(this, info);
    }

    @Override
    public Optional<Type> visit(FunctionCallTree functionCallTree, FunctionInfo info) {
        FunctionTree functionTree = functionNamespace.get(functionCallTree.name());
        if (functionTree == null) {
            throw new SemanticException(functionCallTree.name().span(), "unknown function " + functionCallTree.name().name().asString());
        }

        if (functionCallTree.arguments().size() != functionTree.parameters().size()) {
            throw new SemanticException(functionCallTree.span(), "expected " + functionTree.parameters().size()
                    + " arguments but found " + functionCallTree.arguments().size());
        }

        for (int i = 0; i < functionTree.parameters().size(); i++) {
            ParameterTree parameter = functionTree.parameters().get(i);
            ExpressionTree argument = functionCallTree.arguments().get(i);
            expectType(info, parameter, argument);
        }

        return functionTree.returnType().accept(this, info);
    }

    @Override
    public Optional<Type> visit(BuiltinCallTree builtinCallTree, FunctionInfo info) {
        List<Type> parameterTypes = switch (builtinCallTree.type()) {
            case PRINT -> List.of(BasicType.INT);
            case READ, FLUSH -> List.of();
        };

        if (parameterTypes.size() != builtinCallTree.arguments().size()) {
            throw new SemanticException(builtinCallTree.span(), "expected " + parameterTypes.size()
                    + " arguments but found " + builtinCallTree.arguments().size());
        }

        for (int i = 0; i < parameterTypes.size(); i++) {
            Type expected = parameterTypes.get(i);
            ExpressionTree actual = builtinCallTree.arguments().get(i);
            expectType(info, expected, actual);
        }

        return Optional.of(switch (builtinCallTree.type()) {
            case PRINT, READ, FLUSH -> BasicType.INT;
        });
    }

    @Override
    public Optional<Type> visit(StructTree structTree, FunctionInfo data) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<Type> visit(FieldTree fieldTree, FunctionInfo data) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<Type> visit(ExpressionArrayAccessTree expressionArrayAccessTree, FunctionInfo data) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<Type> visit(ExpressionDereferenceTree expressionDereferenceTree, FunctionInfo data) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<Type> visit(ExpressionFieldTree expressionFieldTree, FunctionInfo data) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<Type> visit(LValueArrayAccessTree lValueArrayAccessTree, FunctionInfo data) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<Type> visit(LValueDereferenceTree lValueDereferenceTree, FunctionInfo data) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<Type> visit(LValueFieldTree lValueFieldTree, FunctionInfo data) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<Type> visit(AllocCallTree allocCallTree, FunctionInfo data) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<Type> visit(AllocArrayCallTree allocArrayCallTree, FunctionInfo data) {
        throw new UnsupportedOperationException(); // TODO
    }

    private Type expectType(FunctionInfo info, Tree expected, Tree actual) {
        return expectType(info, expected.accept(this, info).orElseThrow(), actual);
    }

    private Type expectType(FunctionInfo info, Type expected, Tree tree) {
        return expectType(tree.span(), expected, tree.accept(this, info).orElseThrow());
    }

    private Type expectType(Span span, Type expected, Type actual) {
        if (!expected.equals(actual)) {
            throw new SemanticException(span, "expected type " + expected.asString() + " but got " + actual.asString());
        }
        return expected;
    }

    private Type expectTypeSame(FunctionInfo info, Tree first, Tree second) {
        Type firstType = first.accept(this, info).orElseThrow();
        Type secondType = second.accept(this, info).orElseThrow();
        if (!firstType.equals(secondType)) {
            throw new SemanticException(first.span().merge(second.span()), "mismatched types, expected type to be the same");
        }

        return firstType;
    }
}
