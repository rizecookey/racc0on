package edu.kit.kastel.vads.compiler.ir.node.operation.binary;

import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;

public final class LessOrEqNode extends BinaryOperationNode {
    public LessOrEqNode(Block block, Node left, Node right) {
        super(block, left, right);
    }
}
