package edu.kit.kastel.vads.compiler.parser;

import edu.kit.kastel.vads.compiler.Span;

public class ParseException extends RuntimeException {
    private final Span span;

    public ParseException(Span span, String message) {
        super(message);
        this.span = span;
    }

    public Span getSpan() {
        return span;
    }
}
