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
import spock.lang.Unroll

class WhenAServerSideSyntaxHighlighterIsRegistered extends Specification {

  public static final String NAME_SYNTAXHIGHLIGHTER = 'test'

  private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

  static class TestHighlighter implements SyntaxHighlighterAdapter, Highlighter {

    @Override
    boolean hasDocInfo(LocationType location) {
      false
    }

    @Override
    String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
      throw new IllegalArgumentException('Unexpected call')
    }

    @Override
    HighlightResult highlight(Block node, String source, String lang, Map<String, Object> options) {
      new HighlightResult(
              source.replace('System', '<span class="class-name">System</span>'),
              1)
    }
  }


  @Unroll
  def 'should highlight source'() {

    given:
    def doc = '''= Test Document

[source,java]
----
System.out.println("Hello World");
----
'''

    asciidoctor.syntaxHighlighterRegistry().register(TestHighlighter, NAME_SYNTAXHIGHLIGHTER)

    when:
    String html = asciidoctor.convert(doc, OptionsBuilder.options()
            .safe(SafeMode.UNSAFE)
            .standalone(false)
            .attributes(AttributesBuilder.attributes().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER)))

    org.jsoup.nodes.Document document = Jsoup.parse(html)

    then:
    Elements elements = document.select('div.listingblock div.content pre.test.highlight code span.class-name')
    elements.size() == 1
    elements.text() == 'System'
  }
}
