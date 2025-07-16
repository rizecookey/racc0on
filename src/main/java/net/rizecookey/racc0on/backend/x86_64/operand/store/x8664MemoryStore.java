package net.rizecookey.racc0on.backend.x86_64.operand.store;

import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664Register;
import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664StackStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;
import org.jspecify.annotations.Nullable;

public sealed class x8664MemoryStore implements x8664Store permits x8664StackStore {
    private final x8664Operand.Id id;
    private final @Nullable x8664Register base;
    private final @Nullable x8664Register index;
    private final x8664Operand.Size scalar;
    private final int offset;

    public x8664MemoryStore(@Nullable x8664Register base, @Nullable x8664Register index, x8664Operand.Size scalar, int offset) {
        if (x8664Register.RSP.equals(index)) {
            throw new IllegalArgumentException("Index register cannot be RSP");
        }
        this.id = id(base, index, scalar, offset);
        this.base = base;
        this.index = index;
        this.scalar = scalar;
        this.offset = offset;
    }

    public x8664MemoryStore(x8664Register base) {
        this(base, 0);
    }

    public x8664MemoryStore(x8664Register base, int offset) {
        this(base, null, Size.DOUBLE_WORD, offset);
    }

    public x8664MemoryStore(x8664Register base, x8664Register index, x8664Operand.Size scalar) {
        this(base, index, scalar, 0);
    }

    private static Id id(@Nullable x8664Register base, @Nullable x8664Register index, x8664Operand.Size scalar, int offset) {
        String id = "";
        if (base != null) {
            id = base.getId().qwordName();
        }
        if (index != null) {
            if (!id.isEmpty()) {
                id += " + ";
            }
            id += index.getId().qwordName() + " * " + scalar.getByteSize();
        }
        if (offset > 0) {
            if (!id.isEmpty()) {
                id += " + ";
            }
            id += offset;
        } else if (offset < 0) {
            id += " - " + -offset;
        }
        return new x8664Operand.Id("[" + id + "]");
    }

    public @Nullable x8664Register base() {
        return base;
    }

    public @Nullable x8664Register index() {
        return index;
    }

    public x8664Operand.Size scalar() {
        return scalar;
    }

    public int offset() {
        return offset;
    }

    @Override
    public Id getId() {
        return id;
    }
}
