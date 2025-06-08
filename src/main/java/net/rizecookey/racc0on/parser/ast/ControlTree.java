package net.rizecookey.racc0on.parser.ast;

import net.rizecookey.racc0on.parser.ast.control.ForTree;
import net.rizecookey.racc0on.parser.ast.control.IfElseTree;
import net.rizecookey.racc0on.parser.ast.control.LoopControlTree;
import net.rizecookey.racc0on.parser.ast.control.ReturnTree;
import net.rizecookey.racc0on.parser.ast.control.WhileTree;

public sealed interface ControlTree extends StatementTree permits ForTree, IfElseTree, LoopControlTree, ReturnTree, WhileTree {
}
