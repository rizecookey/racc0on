package net.rizecookey.racc0on.backend.x86_64.store;

import net.rizecookey.racc0on.backend.operation.OperationSchedule;
import net.rizecookey.racc0on.backend.store.InterferenceGraph;
import net.rizecookey.racc0on.backend.store.LivenessMap;
import net.rizecookey.racc0on.backend.store.StoreReference;
import net.rizecookey.racc0on.backend.store.StoreRequests;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Store;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class x8664StoreAllocator {
    private static final int STACK_STORE_SIZE = x8664Operand.Size.DOUBLE_WORD.getByteSize();

    public record Allocation(LivenessMap<x8664Op, x8664Store> livenessMap, Map<StoreReference<x8664Store>, x8664Store> allocations, int stackSize) {}

    public Allocation allocate(OperationSchedule<x8664Op> schedule, StoreRequests<x8664Op, x8664Store> storeRequests) {
        LivenessMap<x8664Op, x8664Store> liveness = LivenessMap.calculateFor(schedule, storeRequests);
        InterferenceGraph<x8664Op, x8664Store> interference = InterferenceGraph.createFrom(schedule, liveness, storeRequests);
        List<x8664Store> availableLocations = new ArrayList<>(x8664Register.getRegisterSet()
                .stream()
                .filter(x8664Register::isGeneralPurpose)
                .toList());

        var stackAllocator = new Supplier<x8664Store>() {
            private int size = 0;

            @Override
            public x8664Store get() {
                return new x8664StackStore(++size * STACK_STORE_SIZE);
            }
        };

        Map<StoreReference<x8664Store>, x8664Store> coloring = interference.createColoring(availableLocations, stackAllocator);
        return new Allocation(liveness, Map.copyOf(coloring), stackAllocator.size * STACK_STORE_SIZE);
    }
}
