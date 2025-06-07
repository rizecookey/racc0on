package net.rizecookey.racc0on.backend.operation;

import java.util.NavigableMap;
import java.util.Set;

public record OperationSchedule<T extends Operation<?, ?>>(NavigableMap<String, OperationBlock<T>> blocks, OperationBlock<T> entryPoint, Set<OperationBlock<T>> exitPoints) {
}
