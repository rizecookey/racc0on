package net.rizecookey.racc0on.test.util;

import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

@NullMarked
public record SampleInfo(Path file, String program, boolean limitedToCategory) {
    @Override
    public String toString() {
        return file.getParent().getParent().relativize(file).toString();
    }
}
