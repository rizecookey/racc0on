package net.rizecookey.racc0on.parser;

import net.rizecookey.racc0on.compilation.InputErrorException;
import net.rizecookey.racc0on.utils.Span;

public final class ParseException extends InputErrorException {
    public ParseException(Span span, String message) {
        super(span, message);
    }
}
