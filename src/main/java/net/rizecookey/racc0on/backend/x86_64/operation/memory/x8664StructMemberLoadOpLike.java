package net.rizecookey.racc0on.backend.x86_64.operation.memory;

import net.rizecookey.racc0on.backend.memory.MemoryLayout;
import net.rizecookey.racc0on.backend.x86_64.memory.x8664MemoryUtils;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664OpLike;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.memory.StructMemberNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class x8664StructMemberLoadOpLike implements x8664OpLike {
    private final StructMemberNode address;
    private final Node struct;

    public x8664StructMemberLoadOpLike(StructMemberNode address, Node struct) {
        this.address = address;
        this.struct = struct;
    }

    @Override
    public List<x8664Op> asOperations() {
        if (address.graph().successors(address).stream().allMatch(node -> node instanceof StructMemberNode)) {
            // chained struct member accesses are collapsed in last node in chain
            return List.of();
        }

        Node predecessor = address;
        StructMemberNode firstMemberAccess = address;
        Deque<Integer> hierarchy = new ArrayDeque<>();
        while (predecessor instanceof StructMemberNode structMemberNode) {
            firstMemberAccess = structMemberNode;
            hierarchy.push(structMemberNode.memberIndex());
            predecessor = structMemberNode.predecessor(StructMemberNode.STRUCT);
        }

        int start = 0;
        MemoryLayout layout = x8664MemoryUtils.createLayout(firstMemberAccess.structLayout());
        while (!hierarchy.isEmpty()) {
            int index = hierarchy.pop();
            layout = ((MemoryLayout.Compound) layout).members().get(index);
            start = layout.start();
        }

        return List.of(
                new x8664AddressNullCheckOp(struct),
                new x8664AddrOffsetCalcOp(address, struct, start)
        );
    }
}
