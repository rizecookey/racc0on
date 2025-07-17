package net.rizecookey.racc0on.ir.node.operation.arithmetic;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;
import net.rizecookey.racc0on.ir.node.operation.AbstractBinaryOperationNode;

public final class ShiftRightNode extends AbstractBinaryOperationNode implements ArithmeticNode {
    public ShiftRightNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    public ValueType valueType() {
        return ValueType.INT;
    }
}
