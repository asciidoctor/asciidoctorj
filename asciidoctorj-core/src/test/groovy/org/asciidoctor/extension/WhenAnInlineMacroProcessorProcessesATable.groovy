package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import spock.lang.Issue
import spock.lang.Specification

@Issue('https://github.com/asciidoctor/asciidoctorj/issues/448')
class WhenAnInlineMacroProcessorProcessesATable extends Specification {

    public static final String UTF8 = 'UTF-8'
    public static final String HREF = 'href'
    public static final String ANCHOR_TAG = 'a'
    public static final String TARGET = 'gittutorial.html'

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    private static final String INLINE_MACRO_DOCUMENT = '''= Test document

|===
| You can find infos on man:gittutorial[7] or man:git[8, 1].
|===

'''

    private static final String INLINE_MACRO_DOCUMENT_ASCIIDOC_STYLE = '''= Test document

|===
a| You can find infos on man:gittutorial[7] or man:git[8, 1].
|===

'''


    def "a InlineMacroProcessor should be able to process table cells"() {
        when:
        asciidoctor.javaExtensionRegistry().inlineMacro(AnnotatedLongInlineMacroProcessor)
        String result = asciidoctor.convert(INLINE_MACRO_DOCUMENT, OptionsBuilder.options().standalone(false))

        then:
        Document doc = Jsoup.parse(result, UTF8)
        Element link = doc.getElementsByTag(ANCHOR_TAG).first()
        link.attr(HREF) == TARGET
    }

    def "a InlineMacroProcessor should be able to process table cells with asciidoc style"() {
        when:
        asciidoctor.javaExtensionRegistry().inlineMacro(AnnotatedLongInlineMacroProcessor)
        String result = asciidoctor.convert(INLINE_MACRO_DOCUMENT_ASCIIDOC_STYLE, OptionsBuilder.options().standalone(false))

        then:
        Document doc = Jsoup.parse(result, UTF8)
        Element link = doc.getElementsByTag(ANCHOR_TAG).first()
        link.attr(HREF) == TARGET
    }

}
