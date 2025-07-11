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
    public static final String ENTRYPOINT_NAME = "$entry$";

    private static final String BOILERPLATE_ENTRY = """
            $entry$:
            call main
            mov r15, rax
            call $flush$
            mov rdi, r15
            mov rax, 0x3C
            syscall
            """;

    private static final String BUILTINS_IMPL = """
            .globl $print$
            .globl $read$
            .globl $flush$
            
            $print$:
            push rbp
            mov rbp, rsp
            call putchar
            mov eax, 0
            leave
            ret
            
            $read$:
            push rbp
            mov rbp, rsp
            call getchar
            mov edi, -1
            cmp eax, 0
            cmovl eax, edi
            leave
            ret
            
            $flush$:
            push rbp
            mov rbp, rsp
            mov rdi, [rip + stdout]
            call fflush
            mov eax, 0
            leave
            ret
            
            $abort$:
            call abort
            leave
            ret
            
            $alloc$:
            push rbp
            mov rbp, rsp
            mov rsi, rdi
            mov rdi, 1
            cmp rsi, 0
            jl $abort$
            cmove rsi, rdi
            call calloc
            leave
            ret
            
            $alloc_array$:
            push rbp
            mov rbp, rsp
            cmp esi, 0
            jl $abort$
            mov r15d, esi
            imul rdi, rsi
            add rdi, 4
            call $alloc$
            mov [rax], r15d
            add rax, 4
            leave
            ret
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
        includeBuiltinWrappers();

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

    public void includeBuiltinWrappers() {
        appendNewline();
        appendLine(BUILTINS_IMPL);
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
