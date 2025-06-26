package net.rizecookey.racc0on.test;

import net.rizecookey.racc0on.compilation.Racc0on;
import net.rizecookey.racc0on.test.util.SampleInfo;
import net.rizecookey.racc0on.test.util.SamplesProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SamplesTest {
    static int timeout = 1;
    static TimeUnit timeoutUnit = TimeUnit.MINUTES;
    static String currentLab = "Lab 3";

    static Stream<SampleInfo> samples() throws IOException {
        return SamplesProvider.provideTests(currentLab);
    }

    @BeforeAll
    static void buildCompiler() throws IOException, InterruptedException {
        ExecutionResult result = execute(new String[] {
                "./gradlew",
                "installDist"
        }, 5, TimeUnit.MINUTES);
        assertEquals(0, result.exitCode(), "building compiler failed");
    }

    @ParameterizedTest
    @MethodSource("samples")
    @Execution(ExecutionMode.CONCURRENT)
    void assertCompilationResultCorrect(SampleInfo sample) throws IOException, InterruptedException {
        ExecutionResult result = execute(new String[] {
                "crow-client",
                "run-test",
                "--test-dir", sample.file().getParent().toString(),
                "--test-id", sample.file().getFileName().toString().replace(".crow-test.md", ""),
                "--compiler-run", "./run.sh"

        }, timeout, timeoutUnit);

        assertTrue(result.terminated(), "tester did not terminate");
        assertEquals(0, result.exitCode(), "tester failed:" + System.lineSeparator() + result.output());
    }

    @ParameterizedTest
    @MethodSource("samples")
    @Execution(ExecutionMode.CONCURRENT)
    void assertCompilationDeterministic(SampleInfo sample) {
        System.out.println("Compiling for initial result");
        String initialResult = null;
        Exception exception = null;
        try {
            initialResult = Racc0on.compile(sample.program());
        } catch (Exception e) {
            exception = e;
        }

        for (int i = 1; i <= 25; i++) {
            System.out.println("Compiling subsequent result number " + i);

            Supplier<String> supplier = () -> Racc0on.compile(sample.program());

            if (exception != null) {
                assertThrows(exception.getClass(), supplier::get, "subsequent compilation did not result in error");
            } else {
                assertEquals(initialResult, supplier.get(), "subsequent result does not match initial");
            }
        }
    }

    record ExecutionResult(boolean terminated, int exitCode, String output) {}

    static class Reference<T> {
        T value = null;
    }

    private static ExecutionResult execute(String[] args, int timeout, TimeUnit unit) throws IOException, InterruptedException {
        Reference<Process> procRef = new Reference<>();

        Thread shutdownHook = new Thread(() -> kill(procRef));
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        Process process = new ProcessBuilder(args)
                .start();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        process.getInputStream().transferTo(out);
        process.getErrorStream().transferTo(out);

        procRef.value = process;

        boolean exited = process.waitFor(timeout, unit);
        ExecutionResult result = new ExecutionResult(exited, exited ? process.exitValue() : -1, out.toString());

        process.destroyForcibly();
        out.close();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);

        return result;
    }

    static void kill(Reference<Process> processReference) {
        if (processReference.value != null) {
            processReference.value.destroyForcibly();
        }
    }
}
