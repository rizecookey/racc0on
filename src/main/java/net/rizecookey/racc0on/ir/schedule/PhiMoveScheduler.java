package net.rizecookey.racc0on.ir.schedule;

import net.rizecookey.racc0on.ir.IrGraphTraverser;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.Phi;

import java.util.HashMap;
import java.util.Map;

public class PhiMoveScheduler extends IrGraphTraverser {

    private final Map<Block, Map<Phi, Node>> phiValues = new HashMap<>();

    @Override
    protected boolean visit(Node node) {
        if (addSeen(node)) {
            node.predecessors().forEach(this::push);
            return true;
        }

        return false;
    }

    @Override
    protected void consume(Node node) {
        node.graph().successors(node).stream()
                .filter(succ -> succ instanceof Phi)
                .map(succ -> (Phi) succ)
                .forEach(phiSuccessor -> {
                    int indexInSuccessor = phiSuccessor.predecessors().indexOf(node);
                    Block predBlock = phiSuccessor.block().predecessor(indexInSuccessor).block();
                    phiValues.computeIfAbsent(predBlock, _ -> new HashMap<>()).put(phiSuccessor, node);
                });
    }

    public Map<Block, Map<Phi, Node>> getPhiValues() {
        return Map.copyOf(phiValues);
    }
}
