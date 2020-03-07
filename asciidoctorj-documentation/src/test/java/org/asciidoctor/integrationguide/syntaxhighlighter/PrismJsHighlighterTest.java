package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.api.AttributesBuilder;
import org.asciidoctor.api.OptionsBuilder;
import org.asciidoctor.api.SafeMode;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class PrismJsHighlighterTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void should_invoke_syntax_highlighter() throws Exception {
//tag::include[]
        File sources_adoc = //...
//end::include[]
            classpathResources.getResource("sources.adoc");

//tag::include[]
        File toDir = // ...
//end::include[]
            tempDir.newFolder();
//tag::include[]

        //tag::include[]
        asciidoctor.syntaxHighlighterRegistry()
            .register(PrismJsHighlighter.class, "prismjs"); // <1>

        asciidoctor.convertFile(sources_adoc,
            OptionsBuilder.options()
                .headerFooter(true)
                .toDir(toDir)
                .safe(SafeMode.UNSAFE)
                .attributes(AttributesBuilder.attributes()
                    .sourceHighlighter("prismjs")           // <1>
                    .copyCss(true)
                    .linkCss(true)));

        File docFile = new File(toDir, "sources.html");

        Document document = Jsoup.parse(new File(toDir, "sources.html"), "UTF-8");
        Elements keywords = document.select("div.content pre.highlight code span.token.keyword"); // <2>
        assertThat(keywords, not(empty()));
        assertThat(keywords.first().text(), is("public"));
//end::include[]
    }

}
