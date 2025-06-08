package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.Span;
import net.rizecookey.racc0on.parser.visitor.Visitor;

public sealed interface Tree permits StatementTree, ExpressionTree, FunctionTree, LValueTree, NameTree, ProgramTree, TypeTree {

    Span span();

    <T, R> R accept(Visitor<T, R> visitor, T data);
}
