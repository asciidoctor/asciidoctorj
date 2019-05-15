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

@RunWith(ArquillianSputnik)
class WhenAHighlightjsAdapterIsImplementedInGroovy extends Specification {

  public static final String NAME_SYNTAXHIGHLIGHTER = 'highlight4J'

  @ArquillianResource
  private Asciidoctor asciidoctor

  def 'should highlight with highlightjs'() {

    given:
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

    asciidoctor.syntaxHighlighterRegistry().register(HighlightJsHighlighter, NAME_SYNTAXHIGHLIGHTER)

    when:
    String htmlJava = asciidoctor.convert(doc, OptionsBuilder.options()
            .safe(SafeMode.UNSAFE)
            .headerFooter(true)
            .attributes(AttributesBuilder.attributes().sourceHighlighter(NAME_SYNTAXHIGHLIGHTER)))

    String htmlRuby = asciidoctor.convert(doc, OptionsBuilder.options()
            .safe(SafeMode.UNSAFE)
            .headerFooter(true)
            .attributes(AttributesBuilder.attributes().sourceHighlighter('highlightjs')))

    then:
    // Cannot use `htmlRuby == htmlJava` because it fails with OOM
    assertEquals(htmlRuby, htmlJava)
  }
}
