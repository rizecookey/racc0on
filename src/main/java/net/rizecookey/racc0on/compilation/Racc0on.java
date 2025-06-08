package net.rizecookey.racc0on.compilation;

import net.rizecookey.racc0on.assembler.Assembler;
import net.rizecookey.racc0on.assembler.AssemblerException;
import net.rizecookey.racc0on.assembler.ExternalGcc;
import net.rizecookey.racc0on.debug.DebugConsumer;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Racc0on {
    private Racc0on() {}

    public static String compile(String input) throws CompilerException {
        return compile(input, null);
    }

    public static String compile(String input, @Nullable DebugConsumer debugConsumer) {
        return new Racc0onCompilation(input, debugConsumer).compile();
    }

    public static void compileAndAssemble(Path input, Path output) throws CompilerException, AssemblerException, IOException {
        compileAndAssemble(input, output, null);
    }

    public static void compileAndAssemble(Path input, Path output, @Nullable DebugConsumer debugConsumer) throws CompilerException, AssemblerException, IOException {
        String inputString;
        try {
            inputString = Files.readString(input);
        } catch (IOException e) {
            throw new IOException("failed to read from input file:", e);
        }

        String asmString = compile(inputString, debugConsumer);

        Assembler assembler = new ExternalGcc();
        byte[] binary = assembler.assemble(asmString);

        try {
            Files.write(output, binary);
            new File(output.toString()).setExecutable(true);
        } catch (IOException e) {
            throw new IOException("could not write to output file", e);
        }
    }
}
