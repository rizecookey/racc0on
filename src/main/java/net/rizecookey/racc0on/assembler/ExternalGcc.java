package net.rizecookey.racc0on.assembler;

import net.rizecookey.racc0on.backend.x86_64.x8664CodeGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExternalGcc implements Assembler {
    @Override
    public byte[] assemble(String assemblyInput) throws AssemblerException {
        byte[] bytes;
        try {
            Path asmOutput = Files.createTempFile("racc0on-gcc-binout", null);
            callGcc(assemblyInput, asmOutput);
            bytes = Files.readAllBytes(asmOutput);
            Files.delete(asmOutput);
        } catch (IOException e) {
            throw new AssemblerException(e);
        }

        return bytes;
    }

    private void callGcc(String input, Path output) throws AssemblerException, IOException {
        Process gcc = Runtime.getRuntime().exec(new String[] {"gcc",
                "-pie",
                "-Wl,--entry=" + x8664CodeGenerator.ENTRYPOINT_NAME,
                "-o", output.toString(),
                "-x", "assembler",
                "-m64",
                "-"});

        if (gcc.isAlive()) {
            var writer = gcc.outputWriter();
            writer.write(input);
            writer.close();
        }

        while (gcc.isAlive()) {
            try {
                gcc.waitFor();
            } catch (InterruptedException _) {}
        }

        if (gcc.exitValue() != 0) {
            StringBuilder errorLines = new StringBuilder();
            BufferedReader reader = gcc.errorReader();

            String line;
            while ((line = reader.readLine()) != null) {
                if (!errorLines.isEmpty()) {
                    errorLines.append("\n");
                }
                errorLines.append("  ").append(line);
            }
            reader.close();
            throw new AssemblerException("gcc failed with error code " + gcc.exitValue(), errorLines.toString());
        }
    }
}
