package net.rizecookey.racc0on.ir;

import net.rizecookey.racc0on.ir.node.Node;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public abstract class IrGraphTraverser {
    private final Set<Node> seen = new HashSet<>();
    private final Deque<Node> stack = new ArrayDeque<>();

    public void traverse(Node node) {
        stack.add(node);

        while (!stack.isEmpty()) {
            Node top = stack.peek();

            if (visit(top)) {
                continue;
            }

            stack.pop();

            consume(top);
        }
    }

    protected void push(Node node) {
        stack.push(node);
    }

    protected Node pop() {
        return stack.pop();
    }

    protected @Nullable Node peek() {
        return stack.peek();
    }

    protected boolean hasSeen(Node node) {
        return seen.contains(node);
    }

    protected boolean addSeen(Node node) {
        return this.seen.add(node);
    }

    protected boolean removeSeen(Node node) {
        return this.seen.remove(node);
    }

    protected abstract boolean visit(Node node);

    protected abstract void consume(Node node);
}
