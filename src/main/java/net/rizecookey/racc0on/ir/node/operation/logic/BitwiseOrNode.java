package net.rizecookey.racc0on.ir.node.operation.logic;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;
import net.rizecookey.racc0on.ir.node.operation.CommutativeBinaryOperationNode;

public final class BitwiseOrNode extends CommutativeBinaryOperationNode {
    public BitwiseOrNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    public ValueType valueType() {
        return ValueType.INT;
    }
}
