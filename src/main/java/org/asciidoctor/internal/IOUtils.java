package org.asciidoctor.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;

public class IOUtils {

    private IOUtils() {
        super();
    }

    public static String readFull(InputStream inputStream) {
        return new Scanner(inputStream).useDelimiter("\\A").next();
    }

    public static String readFull(Reader reader) {
        return new Scanner(reader).useDelimiter("\\A").next();
    }

    public static void writeFull(Writer writer, String content) throws IOException {
        writer.append(content);
        writer.flush();
    }

}
