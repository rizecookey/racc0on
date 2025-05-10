package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.NodeUtils;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstructionSelector;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageAllocator;

import java.util.List;

public class x8664ProcedureGenerator {
    private final IrGraph procedure;
    private final x8664CodeGenerator codeGenerator;
    private final x8664StorageAllocator.Allocation allocation;

    public x8664ProcedureGenerator(x8664CodeGenerator codeGenerator, IrGraph procedure, x8664StorageAllocator.Allocation allocation) {
        this.procedure = procedure;
        this.codeGenerator = codeGenerator;
        this.allocation = allocation;
    }

    public void generate() {
        codeGenerator.append(procedure.name()).appendLine(":");

        x8664InstructionSelector selector = new x8664InstructionSelector(this, allocation);
        prepareStack();
        List<Node> sequential = NodeUtils.transformToSequential(procedure);
        for (Node node : sequential) {
            selector.selectInstruction(node);
        }
    }

    public void prepareStack() {
        if (allocation.stackSize() > 0) {
            codeGenerator.append("sub rsp, ").appendLine(String.valueOf(allocation.stackSize()));
        }
    }

    public void tearDownStack() {
        if (allocation.stackSize() > 0) {
            codeGenerator.append("add rsp, ").appendLine(String.valueOf(allocation.stackSize()));
        }
    }

    public void writeInstruction(String name, x8664Operand... locations) {
        String[] sizedOperands = new String[locations.length];
        for (int i = 0; i < locations.length; i++) {
            sizedOperands[i] = locations[i].getId().dwordName();
        }

        writeInstruction0(name, sizedOperands);
    }

    public void writeInstruction0(String name, String... locations) {
        codeGenerator.append(name);

        if (locations.length == 0) {
            codeGenerator.appendNewline();
            return;
        }

        codeGenerator.append(" ").appendLine(String.join(", ", locations));
    }
}
