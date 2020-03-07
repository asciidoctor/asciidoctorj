package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.api.AttributesBuilder;
import org.asciidoctor.api.OptionsBuilder;
import org.asciidoctor.api.SafeMode;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

@RunWith(Arquillian.class)
public class OrderTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void should_invoke_syntax_highlighter() throws Exception {
        File sources_adoc = //...
            classpathResources.getResource("syntax-highlighting-order.adoc");

        File toDir = // ...
            tempDir.newFolder();

        asciidoctor.syntaxHighlighterRegistry()
            .register(OrderDocumentingHighlighter.class, "order");

        asciidoctor.convertFile(sources_adoc,
            OptionsBuilder.options()
                .headerFooter(true)
                .toDir(toDir)
                .safe(SafeMode.UNSAFE)
                .attributes(AttributesBuilder.attributes()
                    .sourceHighlighter("order")
                    .copyCss(true)
                    .linkCss(true)));

        try (PrintWriter out = new PrintWriter(new FileWriter("build/resources/test/order.txt"))) {
            for (String msg: OrderDocumentingHighlighter.messages) {
                out.println(". " + msg);
            }
        }
    }

}
