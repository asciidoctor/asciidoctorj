package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import spock.lang.Ignore
import spock.lang.Specification

import static junit.framework.Assert.assertEquals

class WhenACoderayAdapterIsImplementedInGroovy extends Specification {

  public static final String NAME_SYNTAXHIGHLIGHTER = 'coderay4J'

  private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

  @Ignore('until the latest release also puts the link to the CSS into the header')
  def 'should highlight with coderay'() {

    given:
    def doc = '''= Test Document
:nofooter:

[source,java]
----
System.out.println("Hello World"); // <1>
----

[source,go,linenums]
----
func main() {
  fmt.Println("Hello World") // <1>
}
----
'''

    asciidoctor.syntaxHighlighterRegistry().register(CoderayHighlighter, NAME_SYNTAXHIGHLIGHTER)

    when:
    String htmlJava = asciidoctor.convert(doc, Options.builder()
            .safe(SafeMode.UNSAFE)
            .standalone(true)
            .attributes(Attributes.builder()
                .sourceHighlighter(NAME_SYNTAXHIGHLIGHTER)
                .linkCss(true)
                .build())
            .build())

    String htmlRuby = asciidoctor.convert(doc, Options.builder()
            .safe(SafeMode.UNSAFE)
            .standalone(true)
            .attributes(Attributes.builder()
                .sourceHighlighter('coderay')
                .linkCss(true)
                .build())
            .build())

    then:
    // Cannot use `htmlRuby == htmlJava` because it fails with OOM
    assertEquals(htmlRuby, htmlJava)
  }
}
