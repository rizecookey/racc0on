package net.rizecookey.racc0on.parser;

import net.rizecookey.racc0on.Span;
import net.rizecookey.racc0on.util.InputErrorException;

public class ParseException extends InputErrorException {
    public ParseException(Span span, String message) {
        super(span, message);
    }
}
