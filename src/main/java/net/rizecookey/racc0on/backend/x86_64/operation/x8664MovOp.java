package net.rizecookey.racc0on.backend.x86_64.operation;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.function.Function;

public record x8664MovOp(x8664StoreLocation to, x8664Operand from) implements x8664Op {
    @Override
    public void write(x8664InstructionGenerator generator, Function<Node, x8664StoreLocation> storeSupplier) {
        x8664Operand actualFrom = from;
        if (to instanceof x8664StackLocation && from instanceof x8664StackLocation) {
            actualFrom = x8664Register.MEMORY_ACCESS_RESERVE;
            generator.write(x8664InstrType.MOV, x8664Register.MEMORY_ACCESS_RESERVE, from);
        }

        generator.write(x8664InstrType.MOV, to, actualFrom);
    }
}
