package net.rizecookey.racc0on.semantic;

import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.parser.ast.StructDeclarationTree;
import net.rizecookey.racc0on.parser.ast.Tree;
import net.rizecookey.racc0on.parser.symbol.Name;
import net.rizecookey.racc0on.parser.type.Type;

import java.util.HashMap;
import java.util.Map;

public class SemanticInformation {
    private final ProgramTree program;
    private final Map<Name, FunctionTree> functions = new HashMap<>();
    private final Map<Name, StructDeclarationTree> structs = new HashMap<>();

    private final Map<Tree, Type> memoryExpressionTypes = new HashMap<>();

    public SemanticInformation(ProgramTree program) {
        this.program = program;
    }

    public ProgramTree program() {
        return program;
    }

    public Map<Name, FunctionTree> functions() {
        return functions;
    }

    public Map<Name, StructDeclarationTree> structs() {
        return structs;
    }

    public Map<Tree, Type> accessTypes() {
        return memoryExpressionTypes;
    }
}
