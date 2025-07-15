package net.rizecookey.racc0on.ir.node.operation.arithmetic;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;
import net.rizecookey.racc0on.ir.node.operation.BinaryOperationNode;

public final class SubNode extends BinaryOperationNode {
    public SubNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    public ValueType valueType() {
        return ValueType.INT;
    }
}
