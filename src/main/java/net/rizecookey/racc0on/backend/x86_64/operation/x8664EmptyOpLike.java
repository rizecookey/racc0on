package net.rizecookey.racc0on.backend.x86_64.operation;

import java.util.List;

public final class x8664EmptyOpLike implements x8664OpLike {
    public x8664EmptyOpLike() {}

    @Override
    public List<x8664Op> asOperations() {
        return List.of();
    }
}
