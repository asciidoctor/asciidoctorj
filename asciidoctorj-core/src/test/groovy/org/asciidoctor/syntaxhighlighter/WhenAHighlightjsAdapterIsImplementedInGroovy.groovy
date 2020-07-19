package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.AttributesBuilder
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.runner.RunWith
import spock.lang.Specification

import static junit.framework.Assert.assertEquals
import static org.hamcrest.Matchers.either
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

@RunWith(ArquillianSputnik)
class WhenAHighlightjsAdapterIsImplementedInGroovy extends Specification {

    public static final String NAME_SYNTAXHIGHLIGHTER_OLD = 'oldhighlight4J'
    public static final String NAME_SYNTAXHIGHLIGHTER_NEW = 'newhighlight4J'
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

        asciidoctor.syntaxHighlighterRegistry().register(OldHighlightJsHighlighter, NAME_SYNTAXHIGHLIGHTER_OLD)
        asciidoctor.syntaxHighlighterRegistry().register(NewHighlightJsHighlighter, NAME_SYNTAXHIGHLIGHTER_NEW)

        when:
        String htmlJavaOld = asciidoctor.convert(doc, OptionsBuilder.options()
                .safe(SafeMode.UNSAFE)
                .headerFooter(true)
                .attributes(AttributesBuilder.attributes().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER_OLD)))
        String htmlJavaNew = asciidoctor.convert(doc, OptionsBuilder.options()
                .safe(SafeMode.UNSAFE)
                .headerFooter(true)
                .attributes(AttributesBuilder.attributes().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER_NEW)))

        String htmlRuby = asciidoctor.convert(doc, OptionsBuilder.options()
                .safe(SafeMode.UNSAFE)
                .headerFooter(true)
                .attributes(AttributesBuilder.attributes().sourceHighlighter(HIGHLIGHTJS)))

        then:
        // Cannot use `htmlRuby == htmlJava` because it fails with OOM
        // Asciidoctor > 2.0.10 changed the location where the javascript is included,
        // therefore test against both possibilities to pass with 2.0.10 and upstream
        assertThat(htmlRuby, either(is(htmlJavaNew)).or(is(htmlJavaOld)))
    }

    def 'should autoregister from extension'() {

        when:
        asciidoctor.convert(doc, OptionsBuilder.options()
                .safe(SafeMode.UNSAFE)
                .headerFooter(true)
                .attributes(AttributesBuilder.attributes().sourceHighlighter(HighlightJsExtension.NAME_HIGHLIGHTER)))

        then:
        assertEquals(2, ObservableHighlightJsHighlighter.called)
    }
}
