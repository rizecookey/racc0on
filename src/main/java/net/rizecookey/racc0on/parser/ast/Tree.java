package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.parser.ast.lvalue.LValueTree;
import net.rizecookey.racc0on.parser.visitor.Visitor;
import net.rizecookey.racc0on.utils.Span;

public sealed interface Tree permits FieldDeclarationTree, FunctionTree, LValueTree, NameTree, ParameterTree, ProgramTree, StatementTree, StructDeclarationTree, TypeTree, net.rizecookey.racc0on.parser.ast.exp.ExpressionTree {

    Span span();

    <T, R> R accept(Visitor<T, R> visitor, T data);
}
