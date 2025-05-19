package net.rizecookey.racc0on.backend.x86_64.operation;

import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;

public class x8664TwoOperandRmMrOrMiOp implements x8664Op {
    private final x8664InstrType type;
    private final Node out, inLeft, inRight;
    private StoreReference<x8664Store> outRef, inLeftRef, inRightRef;

    public x8664TwoOperandRmMrOrMiOp(x8664InstrType type, Operands.Binary<Node> operands) {
        this.type = type;
        this.out = operands.out();
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();
        outRef = inLeftRef = inRightRef = new StoreReference.Null<>();
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
        outRef = service.requestOutputStore(this, out);
        if (!(inLeft instanceof ConstIntNode)) {
            inLeftRef = service.requestInputStore(this, inLeft);
        }

        if (!(inRight instanceof ConstIntNode)) {
            inRightRef = service.requestInputStore(this, inRight);
        }
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664Store outOp = storeSupplier.resolve(outRef).orElseThrow();
        x8664Operand inLeftOp = inLeft instanceof ConstIntNode constNode
                ? new x8664Immediate(constNode.value())
                : storeSupplier.resolve(inLeftRef).orElseThrow();
        x8664Operand inRightOp = inRight instanceof ConstIntNode constNode
                ? new x8664Immediate(constNode.value())
                : storeSupplier.resolve(inRightRef).orElseThrow();

        write(generator, type, outOp, inLeftOp, inRightOp);
    }

    public static void write(x8664InstructionGenerator generator, x8664InstrType type, x8664Store outOp, x8664Operand inLeftOp, x8664Operand inRightOp) {
        if (outOp instanceof x8664StackStore && (inLeftOp instanceof x8664StackStore || inRightOp instanceof x8664StackStore)) {
            generator.move(x8664Register.MEMORY_ACCESS_RESERVE, inLeftOp);
            generator.write(type, x8664Register.MEMORY_ACCESS_RESERVE, inRightOp);
            generator.move(outOp, x8664Register.MEMORY_ACCESS_RESERVE);
        } else {
            x8664Operand actualRight = inRightOp;
            if (!outOp.equals(inLeftOp)) {
                if (outOp.equals(inRightOp)) {
                    actualRight = x8664Register.MEMORY_ACCESS_RESERVE;
                    generator.move(x8664Register.MEMORY_ACCESS_RESERVE, inRightOp);
                }
                generator.move(outOp, inLeftOp);
            }

            generator.write(type, outOp, actualRight);
        }
    }
}
