package net.rizecookey.racc0on.compilation;

import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.parser.ParseException;
import net.rizecookey.racc0on.semantic.SemanticException;

public sealed abstract class InputErrorException extends RuntimeException permits ParseException, SemanticException {
    private final Span span;

    protected InputErrorException(Span span, String message) {
        super(message);
        this.span = span;
    }

    public Span getSpan() {
        return span;
    }
}
