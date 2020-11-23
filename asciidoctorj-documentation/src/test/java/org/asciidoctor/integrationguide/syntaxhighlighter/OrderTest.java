package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.stream.Collectors;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;

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
        String expectedHighlighterMessages =
            readFileToString(classpathResources.getResource("syntax-highlighting-order-output.txt"))
                .replaceAll("\r\n","\n");

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

        String actual = OrderDocumentingHighlighter.messages.stream()
                .map(msg -> ". " + msg + "\n")
                .collect(Collectors.joining());

        assertEquals(expectedHighlighterMessages, actual);
    }

}
