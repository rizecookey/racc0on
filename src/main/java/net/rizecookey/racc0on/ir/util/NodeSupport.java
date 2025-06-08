package net.rizecookey.racc0on.ir.util;

import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ProjNode;

public final class NodeSupport {
    private NodeSupport() {

    }

    public static Node predecessorSkipProj(Node node, int predIdx) {
        Node pred = node.predecessor(predIdx);
        if (pred instanceof ProjNode) {
            return pred.predecessor(ProjNode.IN);
        }
        return pred;
    }
}
