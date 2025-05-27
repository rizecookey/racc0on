package edu.kit.kastel.vads.compiler.ir.node.operation.unary;

import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;

public final class BitwiseNotNode extends UnaryOperationNode {
    public BitwiseNotNode(Block block, Node in) {
        super(block, in);
    }
}
