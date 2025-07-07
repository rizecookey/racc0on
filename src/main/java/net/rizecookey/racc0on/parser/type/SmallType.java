package net.rizecookey.racc0on.parser.type;

import net.rizecookey.racc0on.ir.node.Node;

public sealed interface SmallType extends Type permits ArrayType, BasicType, PointerType {
    Node.ValueType toIrType();
}
