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
import java.util.Deque;
import java.util.Iterator;

public final class NodeUtils {
    private NodeUtils() {}

    public static boolean providesValue(Node node) {
        return switch (node) {
            case BinaryOperationNode _, ConstIntNode _, Phi _ -> true;
            case Block _, ProjNode _, ReturnNode _, StartNode _ -> false;
        };
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
            stack.addAll(next.predecessors());
            return next;
        }
    }
}
