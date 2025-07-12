package net.rizecookey.racc0on.backend.operation;

import net.rizecookey.racc0on.backend.operand.Operand;
import net.rizecookey.racc0on.backend.operand.store.VariableStore;

import java.util.List;

public interface Operation<T extends Operand, U extends VariableStore> {
    default List<String> targetLabels() {
        return List.of();
    }
}
