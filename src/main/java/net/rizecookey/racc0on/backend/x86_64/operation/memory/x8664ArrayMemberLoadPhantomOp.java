package net.rizecookey.racc0on.backend.x86_64.operation.memory;

import net.rizecookey.racc0on.backend.memory.MemoryLayout;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.x86_64.memory.x8664MemoryUtils;
import net.rizecookey.racc0on.backend.x86_64.operation.arithmetic.x8664AddOp;
import net.rizecookey.racc0on.backend.x86_64.operation.arithmetic.x8664IMulOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664OpLike;
import net.rizecookey.racc0on.ir.node.ConstIntNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.memory.ArrayMemberNode;

import java.util.List;

public class x8664ArrayMemberLoadPhantomOp implements x8664OpLike {
    private final ArrayMemberNode address;
    private final Node array, index;

    public x8664ArrayMemberLoadPhantomOp(ArrayMemberNode address, Node array, Node index) {
        this.address = address;
        this.array = array;
        this.index = index;
    }

    @Override
    public List<x8664Op> asOperations() {
        MemoryLayout elementLayout = x8664MemoryUtils.createLayout(address.elementLayout());

        return List.of(
                new x8664ArrayBoundsCheckOp(array, index),
                new x8664IMulOp(new Operands.Binary<>(address, index,
                        new ConstIntNode(address.block(), elementLayout.size()))),
                new x8664AddOp(new Operands.Binary<>(address, address, array))
        );
    }
}
