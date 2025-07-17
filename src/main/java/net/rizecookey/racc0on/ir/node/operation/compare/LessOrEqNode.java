package net.rizecookey.racc0on.ir.node.operation.compare;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;
import net.rizecookey.racc0on.ir.node.operation.AbstractBinaryOperationNode;

public final class LessOrEqNode extends AbstractBinaryOperationNode implements CompareNode {
    public LessOrEqNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    public ValueType valueType() {
        return ValueType.BOOL;
    }
}
