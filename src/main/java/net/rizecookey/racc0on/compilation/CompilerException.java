package net.rizecookey.racc0on.compilation;

public class CompilerException extends RuntimeException {
    private final String input;
    private final InputErrorException cause;

    public CompilerException(String input, InputErrorException cause) {
        super(cause);
        this.input = input;
        this.cause = cause;
    }

    public String input() {
        return input;
    }

    public InputErrorException cause() {
        return cause;
    }
}
