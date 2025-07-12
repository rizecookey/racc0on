package net.rizecookey.racc0on.backend.x86_64.memory;

import net.rizecookey.racc0on.backend.memory.MemoryLayout;
import net.rizecookey.racc0on.ir.memory.MemoryType;
import net.rizecookey.racc0on.ir.node.ValueType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class x8664MemoryUtils {
    private x8664MemoryUtils() {}

    public static MemoryLayout createLayout(MemoryType type) {
        return createLayout(type, 0);
    }

    public static MemoryLayout createLayout(MemoryType type, int suggestedStart) {
        int start = suggestedStart;
        int alignment = getAlignment(type);
        int misalignment = start % alignment;
        if (misalignment != 0) {
            start += alignment - misalignment;
        }

        return switch (type) {
            case MemoryType.Value(ValueType valueType) ->
                    new MemoryLayout.Value(valueType, start, start + alignment);
            case MemoryType.Compound compound -> createCompoundLayout(compound, suggestedStart, alignment);
        };
    }

    public static int getValueSize(ValueType type) {
        return switch (type) {
            case NONE -> 0;
            case INT, BOOL -> 4;
            case POINTER, ARRAY -> 8;
        };
    }

    public static int getAlignment(MemoryType type) {
        return switch (type) {
            case MemoryType.Compound compound -> compound.members().stream()
                    .map(x8664MemoryUtils::getAlignment)
                    .max(Integer::compareTo)
                    .orElse(1);
            case MemoryType.Value value -> getValueSize(value.type());
        };
    }

    private static MemoryLayout.Compound createCompoundLayout(MemoryType.Compound compound, int structStart, int structAlignment) {
        List<MemoryLayout> layoutMembers = new ArrayList<>();
        int memberStart = structStart;
        for (MemoryType member : compound.members()) {
            MemoryLayout memberLayout = createLayout(member, memberStart);
            layoutMembers.add(memberLayout);
            memberStart = memberLayout.end();
        }

        int endMisalignment = memberStart % structAlignment;
        if (endMisalignment != 0) {
            memberStart += structAlignment - endMisalignment;
        }

        return new MemoryLayout.Compound(Collections.unmodifiableList(layoutMembers), structStart, memberStart);
    }
}
