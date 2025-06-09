package net.rizecookey.racc0on.test.util;

import net.rizecookey.racc0on.debug.DebugConsumer;
import net.rizecookey.racc0on.utils.Logger;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.function.Function;

@NullMarked
public class LogOnlyDebugConsumer implements DebugConsumer {
    private final Logger logger;

    public LogOnlyDebugConsumer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String info, String context) {
        logger.log(info, context);
    }

    @Override
    public void artifact(String id, Function<Path, Path> filePathProvider, String content) {}
}
