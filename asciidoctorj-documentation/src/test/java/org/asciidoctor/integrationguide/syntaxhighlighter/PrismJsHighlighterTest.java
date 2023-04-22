package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.util.ClasspathHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class PrismJsHighlighterTest {

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
//tag::include[]
        File sources_adoc = //...
//end::include[]
            classpathResources.getResource("sources.adoc");

//tag::include[]
        File toDir = // ...
//end::include[]
            tempDir;

//tag::include[]
        asciidoctor.syntaxHighlighterRegistry()
            .register(PrismJsHighlighter.class, "prismjs"); // <1>

        asciidoctor.convertFile(sources_adoc,
            OptionsBuilder.options()
                .standalone(true)
                .toDir(toDir)
                .safe(SafeMode.UNSAFE)
                .attributes(AttributesBuilder.attributes()
                    .sourceHighlighter("prismjs")           // <1>
                    .copyCss(true)
                    .linkCss(true)));

        File docFile = new File(toDir, "sources.html");

        Document document = Jsoup.parse(docFile, "UTF-8");
        Elements keywords = document.select("div.content pre.highlight code span.token.keyword"); // <2>
        assertThat(keywords, not(empty()));
        assertThat(keywords.first().text(), is("public"));
//end::include[]
    }

}
