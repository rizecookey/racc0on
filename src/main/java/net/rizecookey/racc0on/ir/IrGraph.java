package net.rizecookey.racc0on.ir;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;

public class IrGraph {
    private final Map<Node, SequencedSet<Node>> successors = new IdentityHashMap<>();
    private final Block startBlock;
    private final Block endBlock;
    private final String name;
    private final Map<Class<? extends Node>, Integer> nodeIds;

    public IrGraph(String name) {
        this.name = name;
        this.nodeIds = new HashMap<>();
        this.startBlock = new Block(this);
        this.endBlock = new Block(this);
    }

    public void registerSuccessor(Node node, Node successor) {
        this.successors.computeIfAbsent(node, _ -> new LinkedHashSet<>()).add(successor);
    }

    public void removeSuccessor(Node node, Node oldSuccessor) {
        this.successors.computeIfAbsent(node, _ -> new LinkedHashSet<>()).remove(oldSuccessor);
    }

    /// {@return the set of nodes that have the given node as one of their inputs}
    public Set<Node> successors(Node node) {
        SequencedSet<Node> successors = this.successors.get(node);
        if (successors == null) {
            return Set.of();
        }
        return Set.copyOf(successors);
    }

    public Block startBlock() {
        return this.startBlock;
    }

    public Block endBlock() {
        return this.endBlock;
    }

    /// {@return the name of this graph}
    public String name() {
        return name;
    }

    public int reserveId(Node node) {
        return nodeIds.compute(node.getClass(), (_, old) -> old == null ? 0 : old + 1);
    }
}
