package edu.kit.kastel.vads.compiler;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.SsaTranslation;
import edu.kit.kastel.vads.compiler.ir.optimize.LocalValueNumbering;
import edu.kit.kastel.vads.compiler.ir.util.GraphVizPrinter;
import edu.kit.kastel.vads.compiler.ir.util.YCompPrinter;
import edu.kit.kastel.vads.compiler.lexer.Lexer;
import edu.kit.kastel.vads.compiler.parser.ParseException;
import edu.kit.kastel.vads.compiler.parser.Parser;
import edu.kit.kastel.vads.compiler.parser.Printer;
import edu.kit.kastel.vads.compiler.parser.TokenSource;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.semantic.SemanticAnalysis;
import edu.kit.kastel.vads.compiler.semantic.SemanticException;
import net.rizecookey.racc0on.backend.AssemblerException;
import net.rizecookey.racc0on.backend.x86_64.x8664CodeGenerator;
import net.rizecookey.racc0on.utils.ConsoleColors;
import net.rizecookey.racc0on.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static final Logger LOGGER = new Logger();
    public static final boolean DEBUG = Boolean.parseBoolean(System.getenv("RACC0ON_DEBUG"));

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            LOGGER.prefixedError("invalid arguments: expected one input file and one output file");
            System.exit(3);
        }
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        String outputFileName = output.getFileName().toString();

        ProgramTree program = lexAndParse(input);
        if (DEBUG) {
            String parsedProgram = Printer.print(program);
            LOGGER.log("Parsed program: ", parsedProgram);
            writeDebugFile(output, outputFileName + ".parsed.l2", parsedProgram);
        }
        try {
            new SemanticAnalysis(program).analyze();
        } catch (SemanticException e) {
            e.printStackTrace();
            System.exit(7);
            return;
        }
        List<IrGraph> graphs = new ArrayList<>();
        for (FunctionTree function : program.topLevelTrees()) {
            SsaTranslation translation = new SsaTranslation(function, new LocalValueNumbering());
            var graph = translation.translate();

            if (DEBUG) {
                String graphString = GraphVizPrinter.print(graph);
                writeDebugFile(output, "graphs/" + outputFileName + "." + graph.name() + ".dot", graphString);

                String vcgString = YCompPrinter.print(graph);
                writeDebugFile(output, "graphs/" + outputFileName + "." + graph.name() + ".vcg", vcgString);
            }

            graphs.add(graph);
        }

        String s = new x8664CodeGenerator().generateCode(graphs);

        if (DEBUG) {
            LOGGER.log("Generated assembly:", s);
            writeDebugFile(output, outputFileName + ".s", s);
        }

        try {
            callAssembler(s, output);
        } catch (IOException e) {
            LOGGER.prefixedError("could not call gcc:", e);
            System.exit(1);
        } catch (AssemblerException e) {
            LOGGER.prefixedError("assembler failed with error code " + e.getExitCode() + ":", e.getMessage());
            System.exit(1);
        }
    }

    private static void writeDebugFile(Path outputBin, String name, String content) throws IOException {
        Path file = outputBin.getParent().resolve(name);
        Path dir = file.getParent();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Files.writeString(file, content);
    }

    private static ProgramTree lexAndParse(Path input) throws IOException {
        String programString = Files.readString(input);
        try {
            Lexer lexer = Lexer.forString(programString);
            TokenSource tokenSource = new TokenSource(lexer);
            Parser parser = new Parser(tokenSource);

            return parser.parseProgram();
        } catch (ParseException e) {
            printParserError(programString, e);
            System.exit(42);
            throw new AssertionError("unreachable");
        }
    }

    private static void printParserError(String program, ParseException e) {
        Span span = e.getSpan();
        List<String> lines = Arrays.asList(program.split("(\\r\\n|\\r|\\n)"));

        LOGGER.prefixedError(String.format("line %s, column %s: %s%n", span.start().line() + 1, span.start().column() + 1, e.getMessage()));

        for (int i = span.start().line(); i <= span.end().line(); i++) {
            String line = lines.get(i);
            LOGGER.errorContext(line);
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
        LOGGER.errorNewline();

        if (DEBUG) {
            LOGGER.error(ConsoleColors.RED + "Stacktrace:");
            LOGGER.errorContext(e);
            LOGGER.error(ConsoleColors.RESET);
        }
    }

    private static void callAssembler(String assembly, Path output) throws IOException, AssemblerException {
        Process gcc = Runtime.getRuntime().exec(new String[] {"gcc",
                "-Wl,--entry=" + x8664CodeGenerator.ENTRYPOINT_NAME,
                "-o", output.toString(),
                "-x", "assembler",
                "-m64",
                "-"});

        if (gcc.isAlive()) {
            var writer = gcc.outputWriter();
            writer.write(assembly);
            writer.close();
        }

        while (gcc.isAlive()) {
            try {
                gcc.waitFor();
            } catch (InterruptedException _) {}
        }

        if (gcc.exitValue() != 0) {
            StringBuilder errorLines = new StringBuilder();
            BufferedReader reader = gcc.errorReader();

            String line;
            while ((line = reader.readLine()) != null) {
                if (!errorLines.isEmpty()) {
                    errorLines.append("\n");
                }
                errorLines.append("  ").append(line);
            }
            reader.close();
            throw new AssemblerException(gcc.exitValue(), errorLines.toString());
        }
    }
}
