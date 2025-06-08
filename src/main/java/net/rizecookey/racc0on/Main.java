package net.rizecookey.racc0on;

import net.rizecookey.racc0on.assembler.Assembler;
import net.rizecookey.racc0on.assembler.ExternalGcc;
import net.rizecookey.racc0on.compilation.Racc0onCompilation;
import net.rizecookey.racc0on.debug.DebugConsumer;
import net.rizecookey.racc0on.debug.DefaultDebugConsumer;
import net.rizecookey.racc0on.parser.ParseException;
import net.rizecookey.racc0on.semantic.SemanticException;
import net.rizecookey.racc0on.compilation.CompilerException;
import net.rizecookey.racc0on.compilation.InputErrorException;
import net.rizecookey.racc0on.assembler.AssemblerException;
import net.rizecookey.racc0on.utils.ConsoleColors;
import net.rizecookey.racc0on.utils.Logger;
import net.rizecookey.racc0on.utils.Span;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static final Logger LOGGER = new Logger();
    public static final boolean DEBUG = Boolean.parseBoolean(System.getenv("RACC0ON_DEBUG"));

    public static void main(String[] args) {
        if (args.length != 2) {
            LOGGER.prefixedError("invalid arguments: expected one input file and one output file");
            System.exit(3);
        }
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        String inputString;
        try {
            inputString = Files.readString(input);
        } catch (IOException e) {
            LOGGER.prefixedError("failed to read from input file:", e);
            System.exit(3);
            throw new IllegalStateException();
        }

        DebugConsumer debugConsumer = new DefaultDebugConsumer(output, LOGGER);
        Racc0onCompilation compilation = new Racc0onCompilation(inputString, debugConsumer);

        String asmString;
        try {
            asmString = compilation.compile();
        } catch (CompilerException e) {
            printInputError(e.input(), e.cause());
            int exitCode = switch (e.cause()) {
                case ParseException _ -> 42;
                case SemanticException _ -> 7;
            };
            System.exit(exitCode);
            throw new IllegalStateException();
        }

        Assembler assembler = new ExternalGcc();
        byte[] binary;
        try {
            binary = assembler.assemble(asmString);
        } catch (AssemblerException e) {
            LOGGER.prefixedError("error while assembling: " + e.getMessage());
            if (e.getContext() != null) {
                LOGGER.errorContext(e.getContext());
            }
            System.exit(1);
            throw new IllegalStateException();
        }

        try {
            Files.write(output, binary);
            new File(output.toString()).setExecutable(true);
        } catch (IOException e) {
            LOGGER.prefixedError("could not write to output file:", e);
            System.exit(3);
        }
    }

    private static void printInputError(String program, InputErrorException e) {
        Span span = e.getSpan();
        List<String> lines = Arrays.asList(program.split("(\\r\\n|\\r|\\n)"));

        LOGGER.prefixedError(String.format("line %s, column %s: %s", span.start().line() + 1, span.start().column() + 1, e.getMessage()));

        if (span.start().line() - 1 > 0) {
            LOGGER.errorContext("...");
        }

        for (int i = Math.max(0, span.start().line() - 1); i <= Math.min(lines.size() - 1, span.end().line()); i++) {
            String line = lines.get(i);
            LOGGER.errorContext(line);

            if (i < span.start().line() || i > span.end().line()) {
                continue;
            }

            StringBuilder positionMarker = new StringBuilder();
            int startMarking = span.start().line() == i ? span.start().column() : 0;
            int stopMarking = span.end().line() == i ? span.end().column() : line.length() - 1;
            positionMarker
                    .append(ConsoleColors.RED)
                    .repeat(' ', startMarking)
                    .repeat('^', stopMarking - startMarking)
                    .repeat(' ', line.length() - 1 + stopMarking)
                    .append(ConsoleColors.RESET);

            LOGGER.errorContext(positionMarker.toString());
        }
        if (span.end().line() + 1 < lines.size() - 1) {
            LOGGER.errorContext("...");
        }
        LOGGER.errorNewline();

        if (DEBUG) {
            LOGGER.error(ConsoleColors.RED + "Stacktrace:");
            LOGGER.errorContext(e);
            LOGGER.error(ConsoleColors.RESET);
        }
    }
}
