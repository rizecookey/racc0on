package net.rizecookey.racc0on.ir.node.operation.compare;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;
import net.rizecookey.racc0on.ir.node.operation.AbstractCommutativeBinaryOperationNode;

public final class EqNode extends AbstractCommutativeBinaryOperationNode implements CompareNode {
    public EqNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    public ValueType valueType() {
        return ValueType.BOOL;
    }
}
