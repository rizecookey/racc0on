package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.operand.Operands;
import net.rizecookey.racc0on.backend.store.StoreConditions;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.instruction.x8664InstrType;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.BiConsumer;

public abstract class x8664OneOperandDoubleWidthMOp implements x8664Op {
    private static final Set<x8664Register> SELF_TAINTED = Set.of(x8664Register.RDX, x8664Register.RAX);

    private final x8664InstrType type;
    private final List<x8664Register> tainted;
    private final x8664Register inData, outData;
    private final Node out, inLeft, inRight;

    private StoreReference<x8664VarStore> outRef, inLeftRef, inRightRef;
    private final List<StoreReference<x8664VarStore>> taintedRefs;

    public x8664OneOperandDoubleWidthMOp(x8664InstrType type, List<x8664Register> tainted,
                                         x8664Register inData, x8664Register outData,
                                         Operands.Binary<Node> operands) {
        this.type = type;
        this.tainted = tainted;
        this.inData = inData;
        this.outData = outData;
        this.out = operands.out();
        this.inLeft = operands.inLeft();
        this.inRight = operands.inRight();

        outRef = inLeftRef = inRightRef = new StoreReference.Null<>();
        taintedRefs = new ArrayList<>();
    }

    private void forEachTaintedBackupPair(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier,
                                          x8664VarStore outStore, BiConsumer<x8664VarStore, x8664VarStore> consumer) {
        SequencedSet<x8664VarStore> live = generator.getLiveStores();
        for (int i = 0; i < tainted.size(); i++) {
            x8664Register taintedReg = tainted.get(i);
            if (!live.contains(taintedReg) || taintedReg.equals(outStore)) {
                continue;
            }

            x8664VarStore backup = storeSupplier.resolve(taintedRefs.get(i)).orElseThrow();
            consumer.accept(taintedReg, backup);
        }
    }

    private void backupTainted(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier, x8664VarStore outStore) {
        forEachTaintedBackupPair(generator, storeSupplier, outStore, (tainted, backup) -> generator.move(x8664Operand.Size.QUAD_WORD, backup, tainted));
    }

    private void restoreTainted(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier, x8664VarStore outStore) {
        forEachTaintedBackupPair(generator, storeSupplier, outStore, (tainted, backup) -> generator.move(x8664Operand.Size.QUAD_WORD, tainted, backup));
    }

    @Override
    public void requestStores(StoreRequestService<x8664Op, x8664VarStore> service) {
        outRef = service.requestOutputStore(this, out);
        inLeftRef = service.requestInputStore(this, inLeft);
        inRightRef = service.requestInputStore(this, inRight);

        StoreConditions<x8664VarStore> backupConditions = StoreConditions.<x8664VarStore>builder()
                .collidesWith(tainted)
                .build();
        for (x8664Register _: tainted) {
            taintedRefs.add(service.requestAdditional(this, backupConditions));
        }
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664VarStore outOp = storeSupplier.resolve(outRef).orElseThrow();
        x8664Operand.Size size = x8664Operand.Size.fromValueType(out.valueType());
        x8664VarStore inLeftOp = storeSupplier.resolve(inLeftRef).orElseThrow();
        x8664VarStore inRightOp = storeSupplier.resolve(inRightRef).orElseThrow();

        backupTainted(generator, storeSupplier, outOp);

        x8664VarStore realRight = inRightOp;
        if (realRight instanceof x8664Register inRightRegister && SELF_TAINTED.contains(inRightRegister)) {
            realRight = x8664Register.MEMORY_ACCESS_RESERVE;
            generator.move(size, realRight, inRightOp);
        }

        if (!inLeftOp.equals(inData)) {
            generator.move(size, inData, inLeftOp);
        }

        generator.write(x8664InstrType.CDQ);
        generator.write(type, size, realRight);
        if (!outOp.equals(outData)) {
            generator.move(size, outOp, outData);
        }

        restoreTainted(generator, storeSupplier, outOp);
    }
}
