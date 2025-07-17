package net.rizecookey.racc0on.ir.node.operation;

import net.rizecookey.racc0on.ir.node.AbstractNode;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.logic.NotNode;

public sealed abstract class AbstractUnaryOperationNode extends AbstractNode implements UnaryOperationNode permits NotNode {
    protected AbstractUnaryOperationNode(Block block, Node in) {
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
