package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.CodeGenerator;
import net.rizecookey.racc0on.backend.NodeUtils;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664Instr;

import java.util.List;

public class x8664CodeGenerator implements CodeGenerator {
    public static final String ENTRYPOINT_NAME = "_entry";

    private static final String BOILERPLATE_ENTRY = """
            _entry:
            enter 0, 0
            call main
            mov rdi, rax
            mov rax, 0x3C
            syscall
            """;

    private final StringBuilder builder = new StringBuilder();

    @Override
    public String generateCode(List<IrGraph> program) {
        appendLine(".intel_syntax noprefix"); // enable intel assembly syntax
        declareGlobal(ENTRYPOINT_NAME);
        for (IrGraph graph : program) {
            declareGlobal(graph.name());
        }

        initializeTextSegment();

        for (IrGraph graph : program) {
            List<Node> statements = NodeUtils.transformToSequential(graph);
            append(graph.name()).appendLine(":");
            generateProcedure(statements);
        }

        return builder.toString();
    }

    public x8664CodeGenerator append(String text) {
        builder.append(text);
        return this;
    }

    public void appendLine(String text) {
        append(text);
        appendNewline();
    }

    public void initializeTextSegment() {
        appendNewline();
        appendLine(".text");
        appendLine(BOILERPLATE_ENTRY);
    }

    public void generateProcedure(List<Node> statements) {
        var instrGenerator = new x8664InstructionGenerator(this, statements);
        List<x8664Instr> instructions = instrGenerator.generateInstructions();
        appendLine(String.join("\n", instructions.stream().map(x8664Instr::toAssembly).toList()));
    }

    public void appendNewline() {
        append("\n");
    }

    public void declareGlobal(String symbolName) {
        append(".globl ").appendLine(symbolName);
    }
}
