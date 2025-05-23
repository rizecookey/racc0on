package net.rizecookey.racc0on.backend.x86_64.operand.stored;

import net.rizecookey.racc0on.backend.operand.stored.VariableStore;
import net.rizecookey.racc0on.backend.x86_64.operand.x8664Operand;

public sealed interface x8664Store extends VariableStore, x8664Operand permits x8664Register, x8664StackStore {
}
