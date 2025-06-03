package edu.kit.kastel.vads.compiler.util;

import edu.kit.kastel.vads.compiler.Span;

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
