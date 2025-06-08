package net.rizecookey.racc0on.debug;

import net.rizecookey.racc0on.utils.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class DefaultDebugConsumer implements DebugConsumer {
    private final Logger logger;
    private final Path outputFile;

    public DefaultDebugConsumer(Path outputFile, Logger logger) {
        this.logger = logger;
        this.outputFile = outputFile;
    }

    @Override
    public void info(String info, String context) {
        logger.log(info + ":", context);
    }

    @Override
    public void artifact(String id, Function<Path, Path> filePathProvider, String content) {
        Path file = filePathProvider.apply(outputFile);
        Path dir = file.getParent();
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Files.writeString(file, content);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
