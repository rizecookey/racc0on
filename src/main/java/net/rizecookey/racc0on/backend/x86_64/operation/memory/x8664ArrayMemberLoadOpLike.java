package net.rizecookey.racc0on.backend.x86_64.operation.memory;

import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664OpLike;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.memory.ArrayMemberNode;

import java.util.List;

public class x8664ArrayMemberLoadOpLike implements x8664OpLike {
    private final ArrayMemberNode address;
    private final Node array, index;

    public x8664ArrayMemberLoadOpLike(ArrayMemberNode address, Node array, Node index) {
        this.address = address;
        this.array = array;
        this.index = index;
    }

    @Override
    public List<x8664Op> asOperations() {
        return List.of(
                new x8664AddressNullCheckOp(array),
                new x8664ArrayBoundsCheckOp(array, index),
                new x8664AddrOffsetCalcOp(address, array, index)
        );
    }
}
