package net.rizecookey.racc0on.backend.x86_64.operand.store.variable;

import net.rizecookey.racc0on.backend.operand.store.VariableStore;
import net.rizecookey.racc0on.backend.x86_64.operand.store.x8664Store;

public sealed interface x8664VarStore extends VariableStore, x8664Store permits x8664Register, x8664StackStore {
}
