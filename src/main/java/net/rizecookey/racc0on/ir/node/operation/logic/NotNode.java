package net.rizecookey.racc0on.ir.node.operation.logic;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;
import net.rizecookey.racc0on.ir.node.operation.UnaryOperationNode;

public final class NotNode extends UnaryOperationNode {
    public NotNode(Block block, Node in) {
        super(block, in);
    }

    @Override
    public ValueType valueType() {
        return ValueType.BOOL;
    }
}
