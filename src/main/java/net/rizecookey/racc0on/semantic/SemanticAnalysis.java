package net.rizecookey.racc0on.semantic;

import net.rizecookey.racc0on.parser.ast.ProgramTree;

public class SemanticAnalysis {

    private final ProgramTree program;

    public SemanticAnalysis(ProgramTree program) {
        this.program = program;
    }

    public SemanticInformation analyze() {
        SemanticInformation info = new SemanticInformation(program);
        this.program.accept(new IntegerLiteralRangeAnalysis(), new Namespace<>());
        this.program.accept(new VariableStatusAnalysis(), new Namespace<>());
        this.program.accept(new ControlFlowAnalysis(), new ControlFlowAnalysis.ControlFlowState(false, false));
        this.program.accept(new TypeAnalysis(info), new TypeAnalysis.FunctionInfo(null));
        this.program.accept(new StructNestingAnalysis(), new Namespace<>());

        return info;
    }

}
