package net.rizecookey.racc0on.ir.node;

import net.rizecookey.racc0on.ir.node.operation.binary.BinaryOperationNode;
import net.rizecookey.racc0on.ir.node.operation.unary.UnaryOperationNode;
import net.rizecookey.racc0on.ir.util.DebugInfo;
import net.rizecookey.racc0on.ir.IrGraph;
import net.rizecookey.racc0on.ir.util.DebugInfoHelper;
import net.rizecookey.racc0on.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// The base class for all nodes.
public sealed abstract class Node permits Block, ConstBoolNode, ConstIntNode, IfNode, JumpNode, Phi, ProjNode, ReturnNode, StartNode, BinaryOperationNode, UnaryOperationNode {
    private static final Map<Pair<IrGraph, Class<? extends Node>>, Integer> NODE_IDS = new HashMap<>();

    private final IrGraph graph;
    private final Block block;
    private final List<Node> predecessors = new ArrayList<>();
    private final DebugInfo debugInfo;
    private final int nodeId;

    protected Node(Block block, Node... predecessors) {
        this.graph = block.graph();
        this.block = block;
        this.predecessors.addAll(List.of(predecessors));
        for (Node predecessor : predecessors) {
            graph.registerSuccessor(predecessor, this);
        }
        this.debugInfo = DebugInfoHelper.getDebugInfo();

        this.nodeId = reserveId(graph);
    }

    protected Node(IrGraph graph) {
        assert this.getClass() == Block.class : "must be used by Block only";
        this.graph = graph;
        this.block = (Block) this;
        this.debugInfo = DebugInfo.NoInfo.INSTANCE;

        this.nodeId = reserveId(graph);
    }

    protected int reserveId(IrGraph graph) {
        return NODE_IDS.compute(new Pair<>(graph, getClass()),
                (_, old) -> old == null ? 0 : old + 1);
    }

    public int id() {
        return nodeId;
    }

    public final IrGraph graph() {
        return this.graph;
    }

    public final Block block() {
        return this.block;
    }

    public final List<? extends Node> predecessors() {
        return List.copyOf(this.predecessors);
    }

    public final void setPredecessor(int idx, Node node) {
        this.graph.removeSuccessor(this.predecessors.get(idx), this);
        this.predecessors.set(idx, node);
        this.graph.registerSuccessor(node, this);
    }

    public final void addPredecessor(Node node) {
        this.predecessors.add(node);
        this.graph.registerSuccessor(node, this);
    }

    public final Node predecessor(int idx) {
        return this.predecessors.get(idx);
    }

    @Override
    public final String toString() {
        return (this.getClass().getSimpleName().replace("Node", "") + " " + info()).stripTrailing();
    }

    protected String info() {
        return String.valueOf(nodeId);
    }

    public DebugInfo debugInfo() {
        return debugInfo;
    }

    protected static int predecessorHash(Node node, int predecessor) {
        return System.identityHashCode(node.predecessor(predecessor));
    }
}
