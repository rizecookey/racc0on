package net.rizecookey.racc0on.backend.x86_64.operation;

import net.rizecookey.racc0on.backend.store.StoreConditions;
import net.rizecookey.racc0on.backend.store.StoreRequestService;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.store.x8664StoreRefResolver;
import net.rizecookey.racc0on.backend.x86_64.x8664InstructionGenerator;
import net.rizecookey.racc0on.ir.node.ParameterNode;

public class x8664LoadParamPhantomOp implements x8664Op {
    private final ParameterNode parameter;

    public x8664LoadParamPhantomOp(ParameterNode parameter) {
        this.parameter = parameter;
    }

    @Override
    public void makeStoreRequests(StoreRequestService<x8664Op, x8664Store> service) {
        x8664Store store;
        int parameterIndex = parameter.index();
        if (parameterIndex < x8664Register.ARGUMENT_REGISTERS.size()) {
            store = x8664Register.ARGUMENT_REGISTERS.get(parameterIndex);
        } else {
            store = new x8664StackStore(-(8 * (parameterIndex - x8664Register.ARGUMENT_REGISTERS.size()) + 16));
        }

        StoreConditions<x8664Store> specificStore = StoreConditions.<x8664Store>builder()
                .targets(store)
                .build();
        // ensure that the parameter is live at the start of a function
        service.requestInputStore(this, parameter, specificStore);
        service.requestOutputStore(this, parameter, specificStore);
    }

    @Override
    public void write(x8664InstructionGenerator generator, x8664StoreRefResolver storeSupplier) {
    }
}
