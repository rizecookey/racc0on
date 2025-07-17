package net.rizecookey.racc0on.ir.node.operation.logic;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;
import net.rizecookey.racc0on.ir.node.operation.AbstractCommutativeBinaryOperationNode;

public final class BitwiseXorNode extends AbstractCommutativeBinaryOperationNode implements BitLogicNode {
    public BitwiseXorNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    public ValueType valueType() {
        return ValueType.INT;
    }
}
