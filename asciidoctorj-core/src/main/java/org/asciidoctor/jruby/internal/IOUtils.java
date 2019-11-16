package org.asciidoctor.jruby.internal;

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
        try (Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")){
            return scanner.next();
        }
    }

    public static String readFull(Reader reader) {
        try (Scanner scanner = new Scanner(reader).useDelimiter("\\A")){
            return scanner.next();
        }
    }

    public static void writeFull(Writer writer, String content) throws IOException {
        writer.append(content);
        writer.flush();
    }

}
