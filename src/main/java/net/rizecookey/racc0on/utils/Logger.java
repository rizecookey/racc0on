package net.rizecookey.racc0on.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class Logger {
    public void writeLog(String message) {
        System.out.println(message);
    }

    public void writeLog(String message, String context) {
        writeLog(message);
        System.out.println(indent(2, context));
    }

    public void writeError(String message) {
        errorPrefix();
        errorMessage(message);
    }

    public void writeError(String message, String context) {
        writeError(message);
        writeErrorContext(context);
    }

    public void writeError(String message, Exception e) {
        writeError(message);
        writeErrorContext(e);
    }

    public void writeErrorAdditional(String message) {
        System.err.println(message);
    }

    private void errorPrefix() {
        System.err.print(ConsoleColors.RED_BOLD + "ERROR: " + ConsoleColors.RESET);
    }

    private void errorMessage(String message) {
        System.err.println(ConsoleColors.YELLOW + message + ConsoleColors.RESET);
    }

    public void writeErrorContext(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter print = new PrintWriter(sw);
        e.printStackTrace(print);
        print.close();
        writeErrorContext(sw.toString());
    }

    public void writeErrorContext(String context) {
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
