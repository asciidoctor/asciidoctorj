package org.asciidoctor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import javax0.jamal.DocumentConverter;

public class ReadmeJamalConversionTest {

    @Test
    @DisplayName("Convert README.adoc.jam to README.adoc")
    void generateDoc() throws Exception {

        DocumentConverter.convert("../README.adoc.jam");
    }

}
