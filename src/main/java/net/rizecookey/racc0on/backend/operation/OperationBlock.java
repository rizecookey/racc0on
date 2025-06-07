package net.rizecookey.racc0on.backend.operation;

import java.util.List;

public record OperationBlock<T extends Operation<?, ?>>(String label, List<T> operations) {
}
