package net.rizecookey.racc0on.ir.node.operation;

import net.rizecookey.racc0on.ir.node.AbstractNode;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.DivNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.ModNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.ShiftLeftNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.ShiftRightNode;
import net.rizecookey.racc0on.ir.node.operation.arithmetic.SubNode;
import net.rizecookey.racc0on.ir.node.operation.compare.GreaterNode;
import net.rizecookey.racc0on.ir.node.operation.compare.GreaterOrEqNode;
import net.rizecookey.racc0on.ir.node.operation.compare.LessNode;
import net.rizecookey.racc0on.ir.node.operation.compare.LessOrEqNode;

public sealed abstract class BinaryOperationNode extends AbstractNode permits CommutativeBinaryOperationNode, DivNode, GreaterNode, GreaterOrEqNode, LessNode, LessOrEqNode, ModNode, ShiftLeftNode, ShiftRightNode, SubNode {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    protected BinaryOperationNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    protected BinaryOperationNode(Block block, Node left, Node right, Node sideEffect) {
        super(block, left, right, sideEffect);
    }

    protected static int commutativeHashCode(BinaryOperationNode node) {
        int h = node.block().hashCode() * 31 + node.getClass().hashCode();
        // commutative operation: we want h(op(x, y)) == h(op(y, x))
        h += 31 * (predecessorHash(node, LEFT) ^ predecessorHash(node, RIGHT));
        return h;
    }

    protected static boolean commutativeEquals(BinaryOperationNode a, Object bObj) {
        if (!(bObj instanceof BinaryOperationNode b)) {
            return false;
        }
        if (a.getClass() != b.getClass()) {
            return false;
        }
        if (a.block() != b.block()) {
            return false;
        }
        if (a.predecessor(LEFT) == b.predecessor(LEFT) && a.predecessor(RIGHT) == b.predecessor(RIGHT)) {
            return true;
        }
        // commutative operation: op(x, y) == op(y, x)
        return a.predecessor(LEFT) == b.predecessor(RIGHT) && a.predecessor(RIGHT) == b.predecessor(LEFT);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BinaryOperationNode binOp)) {
            return false;
        }
        return obj.getClass() == this.getClass()
            && block() == binOp.block()
            && this.predecessor(LEFT) == binOp.predecessor(LEFT)
            && this.predecessor(RIGHT) == binOp.predecessor(RIGHT);
    }

    @Override
    public int hashCode() {
        int h = block().hashCode() * 31;
        h += (predecessorHash(this, LEFT) * 31 + predecessorHash(this, RIGHT)) ^ this.getClass().hashCode();
        return h;
    }
}
