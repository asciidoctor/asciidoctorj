package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.api.AttributesBuilder
import org.asciidoctor.api.OptionsBuilder
import org.asciidoctor.api.SafeMode
import org.asciidoctor.ast.Document
import org.asciidoctor.api.extension.LocationType
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import spock.lang.Specification
import spock.lang.Unroll

@RunWith(ArquillianSputnik)
class WhenASyntaxHighlighterThatWritesStylesheetsIsRegistered extends Specification {

  public static final String NAME_SYNTAXHIGHLIGHTER = 'test'

  public static final String CONTENT_CSS = 'FOOBAR'
  public static final String FILENAME_CSS = 'highlighter.css'

  @ArquillianResource
  private Asciidoctor asciidoctor

  @ArquillianResource
  private TemporaryFolder tmp


  static class TestHighlighter implements SyntaxHighlighterAdapter, StylesheetWriter {

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
      false
    }

    @Override
    String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
      throw new IllegalArgumentException('Unexpected call')
    }

    @Override
    boolean isWriteStylesheet(Document doc) {
      doc.findBy(['context': ':listing']).find {it.attributes['language'] == 'java'} != null
    }

    @Override
    void writeStylesheet(Document doc, File toDir) {
      new File(toDir, FILENAME_CSS).text = CONTENT_CSS
    }
  }


  @Unroll
  def 'should write css to disk for #lang: #exists'(String lang, boolean exists) {

    given:
    def doc = """= Test Document

[source,$lang]
----
System.out.println("Hello World");
----
"""

    def toDir = tmp.newFolder()
    asciidoctor.syntaxHighlighterRegistry().register(TestHighlighter, NAME_SYNTAXHIGHLIGHTER)

    when:
    asciidoctor.convert(doc, OptionsBuilder.options()
            .safe(SafeMode.UNSAFE)
            .headerFooter(true)
            .toFile(new File(toDir,'syntaxhighlighterwithwritestylesheet.html'))
            .attributes(
            AttributesBuilder.attributes().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER)
                    .copyCss(true)
                    .linkCss(true))
    )

    then:

    !exists ^ (new File(toDir, FILENAME_CSS).exists() && new File(toDir, FILENAME_CSS).text == CONTENT_CSS)

    where:
    lang   | exists
    'java' | true
    'go'   | false

  }
}
