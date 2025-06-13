package net.rizecookey.racc0on.backend.x86_64;

import net.rizecookey.racc0on.ir.IrGraph;
import net.rizecookey.racc0on.backend.CodeGenerator;
import net.rizecookey.racc0on.backend.instruction.InstructionBlock;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664Instr;
import net.rizecookey.racc0on.ir.schedule.SsaSchedule;
import net.rizecookey.racc0on.ir.schedule.SsaScheduler;

import java.util.List;
import java.util.stream.Collectors;

public class x8664CodeGenerator implements CodeGenerator {
    public static final String ENTRYPOINT_NAME = "_entry";

    private static final String BOILERPLATE_ENTRY = """
            _entry:
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
            SsaSchedule schedule = SsaScheduler.schedule(graph);
            generateProcedure(schedule);
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

    public void generateProcedure(SsaSchedule schedule) {
        var instrGenerator = new x8664InstructionGenerator(this, schedule);

        List<InstructionBlock<x8664Instr>> codeBlocks = instrGenerator.generateInstructions();
        appendLine(String.join("\n", codeBlocks.stream()
                .map(block -> "\n" + block.label() + ":\n" + block.instructions().stream()
                        .map(x8664Instr::toAssembly)
                        .collect(Collectors.joining("\n")))
                .toList()));
    }

    public void appendNewline() {
        append("\n");
    }

    public void declareGlobal(String symbolName) {
        append(".globl ").appendLine(symbolName);
    }
}
