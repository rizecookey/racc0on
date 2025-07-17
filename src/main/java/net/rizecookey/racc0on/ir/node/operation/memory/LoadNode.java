package net.rizecookey.racc0on.ir.node.operation.memory;

import net.rizecookey.racc0on.ir.node.AbstractNode;
import net.rizecookey.racc0on.ir.node.Block;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.ValueType;

public final class LoadNode extends AbstractNode {
    public static final int ADDRESS = 0;
    public static final int SIDE_EFFECT = 1;

    private final ValueType type;

    public LoadNode(Block block, Node address, ValueType valueType, Node sideEffect) {
        super(block, address, sideEffect);
        this.type = valueType;
    }

    @Override
    public ValueType valueType() {
        return type;
    }
}
