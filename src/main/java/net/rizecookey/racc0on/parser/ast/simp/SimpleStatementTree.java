package net.rizecookey.racc0on.parser.ast.simp;

import net.rizecookey.racc0on.parser.ast.StatementTree;
import net.rizecookey.racc0on.parser.ast.call.CallTree;

public sealed interface SimpleStatementTree extends StatementTree permits AssignmentTree, CallTree, DeclarationTree {
}
