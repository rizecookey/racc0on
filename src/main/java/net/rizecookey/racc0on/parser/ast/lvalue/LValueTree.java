package net.rizecookey.racc0on.parser.ast.lvalue;

import net.rizecookey.racc0on.parser.ast.Tree;

public sealed interface LValueTree extends Tree permits LValueArrayAccessTree, LValueDereferenceTree, LValueFieldAccessTree, LValueIdentTree {
}
