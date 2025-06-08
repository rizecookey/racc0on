package net.rizecookey.racc0on.ir.util;

import net.rizecookey.racc0on.utils.Span;

/// Provides information to ease debugging
public sealed interface DebugInfo {
    enum NoInfo implements DebugInfo {
        INSTANCE
    }

    record SourceInfo(Span span) implements DebugInfo {}
}
