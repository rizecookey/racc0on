package edu.kit.kastel.vads.compiler.semantic;

import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;

public class SemanticAnalysis {

    private final ProgramTree program;

    public SemanticAnalysis(ProgramTree program) {
        this.program = program;
    }

    public void analyze() {
        this.program.accept(new IntegerLiteralRangeAnalysis(), new Namespace<>());
        this.program.accept(new VariableStatusAnalysis(), new Namespace<>());
        this.program.accept(new ReturnAnalysis(), new ReturnAnalysis.ReturnState());
        this.program.accept(new TypeAnalysis(), new Namespace<>());
        /* TODO missing: - control flow analysis */
    }

}
