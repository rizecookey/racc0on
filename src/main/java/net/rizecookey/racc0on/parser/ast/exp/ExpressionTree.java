package net.rizecookey.racc0on.parser.ast.exp;

import net.rizecookey.racc0on.parser.ast.Tree;
import net.rizecookey.racc0on.parser.ast.call.CallTree;

public sealed interface ExpressionTree extends Tree permits CallTree, BinaryOperationTree, BoolLiteralTree, IdentExpressionTree, IntLiteralTree, TernaryExpressionTree, UnaryOperationTree {
}
