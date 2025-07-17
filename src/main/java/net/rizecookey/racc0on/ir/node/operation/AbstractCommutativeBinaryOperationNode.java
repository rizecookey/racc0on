package net.rizecookey.racc0on.ir.node.operation;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.AddNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.MulNode;
import net.rizecookey.racc0on.ir.node.operation.compare.EqNode;
import net.rizecookey.racc0on.ir.node.operation.compare.NotEqNode;
import net.rizecookey.racc0on.ir.node.operation.logic.BitwiseAndNode;
import net.rizecookey.racc0on.ir.node.operation.logic.BitwiseOrNode;
import net.rizecookey.racc0on.ir.node.operation.logic.BitwiseXorNode;

public sealed abstract class AbstractCommutativeBinaryOperationNode extends AbstractBinaryOperationNode permits AddNode, BitwiseAndNode, BitwiseOrNode, BitwiseXorNode, EqNode, MulNode, NotEqNode {
    protected AbstractCommutativeBinaryOperationNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    protected AbstractCommutativeBinaryOperationNode(Block block, Node left, Node right, Node sideEffect) {
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
