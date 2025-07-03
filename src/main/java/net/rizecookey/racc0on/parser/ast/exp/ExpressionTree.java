package net.rizecookey.racc0on.parser.ast.exp;

import net.rizecookey.racc0on.parser.ast.Tree;
import net.rizecookey.racc0on.parser.ast.call.AllocArrayCallTree;
import net.rizecookey.racc0on.parser.ast.call.AllocCallTree;
import net.rizecookey.racc0on.parser.ast.call.CallTree;

public sealed interface ExpressionTree extends Tree permits AllocArrayCallTree, AllocCallTree, CallTree, BinaryOperationTree, BoolLiteralTree, ExpressionArrayAccessTree, ExpressionDereferenceTree, ExpressionFieldTree, IdentExpressionTree, IntLiteralTree, TernaryExpressionTree, UnaryOperationTree {
}
