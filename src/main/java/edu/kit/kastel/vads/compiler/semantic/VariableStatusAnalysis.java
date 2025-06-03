package edu.kit.kastel.vads.compiler.semantic;

import edu.kit.kastel.vads.compiler.lexer.OperatorType;
import edu.kit.kastel.vads.compiler.parser.ast.AssignmentTree;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.DeclarationTree;
import edu.kit.kastel.vads.compiler.parser.ast.IdentExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueIdentTree;
import edu.kit.kastel.vads.compiler.parser.ast.NameTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.ForTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.IfElseTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.LoopControlTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.ReturnTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.WhileTree;
import edu.kit.kastel.vads.compiler.parser.symbol.Name;
import edu.kit.kastel.vads.compiler.parser.visitor.NoOpVisitor;
import edu.kit.kastel.vads.compiler.parser.visitor.RecursivePostorderVisitor;
import edu.kit.kastel.vads.compiler.parser.visitor.Unit;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/// Checks that variables are
/// - declared before assignment
/// - not declared twice
/// - not initialized twice
/// - assigned before referenced
class VariableStatusAnalysis extends RecursivePostorderVisitor<Namespace<VariableStatusAnalysis.VariableStatus>, Unit> {

    VariableStatusAnalysis() {
        super(new NoOpVisitor<>() {});
    }

    @Override
    public Unit visit(BlockTree blockTree, Namespace<VariableStatus> data) {
        data.openScope();
        super.visit(blockTree, data);
        Map<Name, VariableStatus> variables = data.closeScope();
        mergeIntoScope(data, List.of(variables));
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(IfElseTree ifElseTree, Namespace<VariableStatus> data) {
        ifElseTree.condition().accept(this, data);
        data.openScope();
        ifElseTree.thenBranch().accept(this, data);
        Map<Name, VariableStatus> thenScope = data.closeScope();

        if (ifElseTree.elseBranch() == null) {
            return Unit.INSTANCE;
        }

        data.openScope();
        ifElseTree.elseBranch().accept(this, data);
        Map<Name, VariableStatus> elseScope = data.closeScope();

        mergeIntoScope(data, List.of(thenScope, elseScope));
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(WhileTree whileTree, Namespace<VariableStatus> data) {
        whileTree.condition().accept(this, data);
        data.openScope();
        whileTree.body().accept(this, data);
        data.closeScope();
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(ForTree forTree, Namespace<VariableStatus> data) {
        data.openScope();
        if (forTree.initializer() != null) {
            forTree.initializer().accept(this, data);
        }
        data.openScope();
        forTree.condition().accept(this, data);
        forTree.body().accept(this, data);
        if (forTree.step() != null) {
            forTree.step().accept(this, data);
        }
        data.closeScope();
        Map<Name, VariableStatus> outerScope = data.closeScope();
        mergeIntoScope(data, List.of(outerScope));
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(AssignmentTree assignmentTree, Namespace<VariableStatus> data) {
        assignmentTree.expression().accept(this, data);
        switch (assignmentTree.lValue()) {
            case LValueIdentTree(var name) -> {
                VariableStatus status = data.get(name);
                if (assignmentTree.operator().type() == OperatorType.Assignment.DEFAULT) {
                    checkDeclared(name, status);
                } else {
                    checkInitialized(name, status);
                }
                if (status != VariableStatus.INITIALIZED) {
                    // only update when needed, reassignment is totally fine
                    updateStatus(data, VariableStatus.INITIALIZED, name);
                }
            }
        }
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(DeclarationTree declarationTree, Namespace<VariableStatus> data) {
        if (declarationTree.initializer() != null) {
            declarationTree.initializer().accept(this, data);
        }
        checkUndeclared(declarationTree.name(), data.get(declarationTree.name()));
        VariableStatus status = declarationTree.initializer() == null
                ? VariableStatus.DECLARED
                : VariableStatus.INITIALIZED;
        updateStatus(data, status, declarationTree.name());
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(IdentExpressionTree identExpressionTree, Namespace<VariableStatus> data) {
        VariableStatus status = data.get(identExpressionTree.name());
        checkInitialized(identExpressionTree.name(), status);
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(LoopControlTree loopControlTree, Namespace<VariableStatus> data) {
        initializeAll(data);
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(ReturnTree returnTree, Namespace<VariableStatus> data) {
        returnTree.expression().accept(this, data);
        initializeAll(data);
        return Unit.INSTANCE;
    }

    private static void initializeAll(Namespace<VariableStatus> data) {
        Map <Name, VariableStatus> scope = data.currentScope();
        for (Name name : scope.keySet()) {
            if (scope.get(name).ordinal() < VariableStatus.INITIALIZED.ordinal()) {
                data.put(name, VariableStatus.INITIALIZED);
            }
        }
    }

    private static void checkDeclared(NameTree name, @Nullable VariableStatus status) {
        if (status == null) {
            throw new SemanticException(name.span(), "variable " + name.name().asString() + " must be declared before assignment");
        }
    }

    private static void checkInitialized(NameTree name, @Nullable VariableStatus status) {
        if (status == null || status == VariableStatus.DECLARED) {
            throw new SemanticException(name.span(), "variable " + name.name().asString() + " must be initialized before use");
        }
    }

    private static void checkUndeclared(NameTree name, @Nullable VariableStatus status) {
        if (status != null) {
            throw new SemanticException(name.span(), "variable " + name.name().asString() + " is already declared");
        }
    }

    private static void mergeIntoScope(Namespace<VariableStatus> data, List<Map<Name, VariableStatus>> scopes) {
        if (scopes.isEmpty()) {
            return;
        }

        Set<Name> names = scopes.stream().map(Map::keySet).flatMap(Set::stream).collect(Collectors.toSet());
        for (Name name : names) {
            if (data.get(name) == null) {
                continue;
            }
            if (scopes.stream().anyMatch(scope -> scope.get(name).ordinal() < VariableStatus.INITIALIZED.ordinal())) {
                continue;
            }

            data.put(name, VariableStatus.INITIALIZED);
        }
    }

    private static void updateStatus(Namespace<VariableStatus> data, VariableStatus status, NameTree name) {
        data.put(name, status, (existing, replacement) -> {
            if (existing.ordinal() >= replacement.ordinal()) {
                throw new SemanticException(name.span(), "variable is already " + existing + ", cannot be " + replacement + " here.");
            }
            return replacement;
        });
    }

    enum VariableStatus {
        DECLARED,
        INITIALIZED;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
