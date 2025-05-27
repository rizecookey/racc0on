package edu.kit.kastel.vads.compiler.ir.node.operation.binary;

import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;

public final class BitwiseAndNode extends CommutativeBinaryOperationNode {
    public BitwiseAndNode(Block block, Node left, Node right) {
        super(block, left, right);
    }
}
