package net.rizecookey.racc0on.parser.ast;

public sealed interface ExpressionTree extends Tree permits BinaryOperationTree, BoolLiteralTree, IdentExpressionTree, IntLiteralTree, TernaryExpressionTree, UnaryOperationTree {
}
