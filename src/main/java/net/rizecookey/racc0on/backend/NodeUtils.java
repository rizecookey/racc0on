package net.rizecookey.racc0on.backend;

import edu.kit.kastel.vads.compiler.ir.node.ConstBoolNode;
import edu.kit.kastel.vads.compiler.ir.node.IfNode;
import edu.kit.kastel.vads.compiler.ir.node.JumpNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.TernaryNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.unary.UnaryOperationNode;

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
