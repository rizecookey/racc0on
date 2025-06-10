package net.rizecookey.racc0on.ir.schedule;

import net.rizecookey.racc0on.ir.IrGraphTraverser;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.Phi;
import net.rizecookey.racc0on.ir.util.NodeSupport;
import net.rizecookey.racc0on.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhiMoveScheduler extends IrGraphTraverser {

    private final Set<Phi> visited = new HashSet<>();
    private final Map<Block, List<Pair<Phi, Node>>> phiMoves = new HashMap<>();

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
        if (!(node instanceof Phi phi) || !visited.add(phi)) {
            return;
        }

        for (int i = 0; i < phi.predecessors().size(); i++) {
            Node predecessor = NodeSupport.predecessorSkipProj(node, i);
            Block block = phi.block().predecessor(i).block();
            phiMoves.computeIfAbsent(block, _ -> new ArrayList<>()).add(new Pair<>(phi, predecessor));
        }
    }

    public Map<Block, List<Pair<Phi, Node>>> getPhiMoves() {
        return Map.copyOf(phiMoves);
    }
}
