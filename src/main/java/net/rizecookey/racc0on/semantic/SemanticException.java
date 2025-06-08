package net.rizecookey.racc0on.semantic;

import net.rizecookey.racc0on.utils.Span;
import net.rizecookey.racc0on.compilation.InputErrorException;

public final class SemanticException extends InputErrorException {
    public SemanticException(Span span, String message) {
        super(span, message);
    }
}
