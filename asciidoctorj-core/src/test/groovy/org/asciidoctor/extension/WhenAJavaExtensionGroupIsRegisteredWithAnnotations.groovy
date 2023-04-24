package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import spock.lang.Issue
import spock.lang.Specification

@Issue('https://github.com/asciidoctor/asciidoctorj/issues/196')
class WhenAJavaExtensionGroupIsRegisteredWithAnnotations extends Specification {

    public static final String UTF8 = 'UTF-8'
    public static final String TAG_HEAD = 'head'
    public static final String ELEMENTID_FOOTER = 'footer'
    public static final String ATTR_KEY_NAME = 'name'
    public static final String ATTR_VALUE_ROBOTS = 'robots'
    public static final String DO_NOT_TOUCH_THIS = 'Do not touch this'
    public static final String THIS_SHOULD_BE_UPPERCASE = 'This should be uppercase'
    public static final String THIS_SHOULD_ALSO_BE_UPPERCASE = 'This should also be uppercase'
    public static final String HREF = 'href'
    public static final String ANCHOR_TAG = 'a'

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    private static final String DOCUMENT = '''= Test document
'''

    private static final String BLOCK_MACRO_DOCUMENT = '''= Test document

testmacro::target[]

'''

    private static final String BLOCK_DOCUMENT = '''= Test document

[yell]
Do not touch this

[yell]
----
This should be uppercase
----

[yell]
--
This should also be uppercase
--

'''

    private static final String BLOCK_DOCUMENT_2 = '''= Test document

[yell2]
Do not touch this

[yell2]
----
This should be uppercase
----

[yell2]
--
This should also be uppercase
--

'''

    private static final String INLINE_MACRO_DOCUMENT = '''= Test document

You can find infos on man:gittutorial[7] or man:git[8, 1].

'''

    private static final String INLINE_MACRO_REGEXP_DOCUMENT = '''= Test document

You can find infos on man:gittutorial[7].

And even more infos on manpage:git[7].

'''


    def "a docinfoprocessor should be configurable via the Location annotation"() {
        when:
        asciidoctor.createGroup().docinfoProcessor(AnnotatedDocinfoProcessor).register()
        String result = asciidoctor.convert(DOCUMENT, OptionsBuilder.options().standalone(true).safe(SafeMode.SERVER))

        then:
        Document doc = Jsoup.parse(result, UTF8)
        Element footer = doc.getElementById(ELEMENTID_FOOTER)
        footer.nextElementSibling().attr(ATTR_KEY_NAME) == ATTR_VALUE_ROBOTS
    }

    def "a docinfoprocessor instance should be configurable via the Location annotation"() {
        when:
        asciidoctor.createGroup().docinfoProcessor(new AnnotatedDocinfoProcessor()).register()
        String result = asciidoctor.convert(DOCUMENT, OptionsBuilder.options().standalone(true).safe(SafeMode.SERVER))

        then:
        Document doc = Jsoup.parse(result, UTF8)
        Element footer = doc.getElementById(ELEMENTID_FOOTER)
        footer.nextElementSibling().attr(ATTR_KEY_NAME) == ATTR_VALUE_ROBOTS
    }

    @spock.lang.Ignore('Option for docinfo in header changed from :header to :head in AsciidoctorJRuby. Ignore as long as these tests have to run on both Asciidoctor 1.5.2 as well as 1.5.3.dev')
    def "a docinfoprocessor instance can override the annotation from footer to header"() {
        when:
        asciidoctor.createGroup().docinfoProcessor(new AnnotatedDocinfoProcessor(LocationType.HEADER)).register()
        String result = asciidoctor.convert(DOCUMENT, OptionsBuilder.options().standalone(true).safe(SafeMode.SERVER))

        then:
        Document doc = Jsoup.parse(result, UTF8)
        !doc.getElementsByTag(TAG_HEAD)[0].getElementsByAttributeValueContaining(ATTR_KEY_NAME, ATTR_VALUE_ROBOTS).empty
    }

    def "a docinfoprocessor instance can override the annotation from footer to footer"() {
        when:
        asciidoctor.createGroup().docinfoProcessor(new AnnotatedDocinfoProcessor(LocationType.FOOTER)).register()
        String result = asciidoctor.convert(DOCUMENT, OptionsBuilder.options().standalone(true).safe(SafeMode.SERVER))

        then:
        Document doc = Jsoup.parse(result, UTF8)
        Element footer = doc.getElementById(ELEMENTID_FOOTER)
        footer.nextElementSibling().attr(ATTR_KEY_NAME) == ATTR_VALUE_ROBOTS
    }

    def "when registering a BlockMacroProcessor class it should be configurable via annotations"() {

        when:
        asciidoctor.createGroup().blockMacro(AnnotatedBlockMacroProcessor).register()
        String result = asciidoctor.convert(BLOCK_MACRO_DOCUMENT, OptionsBuilder.options().standalone(false))

        then:
        result.contains(AnnotatedBlockMacroProcessor.RESULT)
    }

    // TODO: What about the macro name when passing an instance? Do we really need BlockMacroProcessor.getName()?

    def "when registering a BlockProcessor class it should be configurable via annotations"() {

        when:
        asciidoctor.createGroup().block(AnnotatedBlockProcessor).register()
        String result = asciidoctor.convert(BLOCK_DOCUMENT, OptionsBuilder.options().standalone(false))

        then:
        result.contains(DO_NOT_TOUCH_THIS)
        result.contains(THIS_SHOULD_BE_UPPERCASE.toUpperCase())
        result.contains(THIS_SHOULD_ALSO_BE_UPPERCASE.toUpperCase())
    }

    // TODO: What about the block name when passing an instance? Do we really need BlockMacroProcessor.getName()?

    def "when registering a BlockProcessor instance it should be configurable via annotations"() {

        when:
        asciidoctor.createGroup().block(new AnnotatedBlockProcessor('dummy', 'yell2')).register()
        String result = asciidoctor.convert(BLOCK_DOCUMENT_2, OptionsBuilder.options().standalone(false))

        then:
        result.contains(DO_NOT_TOUCH_THIS)
        result.contains(THIS_SHOULD_BE_UPPERCASE.toUpperCase())
        result.contains(THIS_SHOULD_ALSO_BE_UPPERCASE.toUpperCase())
    }

    def "when registering an InlineMacroProcessor class with long format it should be configurable via annotations"() {
        when:
        asciidoctor.createGroup().inlineMacro(AnnotatedLongInlineMacroProcessor).register()
        String result = asciidoctor.convert(INLINE_MACRO_DOCUMENT, OptionsBuilder.options().standalone(false))

        then:
        Document doc = Jsoup.parse(result, UTF8)
        Element link = doc.getElementsByTag(ANCHOR_TAG).first()
        link.attr(HREF) == 'gittutorial.html'
    }

    def "when registering an InlineMacroProcessor class with regexp it should be configurable via annotations"() {
        when:
        asciidoctor.createGroup().inlineMacro(AnnotatedRegexpInlineMacroProcessor).register()
        String result = asciidoctor.convert(INLINE_MACRO_REGEXP_DOCUMENT, OptionsBuilder.options().standalone(false))

        then:
        Document doc = Jsoup.parse(result, UTF8)
        Element link = doc.getElementsByTag(ANCHOR_TAG).first()
        link.attr(HREF) == 'git.html'
    }

}
