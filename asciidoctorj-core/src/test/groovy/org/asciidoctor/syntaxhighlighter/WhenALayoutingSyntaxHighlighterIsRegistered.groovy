package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.AttributesBuilder
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Document
import org.asciidoctor.extension.LocationType
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenALayoutingSyntaxHighlighterIsRegistered extends Specification {

  public static final String NAME_SYNTAXHIGHLIGHTER = 'test'

  @ArquillianResource
  private Asciidoctor asciidoctor

  static class AllGoHighlighter implements SyntaxHighlighterAdapter, Formatter {

    @Override
    boolean hasDocInfo(LocationType location) {
      false
    }

    @Override
    String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
      throw new IllegalArgumentException('Unexpected call')
    }

    @Override
    String format(Block node, String lang, Map<String, Object> opts) {
      """<pre class="highlight"><code data-lang="go">${node.content}</code></pre>"""
    }
  }


  def 'should layout source'() {

    given:
    def doc = '''= Test Document

[source,java]
----
System.out.println("Hello World");
----
'''

    asciidoctor.syntaxHighlighterRegistry().register(AllGoHighlighter, NAME_SYNTAXHIGHLIGHTER)

    when:
    String html = asciidoctor.convert(doc, OptionsBuilder.options()
            .safe(SafeMode.UNSAFE)
            .headerFooter(false)
            .attributes(AttributesBuilder.attributes().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER)))

    org.jsoup.nodes.Document document = Jsoup.parse(html)

    then:
    Elements elements = document.select('div.listingblock div.content pre.highlight code')
    elements.size() == 1
    elements.first().attr('data-lang') == 'go'
    elements.first().text() == 'System.out.println("Hello World");'
  }
}
