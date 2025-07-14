package net.rizecookey.racc0on.backend.x86_64.operation.memory;

import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664OpLike;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.memory.StructMemberNode;

import java.util.List;

public class x8664StructMemberLoadPhantomOp implements x8664OpLike {
    private final StructMemberNode address;
    private final Node struct;

    public x8664StructMemberLoadPhantomOp(StructMemberNode address, Node struct) {
        this.address = address;
        this.struct = struct;
    }

    @Override
    public List<x8664Op> asOperations() {
        return List.of(
                // TODO probably some optimization potential
                new x8664AddressNullCheckOp(struct),
                new x8664AddrOffsetCalcOp(address, struct)
        );
    }
}
