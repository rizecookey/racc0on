package net.rizecookey.racc0on.backend.regalloc;

import net.rizecookey.racc0on.ir.IrGraph;
import net.rizecookey.racc0on.ir.node.Node;

import java.util.Map;

public interface RegisterAllocator {

    Map<Node, Register> allocateRegisters(IrGraph graph);
}
