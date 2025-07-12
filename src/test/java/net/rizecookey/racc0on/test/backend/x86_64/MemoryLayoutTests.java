package net.rizecookey.racc0on.test.backend.x86_64;

import net.rizecookey.racc0on.backend.memory.MemoryLayout;
import net.rizecookey.racc0on.backend.x86_64.memory.x8664MemoryUtils;
import net.rizecookey.racc0on.ir.memory.MemoryType;
import net.rizecookey.racc0on.ir.node.ValueType;
import net.rizecookey.racc0on.utils.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.FieldSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemoryLayoutTests {
    static final MemoryType.Compound structB = new MemoryType.Compound(List.of(
            new MemoryType.Value(ValueType.BOOL),
            new MemoryType.Value(ValueType.ARRAY),
            new MemoryType.Value(ValueType.INT)
    ));
    static final MemoryLayout.Compound structBLayout = new MemoryLayout.Compound(List.of(
            new MemoryLayout.Value(ValueType.BOOL, 0, 4),
            new MemoryLayout.Value(ValueType.ARRAY, 8, 16),
            new MemoryLayout.Value(ValueType.INT, 16, 20)
    ), 0, 24);

    static final MemoryType.Compound structA = new MemoryType.Compound(List.of(
            new MemoryType.Value(ValueType.INT),
            new MemoryType.Value(ValueType.POINTER),
            structB
    ));
    static final MemoryLayout.Compound structALayout = new MemoryLayout.Compound(List.of(
            new MemoryLayout.Value(ValueType.INT, 0, 4),
            new MemoryLayout.Value(ValueType.POINTER, 8, 16),
            new MemoryLayout.Compound(List.of(
                    new MemoryLayout.Value(ValueType.BOOL, 16, 20),
                    new MemoryLayout.Value(ValueType.ARRAY, 24, 32),
                    new MemoryLayout.Value(ValueType.INT, 32, 36)
            ), 16, 40)
    ), 0, 40);

    static final List<Pair<MemoryType.Compound, MemoryLayout.Compound>> structSamples = List.of(
            new Pair<>(structB, structBLayout),
            new Pair<>(structA, structALayout)
    );

    @ParameterizedTest
    @EnumSource(value = ValueType.class, mode = EnumSource.Mode.EXCLUDE, names = "NONE")
    void testSimpleLayouts(ValueType valueType) {
        MemoryLayout layout = x8664MemoryUtils.createLayout(new MemoryType.Value(valueType));

        assertInstanceOf(MemoryLayout.Value.class, layout);
        assertEquals(0, layout.start());
        assertEquals(x8664MemoryUtils.getValueSize(valueType), layout.end());
    }

    @ParameterizedTest
    @FieldSource(value = "structSamples")
    void testStructLayouts(Pair<MemoryType.Compound, MemoryLayout.Compound> sample) {
        MemoryLayout layout = x8664MemoryUtils.createLayout(sample.first());

        assertEquals(sample.second(), layout);
    }
}
