package net.rizecookey.racc0on.backend.x86_64.operation;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.function.Function;

public interface x8664Op {
    void write(x8664InstructionGenerator generator, Function<Node, x8664StoreLocation> storeSupplier);
}
