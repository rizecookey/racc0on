package net.rizecookey.racc0on.ir.node.operation.binary;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;

public sealed abstract class CommutativeBinaryOperationNode extends BinaryOperationNode permits AddNode, BitwiseAndNode, BitwiseOrNode, BitwiseXorNode, EqNode, MulNode, NotEqNode {
    protected CommutativeBinaryOperationNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    protected CommutativeBinaryOperationNode(Block block, Node left, Node right, Node sideEffect) {
        super(block, left, right, sideEffect);
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass") // we do, but not here
    @Override
    public boolean equals(Object obj) {
        return commutativeEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return commutativeHashCode(this);
    }
}
