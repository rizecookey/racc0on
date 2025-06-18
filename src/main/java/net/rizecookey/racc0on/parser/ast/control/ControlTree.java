package net.rizecookey.racc0on.parser.ast.control;

import net.rizecookey.racc0on.parser.ast.StatementTree;

public sealed interface ControlTree extends StatementTree permits ForTree, IfElseTree, LoopControlTree, ReturnTree, WhileTree {
}
