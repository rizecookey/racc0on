package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.parser.ast.control.ControlTree;
import net.rizecookey.racc0on.parser.ast.simp.SimpleStatementTree;

public sealed interface StatementTree extends Tree permits BlockTree, ControlTree, SimpleStatementTree {
}
