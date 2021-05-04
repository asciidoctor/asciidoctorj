package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.asciidoctor.ast.Document
import org.asciidoctor.extension.LocationType
import org.jsoup.Jsoup
import spock.lang.Specification
import spock.lang.Unroll

class WhenASyntaxHighlighterIsRegistered extends Specification {

  public static final String NAME_SYNTAXHIGHLIGHTER = 'test'

  private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

  static class TestHighlighter implements SyntaxHighlighterAdapter {

    TestHighlighter(String name, String backend, Map<String, Object> options) {
      if (name != NAME_SYNTAXHIGHLIGHTER) {
        throw new IllegalArgumentException("Expected highlighter name $NAME_SYNTAXHIGHLIGHTER, got $name")
      }
      if (backend != 'html5') {
        throw new IllegalArgumentException("Expected backend html5, got $backend")
      }
      Document document = options['document'] as Document
      if (document.doctitle != 'Test Document') {
        throw new IllegalArgumentException("Expected doctitle 'Test Document', got '${document.doctitle}'")
      }
    }

    @Override
    boolean hasDocInfo(LocationType location) {
      true
    }

    @Override
    String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
      switch (location) {
        case LocationType.HEADER:
          return '<meta name="testhighlighter" content="HeaderDocInfo">'
        case LocationType.FOOTER:
          return '<div class="testhighlighter">FooterDocInfo</div>'
        default:
          throw new IllegalArgumentException("Unexpected location: $location")
      }
    }
  }

  static class HeaderOnlyHighlighter extends TestHighlighter {
    HeaderOnlyHighlighter(String name, String backend, Map<String, Object> options) {
      super(name, backend, options)
    }

    boolean hasDocInfo(LocationType location) {
      location == LocationType.HEADER
    }
  }

  static class FooterOnlyHighlighter extends TestHighlighter {
    FooterOnlyHighlighter(String name, String backend, Map<String, Object> options) {
      super(name, backend, options)
    }

    boolean hasDocInfo(LocationType location) {
      location == LocationType.FOOTER
    }
  }

  static class HeaderAndFooterHighlighter extends TestHighlighter {
    HeaderAndFooterHighlighter(String name, String backend, Map<String, Object> options) {
      super(name, backend, options)
    }

    boolean hasDocInfo(LocationType location) {
      true
    }
  }

  static class NoneHighlighter extends TestHighlighter {
    NoneHighlighter(String name, String backend, Map<String, Object> options) {
      super(name, backend, options)
    }

    boolean hasDocInfo(LocationType location) {
      false
    }
  }

  def DOC = '''= Test Document

[source,java]
----
System.out.println("Hello World");
----'''

  @Unroll
  def '#highlighter.simpleName should create header docinfo #expectHeader and footer docinfo #expectFooter'() {

    expect:
    asciidoctor.syntaxHighlighterRegistry().register(highlighter, NAME_SYNTAXHIGHLIGHTER)
    String html = asciidoctor.convert(DOC, Options.builder()
            .safe(SafeMode.UNSAFE)
            .headerFooter(true)
            .attributes(
                    Attributes.builder().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER).build())
            .build())

    org.jsoup.nodes.Document doc = Jsoup.parse(html)

    def headerDocInfo = doc.head().getElementsByTag('meta').find { it.attr('name') == 'testhighlighter' }

    !expectHeader ^ (headerDocInfo != null)

    def hasFooterDocInfo = !doc.body().select('div.testhighlighter').empty

    !expectFooter ^ hasFooterDocInfo

    where:
      highlighter                | expectHeader | expectFooter
      HeaderOnlyHighlighter      | true         | false
      FooterOnlyHighlighter      | false        | true
      HeaderAndFooterHighlighter | true         | true
      NoneHighlighter            | false        | false

  }

}
