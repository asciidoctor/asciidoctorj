package org.asciidoctor.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An interface that can be implemented by a {@link org.asciidoctor.core.converter.Converter} to allow the converter to
 * control how the output is written to disk.
 *
 * <p>A converter that produces an output other than a String or a byte array (e.g., a document object of a PDF library)
 * must implement this interface to write its output.
 *
 * @param <T> the type of the output of the converter
 */
public interface Writer<T> {

    /**
     * Writes the output to the specified target file name or stream.
     *
     * <p>The default implementation writes a String (ensuring there's a trailing newline when written to a stream) or a
     * byte array.
     *
     * @param output The output to write
     * @param target The String file name or stream object (an {@link OutputStream} or {@link Appendable}) to which the
     *            output should be written.
     */
    default void write(T output, Object target) {
        try {
            if (output instanceof byte[] bytes) {
                writeBytes(bytes, target);
            } else if (target instanceof OutputStream stream) {
                // ensure there's a trailing newline to be nice to terminals
                stream.write((Strings.chomp(String.valueOf(output)) + Asciidoctor.LF).getBytes(StandardCharsets.UTF_8));
                stream.flush();
            } else if (target instanceof Appendable appendable) {
                // ensure there's a trailing newline to be nice to terminals
                appendable.append(Strings.chomp(String.valueOf(output))).append(Asciidoctor.LF);
            } else {
                // QUESTION shouldn't we ensure a trailing newline here too?
                Helpers.writeFile(String.valueOf(target), String.valueOf(output));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Writes the bytes to the target file name or stream (an Appendable cannot receive bytes).
     */
    static void writeBytes(byte[] bytes, Object target) throws IOException {
        if (target instanceof OutputStream stream) {
            stream.write(bytes);
            stream.flush();
        } else if (target instanceof Appendable) {
            throw new IllegalArgumentException("Cannot write a binary output to " + target.getClass().getName());
        } else {
            Files.write(Path.of(String.valueOf(target)), bytes);
        }
    }
}
