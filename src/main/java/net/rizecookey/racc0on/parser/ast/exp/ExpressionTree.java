package net.rizecookey.racc0on.parser.ast.exp;

import net.rizecookey.racc0on.parser.ast.Tree;

public sealed interface ExpressionTree extends Tree permits BinaryOperationTree, BoolLiteralTree, IdentExpressionTree, IntLiteralTree, TernaryExpressionTree, UnaryOperationTree {
}
