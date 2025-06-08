package net.rizecookey.racc0on.ir.node.operation.binary;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;

public final class EqNode extends CommutativeBinaryOperationNode {
    public EqNode(Block block, Node left, Node right) {
        super(block, left, right);
    }
}
