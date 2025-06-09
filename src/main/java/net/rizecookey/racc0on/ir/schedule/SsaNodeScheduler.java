package net.rizecookey.racc0on.ir.schedule;

import net.rizecookey.racc0on.ir.IrGraphTraverser;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.StartNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SsaNodeScheduler extends IrGraphTraverser {

    private final Set<Node> visited = new HashSet<>();
    private final Map<Block, List<Node>> schedules = new HashMap<>();

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
            schedules.computeIfAbsent(block, _ -> new ArrayList<>());
            block.predecessors().stream()
                    .map(Node::block)
                    .distinct()
                    .filter(pred -> !visited.contains(pred))
                    .forEach(this::push);
            return;
        }

        List<Node> schedule = schedules.computeIfAbsent(node.block(), _ -> new ArrayList<>());
        if (!visited.add(node)) {
            return;
        }

        if (node instanceof StartNode) {
            schedule.addFirst(node);
        } else {
            schedule.add(node);
        }
    }

    public Map<Block, List<Node>> getSchedules() {
        return Map.copyOf(schedules);
    }
}
