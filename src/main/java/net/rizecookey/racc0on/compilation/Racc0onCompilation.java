package net.rizecookey.racc0on.compilation;

import net.rizecookey.racc0on.backend.x86_64.x8664CodeGenerator;
import net.rizecookey.racc0on.debug.DebugConsumer;
import net.rizecookey.racc0on.ir.IrGraph;
import net.rizecookey.racc0on.ir.SsaTranslation;
import net.rizecookey.racc0on.ir.optimize.NodeOptimizations;
import net.rizecookey.racc0on.ir.util.GraphVizPrinter;
import net.rizecookey.racc0on.ir.util.YCompPrinter;
import net.rizecookey.racc0on.lexer.Lexer;
import net.rizecookey.racc0on.parser.ParseException;
import net.rizecookey.racc0on.parser.Parser;
import net.rizecookey.racc0on.parser.Printer;
import net.rizecookey.racc0on.parser.TokenSource;
import net.rizecookey.racc0on.parser.ast.FunctionTree;
import net.rizecookey.racc0on.parser.ast.ProgramTree;
import net.rizecookey.racc0on.semantic.SemanticAnalysis;
import net.rizecookey.racc0on.semantic.SemanticException;
import net.rizecookey.racc0on.semantic.SemanticInformation;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Racc0onCompilation {
    private final String input;
    private final @Nullable DebugConsumer debugConsumer;

    public Racc0onCompilation(String input) {
        this(input, null);
    }

    public Racc0onCompilation(String input, @Nullable DebugConsumer debugConsumer) {
        this.input = input;
        this.debugConsumer = debugConsumer;
    }

    public String getInput() {
        return input;
    }

    public String compile() throws CompilerException {
        ProgramTree program;
        try {
            program = lexAndParse(input);
        } catch (ParseException e) {
            throw new CompilerException(input, e);
        }
        debug(consumer -> consumer.artifactInfo("parsed-program", "Parsed program",
                debugFile(name -> name + ".parsed.l2"), Printer.print(program)));

        SemanticInformation information;
        try {
            information = runSemanticAnalysis(program);
        } catch (SemanticException e) {
            throw new CompilerException(input, e);
        }

        List<IrGraph> programInSsaForm = translateToSsa(program, information);
        debug(consumer -> {
            for (IrGraph procedure : programInSsaForm) {
                consumer.artifact("graphviz-graph-" + procedure.name(),
                        debugFile(name -> "graphs/" + name + "." + procedure.name() + ".dot"),
                        GraphVizPrinter.print(procedure));

                consumer.artifact("ycomp-graph-" + procedure.name(),
                        debugFile(name -> "graphs/" + name + "." + procedure.name() + ".vcg"),
                        YCompPrinter.print(procedure));
            }
        });

        String assemblyString = generateAssembly(programInSsaForm);
        debug(consumer -> consumer.artifactInfo("generated-assembly", "Generated assembly",
                debugFile(name -> name + ".s"), assemblyString));

        return assemblyString;
    }

    private ProgramTree lexAndParse(String programString) throws ParseException {
        Lexer lexer = Lexer.forString(programString);
        TokenSource tokenSource = new TokenSource(lexer);
        Parser parser = new Parser(tokenSource);

        return parser.parseProgram();
    }

    private SemanticInformation runSemanticAnalysis(ProgramTree program) throws SemanticException {
        return new SemanticAnalysis(program).analyze();
    }

    private List<IrGraph> translateToSsa(ProgramTree program, SemanticInformation semanticInfo) {
        List<IrGraph> functions = new ArrayList<>();

        for (FunctionTree function : program.functions()) {
            SsaTranslation translator = new SsaTranslation(function, new NodeOptimizations(), semanticInfo);
            functions.add(translator.translate());
        }

        return functions;
    }

    private String generateAssembly(List<IrGraph> program) {
        return new x8664CodeGenerator().generateCode(program);
    }

    private void debug(Consumer<DebugConsumer> consumer) {
        if (debugConsumer == null) {
            return;
        }

        consumer.accept(debugConsumer);
    }

    private static Function<Path, Path> debugFile(Function<String, String> fileNameSpecifier) {
        return out -> out.getParent().resolve(fileNameSpecifier.apply(out.getFileName().toString()));
    }
}
