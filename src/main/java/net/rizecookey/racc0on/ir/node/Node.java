package net.rizecookey.racc0on.ir.node;

import net.rizecookey.racc0on.ir.IrGraph;
import net.rizecookey.racc0on.ir.node.constant.ConstNode;
import net.rizecookey.racc0on.ir.node.operation.BinaryOperationNode;
import net.rizecookey.racc0on.ir.node.operation.UnaryOperationNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.ArithmeticNode;
import net.rizecookey.racc0on.ir.util.DebugInfo;

import java.util.List;

/// The interface declaring all common public Node methods, allowing them to be used directly on the grouping interfaces like {@link ArithmeticNode}.
public sealed interface Node permits AbstractNode, ConstNode, BinaryOperationNode, UnaryOperationNode {
    int id();

    IrGraph graph();

    Block block();

    List<? extends Node> predecessors();

    void setPredecessor(int idx, Node node);

    void addPredecessor(Node node);

    Node predecessor(int idx);

    @Override
    String toString();

    DebugInfo debugInfo();

    ValueType valueType();
}
