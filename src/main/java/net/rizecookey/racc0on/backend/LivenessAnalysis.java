package net.rizecookey.racc0on.backend;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.stream.Collectors;

public final class LivenessAnalysis {
    private LivenessAnalysis() {}

    public static Map<Node, SequencedSet<Node>> getLiveness(IrGraph program) {
        Map<Node, SequencedSet<Node>> liveNodes = new HashMap<>();

        for (Node node : NodeUtils.traverseBackwards(program)) {
            List<? extends Node> predecessors = node.predecessors();

            List<? extends Node> predsWithoutNoValueNodes = predecessors.stream()
                    .filter(NodeUtils::providesValue)
                    .toList();
            if (NodeUtils.providesValue(node)) {
                liveNodes.computeIfAbsent(node, _ -> new LinkedHashSet<>()).addAll(predsWithoutNoValueNodes);
            }

            for (Node pred : node.predecessors()) {
                if (!NodeUtils.providesValue(pred)) {
                    continue;
                }

                liveNodes.computeIfAbsent(pred, _ -> new LinkedHashSet<>())
                        .addAll(predsWithoutNoValueNodes.stream()
                                .filter(n -> !n.equals(pred))
                                .collect(Collectors.toSet()));
            }
        }

        return liveNodes;
    }
}
