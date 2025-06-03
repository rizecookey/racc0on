package edu.kit.kastel.vads.compiler.semantic;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.util.InputErrorException;

public class SemanticException extends InputErrorException {
    public SemanticException(Span span, String message) {
        super(span, message);
    }
}
