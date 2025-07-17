package net.rizecookey.racc0on.ir.node.constant;

import net.rizecookey.racc0on.ir.node.Node;

public sealed interface ConstNode extends Node permits ConstIntNode, ConstBoolNode, ConstAddressNode {
}
