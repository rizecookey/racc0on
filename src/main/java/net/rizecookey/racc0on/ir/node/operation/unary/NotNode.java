package net.rizecookey.racc0on.ir.node.operation.unary;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;

public final class NotNode extends UnaryOperationNode {
    public NotNode(Block block, Node in) {
        super(block, in);
    }

    @Override
    public ValueType valueType() {
        return ValueType.BOOL;
    }
}
