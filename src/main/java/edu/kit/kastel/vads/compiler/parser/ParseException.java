package edu.kit.kastel.vads.compiler.parser;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.util.InputErrorException;

public class ParseException extends InputErrorException {
    public ParseException(Span span, String message) {
        super(span, message);
    }
}
