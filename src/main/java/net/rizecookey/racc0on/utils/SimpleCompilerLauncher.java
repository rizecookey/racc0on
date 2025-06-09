package net.rizecookey.racc0on.utils;

import net.rizecookey.racc0on.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class SimpleCompilerLauncher {
    private static final Logger LOGGER = new Logger();

    private SimpleCompilerLauncher() {}

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            LOGGER.log("No subcommand entered, try 'compile' or 'run'.");
            System.exit(1);
            return;
        }
        boolean runAfterwards = switch (args[0]) {
            case "compile" -> false;
            case "run" -> true;
            default -> {
                LOGGER.prefixedError("first argument must be either 'compile' or 'run'");
                System.exit(1);
                throw new IllegalStateException();
            }
        };

        if (args.length < 2) {
            LOGGER.error("missing file argument");
            System.exit(1);
            return;
        }

        List<Path> files = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            Path input = Path.of(arg);

            if (Files.isDirectory(input)) {
                try (var stream = Files.walk(input, 1)) {
                    files.addAll(stream
                            .filter(path -> !path.equals(input))
                            .filter(path -> path.getFileName().toString().matches(".*\\.l."))
                            .toList());
                }
            } else {
                if (!input.getFileName().toString().matches(".*\\.l.")) {
                    LOGGER.prefixedError("expected a .l<1|2> file but got " + input);
                    System.exit(1);
                    return;
                }
                files.add(input);
            }
        }

        int exitCode = 0;
        for (int i = 0; i < files.size(); i++) {
            Path input = files.get(i);
            String filename = input.getFileName().toString();
            Path output = input.getParent().resolve("bin/").resolve(filename.substring(0, filename.length() - 3));

            LOGGER.log("Compiling " + input + " > " + output);
            try {
                Main.runCompiler(input, output, true);
            } catch (Exception e) {
                LOGGER.prefixedError("compiler failed", e);
                LOGGER.log(System.lineSeparator());
                exitCode = 1;
                continue;
            }

            if (runAfterwards) {
                run(output);
            }

            if (i < files.size() - 1) {
                LOGGER.log(System.lineSeparator());
            }
        }
        System.exit(exitCode);
    }

    private static void run(Path file) throws IOException, InterruptedException {
        LOGGER.log("");
        LOGGER.log("Running " + file + ":");

        Process proc = Runtime.getRuntime().exec(new String[] {file.toString()});
        Thread shutdownHook = new Thread(proc::destroyForcibly);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        proc.getInputStream().transferTo(System.out);
        proc.getErrorStream().transferTo(System.out);

        boolean exited = proc.waitFor(1, TimeUnit.MINUTES);
        if (!exited) {
            LOGGER.prefixedError("timed out: program did not end after 1 minute");
            proc.destroyForcibly();
        }

        LOGGER.log("Program exited with status code " + proc.exitValue());

        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }
}
