package net.rizecookey.racc0on.backend.x86_64.operand.store.variable;

import net.rizecookey.racc0on.backend.x86_64.operand.store.x8664MemoryStore;

public final class x8664StackStore extends x8664MemoryStore implements x8664VarStore {
    public x8664StackStore(int rbpRelativeOffset) {
        super(x8664Register.RBP, -rbpRelativeOffset);
    }

    public int rbpRelativeOffset() {
        return -offset();
    }
}
