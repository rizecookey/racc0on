package net.rizecookey.racc0on.ir.util;

import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.BuiltinCallNode;
import net.rizecookey.racc0on.ir.node.CallNode;
import net.rizecookey.racc0on.ir.node.ConstBoolNode;
import net.rizecookey.racc0on.ir.node.ConstIntNode;
import net.rizecookey.racc0on.ir.node.IfNode;
import net.rizecookey.racc0on.ir.node.JumpNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ParameterNode;
import net.rizecookey.racc0on.ir.node.Phi;
import net.rizecookey.racc0on.ir.node.ProjNode;
import net.rizecookey.racc0on.ir.node.ReturnNode;
import net.rizecookey.racc0on.ir.node.StartNode;
import net.rizecookey.racc0on.ir.node.operation.binary.BinaryOperationNode;
import net.rizecookey.racc0on.ir.node.operation.binary.DivNode;
import net.rizecookey.racc0on.ir.node.operation.binary.ModNode;
import net.rizecookey.racc0on.ir.node.operation.unary.UnaryOperationNode;

import java.util.List;

public final class NodeSupport {
    private NodeSupport() {

    }

    public static boolean providesValue(Node node) {
        return switch (node) {
            case StartNode _, Block _, ReturnNode _, IfNode _, ProjNode _, JumpNode _ -> false;
            case ConstBoolNode _, ConstIntNode _, Phi _, BinaryOperationNode _, UnaryOperationNode _,
                 CallNode _, BuiltinCallNode _, ParameterNode _ -> true;
        };
    }

    public static Node skipProj(Node node) {
        if (node instanceof ProjNode proj) {
            return proj.predecessor(ProjNode.IN);
        }

        return node;
    }

    public static Node predecessorSkipProj(Node node, int predIdx) {
        Node pred = node.predecessor(predIdx);
        if (pred instanceof ProjNode) {
            return pred.predecessor(ProjNode.IN);
        }
        return pred;
    }

    public static List<Node> predecessorsSkipProj(Node node) {
        return node.predecessors().stream()
                .map(pred -> {
                    if (pred instanceof ProjNode proj) {
                        return proj.predecessor(ProjNode.IN);
                    }

                    return pred;
                })
                .toList();
    }

    public static boolean causesSideEffect(Node node) {
        return switch (node) {
            case DivNode _, ModNode _, CallNode _, BuiltinCallNode _ -> true;
            case Block _, ConstBoolNode _, ConstIntNode _, IfNode _, JumpNode _, ParameterNode _, Phi _, ProjNode _,
                 ReturnNode _, StartNode _, BinaryOperationNode _, UnaryOperationNode _ -> false;
        };
    }

    public static boolean isSideEffect(Phi phi) {
        for (Node pred : phi.predecessors()) {
            return switch (pred) {
                case ProjNode proj when proj.projectionInfo() == ProjNode.SimpleProjectionInfo.SIDE_EFFECT -> true;
                case Phi predPhi -> isSideEffect(predPhi);
                default -> false;
            };
        }

        return false;
    }
}
