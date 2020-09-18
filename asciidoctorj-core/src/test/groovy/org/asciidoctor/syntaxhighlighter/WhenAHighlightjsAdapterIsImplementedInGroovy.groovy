package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.AttributesBuilder
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.runner.RunWith
import spock.lang.Specification

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.either
import static org.hamcrest.Matchers.equalTo
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
        assertThat(htmlJavaOld, containsString('highlight.js/9.15.5'))
        assertThat(htmlJavaNew, containsString('highlight.js/9.15.6'))
        // Cannot use `htmlRuby == htmlJava` because it fails with OOM
        // Asciidoctor > 2.0.10 changed the location where the javascript is included,
        // therefore test against both possibilities to pass with 2.0.10 and upstream
        assertThat(stripHighlightjsVersion(htmlRuby),
                either(is(stripHighlightjsVersion(htmlJavaNew))).or(is(stripHighlightjsVersion(htmlJavaOld))))
    }

    private String stripHighlightjsVersion(String htmlRuby) {
        htmlRuby.replaceAll('highlight\\.js/(\\d)+\\.(\\d)+\\.(\\d)+/', 'highlight\\.js/ma\\.min\\.fix/')
    }

    def 'should autoregister from extension'() {

        when:
        asciidoctor.convert(doc, OptionsBuilder.options()
                .safe(SafeMode.UNSAFE)
                .headerFooter(true)
                .attributes(AttributesBuilder.attributes().sourceHighlighter(HighlightJsExtension.NAME_HIGHLIGHTER)))

        then:
        assertThat(ObservableHighlightJsHighlighter.called, is(equalTo(2)))
    }
}
