package net.rizecookey.racc0on.parser.ast;

public sealed interface StatementTree extends Tree permits BlockTree, ControlTree, SimpleStatementTree {
}
