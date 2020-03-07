package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.api.AttributesBuilder
import org.asciidoctor.api.OptionsBuilder
import org.asciidoctor.api.SafeMode
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.runner.RunWith
import spock.lang.Specification

import static junit.framework.Assert.assertEquals

@RunWith(ArquillianSputnik)
class WhenAHighlightjsAdapterIsImplementedInGroovy extends Specification {

    public static final String NAME_SYNTAXHIGHLIGHTER = 'highlight4J'
    public static final String HIGHLIGHTJS = 'highlightjs'

    @ArquillianResource
    private Asciidoctor asciidoctor

    def doc = '''= Test Document
:nofooter:

[source,java]
----
System.out.println("Hello World");
----

[source,go]
----
func main() {
  fmt.Println("Hello World")
}
----
'''

    def 'should highlight with highlightjs'() {

        given:

        asciidoctor.syntaxHighlighterRegistry().register(HighlightJsHighlighter, NAME_SYNTAXHIGHLIGHTER)

        when:
        String htmlJava = asciidoctor.convert(doc, OptionsBuilder.options()
                .safe(SafeMode.UNSAFE)
                .headerFooter(true)
                .attributes(AttributesBuilder.attributes().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER)))

        String htmlRuby = asciidoctor.convert(doc, OptionsBuilder.options()
                .safe(SafeMode.UNSAFE)
                .headerFooter(true)
                .attributes(AttributesBuilder.attributes().sourceHighlighter(HIGHLIGHTJS)))

        then:
        // Cannot use `htmlRuby == htmlJava` because it fails with OOM
        assertEquals(htmlRuby, htmlJava)
    }

    def 'should autoregister from extension'() {

        when:
        String htmlJava = asciidoctor.convert(doc, OptionsBuilder.options()
                .safe(SafeMode.UNSAFE)
                .headerFooter(true)
                .attributes(AttributesBuilder.attributes().sourceHighlighter(HighlightJsExtension.NAME_HIGHLIGHTER)))

        String htmlRuby = asciidoctor.convert(doc, OptionsBuilder.options()
                .safe(SafeMode.UNSAFE)
                .headerFooter(true)
                .attributes(AttributesBuilder.attributes().sourceHighlighter(HIGHLIGHTJS)))

        then:
        // Cannot use `htmlRuby == htmlJava` because it fails with OOM
        assertEquals(htmlRuby, htmlJava)
    }
}
