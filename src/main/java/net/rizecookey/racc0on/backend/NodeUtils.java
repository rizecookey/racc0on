package net.rizecookey.racc0on.backend;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.operation.binary.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.operation.unary.UnaryOperationNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NodeUtils {
    private NodeUtils() {}

    public static boolean providesValue(Node node) {
        return switch (node) {
            case BinaryOperationNode _, UnaryOperationNode _, ConstIntNode _, Phi _ -> true;
            case Block _, ReturnNode _, StartNode _, ProjNode _ -> false;
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

    public static List<Node> transformToSequential(IrGraph program) {
        Set<Node> seen = new HashSet<>();
        List<Node> sequential = new ArrayList<>();

        Deque<Node> stack = new ArrayDeque<>();
        stack.add(program.endBlock());
        while (!stack.isEmpty()) {
            Node node = stack.peek();

            if (seen.add(node)) {
                node.predecessors().forEach(stack::push);
                continue;
            }

            stack.remove(node);
            if (!sequential.contains(node)) {
                sequential.add(node);
            }
        }

        return sequential;
    }

    public static String printSequential(List<Node> sequential) {
        StringBuilder sb = new StringBuilder();
        for (Node node : sequential) {
            sb.append(sequential.indexOf(node)).append(": ");
            switch (node) {
                case BinaryOperationNode bin -> sb.append(bin)
                        .append(" ")
                        .append(sequential.indexOf(bin.predecessor(BinaryOperationNode.LEFT)))
                        .append(" ")
                        .append(sequential.indexOf(bin.predecessor(BinaryOperationNode.RIGHT)));
                case UnaryOperationNode un -> sb.append(un)
                        .append(" ")
                        .append(sequential.indexOf(un.predecessor(UnaryOperationNode.IN)));
                case ConstIntNode con -> sb.append(con);
                case ReturnNode ret -> sb.append(ret).append(" ").append(sequential.indexOf(ret));
                case ProjNode projNode -> sb.append(projNode).append(" ").append(sequential.indexOf(projNode.predecessor(ProjNode.IN)));
                case Phi _, StartNode _, Block _ -> sb.append(node);
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
