package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.store.StoreConditions;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.BuiltinCallNode;
import net.rizecookey.racc0on.ir.node.CallNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.util.NodeSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class x8664CallOp implements x8664Op {
    private static final List<x8664Register> CALLER_SAVED_REGS = x8664Register.getRegisterSet().stream()
            .filter(x8664Register::isCallerSaved)
            .toList();

    private final String target;
    private final List<Node> arguments;
    private final Node out;
    private final List<StoreReference<x8664Store>> argRefs = new ArrayList<>();
    private StoreReference<x8664Store> outRef = new StoreReference.Null<>();
    private final Map<x8664Register, StoreReference<x8664Store>> backupRefs = new HashMap<>();

    public x8664CallOp(CallNode callNode) {
        this.target = callNode.target();
        this.arguments = List.copyOf(callNode.arguments());
        this.out = callNode;
    }

    public x8664CallOp(BuiltinCallNode builtinCallNode) {
        this.target = "$" + builtinCallNode.type().keyword() + "$";
        this.arguments = List.copyOf(builtinCallNode.arguments());
        this.out = builtinCallNode;
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
        for (var arg : arguments) {
            // TODO only backup necessary registers?
            argRefs.add(service.requestInputStore(this, NodeSupport.skipProj(arg)));
        }

        outRef = service.requestOutputStore(this, out);

        StoreConditions<x8664Store> notCallerSaved = StoreConditions.<x8664Store>builder()
                .collidesWith(CALLER_SAVED_REGS)
                .build();
        for (var reg : CALLER_SAVED_REGS) {
            backupRefs.put(reg, service.requestAdditional(this, notCallerSaved));
        }
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
        x8664Store out = storeSupplier.resolve(outRef).orElseThrow();

        Map<x8664Register, x8664Store> backupStores = new HashMap<>();
        // TODO only backup live registers
        for (var register : CALLER_SAVED_REGS) {
            x8664Store backupStore = storeSupplier.resolve(backupRefs.get(register)).orElseThrow();
            backupStores.put(register, backupStore);
            generator.move(backupStore, register, x8664Operand.Size.QUAD_WORD);
        }

        List<x8664Store> arguments = argRefs.stream()
                .map(storeSupplier::resolve)
                .map(Optional::orElseThrow)
                .toList();
        generator.call(target, arguments, backupStores);

        for (var register : CALLER_SAVED_REGS) {
            if (register.equals(out)) {
                continue;
            }
            if (register.equals(x8664Register.RAX)) {
                generator.move(out, x8664Register.RAX, x8664Operand.Size.QUAD_WORD);
            }
            x8664Store backupStore = backupStores.get(register);
            generator.move(register, backupStore, x8664Operand.Size.QUAD_WORD);
        }

    }
}
