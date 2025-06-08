package net.rizecookey.racc0on.ir;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record SsaSchedule(Map<Block, List<Node>> blockSchedules, IrGraph programGraph) {
    public static SsaSchedule generate(IrGraph program) {
        Map<Block, List<Node>> blockSchedules = new HashMap<>();
        new DataflowTraverser(blockSchedules).traverse(program);
        new ControlFlowTraverser(blockSchedules).traverse(program);

        return new SsaSchedule(Map.copyOf(blockSchedules), program);
    }

    private static class DataflowTraverser extends IrGraphTraverser {
        private final Map<Block, List<Node>> schedulesReference;
        private DataflowTraverser(Map<Block, List<Node>> schedulesReference) {
            this.schedulesReference = schedulesReference;
        }


        @Override
        public List<? extends Node> getPredecessors(Node node) {
            return node.predecessors();
        }

        @Override
        public void consume(Node node) {
            List<Node> blockSchedule = schedulesReference.computeIfAbsent(node.block(), _ -> new ArrayList<>());
            if (!blockSchedule.contains(node)) {
                if (node instanceof StartNode) {
                    blockSchedule.addFirst(node);
                } else {
                    blockSchedule.add(node);
                }
            }
        }
    }

    private static class ControlFlowTraverser extends IrGraphTraverser {
        private final Map<Block, List<Node>> schedulesReference;
        private final Set<Node> visited;

        private ControlFlowTraverser(Map<Block, List<Node>> schedulesReference) {
            this.schedulesReference = schedulesReference;
            visited = schedulesReference.values().stream().flatMap(List::stream).collect(Collectors.toCollection(HashSet::new));
        }

        @Override
        public List<? extends Node> getPredecessors(Node node) {
            List<Node> predecessors = new ArrayList<>();

            if (addSeen(node.block())) {
                predecessors.addAll(node.block().predecessors());
            }
            predecessors.addAll(node.predecessors());

            return predecessors;
        }

        @Override
        public void consume(Node node) {
            if (!visited.add(node) || node instanceof Block) {
                return;
            }
            schedulesReference.computeIfAbsent(node.block(), _ -> new ArrayList<>()).add(node);
        }
    }
}
