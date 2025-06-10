package net.rizecookey.racc0on.ir.schedule;

import net.rizecookey.racc0on.ir.IrGraphTraverser;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.IfNode;
import net.rizecookey.racc0on.ir.node.JumpNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ProjNode;
import net.rizecookey.racc0on.ir.node.StartNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
            schedules.computeIfAbsent(block, _ -> List.of());
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
        Map<Block, List<Node>> finalSchedules = new HashMap<>();

        for (Block block : schedules.keySet()) {
            List<Node> schedule = schedules.get(block);
            finalSchedules.put(block, Stream.concat(
                    schedule.stream().filter(Predicate.not(SsaNodeScheduler::moveToEnd)),
                    schedule.stream().filter(SsaNodeScheduler::moveToEnd)
            ).toList());
        }

        return Map.copyOf(finalSchedules);
    }

    private static boolean moveToEnd(Node node) {
        return switch (node) {
            case JumpNode _, IfNode _ -> true;
            case ProjNode projNode when projNode.projectionInfo() == ProjNode.SimpleProjectionInfo.IF_TRUE
                    || projNode.projectionInfo() == ProjNode.SimpleProjectionInfo.IF_FALSE -> true;
            default -> false;
        };
    }
}
