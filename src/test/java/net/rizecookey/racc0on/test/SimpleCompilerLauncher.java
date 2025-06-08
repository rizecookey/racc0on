package net.rizecookey.racc0on.test;

import net.rizecookey.racc0on.compilation.Racc0on;
import net.rizecookey.racc0on.debug.DefaultDebugConsumer;
import net.rizecookey.racc0on.utils.Logger;

import java.io.IOException;
import java.nio.file.Path;

public final class SimpleCompilerLauncher {
    private SimpleCompilerLauncher() {}

    public static void main(String[] args) throws IOException {
        Logger logger = new Logger();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            Path input = Path.of(arg);
            String filename = input.getFileName().toString();
            Path output = input.getParent().resolve("bin/").resolve(filename.substring(0, filename.length() - 3));

            logger.log("Compiling " + input + " > " + output);
            Racc0on.compileAndAssemble(input, output, new DefaultDebugConsumer(output, logger));

            if (i < args.length - 1) {
                logger.log("");
            }
        }
    }
}
