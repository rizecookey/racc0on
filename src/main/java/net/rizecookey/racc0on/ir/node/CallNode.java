package net.rizecookey.racc0on.ir.node;

import java.util.List;
import java.util.stream.Stream;

public final class CallNode extends Node {
    public static final int SIDE_EFFECT = 0;

    private final String target;

    public CallNode(Block block, String target, Node sideEffect, Node... arguments) {
        super(block, Stream.concat(Stream.of(sideEffect), Stream.of(arguments)).toArray(Node[]::new));
        this.target = target;
    }

    public String target() {
        return target;
    }

    public List<? extends Node> arguments() {
        List<? extends Node> predecessors = predecessors();
        return predecessors.subList(1, predecessors.size());
    }

    public Node argument(int index) {
        return arguments().get(index);
    }

    @Override
    protected String info() {
        return "[" + target() + "]";
    }
}
