package net.rizecookey.racc0on.backend.memory;

import net.rizecookey.racc0on.ir.node.ValueType;

import java.util.List;

public sealed interface MemoryLayout {
    int start();
    int end();

    default int size() {
        return end() - start();
    }

    record Value(ValueType valueType, int start, int end) implements MemoryLayout {}
    record Compound(List<MemoryLayout> members, int start, int end) implements MemoryLayout {}
}
