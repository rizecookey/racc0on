package net.rizecookey.racc0on.backend;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class LivenessAnalysis {
    private LivenessAnalysis() {}

    public static Map<Node, Set<Node>> getLiveness(IrGraph program) {
        Map<Node, Set<Node>> liveNodes = new HashMap<>();

        Deque<Node> nodeStack = new ArrayDeque<>();
        nodeStack.add(program.endBlock());
        while (!nodeStack.isEmpty()) {
            Node node = nodeStack.pop();
            List<? extends Node> predecessors = node.predecessors();
            nodeStack.addAll(predecessors);

            List<? extends Node> predsWithoutNoValueNodes = predecessors.stream()
                    .filter(NodeUtils::providesValue)
                    .toList();
            if (NodeUtils.providesValue(node)) {
                liveNodes.computeIfAbsent(node, _ -> new HashSet<>()).addAll(predsWithoutNoValueNodes);
            }

            for (Node pred : node.predecessors()) {
                if (!NodeUtils.providesValue(pred)) {
                    continue;
                }

                liveNodes.computeIfAbsent(pred, _ -> new HashSet<>())
                        .addAll(predsWithoutNoValueNodes.stream()
                                .filter(n -> !n.equals(pred))
                                .collect(Collectors.toSet()));
            }
        }

        return liveNodes;
    }
}
