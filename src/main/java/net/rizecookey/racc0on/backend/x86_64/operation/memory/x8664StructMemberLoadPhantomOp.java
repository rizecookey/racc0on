package net.rizecookey.racc0on.backend.x86_64.operation.memory;

import net.rizecookey.racc0on.backend.memory.MemoryLayout;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.x86_64.memory.x8664MemoryUtils;
import net.rizecookey.racc0on.backend.x86_64.operation.arithmetic.x8664AddOp;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664OpLike;
import net.rizecookey.racc0on.ir.node.ConstIntNode;
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
        MemoryLayout.Compound structLayout = (MemoryLayout.Compound) x8664MemoryUtils.createLayout(address.structLayout());
        MemoryLayout memberLayout = structLayout.members().get(address.memberIndex());

        return List.of(
                // TODO probably some optimization potential
                new x8664AddressNullCheckOp(struct),
                new x8664AddOp(new Operands.Binary<>(address, struct, new ConstIntNode(address.block(), memberLayout.start())))
        );
    }
}
