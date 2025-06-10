package net.rizecookey.racc0on.test;

import net.rizecookey.racc0on.assembler.Assembler;
import net.rizecookey.racc0on.assembler.ExternalGcc;
import net.rizecookey.racc0on.compilation.Racc0on;
import net.rizecookey.racc0on.debug.DebugConsumer;
import net.rizecookey.racc0on.test.util.LogOnlyDebugConsumer;
import net.rizecookey.racc0on.test.util.SamplesProvider;
import net.rizecookey.racc0on.utils.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SamplesTest {
    Logger logger = new Logger();
    DebugConsumer debugConsumer = new LogOnlyDebugConsumer(logger);
    Assembler assembler = new ExternalGcc();
    int timeout = 10;
    TimeUnit timeoutUnit = TimeUnit.SECONDS;

    List<Process> subProcesses = new ArrayList<>();

    static Stream<Path> samples() throws IOException {
        return SamplesProvider.provideSampleFiles();
    }

    @ParameterizedTest
    @MethodSource("samples")
    void assertCompilationSuccessful(Path sample) {
        String asm = assertDoesNotThrow(() -> Racc0on.compile(Files.readString(sample), debugConsumer));
        assertDoesNotThrow(() -> assembler.assemble(asm));
    }

    @ParameterizedTest
    @MethodSource("samples")
    void assertCompilationResultCorrect(Path sample) throws IOException, InterruptedException {
        byte[] bin = assembler.assemble(Racc0on.compile(Files.readString(sample)));

        Path selfCompiled = Files.createTempFile("self-compiled", null);
        Files.write(selfCompiled, bin);
        new File(selfCompiled.toString()).setExecutable(true);
        Path reference = Files.createTempFile("gcc-compiled", null);
        compileReference(sample, reference);
        new File(reference.toString()).setExecutable(true);

        compareExecution(selfCompiled, reference);
    }

    @ParameterizedTest
    @MethodSource("samples")
    void assertCompilationDeterministic(Path sample) throws IOException {
        System.out.println("Compiling for initial result");
        String input = Files.readString(sample);
        String initialResult = Racc0on.compile(input);
        for (int i = 1; i <= 100; i++) {
            System.out.println("Compiling subsequent result number " + i);
            String subsequentResult = Racc0on.compile(input);

            assertEquals(subsequentResult, initialResult,
                    "compilation resulted in different assembly output for the same program");
        }
    }

    @ParameterizedTest
    @MethodSource("samples")
    void assertCompilationResultDeterministic(Path sample) throws IOException, InterruptedException {
        byte[] bin = assembler.assemble(Racc0on.compile(Files.readString(sample)));
        Path result = Files.createTempFile("test-file", null);
        Files.write(result, bin);
        new File(result.toString()).setExecutable(true);

        ExecutionResult initial = execute(result, timeout, timeoutUnit);
        for (int i = 1; i <= 100; i++) {
            ExecutionResult subsequent = execute(result, timeout, timeoutUnit);
            assertEquals(initial, subsequent, "different execution result for the same binary occured");
        }
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
        var expectedRes = execute(reference, timeout, timeoutUnit);
        var actualRes = execute(selfCompiled, timeout, timeoutUnit);

        assertEquals(expectedRes.terminated(), actualRes.terminated(), "process did not terminate as expected");

        if (expectedRes.terminated()) {
            assertEquals(expectedRes.exitCode(), actualRes.exitCode(), "incorrect exit code");
        }
    }

    record ExecutionResult(boolean terminated, int exitCode) {}
    private ExecutionResult execute(Path binary, int timeout, TimeUnit unit) throws IOException, InterruptedException {
        Thread shutdownHook = new Thread(this::killProcesses);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        Process process = Runtime.getRuntime().exec(new String[] {binary.toString()});
        subProcesses.add(process);

        boolean exited = process.waitFor(timeout, unit);
        ExecutionResult result = new ExecutionResult(exited, exited ? process.exitValue() : -1);

        process.destroyForcibly();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);

        return result;
    }

    @AfterEach
    void killProcesses() {
        for (Process subProcess : List.copyOf(subProcesses)) {
            subProcess.destroyForcibly();
            subProcesses.remove(subProcess);
        }
    }
}
