package edu.kit.kastel.vads.compiler.ir.node.operation;

import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;

public final class TernaryNode extends Node {
    public static final int CONDITION = 0;
    public static final int IF_TRUE = 1;
    public static final int IF_FALSE = 2;

    public TernaryNode(Block block, Node condition, Node ifTrue, Node ifFalse) {
        super(block, condition, ifTrue, ifFalse);
    }
}
