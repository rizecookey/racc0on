package edu.kit.kastel.vads.compiler.semantic;

import edu.kit.kastel.vads.compiler.Position;
import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.parser.ast.AssignmentTree;
import edu.kit.kastel.vads.compiler.parser.ast.BinaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.BoolLiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.DeclarationTree;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IdentExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IntLiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueIdentTree;
import edu.kit.kastel.vads.compiler.parser.ast.NameTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.TernaryExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.Tree;
import edu.kit.kastel.vads.compiler.parser.ast.TypeTree;
import edu.kit.kastel.vads.compiler.parser.ast.UnaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.ForTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.IfElseTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.LoopControlTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.ReturnTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.WhileTree;
import edu.kit.kastel.vads.compiler.parser.type.BasicType;
import edu.kit.kastel.vads.compiler.parser.type.Type;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class TypeAnalysis implements Visitor<Namespace<Type>, Optional<Type>> {
    @Override
    public Optional<Type> visit(AssignmentTree assignmentTree, Namespace<Type> namespace) {
        Type type = expectTypeSame(namespace, assignmentTree.lValue(), assignmentTree.expression());
        type = switch (assignmentTree.type()) {
            case MINUS, PLUS, MUL, DIV, MOD, BITWISE_AND, BITWISE_OR, BITWISE_XOR, SHIFT_LEFT, SHIFT_RIGHT
                    -> expectType(assignmentTree.span(), BasicType.INT, type);
            case DEFAULT -> type;
        };
        return Optional.of(type);
    }

    @Override
    public Optional<Type> visit(BinaryOperationTree binaryOperationTree, Namespace<Type> namespace) {
        Type operandsType = expectTypeSame(namespace, binaryOperationTree.lhs(), binaryOperationTree.rhs());

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
    public Optional<Type> visit(BlockTree blockTree, Namespace<Type> namespace) {
        for (StatementTree statement : blockTree.statements()) {
            statement.accept(this, namespace);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(DeclarationTree declarationTree, Namespace<Type> namespace) {
        Type type = declarationTree.initializer() != null
                ? expectTypeSame(namespace, declarationTree.type(), declarationTree.initializer())
                : declarationTree.type().accept(this, namespace).orElseThrow();
        namespace.put(declarationTree.name().name(), type);
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(FunctionTree functionTree, Namespace<Type> namespace) {
        functionTree.body().accept(this, namespace);
        return functionTree.returnType().accept(this, namespace);
    }

    @Override
    public Optional<Type> visit(IdentExpressionTree identExpressionTree, Namespace<Type> namespace) {
        Type type = namespace.get(identExpressionTree.name());
        return type != null ? Optional.of(type) : Optional.empty();
    }

    @Override
    public Optional<Type> visit(IntLiteralTree intLiteralTree, Namespace<Type> namespace) {
        return Optional.of(BasicType.INT);
    }

    @Override
    public Optional<Type> visit(BoolLiteralTree boolLiteralTree, Namespace<Type> namespace) {
        return Optional.of(BasicType.BOOL);
    }

    @Override
    public Optional<Type> visit(LValueIdentTree lValueIdentTree, Namespace<Type> namespace) {
        Type type = namespace.get(lValueIdentTree.name());
        return type != null ? Optional.of(type) : Optional.empty();
    }

    @Override
    public Optional<Type> visit(NameTree nameTree, Namespace<Type> namespace) {
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(UnaryOperationTree unaryOperationTree, Namespace<Type> namespace) {
        Type type = switch (unaryOperationTree.type()) {
            case NEGATION, BITWISE_NOT -> expectType(unaryOperationTree.expression(), BasicType.INT, namespace);
            case NOT -> expectType(unaryOperationTree.expression(), BasicType.BOOL, namespace);
        };

        return Optional.of(type);
    }

    @Override
    public Optional<Type> visit(ProgramTree programTree, Namespace<Type> namespace) {
        programTree.topLevelTrees().forEach(t -> t.accept(this, new Namespace<>()));
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(ReturnTree returnTree, Namespace<Type> namespace) {
        expectType(returnTree.expression(), BasicType.INT, namespace);
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(TypeTree typeTree, Namespace<Type> namespace) {
        return Optional.of(typeTree.type());
    }

    @Override
    public Optional<Type> visit(LoopControlTree loopControlTree, Namespace<Type> namespace) {
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(IfElseTree ifElseTree, Namespace<Type> namespace) {
        expectType(ifElseTree.condition(), BasicType.BOOL, namespace);
        ifElseTree.thenBranch().accept(this, namespace);
        if (ifElseTree.elseBranch() != null) {
            ifElseTree.elseBranch().accept(this, namespace);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(WhileTree whileTree, Namespace<Type> namespace) {
        expectType(whileTree.condition(), BasicType.BOOL, namespace);
        whileTree.body().accept(this, namespace);
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(ForTree forTree, Namespace<Type> namespace) {
        if (forTree.initializer() != null) {
            forTree.initializer().accept(this, namespace);
        }
        expectType(forTree.condition(), BasicType.BOOL, namespace);
        forTree.body().accept(this, namespace);
        if (forTree.step() != null) {
            forTree.step().accept(this, namespace);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Type> visit(TernaryExpressionTree ternaryExpressionTree, Namespace<Type> namespace) {
        expectType(ternaryExpressionTree.condition(), BasicType.BOOL, namespace);
        return Optional.of(expectTypeSame(namespace, ternaryExpressionTree.ifBranch(), ternaryExpressionTree.elseBranch()));
    }

    private Type expectType(Tree tree, Type expected, Namespace<Type> namespace) {
        return expectType(tree.span(), expected, tree.accept(this, namespace).orElseThrow());
    }

    private Type expectType(Span span, Type expected, Type actual) {
        if (!expected.equals(actual)) {
            throw new SemanticException(span, "expected type " + expected.asString() + " but got " + actual.asString());
        }
        return expected;
    }

    private Type expectTypeSame(Namespace<Type> namespace, Tree... trees) {
        List<Tree> treesList = Arrays.asList(trees);
        List<Type> types = treesList.stream()
                .map(tree -> tree.accept(this, namespace).orElseThrow())
                .distinct()
                .toList();
        if (types.size() > 1) {
            Position start = treesList.stream()
                    .map(tree -> tree.span().start())
                    .min(Comparator.comparing(Function.identity()))
                    .orElseThrow();
            Position end = treesList.stream()
                    .map(tree -> tree.span().end())
                    .max(Comparator.comparing(Function.identity()))
                    .orElseThrow();
            throw new SemanticException(new Span.SimpleSpan(start, end), "mismatched types, expected type to be the same");
        }

        return types.getFirst();
    }
}
