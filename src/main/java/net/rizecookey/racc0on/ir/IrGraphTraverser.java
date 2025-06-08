package net.rizecookey.racc0on.ir;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class IrGraphTraverser {
    private final Set<Node> seen = new HashSet<>();

    public void traverse(IrGraph graph) {
        clearSeen();
        Deque<Node> stack = new ArrayDeque<>();
        stack.add(graph.endBlock());

        while (!stack.isEmpty()) {
            Node node = stack.peek();

            if (addSeen(node)) {
                getPredecessors(node).reversed().forEach(stack::push);
                continue;
            }

            stack.pop();

            consume(node);
        }
    }

    public void clearSeen() {
        seen.clear();
    }

    public boolean hasSeen(Node node) {
        return seen.contains(node);
    }

    public boolean addSeen(Node node) {
        return seen.add(node);
    }

    public abstract List<? extends Node> getPredecessors(Node node);

    public abstract void consume(Node node);
}
