package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.stream.Collectors;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;

public class OrderTest {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @TempDir(cleanup = CleanupMode.NEVER)
    public File tempDir;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

    @Test
    public void should_invoke_syntax_highlighter() throws Exception {
        File sources_adoc = //...
                classpathResources.getResource("syntax-highlighting-order.adoc");
        String expectedHighlighterMessages =
                readFileToString(classpathResources.getResource("syntax-highlighting-order-output.txt"))
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
