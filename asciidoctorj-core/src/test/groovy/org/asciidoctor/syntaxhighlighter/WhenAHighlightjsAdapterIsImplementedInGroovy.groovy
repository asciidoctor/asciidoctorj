package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class WhenAHighlightjsAdapterIsImplementedInGroovy extends Specification {

    public static final String NAME_SYNTAXHIGHLIGHTER = 'highlight4J'
    public static final String EMPTY_STRING = ''

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

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
        String html = asciidoctor.convert(doc, Options.builder()
                .safe(SafeMode.UNSAFE)
                .standalone(true)
                .attributes(Attributes.builder().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER).build())
                .build())

        then:
        Document document = Jsoup.parse(html)
        document.head().html().contains(HighlightJsHighlighter.DOCINFO_HEADER)
        strip(document.body().html()).contains(strip(HighlightJsHighlighter.DOCINFO_FOOTER))
        document.body().html().contains('<pre class="highlightjs highlight">')
    }

    def strip(String s) {
        s.replace('\n', EMPTY_STRING).replace(' ', EMPTY_STRING)
    }

    def 'should autoregister from extension'() {

        when:
        asciidoctor.convert(doc, Options.builder()
                .safe(SafeMode.UNSAFE)
                .standalone(true)
                .attributes(Attributes.builder().sourceHighlighter(HighlightJsExtension.NAME_HIGHLIGHTER).build())
                .build())

        then:
        assertThat(ObservableHighlightJsHighlighter.called, is(equalTo(2)))
    }
}
