package net.rizecookey.racc0on.test;

import net.rizecookey.racc0on.assembler.Assembler;
import net.rizecookey.racc0on.assembler.AssemblerException;
import net.rizecookey.racc0on.assembler.ExternalGcc;
import net.rizecookey.racc0on.compilation.Racc0onCompilation;
import net.rizecookey.racc0on.debug.DebugConsumer;
import net.rizecookey.racc0on.debug.DefaultDebugConsumer;
import net.rizecookey.racc0on.utils.Logger;
import net.rizecookey.racc0on.utils.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SamplesTest {
    Logger logger = new Logger();
    Assembler assembler = new ExternalGcc();

    @ParameterizedTest
    @MethodSource("provideSampleFiles")
    void runSamples(Pair<Path, Path> target) throws IOException {
        String input = Files.readString(target.first());
        DebugConsumer debugConsumer = new DefaultDebugConsumer(target.second(), logger);
        Racc0onCompilation compilation = new Racc0onCompilation(input, debugConsumer);
        String out = assertDoesNotThrow(compilation::compile);

        AssemblerException exception = null;
        try {
            assembler.assemble(out);
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
    }

    private static Stream<Pair<Path, Path>> provideSampleFiles() throws IOException {
        Path samplesDir = Path.of("sample/");
        List<Pair<Path, Path>> targets;
        try (Stream<Path> files = Files.list(samplesDir)) {
            targets = files.filter(path -> path.getParent().equals(samplesDir))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches(".*\\.l."))
                    .map(file -> {
                        String filename = file.getFileName().toString();
                        String filenameWithoutEnding = filename.substring(0, filename.length() - 3);
                        return new Pair<>(file, file.getParent().resolve("bin/").resolve(filenameWithoutEnding));
                    }).toList();
        }

        return targets.stream();
    }
}
