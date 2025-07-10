package net.rizecookey.racc0on.ir.memory;

import net.rizecookey.racc0on.ir.node.ValueType;

import java.util.List;

public sealed interface MemoryType permits MemoryType.Value, MemoryType.Compound {
    record Value(ValueType type) implements MemoryType {}
    record Compound(List<MemoryType> members) implements MemoryType {}
}
