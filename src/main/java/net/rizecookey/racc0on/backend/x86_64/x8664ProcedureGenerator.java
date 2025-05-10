package net.rizecookey.racc0on.backend.x86_64;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstructionSelector;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageAllocator;

import java.util.List;

public class x8664ProcedureGenerator {
    private final String name;
    private final List<Node> statements;
    private final x8664CodeGenerator codeGenerator;
    private final x8664StorageAllocator.Allocation allocation;

    public x8664ProcedureGenerator(x8664CodeGenerator codeGenerator, String name, List<Node> statements, x8664StorageAllocator.Allocation allocation) {
        this.name = name;
        this.statements = statements;
        this.codeGenerator = codeGenerator;
        this.allocation = allocation;
    }

    public void generate() {
        codeGenerator.append(name).appendLine(":");

        x8664InstructionSelector selector = new x8664InstructionSelector(this, allocation);
        prepareStack();
        for (Node node : statements) {
            selector.selectInstruction(node);
        }
    }

    public void prepareStack() {
        writeInstruction0("enter", String.valueOf(allocation.stackSize()), String.valueOf(0));
    }

    public void tearDownStack() {
        writeInstruction0("leave");
    }

    public void writeInstruction(String name, x8664Operand.Size size, x8664Operand... locations) {
        String[] sizedOperands = new String[locations.length];
        boolean typeExplicit = locations.length == 0;
        for (int i = 0; i < locations.length; i++) {
            x8664Operand operand = locations[i];
            if (operand instanceof x8664Register) {
                typeExplicit = true;
            }

            sizedOperands[i] = operand.getId().getName(size);
        }

        if (!typeExplicit) {
            name += " " + size.getPrefix();
        }

        writeInstruction0(name, sizedOperands);
    }

    public void writeInstruction(String name, x8664Operand... locations) {
        writeInstruction(name, x8664Operand.Size.DOUBLE_WORD, locations);
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
