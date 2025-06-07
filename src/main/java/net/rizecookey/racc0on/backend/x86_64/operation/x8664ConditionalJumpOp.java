package net.rizecookey.racc0on.backend.x86_64.operation;

import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Label;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

import java.util.List;

public class x8664ConditionalJumpOp implements x8664Op {
    private final Node condition;
    private final boolean negate;
    private final String target;
    private StoreReference<x8664Store> inRef;

    public x8664ConditionalJumpOp(Node condition, boolean negate, String target) {
        this.condition = condition;
        this.negate = negate;
        this.target = target;
        inRef = new StoreReference.Null<>();
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
        inRef = service.requestInputStore(this, condition);
    }

    @Override
    public List<String> targetLabels() {
        return List.of(target);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664Store in = storeSupplier.resolve(inRef).orElseThrow();
        generator.test(in, in);
        generator.write(negate ? x8664InstrType.JZ : x8664InstrType.JNZ, new x8664Label(target));
    }
}
