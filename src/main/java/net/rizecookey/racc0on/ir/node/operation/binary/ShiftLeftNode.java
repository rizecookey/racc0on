package net.rizecookey.racc0on.ir.node.operation.binary;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;

public final class ShiftLeftNode extends BinaryOperationNode {
    public ShiftLeftNode(Block block, Node left, Node right) {
        super(block, left, right);
    }
}
