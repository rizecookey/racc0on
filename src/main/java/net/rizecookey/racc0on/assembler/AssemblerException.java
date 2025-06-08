package net.rizecookey.racc0on.assembler;

import org.jspecify.annotations.Nullable;

public class AssemblerException extends RuntimeException {
    private final @Nullable String context;

    public AssemblerException(String message) {
        this(message, null);
    }

    public AssemblerException(String message, @Nullable String context) {
        super(message);
        this.context = context;
    }

    public AssemblerException(Exception cause) {
        super(cause);
        this.context = null;
    }

    public @Nullable String getContext() {
        return context;
    }
}
