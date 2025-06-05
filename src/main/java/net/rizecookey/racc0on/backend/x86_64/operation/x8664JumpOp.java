package net.rizecookey.racc0on.backend.x86_64.operation;

import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.JumpNode;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.List;

public class x8664JumpOp implements x8664Op {
    private final JumpNode jumpNode;

    public x8664JumpOp(JumpNode jumpNode) {
        this.jumpNode = jumpNode;
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        List<Block> blockSuccessor = jumpNode.graph().successors(jumpNode).stream()
                .filter(succ -> succ instanceof Block)
                .map(succ -> (Block) succ)
                .toList();
        if (blockSuccessor.size() != 1) {
            throw new IllegalStateException("Jump operation requires exactly one successor");
        }

        String targetLabel = generator.codeGenerator().getLabel(blockSuccessor.getFirst());
        generator.write(x8664InstrType.JMP, new x8664Immediate(targetLabel));
    }
}
