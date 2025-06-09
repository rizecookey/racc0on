package net.rizecookey.racc0on.test.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class SamplesProvider {
    public static Stream<Path> provideSampleFiles() throws IOException {
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
