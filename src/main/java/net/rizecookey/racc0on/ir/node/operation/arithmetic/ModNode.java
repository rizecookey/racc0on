package net.rizecookey.racc0on.ir.node.operation.arithmetic;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;
import net.rizecookey.racc0on.ir.node.operation.BinaryOperationNode;

public final class ModNode extends BinaryOperationNode {
    public static final int SIDE_EFFECT = 2;
    public ModNode(Block block, Node left, Node right, Node sideEffect) {
        super(block, left, right, sideEffect);
    }

    @Override
    public boolean equals(Object obj) {
        // side effect, must be very careful with value numbering.
        // this is the most conservative approach
        return obj == this;
    }

    @Override
    public ValueType valueType() {
        return ValueType.INT;
    }
}
