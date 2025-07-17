package net.rizecookey.racc0on.ir.optimize;

import net.rizecookey.racc0on.ir.node.Node;

public final class NodeOptimizations implements Optimizer {
    private final ConstantFolding constFolding = new ConstantFolding();
    private final LocalValueNumbering localValueNumbering = new LocalValueNumbering();

    @Override
    public Node transform(Node node) {
        Node result = constFolding.transform(node);

        return localValueNumbering.transform(result);
    }
}
