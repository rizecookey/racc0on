package net.rizecookey.racc0on.backend;

import net.rizecookey.racc0on.ir.node.ConstBoolNode;
import net.rizecookey.racc0on.ir.node.IfNode;
import net.rizecookey.racc0on.ir.node.JumpNode;
import net.rizecookey.racc0on.ir.node.operation.TernaryNode;
import net.rizecookey.racc0on.ir.node.operation.binary.BinaryOperationNode;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.ConstIntNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.Phi;
import net.rizecookey.racc0on.ir.node.ProjNode;
import net.rizecookey.racc0on.ir.node.ReturnNode;
import net.rizecookey.racc0on.ir.node.StartNode;
import net.rizecookey.racc0on.ir.node.operation.unary.UnaryOperationNode;

import java.util.List;

public final class NodeUtils {
    private NodeUtils() {}

    public static boolean providesValue(Node node) {
        return switch (node) {
            case BinaryOperationNode _, UnaryOperationNode _, ConstIntNode _, ConstBoolNode _, Phi _, TernaryNode _ -> true;
            case Block _, ReturnNode _, StartNode _, ProjNode _, IfNode _, JumpNode _ -> false;
        };
    }

    public static List<Node> shortcutPredecessors(Node node) {
        return node.predecessors().stream()
                .map(pred -> {
                    if (pred instanceof ProjNode projNode) {
                        return projNode.predecessor(ProjNode.IN);
                    }

                    return pred;
                })
                .toList();
    }
}
