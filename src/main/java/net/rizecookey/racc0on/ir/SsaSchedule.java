package net.rizecookey.racc0on.ir;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.StartNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record SsaSchedule(Map<Block, List<Node>> blockSchedules, IrGraph programGraph) {
    public static SsaSchedule generate(IrGraph program) {
        Map<Block, List<Node>> blockSchedules = new HashMap<>();

        Deque<Node> stack = new ArrayDeque<>();
        Set<Node> seen = new HashSet<>();
        Set<Node> visited = new HashSet<>();

        stack.add(program.endBlock());
        while (!stack.isEmpty()) {
            Node node = stack.peek();

            if (seen.add(node)) {
                node.predecessors().stream()
                        .filter(pred -> !visited.contains(pred))
                        .forEach(stack::push);
                continue;
            }

            stack.pop();

            if (node instanceof Block block && visited.add(block)) {
                blockSchedules.computeIfAbsent(block, _ -> new ArrayList<>());
                block.predecessors().stream()
                        .map(Node::block)
                        .distinct()
                        .filter(pred -> !visited.contains(pred))
                        .forEach(stack::push);
                continue;
            }

            List<Node> blockSchedule = blockSchedules.computeIfAbsent(node.block(), _ -> new ArrayList<>());
            if (!visited.add(node)) {
                continue;
            }

            if (node instanceof StartNode) {
                blockSchedule.addFirst(node);
            } else {
                blockSchedule.add(node);
            }
        }

        return new SsaSchedule(Map.copyOf(blockSchedules), program);
    }
}
