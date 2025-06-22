package net.rizecookey.racc0on.ir.node;

public final class GlobalSymbolNode extends Node {
    public static int SIDE_EFFECT = 0;

    private final String symbolName;

    public GlobalSymbolNode(Block block, String symbolName, Node sideEffect) {
        super(block, sideEffect);
        this.symbolName = symbolName;
    }

    public String symbolName() {
        return symbolName;
    }

    @Override
    protected String info() {
        return "[" + symbolName() + "]";
    }
}
