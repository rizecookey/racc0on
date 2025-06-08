package net.rizecookey.racc0on.ir.optimize;

import net.rizecookey.racc0on.ir.node.Node;

/// An interface that allows replacing a node with a more optimal one.
public interface Optimizer {

    Node transform(Node node);
}
