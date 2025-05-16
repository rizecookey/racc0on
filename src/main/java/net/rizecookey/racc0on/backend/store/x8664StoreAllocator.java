package net.rizecookey.racc0on.backend.store;

import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StackLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.stored.x8664StoreLocation;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import net.rizecookey.racc0on.backend.x86_64.operation.x8664Op;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* TODO: Deal with instructions requiring special registers */
public class x8664StoreAllocator {
    private static final int STACK_STORE_SIZE = x8664Operand.Size.DOUBLE_WORD.getByteSize();

    private final Map<StoreReference<x8664StoreLocation>, x8664StoreLocation> allocations = new HashMap<>();

    public record Allocation(Map<StoreReference<x8664StoreLocation>, x8664StoreLocation> allocations, int stackSize) {}

    public Allocation allocate(List<x8664Op> sequentialProgram, StoreRequests<x8664Op, x8664StoreLocation> storeRequests) {
        LivenessMap<x8664Op, x8664StoreLocation> liveness = LivenessMap.calculateFor(sequentialProgram, storeRequests);
        InterferenceGraph<x8664Op, x8664StoreLocation> interference = InterferenceGraph.createFrom(sequentialProgram, liveness, storeRequests);
        Map<StoreReference<x8664StoreLocation>, Integer> coloring = interference.createColoring();
        int maxColor = coloring.values().stream().max(Integer::compareTo).orElseThrow();

        List<x8664StoreLocation> availableLocations = new ArrayList<>(x8664Register.getRegisterSet()
                .stream()
                .filter(x8664Register::isGeneralPurpose)
                .toList());
        int availableRegisters = availableLocations.size();
        int requiredRegisters = maxColor + 1 - availableRegisters;
        for (int i = 0; i < requiredRegisters; i++) {
            availableLocations.add(new x8664StackLocation((i + 1) * STACK_STORE_SIZE));
        }

        for (var store : coloring.keySet()) {
            allocations.put(store, availableLocations.get(coloring.get(store)));
        }

        return new Allocation(Map.copyOf(allocations), Math.max(0, requiredRegisters * STACK_STORE_SIZE));
    }
}
