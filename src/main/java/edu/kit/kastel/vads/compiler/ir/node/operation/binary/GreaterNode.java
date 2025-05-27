package edu.kit.kastel.vads.compiler.ir.node.operation.binary;

import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;

public final class GreaterNode extends BinaryOperationNode {
    public GreaterNode(Block block, Node left, Node right) {
        super(block, left, right);
    }
}
