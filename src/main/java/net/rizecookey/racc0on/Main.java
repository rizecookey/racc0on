package net.rizecookey.racc0on;

import net.rizecookey.racc0on.assembler.AssemblerException;
import net.rizecookey.racc0on.compilation.CompilerException;
import net.rizecookey.racc0on.compilation.InputErrorException;
import net.rizecookey.racc0on.compilation.Racc0on;
import net.rizecookey.racc0on.debug.DefaultDebugConsumer;
import net.rizecookey.racc0on.parser.ParseException;
import net.rizecookey.racc0on.semantic.SemanticException;
import net.rizecookey.racc0on.utils.ConsoleColors;
import net.rizecookey.racc0on.utils.Logger;
import net.rizecookey.racc0on.utils.Span;

import java.io.IOException;
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
        System.exit(runCompiler(input, output, DEBUG));
    }

    public static int runCompiler(Path input, Path output, boolean debug) {
        try {
            Racc0on.compileAndAssemble(input, output, debug ? new DefaultDebugConsumer(output, LOGGER) : null);
        } catch (IOException e) {
            LOGGER.prefixedError(e.getMessage(), e);
            return 3;
        } catch (CompilerException e) {
            printInputError(e.input(), e.cause());
            return switch (e.cause()) {
                case ParseException _ -> 42;
                case SemanticException _ -> 7;
            };
        } catch (AssemblerException e) {
            LOGGER.prefixedError("error while assembling: " + e.getMessage());
            if (e.getContext() != null) {
                LOGGER.errorContext(e.getContext());
            }

            return 3;
        }

        return 0;
    }

    private static void printInputError(String program, InputErrorException e) {
        Span span = e.getSpan();
        List<String> lines = Arrays.asList(program.split("(\\r\\n|\\r|\\n)"));

        LOGGER.prefixedError(String.format("line %s, column %s: %s", span.start().line() + 1, span.start().column() + 1, e.getMessage()));
        if (!program.isEmpty()) {
            int contextStart = Math.max(0, span.start().line() - 1);
            int contextEnd = Math.min(lines.size() - 1, span.end().line() + 1);
            if (contextStart > 0) {
                LOGGER.errorContext("...");
            }

            for (int i = contextStart; i <= contextEnd; i++) {
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
            if (contextEnd < lines.size() - 1) {
                LOGGER.errorContext("...");
            }
            LOGGER.errorNewline();
        }

        if (DEBUG) {
            LOGGER.error(ConsoleColors.RED + "Stacktrace:");
            LOGGER.errorContext(e);
            LOGGER.error(ConsoleColors.RESET);
        }
    }
}
