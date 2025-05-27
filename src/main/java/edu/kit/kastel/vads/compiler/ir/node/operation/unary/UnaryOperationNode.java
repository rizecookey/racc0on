package edu.kit.kastel.vads.compiler.ir.node.operation.unary;

import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;

public sealed abstract class UnaryOperationNode extends Node permits BitwiseNotNode, NotNode {
    public static final int IN = 0;

    protected UnaryOperationNode(Block block, Node in) {
        super(block, in);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UnaryOperationNode unOp)) {
            return false;
        }
        return obj.getClass() == this.getClass()
                && this.predecessor(IN) == unOp.predecessor(IN);
    }

    @Override
    public int hashCode() {
        return predecessorHash(this, IN) ^ this.getClass().hashCode();
    }
}
