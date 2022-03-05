/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.document;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.internal.util.AssertionUtil;
import net.sourceforge.pmd.internal.util.BaseCloseable;
import net.sourceforge.pmd.lang.LanguageVersion;

/**
 * A {@link TextFile} backed by a file in some {@link FileSystem}.
 */
class NioTextFile extends BaseCloseable implements TextFile {

    private final Path path;
    private final Charset charset;
    private final LanguageVersion languageVersion;
    private final @Nullable String displayName;

    NioTextFile(Path path, Charset charset, LanguageVersion languageVersion, @Nullable String displayName) {
        AssertionUtil.requireParamNotNull("path", path);
        AssertionUtil.requireParamNotNull("charset", charset);
        AssertionUtil.requireParamNotNull("language version", languageVersion);

        this.displayName = displayName;
        this.path = path;
        this.charset = charset;
        this.languageVersion = languageVersion;
    }

    @Override
    public @NonNull LanguageVersion getLanguageVersion() {
        return languageVersion;
    }

    @Override
    public @NonNull String getDisplayName() {
        return displayName == null ? path.toString() : displayName;
    }

    @Override
    public String getPathId() {
        return path.toAbsolutePath().toString();
    }

    @Override
    public boolean isReadOnly() {
        return !Files.isWritable(path);
    }

    @Override
    public void writeContents(TextFileContent content) throws IOException {
        ensureOpen();
        try (BufferedWriter bw = Files.newBufferedWriter(path, charset)) {
            if (content.getLineTerminator().equals(TextFileContent.NORMALIZED_LINE_TERM)) {
                content.getNormalizedText().writeFully(bw);
            } else {
                for (Chars line : content.getNormalizedText().lines()) {
                    line.writeFully(bw);
                    bw.write(content.getLineTerminator());
                }
            }
        }
    }

    @Override
    public TextFileContent readContents() throws IOException {
        ensureOpen();

        if (!Files.isRegularFile(path)) {
            throw new IOException("Not a regular file: " + path);
        }

        return TextFileContent.fromInputStream(Files.newInputStream(path), charset);
    }


    @Override
    protected void doClose() throws IOException {
        // nothing to do.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        @SuppressWarnings("PMD.CloseResource")
        NioTextFile that = (NioTextFile) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "NioTextFile[charset=" + charset + ", path=" + path + ']';
    }
}
