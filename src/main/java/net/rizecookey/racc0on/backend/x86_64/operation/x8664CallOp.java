package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.store.StoreConditions;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Immediate;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.BuiltinCallNode;
import net.rizecookey.racc0on.ir.node.CallNode;
import net.rizecookey.racc0on.ir.node.ConstBoolNode;
import net.rizecookey.racc0on.ir.node.ConstIntNode;
import net.rizecookey.racc0on.ir.node.Node;
import net.rizecookey.racc0on.ir.util.NodeSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Node argNode = NodeSupport.skipProj(arg);
            if (argNode instanceof ConstIntNode || argNode instanceof ConstBoolNode) {
                // use immediates instead
                argRefs.add(new StoreReference.Null<>());
                continue;
            }
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
        for (var register : CALLER_SAVED_REGS) {
            x8664Store backupStore = storeSupplier.resolve(backupRefs.get(register)).orElseThrow();
            backupStores.put(register, backupStore);
        }

        List<x8664Operand> argumentOperands = new ArrayList<>();
        for (int i = 0; i < argRefs.size(); i++) {
            x8664Operand operand = storeSupplier.resolve(argRefs.get(i)).orElse(null);
            if (operand == null) {
                Node argumentNode = arguments.get(i);
                int immediate = switch (argumentNode) {
                    case ConstBoolNode constBoolNode -> constBoolNode.value() ? 1 : 0;
                    case ConstIntNode constIntNode -> constIntNode.value();
                    default -> throw new IllegalStateException("No value for reference " + argRefs.get(i) + " found");
                };
                operand = new x8664Immediate(immediate);
            }
            argumentOperands.add(operand);
        }

        generator.call(target, out, argumentOperands, backupStores);
    }
}
