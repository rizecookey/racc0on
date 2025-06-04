package edu.kit.kastel.vads.compiler.parser;

import edu.kit.kastel.vads.compiler.parser.ast.AssignmentTree;
import edu.kit.kastel.vads.compiler.parser.ast.BinaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.BoolLiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.IdentExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueIdentTree;
import edu.kit.kastel.vads.compiler.parser.ast.IntLiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.SimpleStatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.TernaryExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.ForTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.IfElseTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.LoopControlTree;
import edu.kit.kastel.vads.compiler.parser.ast.NameTree;
import edu.kit.kastel.vads.compiler.parser.ast.UnaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.ReturnTree;
import edu.kit.kastel.vads.compiler.parser.ast.Tree;
import edu.kit.kastel.vads.compiler.parser.ast.DeclarationTree;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.TypeTree;
import edu.kit.kastel.vads.compiler.parser.ast.control.WhileTree;

import java.util.List;

/// This is a utility class to help with debugging the parser.
public class Printer {

    private final Tree ast;
    private final StringBuilder builder = new StringBuilder();
    private boolean requiresIndent;
    private int indentDepth;

    public Printer(Tree ast) {
        this.ast = ast;
    }

    public static String print(Tree ast) {
        Printer printer = new Printer(ast);
        printer.printRoot();
        return printer.builder.toString();
    }

    private void printRoot() {
        printTree(this.ast);
    }

    private void printTree(Tree tree) {
        switch (tree) {
            case BlockTree(List<StatementTree> statements, _) -> {
                print("{");
                lineBreak();
                this.indentDepth++;
                for (StatementTree statement : statements) {
                    printTree(statement);
                    if (statement instanceof SimpleStatementTree) {
                        semicolon();
                    }
                    lineBreak();
                }
                this.indentDepth--;
                print("}");
            }
            case FunctionTree(var returnType, var name, var body) -> {
                printTree(returnType);
                space();
                printTree(name);
                print("()");
                space();
                printTree(body);
            }
            case NameTree(var name, _) -> print(name.asString());
            case ProgramTree(var topLevelTrees) -> {
                for (FunctionTree function : topLevelTrees) {
                    printTree(function);
                    lineBreak();
                }
            }
            case TypeTree(var type, _) -> print(type.asString());
            case BinaryOperationTree(var lhs, var rhs, var op) -> {
                print("(");
                printTree(lhs);
                print(")");
                space();
                this.builder.append(op);
                space();
                print("(");
                printTree(rhs);
                print(")");
            }
            case IntLiteralTree(var value, _, _) -> this.builder.append(value);
            case BoolLiteralTree(var value, _) -> this.builder.append(value);
            case UnaryOperationTree(var type, var expression, _) -> {
                print(type.toString());
                print("(");
                printTree(expression);
                print(")");
            }
            case AssignmentTree(var lValue, var expression, var op) -> {
                printTree(lValue);
                space();
                this.builder.append(op);
                space();
                printTree(expression);
            }
            case DeclarationTree(var type, var name, var initializer) -> {
                printTree(type);
                space();
                printTree(name);
                if (initializer != null) {
                    print(" = ");
                    printTree(initializer);
                }
            }
            case ReturnTree(var expr, _) -> {
                print("return ");
                printTree(expr);
                semicolon();
            }
            case LValueIdentTree(var name) -> printTree(name);
            case IdentExpressionTree(var name) -> printTree(name);
            case LoopControlTree(var keywordType, _) -> {
                print(keywordType.keyword().keyword());
                semicolon();
            }
            case ForTree forTree -> {
                print("for (");
                if (forTree.initializer() != null) {
                    printTree(forTree.initializer());
                }
                semicolon();
                space();
                printTree(forTree.condition());
                semicolon();
                space();
                if (forTree.step() != null) {
                    printTree(forTree.step());
                } else {
                    space();
                }
                print(") ");
                printTree(forTree.body());
            }
            case IfElseTree ifElseTree -> {
                print("if (");
                printTree(ifElseTree.condition());
                print(") ");
                printTree(ifElseTree.thenBranch());
                if (ifElseTree.elseBranch() != null) {
                    print(" else ");
                    printTree(ifElseTree.elseBranch());
                }
            }
            case WhileTree whileTree -> {
                print("while (");
                printTree(whileTree.condition());
                print(") ");
                printTree(whileTree.body());
            }
            case TernaryExpressionTree ternaryExpressionTree -> {
                print("(");
                printTree(ternaryExpressionTree.condition());
                print(") ? (");
                printTree(ternaryExpressionTree.ifBranch());
                print(") : (");
                printTree(ternaryExpressionTree.elseBranch());
                print(")");
            }
        }
    }

    private void print(String str) {
        if (this.requiresIndent) {
            this.requiresIndent = false;
            this.builder.append(" ".repeat(4 * this.indentDepth));
        }
        this.builder.append(str);
    }

    private void lineBreak() {
        this.builder.append("\n");
        this.requiresIndent = true;
    }

    private void semicolon() {
        this.builder.append(";");
    }

    private void space() {
        this.builder.append(" ");
    }

}
