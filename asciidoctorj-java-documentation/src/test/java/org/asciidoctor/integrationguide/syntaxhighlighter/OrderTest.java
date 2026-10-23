package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.stream.Collectors;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class OrderTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("syntax-highlighting-order.adoc")
    private File syntaxHighlightingDocument;

    @ClasspathResource("syntax-highlighting-order-output.txt")
    private File syntaxHighlightingOutput;

    @TempDir(cleanup = CleanupMode.NEVER)
    public File tempDir;


    @Test
    public void should_invoke_syntax_highlighter() throws Exception {
        File sources_adoc = //...
                syntaxHighlightingDocument;
        String expectedHighlighterMessages =
                readFileToString(syntaxHighlightingOutput)
                        .replaceAll("\r\n", "\n");

        File toDir = // ...
                tempDir;

        asciidoctor.syntaxHighlighterRegistry()
                .register(OrderDocumentingHighlighter.class, "order");

        asciidoctor.convertFile(sources_adoc,
                Options.builder()
                        .standalone(true)
                        .toDir(toDir)
                        .safe(SafeMode.UNSAFE)
                        .attributes(Attributes.builder()
                                .sourceHighlighter("order")
                                .copyCss(true)
                                .linkCss(true)
                                .build())
                        .build());

        String actual = OrderDocumentingHighlighter.messages.stream()
                .map(msg -> ". " + msg + "\n")
                .collect(Collectors.joining());

        assertThat(expectedHighlighterMessages).isEqualTo(actual);
    }

}
