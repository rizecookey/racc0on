package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.TernaryNode;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

public class x8664TernaryOp implements x8664Op {
    private final Node condition, out, ifTrue, ifFalse;
    private StoreReference<x8664Store> conditionRef, outRef, ifTrueRef, ifFalseRef;

    public x8664TernaryOp(TernaryNode ternaryNode) {
        this.condition = ternaryNode.predecessor(TernaryNode.CONDITION);
        this.out = ternaryNode;
        this.ifTrue = ternaryNode.predecessor(TernaryNode.IF_TRUE);
        this.ifFalse = ternaryNode.predecessor(TernaryNode.IF_FALSE);

        conditionRef = outRef = ifTrueRef = ifFalseRef = new StoreReference.Null<>();
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
        conditionRef = service.requestInputStore(this, condition);
        outRef = service.requestOutputStore(this, out);
        ifTrueRef = service.requestInputStore(this, ifTrue);
        ifFalseRef = service.requestInputStore(this, ifFalse);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664Store out = storeSupplier.resolve(outRef).orElseThrow();
        x8664Store condition = storeSupplier.resolve(conditionRef).orElseThrow();
        x8664Store ifTrue = storeSupplier.resolve(ifTrueRef).orElseThrow();
        x8664Store ifFalse = storeSupplier.resolve(ifFalseRef).orElseThrow();

        generator.test(condition, condition);

        x8664Store currentOut = out instanceof x8664StackStore ? x8664Register.MEMORY_ACCESS_RESERVE : out;
        generator.write(x8664InstrType.CMOVNZ, currentOut, ifTrue);
        generator.write(x8664InstrType.CMOVZ, currentOut, ifFalse);

        if (currentOut != out) {
            generator.move(out, currentOut);
        }
    }
}
