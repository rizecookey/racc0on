package net.rizecookey.racc0on.ir.schedule;

import net.rizecookey.racc0on.ir.IrGraphTraverser;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;

public class NodeCollector extends IrGraphTraverser {
    private final Set<Node> visited = new HashSet<>();
    private final Map<Block, SequencedSet<Node>> blockNodes = new HashMap<>();

    @Override
    protected boolean visit(Node node) {
        if (addSeen(node)) {
            node.predecessors().stream()
                    .filter(pred -> !visited.contains(pred))
                    .forEach(this::push);
            return true;
        }

        return false;
    }

    @Override
    protected void consume(Node node) {
        if (node instanceof Block block && visited.add(block)) {
            blockNodes.computeIfAbsent(block, _ -> new LinkedHashSet<>());
            block.predecessors().stream()
                    .map(Node::block)
                    .distinct()
                    .filter(pred -> !visited.contains(pred))
                    .forEach(this::push);
            return;
        }

        SequencedSet<Node> nodes = blockNodes.computeIfAbsent(node.block(), _ -> new LinkedHashSet<>());
        if (visited.add(node)) {
            nodes.add(node);
        }
    }

    public Map<Block, SequencedSet<Node>> getBlockNodes() {
        return blockNodes;
    }
}
