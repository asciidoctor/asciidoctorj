package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.AttributesBuilder
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Document
import org.asciidoctor.extension.LocationType
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import spock.lang.Specification

class WhenALayoutingSyntaxHighlighterIsRegistered extends Specification {

  public static final String NAME_SYNTAXHIGHLIGHTER = 'test'

  private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

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
            .standalone(false)
            .attributes(AttributesBuilder.attributes().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER)))

    org.jsoup.nodes.Document document = Jsoup.parse(html)

    then:
    Elements elements = document.select('div.listingblock div.content pre.highlight code')
    elements.size() == 1
    elements.first().attr('data-lang') == 'go'
    elements.first().text() == 'System.out.println("Hello World");'
  }
}
