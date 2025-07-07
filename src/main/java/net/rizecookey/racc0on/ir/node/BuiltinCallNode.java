package net.rizecookey.racc0on.ir.node;

import java.util.List;
import java.util.stream.Stream;

public final class BuiltinCallNode extends Node {
    public static final int SIDE_EFFECT = 0;

    private final String builtinName;

    public BuiltinCallNode(Block block, String builtinName, Node sideEffect, Node... arguments) {
        super(block, Stream.concat(Stream.of(sideEffect), Stream.of(arguments)).toArray(Node[]::new));
        this.builtinName = builtinName;
    }

    public String builtinName() {
        return builtinName;
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
        return "[" + builtinName() + "]";
    }

    @Override
    public ValueType valueType() {
        return ValueType.INT;
    }
}
