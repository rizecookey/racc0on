package net.rizecookey.racc0on.debug;

import java.nio.file.Path;
import java.util.function.Function;

public interface DebugConsumer {
    void info(String info, String context);
    void artifact(String id, Function<Path, Path> filePathProvider, String content);
    default void artifactInfo(String id, String description, Function<Path, Path> filePathProvider, String content) {
        info(description, content);
        artifact(id, filePathProvider, content);
    }
}
