package net.rizecookey.racc0on.backend.x86_64.operand.store;

import net.rizecookey.racc0on.backend.x86_64.operand.store.variable.x8664VarStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664ValOperand;

public sealed interface x8664Store extends x8664ValOperand permits x8664VarStore, x8664MemoryStore {
}
