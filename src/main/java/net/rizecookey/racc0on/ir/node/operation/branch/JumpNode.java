package net.rizecookey.racc0on.ir.node.operation.branch;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;

import java.util.List;

public final class JumpNode extends Node {
    public JumpNode(Block block) {
        super(block);
    }

    public Block target() {
        List<Node> successors = List.copyOf(graph().successors(this));
        if (successors.size() != 1) {
            throw new IllegalStateException("Jump should have exactly one target");
        }

        if (successors.getFirst() instanceof Block block) {
            return block;
        }

        throw new IllegalStateException("Successor isn't a block");
    }
}
