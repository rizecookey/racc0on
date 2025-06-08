package net.rizecookey.racc0on.test;

import net.rizecookey.racc0on.assembler.Assembler;
import net.rizecookey.racc0on.assembler.AssemblerException;
import net.rizecookey.racc0on.assembler.ExternalGcc;
import net.rizecookey.racc0on.compilation.Racc0onCompilation;
import net.rizecookey.racc0on.debug.DebugConsumer;
import net.rizecookey.racc0on.debug.DefaultDebugConsumer;
import net.rizecookey.racc0on.utils.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SamplesTest {
    Logger logger = new Logger();
    Assembler assembler = new ExternalGcc();

    Process actual;
    Process expected;

    @ParameterizedTest
    @MethodSource("provideSampleFiles")
    void runSamples(Path sample) throws IOException, InterruptedException {
        String outputName = sample.getFileName().toString();
        outputName = outputName.substring(0, outputName.length() - 3);
        Path output = sample.getParent().resolve("bin/").resolve(outputName);
        String asm = compile(sample, output);
        byte[] bin = assemble(asm);

        Path selfCompiled = Files.createTempFile("self-compiled", null);
        Files.write(selfCompiled, bin);
        new File(selfCompiled.toString()).setExecutable(true);
        Path reference = Files.createTempFile("gcc-compiled", null);
        compileReference(sample, reference);
        new File(reference.toString()).setExecutable(true);

        compareExecution(selfCompiled, reference);
    }

    String compile(Path input, Path output) throws IOException {
        DebugConsumer debugConsumer = new DefaultDebugConsumer(output, logger);
        Racc0onCompilation compilation = new Racc0onCompilation(Files.readString(input), debugConsumer);
        return assertDoesNotThrow(compilation::compile);
    }

    byte[] assemble(String asm) {
        AssemblerException exception = null;
        byte[] bin = null;
        try {
            bin = assembler.assemble(asm);
        } catch (AssemblerException e) {
            exception = e;
        }

        AssemblerException finalException = exception;
        assertNull(exception, () -> {
            String message = "assembler failed";

            if (finalException != null && finalException.getContext() != null) {
                message += "\n" + finalException.getContext() + "\n";
            }

            return message;
        });

        return bin;
    }

    private void compileReference(Path input, Path output) throws IOException, InterruptedException {
        Process proc = Runtime.getRuntime().exec(new String[] {
                "gcc",
                "-x", "c",
                input.toString(),
                "-o", output.toString()
        });
        proc.getInputStream().transferTo(System.out);
        proc.getErrorStream().transferTo(System.err);
        proc.waitFor();
    }

    private void compareExecution(Path selfCompiled, Path reference) throws IOException, InterruptedException {
        Thread shutdownHook = new Thread(this::killProcesses);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        actual = Runtime.getRuntime().exec(new String[] {selfCompiled.toString()});
        expected = Runtime.getRuntime().exec(new String[] {reference.toString()});

        boolean actualExited = actual.waitFor(1, TimeUnit.MINUTES);
        boolean expectedExited = expected.waitFor(1, TimeUnit.MINUTES);

        assertEquals(actualExited, expectedExited, "process did not terminate as expected");

        if (actualExited) {
            assertEquals(expected.exitValue(), actual.exitValue(), "incorrect exit code");
        }

        actual.destroyForcibly();
        expected.destroyForcibly();

        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

    @AfterEach
    void killProcesses() {
        if (actual != null) {
            actual.destroyForcibly();
        }
        if (expected != null) {
            expected.destroyForcibly();
        }
    }

    private static Stream<Path> provideSampleFiles() throws IOException {
        Path samplesDir = Path.of("sample/");
        List<Path> targets;
        try (Stream<Path> files = Files.list(samplesDir)) {
            targets = files.filter(path -> path.getParent().equals(samplesDir))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches(".*\\.l."))
                    .toList();
        }

        return targets.stream();
    }
}
