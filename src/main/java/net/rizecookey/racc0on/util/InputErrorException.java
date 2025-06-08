package net.rizecookey.racc0on.util;

import net.rizecookey.racc0on.Span;

public abstract class InputErrorException extends RuntimeException {
    private final Span span;

    protected InputErrorException(Span span, String message) {
        super(message);
        this.span = span;
    }

    public Span getSpan() {
        return span;
    }
}
