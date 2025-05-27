package net.rizecookey.racc0on.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class Logger {
    public void log(String message) {
        System.out.println(message);
    }

    public void log(String message, String context) {
        log(message);
        System.out.println(indent(2, context));
    }

    public void logContext(String context) {
        System.out.println(indent(2, context));
    }

    public void error(String message) {
        System.err.println(message);
    }

    public void prefixedError(String message) {
        errorPrefix();
        errorMessage(message);
    }

    public void prefixedError(String message, String context) {
        prefixedError(message);
        errorContext(context);
    }

    public void prefixedError(String message, Exception e) {
        prefixedError(message);
        errorContext(e);
    }

    private void errorPrefix() {
        System.err.print(ConsoleColors.RED_BOLD + "ERROR: " + ConsoleColors.RESET);
    }

    private void errorMessage(String message) {
        System.err.println(ConsoleColors.YELLOW + message + ConsoleColors.RESET);
    }

    public void errorContext(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter print = new PrintWriter(sw);
        e.printStackTrace(print);
        print.close();
        errorContext(sw.toString());
    }

    public void errorContext(String context) {
        System.err.println(indent(2, context));
    }

    public void errorNewline() {
        System.err.println();
    }

    private static String indent(int amount, String multiline) {
        String indent = new StringBuilder().repeat(' ', amount).toString();
        return Arrays.stream(multiline.split("(\\r\\n|\\r|\\n)"))
                .map(string -> indent + string)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
