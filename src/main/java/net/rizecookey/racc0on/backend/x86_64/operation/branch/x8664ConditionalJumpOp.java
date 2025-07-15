package net.rizecookey.racc0on.backend.x86_64.operation.branch;

import net.rizecookey.racc0on.backend.store.StoreConditions;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Label;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.node.operation.compare.CompareNode;
import net.rizecookey.racc0on.ir.node.operation.compare.EqNode;
import net.rizecookey.racc0on.ir.node.operation.compare.GreaterNode;
import net.rizecookey.racc0on.ir.node.operation.compare.GreaterOrEqNode;
import net.rizecookey.racc0on.ir.node.operation.compare.LessNode;
import net.rizecookey.racc0on.ir.node.operation.compare.LessOrEqNode;
import net.rizecookey.racc0on.ir.node.operation.compare.NotEqNode;

import java.util.List;

public class x8664ConditionalJumpOp implements x8664Op {
    private final Node condition;
    private final boolean negate;
    private final String target;
    private StoreReference<x8664VarStore> inRef;

    public x8664ConditionalJumpOp(Node condition, boolean negate, String target) {
        this.condition = condition;
        this.negate = negate;
        this.target = target;
        inRef = new StoreReference.Null<>();
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        /* compare operations will not allocate the output by default, so we save one register
           as well as a few operations if the condition is only used by the conditional jump */
        StoreConditions<x8664VarStore> conditions = condition instanceof CompareNode
                ? StoreConditions.noAllocation()
                : StoreConditions.empty();
        inRef = service.requestInputStore(this, condition, conditions);
    }

    @Override
    public List<String> targetLabels() {
        return List.of(target);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664VarStore in = storeSupplier.resolve(inRef).orElse(null);
        x8664InstrType jumpOp;
        if (in != null) {
            generator.test(x8664Operand.Size.BYTE, in, in);
            jumpOp = negate ? x8664InstrType.JZ : x8664InstrType.JNZ;
        } else {
            if (!(condition instanceof CompareNode compareNode)) {
                throw new IllegalStateException("Cannot infer comparison type");
            }
            // condition was tested before this operation, so we can use the arithmetic flags
            jumpOp = switch (compareNode) {
                case EqNode _ -> negate ? x8664InstrType.JNE : x8664InstrType.JE;
                case GreaterNode _ -> negate ? x8664InstrType.JLE : x8664InstrType.JG;
                case GreaterOrEqNode _ -> negate ? x8664InstrType.JL : x8664InstrType.JGE;
                case LessNode _ -> negate ? x8664InstrType.JGE : x8664InstrType.JL;
                case LessOrEqNode _ -> negate ? x8664InstrType.JG : x8664InstrType.JLE;
                case NotEqNode _ -> negate ? x8664InstrType.JE : x8664InstrType.JNE;
            };
        }

        generator.write(jumpOp, new x8664Label(target));
    }
}
