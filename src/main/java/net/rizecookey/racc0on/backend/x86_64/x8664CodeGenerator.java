package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.NodeUtils;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageAllocator;

import java.util.List;

public class x8664CodeGenerator {
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

    public String generateCode(List<IrGraph> program) {
        appendLine(".intel_syntax noprefix"); // enable intel assembly syntax
        declareGlobal(ENTRYPOINT_NAME);
        for (IrGraph graph : program) {
            declareGlobal(graph.name());
        }

        initializeTextSegment();

        for (IrGraph graph : program) {
            List<Node> statements = NodeUtils.transformToSequential(graph);
            x8664StorageAllocator allocator = new x8664StorageAllocator();
            x8664StorageAllocator.Allocation allocation = allocator.allocate(statements);
            generateProcedure(graph.name(), statements, allocation);
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

    public void generateProcedure(String name, List<Node> statements, x8664StorageAllocator.Allocation allocation) {
        new x8664ProcedureGenerator(this, name, statements, allocation).generate();
    }

    public void appendNewline() {
        append("\n");
    }

    public void declareGlobal(String symbolName) {
        append(".globl ").appendLine(symbolName);
    }
}
