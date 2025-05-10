package net.rizecookey.racc0on.backend.x86_64.instruction;

import edu.kit.kastel.vads.compiler.ir.node.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.SubNode;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664Operands;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageAllocator;
import net.rizecookey.racc0on.backend.x86_64.storage.x8664StorageLocation;
import net.rizecookey.racc0on.backend.x86_64.x8664ProcedureGenerator;

import java.util.Map;

public class x8664InstructionSelector {
    private final x8664ProcedureGenerator generator;
    private final Map<Node, x8664StorageLocation> locations;

    public x8664InstructionSelector(x8664ProcedureGenerator generator, x8664StorageAllocator.Allocation allocation) {
        this.generator = generator;
        this.locations = Map.copyOf(allocation.allocations());
    }

    public void selectInstruction(Node node) {
        x8664Instr instruction = switch (node) {
            case AddNode addNode -> new x8664AddInstr(extractBinaryOperands(addNode));
            case SubNode subNode -> new x8664SubInstr(extractBinaryOperands(subNode));
            case MulNode mulNode -> new x8664IMulInstr(extractBinaryOperands(mulNode));
            case DivNode divNode -> new x8664DivPhantomInstr(extractBinaryOperands(divNode));
            case ModNode modNode -> new x8664ModPhantomInstr(extractBinaryOperands(modNode));
            case ConstIntNode constIntNode -> new x8664MovInstr(locations.get(constIntNode), new x8664Immediate(String.valueOf(constIntNode.value())));
            case ReturnNode returnNode -> new x8664RetInstr(locations.get(returnNode.predecessor(ReturnNode.RESULT)));
            case Phi _ -> throw new IllegalStateException("Phi instruction not supported");
            case Block _, ProjNode _, StartNode _ -> x8664EmptyInstr.INSTANCE;
        };

        instruction.write(generator);
    }

    private x8664Operands.Binary<x8664StorageLocation> extractBinaryOperands(BinaryOperationNode node) {
        return new x8664Operands.Binary<>(locations.get(node), locations.get(node.predecessor(BinaryOperationNode.LEFT)), locations.get(node.predecessor(BinaryOperationNode.RIGHT)));
    }
}
