package net.rizecookey.racc0on.backend.operation;

import java.util.SequencedMap;
import java.util.Set;

public record OperationSchedule<T extends Operation<?, ?>>(SequencedMap<String, OperationBlock<T>> blocks, OperationBlock<T> entryPoint, Set<OperationBlock<T>> exitPoints) {
}
