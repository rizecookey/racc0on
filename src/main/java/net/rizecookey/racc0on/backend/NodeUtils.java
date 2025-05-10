package net.rizecookey.racc0on.backend;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public final class NodeUtils {
    private NodeUtils() {}

    public static boolean providesValue(Node node) {
        return switch (node) {
            case BinaryOperationNode _, ConstIntNode _, Phi _, ProjNode _ -> true;
            case Block _, ReturnNode _, StartNode _ -> false;
        };
    }

    public static List<Node> transformToSequential(IrGraph program) {
        List<Node> sequential = new ArrayList<>();
        for (Node node : traverseBackwards(program)) {
            sequential.remove(node);
            sequential.addFirst(node);
        }

        return sequential;
    }

    public static String printSequential(List<Node> sequential) {
        StringBuilder sb = new StringBuilder();
        for (Node node : sequential) {
            if (!NodeUtils.providesValue(node)) {
                continue;
            }

            sb.append(sequential.indexOf(node)).append(": ");
            switch (node) {
                case BinaryOperationNode bin -> sb.append(bin)
                        .append(" ")
                        .append(sequential.indexOf(bin.predecessor(BinaryOperationNode.LEFT)))
                        .append(" ")
                        .append(sequential.indexOf(bin.predecessor(BinaryOperationNode.RIGHT)));
                case ConstIntNode con -> sb.append(con);
                case ReturnNode ret -> sb.append(ret).append(" ").append(sequential.indexOf(ret));
                case Phi _, ProjNode _, StartNode _, Block _ -> {}
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public static Iterable<Node> traverseBackwards(IrGraph program) {
        return () -> new BackwardsProgramIterator(program);
    }

    public static class BackwardsProgramIterator implements Iterator<Node> {
        private final Deque<Node> stack;

        private BackwardsProgramIterator(IrGraph program) {
            stack = new ArrayDeque<>();
            stack.push(program.endBlock());
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public Node next() {
            Node next = stack.pop();
            next.predecessors().forEach(stack::push);
            return next;
        }
    }
}
