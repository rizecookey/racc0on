package net.rizecookey.racc0on.semantic;

import net.rizecookey.racc0on.Span;
import net.rizecookey.racc0on.util.InputErrorException;

public class SemanticException extends InputErrorException {
    public SemanticException(Span span, String message) {
        super(span, message);
    }
}
