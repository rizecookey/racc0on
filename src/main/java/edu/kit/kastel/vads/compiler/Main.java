package edu.kit.kastel.vads.compiler;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.SsaTranslation;
import edu.kit.kastel.vads.compiler.ir.optimize.LocalValueNumbering;
import edu.kit.kastel.vads.compiler.lexer.Lexer;
import edu.kit.kastel.vads.compiler.parser.ParseException;
import edu.kit.kastel.vads.compiler.parser.Parser;
import edu.kit.kastel.vads.compiler.parser.TokenSource;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.semantic.SemanticAnalysis;
import edu.kit.kastel.vads.compiler.semantic.SemanticException;
import net.rizecookey.racc0on.backend.AssemblerException;
import net.rizecookey.racc0on.backend.x86_64.x8664CodeGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Invalid arguments: Expected one input file and one output file");
            System.exit(3);
        }
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        ProgramTree program = lexAndParse(input);
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
            graphs.add(translation.translate());
        }

        String s = new x8664CodeGenerator().generateCode(graphs);
        try {
            callAssembler(s, output);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (AssemblerException e) {
            System.err.println("Assembler failed with error code " + e.getExitCode() + ":");
            System.err.println(e.getMessage());
        }
    }

    private static ProgramTree lexAndParse(Path input) throws IOException {
        try {
            Lexer lexer = Lexer.forString(Files.readString(input));
            TokenSource tokenSource = new TokenSource(lexer);
            Parser parser = new Parser(tokenSource);
            return parser.parseProgram();
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(42);
            throw new AssertionError("unreachable");
        }
    }

    private static void callAssembler(String assembly, Path output) throws IOException, AssemblerException {
        Process gcc = Runtime.getRuntime().exec(new String[] {"gcc", "-Wl,--entry=_entry", "-o", output.toString(), "-x", "assembler", "-"});
        var writer = new OutputStreamWriter(gcc.getOutputStream());
        writer.write(assembly);
        writer.close();

        while (gcc.isAlive()) {
            try {
                gcc.waitFor();
            } catch (InterruptedException _) {}
        }

        if (gcc.exitValue() != 0) {
            StringBuilder errorLines = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(gcc.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!errorLines.isEmpty()) {
                    errorLines.append("\n");
                }
                errorLines.append(line);
            }
            reader.close();
            throw new AssemblerException(gcc.exitValue(), errorLines.toString());
        }
    }
}