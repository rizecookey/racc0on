package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import net.rizecookey.racc0on.backend.CodeGenerator;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664Instr;
import net.rizecookey.racc0on.ir.SsaSchedule;
import net.rizecookey.racc0on.ir.xir.XIrSchedule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<Block, String> labels = new HashMap<>();

    @Override
    public String generateCode(List<IrGraph> program) {
        appendLine(".intel_syntax noprefix"); // enable intel assembly syntax
        declareGlobal(ENTRYPOINT_NAME);
        for (IrGraph graph : program) {
            declareGlobal(graph.name());
        }

        initializeTextSegment();

        for (IrGraph graph : program) {
            XIrSchedule schedule = XIrSchedule.extend(SsaSchedule.generate(graph));
            generateProcedure(graph, schedule);
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

    public void generateProcedure(IrGraph program, XIrSchedule schedule) {
        int idx = 0;
        for (var block : schedule.blockOrder()) {
            if (block == program.endBlock()) {
                labels.put(block, program.name());
                continue;
            }

            labels.put(block, program.name() + "#" + idx++);
        }
        for (var block : schedule.blockOrder()) {
            generateLabel(labels.get(block));
            var instrGenerator = new x8664InstructionGenerator(this, schedule.blockSchedules().get(block));
            List<x8664Instr> instructions = instrGenerator.generateInstructions();
            appendLine(String.join("\n", instructions.stream().map(x8664Instr::toAssembly).toList()));
        }
    }

    public void appendNewline() {
        append("\n");
    }

    public void declareGlobal(String symbolName) {
        append(".globl ").appendLine(symbolName);
    }

    public String getLabel(Block block) {
        return labels.get(block);
    }

    public void generateLabel(String label) {
        append(label).appendLine(":");
    }
}
