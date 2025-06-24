package net.rizecookey.racc0on.ir.node;

import net.rizecookey.racc0on.lexer.keyword.BuiltinFunctionsKeywordType;

import java.util.List;
import java.util.stream.Stream;

public final class BuiltinCallNode extends Node {
    public static final int SIDE_EFFECT = 0;

    private final BuiltinFunctionsKeywordType type;

    public BuiltinCallNode(Block block, BuiltinFunctionsKeywordType type, Node sideEffect, Node... arguments) {
        super(block, Stream.concat(Stream.of(sideEffect), Stream.of(arguments)).toArray(Node[]::new));
        this.type = type;
    }

    public BuiltinFunctionsKeywordType type() {
        return type;
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
        return "[" + type().keyword() + "]";
    }
}
