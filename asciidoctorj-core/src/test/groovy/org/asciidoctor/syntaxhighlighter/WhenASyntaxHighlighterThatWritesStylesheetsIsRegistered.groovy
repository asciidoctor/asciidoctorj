package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.asciidoctor.ast.Document
import org.asciidoctor.extension.LocationType
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

class WhenASyntaxHighlighterThatWritesStylesheetsIsRegistered extends Specification {

  public static final String NAME_SYNTAXHIGHLIGHTER = 'test'

  public static final String CONTENT_CSS = 'FOOBAR'
  public static final String FILENAME_CSS = 'highlighter.css'

  private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

  @TempDir
  public File tempDir

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

    def toDir = tempDir
    asciidoctor.syntaxHighlighterRegistry().register(TestHighlighter, NAME_SYNTAXHIGHLIGHTER)

    when:
    asciidoctor.convert(doc, Options.builder()
            .safe(SafeMode.UNSAFE)
            .standalone(true)
            .toFile(new File(toDir,'syntaxhighlighterwithwritestylesheet.html'))
            .attributes(
                    Attributes.builder().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER)
                            .copyCss(true)
                            .linkCss(true).build())
            .build())

    then:

    !exists ^ (new File(toDir, FILENAME_CSS).exists() && new File(toDir, FILENAME_CSS).text == CONTENT_CSS)

    where:
    lang   | exists
    'java' | true
    'go'   | false
  }
}
