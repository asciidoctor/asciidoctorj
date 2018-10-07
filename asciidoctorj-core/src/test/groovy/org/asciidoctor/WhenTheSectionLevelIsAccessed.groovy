package org.asciidoctor

import org.asciidoctor.ast.Section
import org.asciidoctor.internal.JRubyAsciidoctor
import org.asciidoctor.util.ClasspathResources
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.Rule
import org.junit.runner.RunWith
import spock.lang.Specification

/**
 * Tests that the unsubstituted text can be retrieved from nodes
 */
@RunWith(ArquillianSputnik)
class WhenTheSectionLevelIsAccessed extends Specification {

    public static final String DOC_TITLE = 'Include other files'
    public static final String TITLE_FIRST_CHAPTER = 'First chapter'
    public static final String TITLE_FIRST_SUBSECTION = 'More First'
    public static final String TITLE_SECOND_CHAPTER = 'Second chapter'

    public static final String FILENAME_FLAT = 'parts/mainflat.adoc'
    public static final String FILENAME_WITH_INCLUDES = 'parts/main.adoc'

    public static final int ONE = 1
    public static final int TWO = 2
    public static final int THREE = 3
    public static final String CONTEXT_PARAGRAPH = 'paragraph'

    @ArquillianResource
    private Asciidoctor asciidoctor

    @Rule
    ClasspathResources classpath = new ClasspathResources()

    def 'it should return the correct level from the ContentPart API'() {

        when:
        def doc = asciidoctor.readDocumentStructure(classpath.getResource(FILENAME_FLAT),
                OptionsBuilder.options()
                        .safe(SafeMode.UNSAFE)
                        .option(JRubyAsciidoctor.STRUCTURE_MAX_LEVEL, THREE)
                        .asMap())

        then:
        doc.parts.size() == TWO
        doc.parts[0].level == ONE
        doc.parts[0].title == TITLE_FIRST_CHAPTER
        doc.parts[0].parts.size() == TWO
        def subsection = doc.parts[0].parts[ONE]
        subsection.level == THREE
        subsection.title == TITLE_FIRST_SUBSECTION
        subsection.parts.size() == ONE
        doc.parts[ONE].level == ONE
        doc.parts[ONE].title == TITLE_SECOND_CHAPTER
        doc.parts[ONE].parts.size() == ONE
    }

    def 'it should not recurse deeper than max level'() {

        when:
        def doc = asciidoctor.readDocumentStructure(classpath.getResource(FILENAME_FLAT),
                OptionsBuilder.options()
                        .safe(SafeMode.UNSAFE)
                        .option(JRubyAsciidoctor.STRUCTURE_MAX_LEVEL, TWO)
                        .asMap())

        then:
        doc.parts.size() == TWO
        doc.parts[0].level == ONE
        doc.parts[0].title == TITLE_FIRST_CHAPTER
        doc.parts[0].parts.size() == ONE
        def paragraph = doc.parts[0].parts[0]
        paragraph.level == ONE
        paragraph.title == null
        paragraph.parts == null || paragraph.parts.empty
        paragraph.context == CONTEXT_PARAGRAPH
        doc.parts[ONE].level == ONE
        doc.parts[ONE].title == TITLE_SECOND_CHAPTER
        doc.parts[ONE].parts.size() == ONE
    }

    def 'it should return the correct level from the ContentPart API with leveloffset'() {

        when:
        def doc = asciidoctor.readDocumentStructure(classpath.getResource(FILENAME_WITH_INCLUDES),
                OptionsBuilder.options()
                        .safe(SafeMode.UNSAFE)
                        .option(JRubyAsciidoctor.STRUCTURE_MAX_LEVEL, THREE)
                        .asMap())

        then:
        doc.parts.size() == TWO
        doc.parts[0].level == ONE
        doc.parts[0].title == TITLE_FIRST_CHAPTER
        doc.parts[0].parts.size() == TWO
        def subsection = doc.parts[0].parts[ONE]
        subsection.level == THREE
        subsection.title == TITLE_FIRST_SUBSECTION
        subsection.parts.size() == ONE
        doc.parts[ONE].level == ONE
        doc.parts[ONE].title == TITLE_SECOND_CHAPTER
        doc.parts[ONE].parts.size() == ONE
    }

    def 'it should not recurse deeper than max level with level offset'() {

        when:
        def doc = asciidoctor.readDocumentStructure(classpath.getResource(FILENAME_WITH_INCLUDES),
                OptionsBuilder.options()
                        .safe(SafeMode.UNSAFE)
                        .option(JRubyAsciidoctor.STRUCTURE_MAX_LEVEL, TWO)
                        .asMap())

        then:
        doc.parts.size() == TWO
        doc.parts[0].level == ONE
        doc.parts[0].title == TITLE_FIRST_CHAPTER
        doc.parts[0].parts.size() == ONE
        def paragraph = doc.parts[0].parts[0]
        paragraph.level == ONE
        paragraph.title == null
        paragraph.parts == null || paragraph.parts.empty
        paragraph.context == CONTEXT_PARAGRAPH
        doc.parts[ONE].level == ONE
        doc.parts[ONE].title == TITLE_SECOND_CHAPTER
        doc.parts[ONE].parts.size() == ONE
    }


    def 'it should return the correct level from the AST API'() {


        when:
        def doc = asciidoctor.loadFile(classpath.getResource(FILENAME_WITH_INCLUDES),
                OptionsBuilder.options().safe(SafeMode.UNSAFE).asMap())

        then:
        doc.level == 0
        doc.title == DOC_TITLE
        doc.blocks.size() == TWO
        def section1 = doc.blocks[0] as Section
        section1.level == ONE
        section1.title == TITLE_FIRST_CHAPTER
        section1.blocks.size() == TWO
        section1.blocks[ONE].level == THREE
        section1.blocks[ONE].title == TITLE_FIRST_SUBSECTION
        def section2 = doc.blocks[ONE] as Section
        section2.level == ONE
        section2.title == TITLE_SECOND_CHAPTER
        section2.blocks.size() == ONE
    }

}
