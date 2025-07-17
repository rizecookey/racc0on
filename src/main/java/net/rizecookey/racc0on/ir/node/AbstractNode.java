package net.rizecookey.racc0on.ir.node;

import net.rizecookey.racc0on.ir.IrGraph;
import net.rizecookey.racc0on.ir.node.constant.ConstAddressNode;
import net.rizecookey.racc0on.ir.node.constant.ConstBoolNode;
import net.rizecookey.racc0on.ir.node.constant.ConstIntNode;
import net.rizecookey.racc0on.ir.node.operation.BinaryOperationNode;
import net.rizecookey.racc0on.ir.node.operation.UnaryOperationNode;
import net.rizecookey.racc0on.ir.node.operation.branch.IfNode;
import net.rizecookey.racc0on.ir.node.operation.branch.JumpNode;
import net.rizecookey.racc0on.ir.node.operation.memory.AllocArrayNode;
import net.rizecookey.racc0on.ir.node.operation.memory.AllocNode;
import net.rizecookey.racc0on.ir.node.operation.memory.ArrayMemberNode;
import net.rizecookey.racc0on.ir.node.operation.memory.LoadNode;
import net.rizecookey.racc0on.ir.node.operation.memory.StoreNode;
import net.rizecookey.racc0on.ir.node.operation.memory.StructMemberNode;
import net.rizecookey.racc0on.ir.util.DebugInfo;
import net.rizecookey.racc0on.ir.util.DebugInfoHelper;

import java.util.ArrayList;
import java.util.List;

/// The base class for all nodes, implementing the Node interface.
public sealed abstract class AbstractNode implements Node permits Block, BuiltinCallNode, CallNode, ConstAddressNode, ConstBoolNode, ConstIntNode, IfNode, JumpNode, ParameterNode, Phi, ProjNode, ReturnNode, StartNode, BinaryOperationNode, AllocArrayNode, AllocNode, ArrayMemberNode, LoadNode, StoreNode, StructMemberNode, UnaryOperationNode {
    private final IrGraph graph;
    private final Block block;
    private final List<Node> predecessors = new ArrayList<>();
    private final DebugInfo debugInfo;
    private final int nodeId;

    protected AbstractNode(Block block, Node... predecessors) {
        this.graph = block.graph();
        this.block = block;
        this.predecessors.addAll(List.of(predecessors));
        for (Node predecessor : predecessors) {
            graph.registerSuccessor(predecessor, this);
        }
        this.debugInfo = DebugInfoHelper.getDebugInfo();

        this.nodeId = graph.reserveId(this);
    }

    protected AbstractNode(IrGraph graph) {
        assert this.getClass() == Block.class : "must be used by Block only";
        this.graph = graph;
        this.block = (Block) this;
        this.debugInfo = DebugInfo.NoInfo.INSTANCE;

        this.nodeId = graph.reserveId(this);
    }

    @Override
    public int id() {
        return nodeId;
    }

    @Override
    public final IrGraph graph() {
        return this.graph;
    }

    @Override
    public final Block block() {
        return this.block;
    }

    @Override
    public final List<? extends Node> predecessors() {
        return List.copyOf(this.predecessors);
    }

    @Override
    public final void setPredecessor(int idx, Node node) {
        this.graph.removeSuccessor(this.predecessors.get(idx), this);
        this.predecessors.set(idx, node);
        this.graph.registerSuccessor(node, this);
    }

    @Override
    public final void addPredecessor(Node node) {
        this.predecessors.add(node);
        this.graph.registerSuccessor(node, this);
    }

    @Override
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

    @Override
    public DebugInfo debugInfo() {
        return debugInfo;
    }

    protected static int predecessorHash(Node node, int predecessor) {
        return System.identityHashCode(node.predecessor(predecessor));
    }

    @Override
    public ValueType valueType() {
        return ValueType.NONE;
    }
}
