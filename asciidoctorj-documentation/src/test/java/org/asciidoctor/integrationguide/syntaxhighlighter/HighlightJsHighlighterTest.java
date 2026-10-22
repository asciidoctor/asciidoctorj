package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.asciidoctor.*;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class HighlightJsHighlighterTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("sources.adoc")
    private File sourcesDocument;

    @TempDir
    public File tempDir;

    @Test
    public void should_invoke_syntax_highlighter() {
//tag::include[]
        File sources_adoc = //...
//end::include[]
                sourcesDocument;

        //tag::include[]

        asciidoctor.syntaxHighlighterRegistry()
            .register(HighlightJsHighlighter.class, "myhighlightjs"); // <1>

        String result = asciidoctor.convertFile(sources_adoc,
            Options.builder()
                .standalone(true) // <2>
                .toFile(false)
                .attributes(Attributes.builder().sourceHighlighter("myhighlightjs").build()) // <3>
                .build());

        assertThat(result,
            containsString("<script>hljs.initHighlighting()</script>"));
//end::include[]
    }

    @Test
    public void should_invoke_syntax_highlighter_with_3_params() {
        File sources_adoc = sourcesDocument;

        asciidoctor.syntaxHighlighterRegistry()
            .register(org.asciidoctor.integrationguide.syntaxhighlighter.threeparams.HighlightJsHighlighter.class, "myhighlightjs");

        String result = asciidoctor.convertFile(sources_adoc,
            Options.builder()
                .standalone(true)
                .toFile(false)
                .attributes(Attributes.builder()
                        .sourceHighlighter("myhighlightjs")
                        .build())
                .build());

        assertThat(result,
            containsString("<script>hljs.initHighlighting()</script>"));
    }

    @Test
    public void should_invoke_formatting_syntax_highlighter() {
        File sources_adoc = sourcesDocument;

//tag::includeformatter[]

        asciidoctor.syntaxHighlighterRegistry()
            .register(HighlightJsWithLanguageHighlighter.class, "myhighlightjs");

        String result = asciidoctor.convertFile(sources_adoc,
            Options.builder()
                .standalone(true)
                .toFile(false)
                .attributes(Attributes.builder()
                        .sourceHighlighter("myhighlightjs")
                        .build())
                    .build());

        assertThat(result,
            containsString("<script>hljs.initHighlighting()</script>"));
        assertThat(result,
            containsString("<code class='java'>public static class Test"));
//end::includeformatter[]
    }

    @Test
    public void should_invoke_stylesheet_writing_syntax_highlighter() throws Exception {
        File sources_adoc = sourcesDocument;

//tag::includestylesheetwriter[]
        File toDir = // ...
//end::includestylesheetwriter[]
            tempDir;
//tag::includestylesheetwriter[]

        asciidoctor.syntaxHighlighterRegistry()
            .register(HighlightJsWithOfflineStylesHighlighter.class, "myhighlightjs");

        asciidoctor.convertFile(sources_adoc,
            Options.builder()
                .standalone(true)
                .toDir(toDir)              // <1>
                .safe(SafeMode.UNSAFE)
                .attributes(Attributes.builder()
                    .sourceHighlighter("myhighlightjs")
                    .copyCss(true)         // <1>
                    .linkCss(true)
                    .build())
                .build());

        File docFile = new File(toDir, "sources.html");
        assertTrue(docFile.exists());

        File cssFile = new File(toDir, "github.min.css");
        assertTrue(cssFile.exists());

        File jsFile = new File(toDir, "highlight.min.js");
        assertTrue(jsFile.exists());

        String html = Files.readString(Path.of(toDir.toURI()).resolve("sources.html"));
        assertThat(html, containsString("<link rel=\"stylesheet\" href=\"github.min.css\">"));
        assertThat(html, containsString("<script src=\"highlight.min.js\"></script>"));
//end::includestylesheetwriter[]
    }

}
