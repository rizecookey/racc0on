package net.rizecookey.racc0on.ir.node.operation.binary;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;

public final class NotEqNode extends CommutativeBinaryOperationNode {
    public NotEqNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    public ValueType valueType() {
        return ValueType.BOOL;
    }
}
