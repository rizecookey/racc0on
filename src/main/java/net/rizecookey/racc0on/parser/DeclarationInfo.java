package net.rizecookey.racc0on.parser;

import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.StructDeclarationTree;
import net.rizecookey.racc0on.parser.symbol.Name;

import java.util.Map;

public record DeclarationInfo(Map<Name, FunctionTree> functions, Map<Name, StructDeclarationTree> structs) {
}
