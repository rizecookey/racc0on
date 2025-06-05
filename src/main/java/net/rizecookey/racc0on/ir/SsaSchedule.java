package net.rizecookey.racc0on.ir;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record SsaSchedule(List<Block> blockOrder, Map<Block, List<Node>> blockSchedules) {
    public static SsaSchedule generate(IrGraph program) {
        Map<Block, List<Node>> blockSchedules = scheduleWithinBlocks(program);
        List<Block> blockOrder = scheduleControlFlow(program, blockSchedules);

        return new SsaSchedule(List.copyOf(blockOrder), Map.copyOf(blockSchedules));
    }

    private static Map<Block, List<Node>> scheduleWithinBlocks(IrGraph program) {
        Set<Node> seen = new HashSet<>();
        Map<Block, List<Node>> blockSchedules = new HashMap<>();

        Deque<Node> stack = new ArrayDeque<>();
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
                blockSchedule.add(node);
            }
        }

        return blockSchedules;
    }

    private static List<Block> scheduleControlFlow(IrGraph program, Map<Block, List<Node>> blockSchedules) {
        Set<Node> seen = new HashSet<>();
        List<Block> blockOrder = new ArrayList<>();
        Deque<Block> blockStack = new ArrayDeque<>();
        blockStack.add(program.endBlock());

        while (!blockStack.isEmpty()) {
            Block block = blockStack.peek();

            if (seen.add(block)) {
                block.predecessors().stream().map(Node::block).forEach(blockStack::push);
                continue;
            }

            blockStack.pop();
            if (!blockOrder.contains(block)) {
                blockOrder.add(block);
                blockSchedules.computeIfAbsent(block, _ -> new ArrayList<>()).addAll(block.getExits());
            }
        }

        return blockOrder;
    }
}
