package net.rizecookey.racc0on.test.util;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class SamplesProvider {
    private static final Parser PARSER = Parser.builder().build();

    public static Stream<SampleInfo> provideTests(String validCategory) throws IOException {
        Path testsDir = Path.of("tests/crow/");
        List<Path> targets;
        try (Stream<Path> files = Files.walk(testsDir)) {
            targets = files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".crow-test.md"))
                    .toList();
        }

        return targets.stream()
                .map(SamplesProvider::parseTest)
                .filter(sample -> {
                    if (!sample.limitedToCategory()) {
                        return true;
                    }

                    return sample.file().getParent().getFileName().toString().equals(validCategory);
                });
    }

    private static SampleInfo parseTest(Path file) {
        String content;
        try {
            content = Files.readString(file);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read file content", e);
        }
        Node node = PARSER.parse(content);
        ProgramParser programParser = new ProgramParser();
        node.accept(programParser);

        if (programParser.program == null) {
            throw new IllegalStateException("No program found");
        }

        return new SampleInfo(file, programParser.program, programParser.limitedToCategory);
    }

    private static class ProgramParser extends AbstractVisitor {
        private String lastHeading = null;

        private String program = null;
        private boolean limitedToCategory = false;

        @Override
        public void visit(Text text) {
            if ((text.getParent() instanceof Heading)) {
                lastHeading = text.getLiteral();
            }

            super.visit(text);
        }

        @Override
        public void visit(FencedCodeBlock fencedCodeBlock) {
            switch (lastHeading.trim()) {
                case "ProgramArgumentFile" -> program = fencedCodeBlock.getLiteral();
                case "Limited to Category" -> limitedToCategory = Boolean.parseBoolean(fencedCodeBlock.getLiteral().trim());
            }

            super.visit(fencedCodeBlock);
        }
    }
}
