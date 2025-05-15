package net.rizecookey.racc0on.backend.instruction;

import net.rizecookey.racc0on.backend.operand.stored.VariableStore;
import net.rizecookey.racc0on.utils.CastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

public class InstructionStream<T extends Instruction<T, ?, U>, U extends VariableStore> {
    private final List<T> instructions;
    private final Map<InstructionType<T, U>, NavigableSet<Integer>> instructionLocations = new HashMap<>();
    private final Map<U, NavigableSet<Integer>> storeReferences;
    private final Map<U, NavigableSet<Integer>> writeStoreReferences;
    private final Class<U> storeType;
    private boolean dirty;
    private int lowestModifiedIndex = 0;

    public InstructionStream(Class<U> storeType) {
        this.instructions = new ArrayList<>();
        this.storeReferences = new HashMap<>();
        this.writeStoreReferences = new HashMap<>();
        this.storeType = storeType;
        this.dirty = false;
    }

    public InstructionStream(List<T> instructions, Class<U> storeType) {
        this(storeType);
        for (T instruction : instructions) {
            add(instruction);
        }

        resetDirtyMark();
    }

    public boolean isMarkedDirty() {
        return dirty;
    }

    public int getLowestModifiedIndex() {
        return !dirty ? -1 : lowestModifiedIndex;
    }

    public void resetDirtyMark() {
        this.dirty = false;
    }

    public T get(int index) {
        return instructions.get(index);
    }

    public NavigableSet<Integer> getReferences(U store) {
        return storeReferences.containsKey(store) ? storeReferences.get(store) : new TreeSet<>();
    }

    public NavigableSet<Integer> getWriteReferences(U store) {
        return writeStoreReferences.containsKey(store) ? writeStoreReferences.get(store) : new TreeSet<>();
    }

    public NavigableSet<Integer> getInstructionLocations(InstructionType<T, U> type) {
        return instructionLocations.containsKey(type) ? instructionLocations.get(type) : new TreeSet<>();
    }

    public void remove(int index) {
        if (index < 0 || index >= instructions.size()) {
            return;
        }

        for (U store : storeReferences.keySet()) {
            NavigableSet<Integer> references = storeReferences.get(store);
            references.remove(index);
        }

        for (U store : writeStoreReferences.keySet()) {
            NavigableSet<Integer> references = writeStoreReferences.get(store);
            references.remove(index);
        }

        T instruction = instructions.get(index);
        if (instructionLocations.containsKey(instruction.type())) {
            instructionLocations.get(instruction.type()).remove(index);
        }

        instructions.remove(index);
        lowestModifiedIndex = index;
        dirty = true;
    }

    public int add(T instr) {
        return add(instructions.size(), instr);
    }

    public int add(int index, T instr) {
        if (index < 0 || index > instructions.size()) {
            throw new IllegalArgumentException("Index out of bounds");
        }

        instructions.add(index, instr);
        instructionLocations.computeIfAbsent(instr.type(), _ -> new TreeSet<>()).add(index);
        instr.operands().stream()
                .map(val -> CastUtils.safeGenericCast(val, storeType))
                .filter(Objects::nonNull)
                .forEach(store -> {
                   storeReferences.computeIfAbsent(store, _ -> new TreeSet<>()).add(index);
                   if (instr.type().getOverridenStores(instr).contains(store)) {
                       writeStoreReferences.computeIfAbsent(store, _ -> new TreeSet<>()).add(index);
                   }
                });

        dirty = true;
        lowestModifiedIndex = index;
        return index;
    }

    public int size() {
        return instructions.size();
    }

    public List<T> toInstructionList() {
        return List.copyOf(instructions);
    }
}
