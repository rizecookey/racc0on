package net.rizecookey.racc0on.backend.x86_64.operand;

import net.rizecookey.racc0on.backend.x86_64.operand.store.x8664Store;

public sealed interface x8664ValOperand extends x8664Operand permits x8664Store, x8664Immediate {
}
