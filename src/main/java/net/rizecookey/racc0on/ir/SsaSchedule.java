package net.rizecookey.racc0on.ir;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record SsaSchedule(Map<Block, List<Node>> blockSchedules, IrGraph programGraph) {
    public static SsaSchedule generate(IrGraph program) {
        Map<Block, List<Node>> blockSchedules = new HashMap<>();
        scheduleDataflow(program, blockSchedules);
        scheduleControlFlow(program, blockSchedules);

        return new SsaSchedule(Map.copyOf(blockSchedules), program);
    }

    private static void scheduleDataflow(IrGraph program, Map<Block, List<Node>> blockSchedules) {
        Deque<Node> stack = new ArrayDeque<>();
        Set<Node> seen = new HashSet<>();
        stack.add(program.endBlock());
        while (!stack.isEmpty()) {
            Node node = stack.peek();
            if (seen.add(node)) {
                node.predecessors().forEach(stack::push);
                continue;
            }

            stack.pop();

            List<Node> blockSchedule = blockSchedules.computeIfAbsent(node.block(), _ -> new ArrayList<>());
            if (!blockSchedule.contains(node)) {
                if (node instanceof StartNode) {
                    blockSchedule.addFirst(node);
                } else {
                    blockSchedule.add(node);
                }
            }
        }
    }

    private static void scheduleControlFlow(IrGraph program, Map<Block, List<Node>> blockSchedules) {
        Deque<Node> stack = new ArrayDeque<>();
        Set<Node> visited = blockSchedules.values().stream().flatMap(List::stream).collect(Collectors.toCollection(HashSet::new));
        Set<Node> seen = new HashSet<>();

        stack.add(program.endBlock());
        while (!stack.isEmpty()) {
            Node node = stack.peek();

            if (seen.add(node)) {
                node.predecessors().forEach(stack::push);
                if (seen.add(node.block())) {
                    node.block().predecessors().forEach(stack::push);
                }
                continue;
            }

            stack.pop();

            if (!visited.add(node) || node instanceof Block) {
                continue;
            }
            blockSchedules.computeIfAbsent(node.block(), _ -> new ArrayList<>()).add(node);
        }
    }
}
