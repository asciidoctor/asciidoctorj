package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.apache.commons.io.IOUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class HighlightJsHighlighterTest {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @TempDir
    public File tempDir;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

    @Test
    public void should_invoke_syntax_highlighter() {
//tag::include[]
        File sources_adoc = //...
//end::include[]
            classpathResources.getResource("sources.adoc");

        //tag::include[]

        asciidoctor.syntaxHighlighterRegistry()
            .register(HighlightJsHighlighter.class, "myhighlightjs"); // <1>

        String result = asciidoctor.convertFile(sources_adoc,
            OptionsBuilder.options()
                .standalone(true) // <2>
                .toFile(false)
                .attributes(AttributesBuilder.attributes().sourceHighlighter("myhighlightjs"))); // <3>

        assertThat(result,
            containsString("<script>hljs.initHighlighting()</script>"));
//end::include[]
    }

    @Test
    public void should_invoke_syntax_highlighter_with_3_params() {
        File sources_adoc =
            classpathResources.getResource("sources.adoc");


        asciidoctor.syntaxHighlighterRegistry()
            .register(org.asciidoctor.integrationguide.syntaxhighlighter.threeparams.HighlightJsHighlighter.class, "myhighlightjs");

        String result = asciidoctor.convertFile(sources_adoc,
            OptionsBuilder.options()
                .standalone(true)
                .toFile(false)
                .attributes(AttributesBuilder.attributes().sourceHighlighter("myhighlightjs")));

        assertThat(result,
            containsString("<script>hljs.initHighlighting()</script>"));
    }

    @Test
    public void should_invoke_formatting_syntax_highlighter() {
        File sources_adoc =
            classpathResources.getResource("sources.adoc");


//tag::includeformatter[]

        asciidoctor.syntaxHighlighterRegistry()
            .register(HighlightJsWithLanguageHighlighter.class, "myhighlightjs");

        String result = asciidoctor.convertFile(sources_adoc,
            OptionsBuilder.options()
                .standalone(true)
                .toFile(false)
                .attributes(AttributesBuilder.attributes().sourceHighlighter("myhighlightjs")));

        assertThat(result,
            containsString("<script>hljs.initHighlighting()</script>"));
        assertThat(result,
            containsString("<code class='java'>public static class Test"));
//end::includeformatter[]
    }

    @Test
    public void should_invoke_stylesheet_writing_syntax_highlighter() throws Exception {
        File sources_adoc =
            classpathResources.getResource("sources.adoc");

//tag::includestylesheetwriter[]
        File toDir = // ...
//end::includestylesheetwriter[]
            tempDir;
//tag::includestylesheetwriter[]

        asciidoctor.syntaxHighlighterRegistry()
            .register(HighlightJsWithOfflineStylesHighlighter.class, "myhighlightjs");

        asciidoctor.convertFile(sources_adoc,
            OptionsBuilder.options()
                .standalone(true)
                .toDir(toDir)              // <1>
                .safe(SafeMode.UNSAFE)
                .attributes(AttributesBuilder.attributes()
                    .sourceHighlighter("myhighlightjs")
                    .copyCss(true)         // <1>
                    .linkCss(true)));

        File docFile = new File(toDir, "sources.html");
        assertTrue(docFile.exists());

        File cssFile = new File(toDir, "github.min.css");
        assertTrue(cssFile.exists());

        File jsFile = new File(toDir, "highlight.min.js");
        assertTrue(jsFile.exists());

        try (FileReader docReader = new FileReader(new File(toDir, "sources.html"))) {
            String html = IOUtils.toString(docReader);
            assertThat(html, containsString("<link rel=\"stylesheet\" href=\"github.min.css\">"));
            assertThat(html, containsString("<script src=\"highlight.min.js\"></script>"));
        }
//end::includestylesheetwriter[]
    }

}
