package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class PrismJsHighlighterTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("sources.adoc")
    private File sourcesDocument;

    @TempDir(cleanup = CleanupMode.NEVER)
    public File tempDir;


    @Test
    public void should_invoke_syntax_highlighter() throws Exception {
//tag::include[]
        File sources_adoc = //...
//end::include[]
                sourcesDocument;

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
