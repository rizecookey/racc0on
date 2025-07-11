package net.rizecookey.racc0on.parser;

import net.rizecookey.racc0on.parser.ast.BlockTree;
import net.rizecookey.racc0on.parser.ast.FieldDeclarationTree;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.NameTree;
import net.rizecookey.racc0on.parser.ast.ParameterTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.parser.ast.StatementTree;
import net.rizecookey.racc0on.parser.ast.StructDeclarationTree;
import net.rizecookey.racc0on.parser.ast.Tree;
import net.rizecookey.racc0on.parser.ast.TypeTree;
import net.rizecookey.racc0on.parser.ast.call.AllocArrayCallTree;
import net.rizecookey.racc0on.parser.ast.call.AllocCallTree;
import net.rizecookey.racc0on.parser.ast.call.CallTree;
import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.control.ReturnTree;
import net.rizecookey.racc0on.parser.ast.control.WhileTree;
import net.rizecookey.racc0on.parser.ast.exp.BinaryOperationTree;
import net.rizecookey.racc0on.parser.ast.exp.BoolLiteralTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpDereferenceTree;
import net.rizecookey.racc0on.parser.ast.exp.ExpFieldAccessTree;
import net.rizecookey.racc0on.parser.ast.exp.IdentExpressionTree;
import net.rizecookey.racc0on.parser.ast.exp.IntLiteralTree;
import net.rizecookey.racc0on.parser.ast.exp.PointerLiteralTree;
import net.rizecookey.racc0on.parser.ast.exp.TernaryExpressionTree;
import net.rizecookey.racc0on.parser.ast.exp.UnaryOperationTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueArrayAccessTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueDereferenceTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueFieldAccessTree;
import net.rizecookey.racc0on.parser.ast.lvalue.LValueIdentTree;
import net.rizecookey.racc0on.parser.ast.simp.AssignmentTree;
import net.rizecookey.racc0on.parser.ast.simp.DeclarationTree;
import net.rizecookey.racc0on.parser.ast.simp.SimpleStatementTree;

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
            case FunctionTree(var returnType, var name, var params, var body) -> {
                printTree(returnType);
                space();
                printTree(name);
                print("(");
                for (int i = 0; i < params.size(); i++) {
                    printTree(params.get(i));

                    if (i < params.size() - 1) {
                        print(", ");
                    }
                }
                print(")");
                space();
                printTree(body);
            }
            case NameTree(var name, _) -> print(name.asString());
            case ProgramTree(var structs, var functions, _) -> {
                for (StructDeclarationTree struct : structs) {
                    printTree(struct);
                    lineBreak();
                }
                for (FunctionTree function : functions) {
                    printTree(function);
                    lineBreak();
                }
            }
            case TypeTree(var type, _, _) -> print(type.asString());
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
                if (forTree.body() instanceof SimpleStatementTree) {
                    semicolon();
                }
            }
            case IfElseTree ifElseTree -> {
                print("if (");
                printTree(ifElseTree.condition());
                print(") ");
                printTree(ifElseTree.thenBranch());
                if (ifElseTree.thenBranch() instanceof SimpleStatementTree) {
                    semicolon();
                }
                if (ifElseTree.elseBranch() != null) {
                    print(" else ");
                    printTree(ifElseTree.elseBranch());
                    if (ifElseTree.elseBranch() instanceof SimpleStatementTree) {
                        semicolon();
                    }
                }
            }
            case WhileTree whileTree -> {
                print("while (");
                printTree(whileTree.condition());
                print(") ");
                printTree(whileTree.body());
                if (whileTree.body() instanceof SimpleStatementTree) {
                    semicolon();
                }
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
            case ParameterTree parameterTree -> {
                printTree(parameterTree.type());
                space();
                printTree(parameterTree.name());
            }
            case AllocCallTree allocCallTree -> {
                print("alloc(");
                printTree(allocCallTree.type());
                print(")");
            }
            case AllocArrayCallTree allocArrayCallTree -> {
                print("alloc_array(");
                printTree(allocArrayCallTree.type());
                print(", ");
                printTree(allocArrayCallTree.elementCount());
                print(")");
            }
            case CallTree callTree -> {
                print(callTree.functionName().asString());
                print("(");
                for (int i = 0; i < callTree.arguments().size(); i++) {
                    printTree(callTree.arguments().get(i));

                    if (i < callTree.arguments().size() - 1) {
                        print(", ");
                    }
                }
                print(")");
            }
            case FieldDeclarationTree fieldDeclarationTree -> {
                printTree(fieldDeclarationTree.type());
                space();
                printTree(fieldDeclarationTree.name());
            }
            case StructDeclarationTree structDeclarationTree -> {
                print("struct ");
                printTree(structDeclarationTree.name());
                print(" {");
                lineBreak();
                this.indentDepth++;
                for (FieldDeclarationTree field : structDeclarationTree.fields()) {
                    printTree(field);
                    semicolon();
                    lineBreak();
                }
                this.indentDepth--;
                print("};");
            }
            case ExpArrayAccessTree expArrayAccessTree -> {
                print("(");
                printTree(expArrayAccessTree.array());
                print(")");
                print("[");
                printTree(expArrayAccessTree.index());
                print("]");
            }
            case LValueArrayAccessTree lValueArrayAccessTree -> {
                print("(");
                printTree(lValueArrayAccessTree.array());
                print(")");
                print("[");
                printTree(lValueArrayAccessTree.index());
                print("]");
            }
            case ExpDereferenceTree expDereferenceTree -> {
                print("*");
                printTree(expDereferenceTree.pointer());
            }
            case LValueDereferenceTree lValueDereferenceTree -> {
                print("*");
                printTree(lValueDereferenceTree.pointer());
            }
            case ExpFieldAccessTree expFieldAccessTree -> {
                print("(");
                printTree(expFieldAccessTree.struct());
                print(")");
                print(".");
                printTree(expFieldAccessTree.fieldName());
            }
            case LValueFieldAccessTree lValueFieldAccessTree -> {
                print("(");
                printTree(lValueFieldAccessTree.struct());
                print(")");
                print(".");
                printTree(lValueFieldAccessTree.fieldName());
            }
            case PointerLiteralTree pointerLiteralTree -> print(pointerLiteralTree.type().keyword());
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
