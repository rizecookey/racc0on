package net.rizecookey.racc0on.backend;

public class AssemblerException extends RuntimeException {
    private final int exitCode;
    public AssemblerException(int exitCode, String stdout) {
        super(stdout);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
