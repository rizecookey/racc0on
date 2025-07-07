package net.rizecookey.racc0on.semantic;

import net.rizecookey.racc0on.parser.DeclarationInfo;
import net.rizecookey.racc0on.parser.ast.ProgramTree;

public class SemanticAnalysis {

    private final ProgramTree program;

    public SemanticAnalysis(ProgramTree program) {
        this.program = program;
    }

    public DeclarationInfo analyze() {
        this.program.accept(new IntegerLiteralRangeAnalysis(), new Namespace<>());
        this.program.accept(new VariableStatusAnalysis(), new Namespace<>());
        this.program.accept(new ControlFlowAnalysis(), new ControlFlowAnalysis.ControlFlowState(false, false));
        TypeAnalysis typeAnalysis = new TypeAnalysis();
        this.program.accept(typeAnalysis, new TypeAnalysis.FunctionInfo(null));
        this.program.accept(new StructNestingAnalysis(), new Namespace<>());

        return new DeclarationInfo(typeAnalysis.functionNamespace.currentScope(), typeAnalysis.structDeclarations.currentScope());
    }

}
