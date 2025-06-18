package net.rizecookey.racc0on.parser.ast.call;

import net.rizecookey.racc0on.parser.ast.exp.ExpressionTree;
import net.rizecookey.racc0on.parser.ast.simp.SimpleStatementTree;
import net.rizecookey.racc0on.parser.symbol.Name;

import java.util.List;

public sealed interface CallTree extends ExpressionTree, SimpleStatementTree permits BuiltinCallTree, FunctionCallTree {
    Name functionName();
    List<ExpressionTree> arguments();
}
