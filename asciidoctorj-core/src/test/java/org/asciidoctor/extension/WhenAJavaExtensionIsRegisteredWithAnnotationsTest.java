package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Issue: https://github.com/asciidoctor/asciidoctorj/issues/196
public class WhenAJavaExtensionIsRegisteredWithAnnotationsTest {

    public static final String UTF8 = "UTF-8";
    public static final String TAG_HEAD = "head";
    public static final String ELEMENTID_FOOTER = "footer";
    public static final String ATTR_KEY_NAME = "name";
    public static final String ATTR_VALUE_ROBOTS = "robots";
    public static final String DO_NOT_TOUCH_THIS = "Do not touch this";
    public static final String THIS_SHOULD_BE_UPPERCASE = "This should be uppercase";
    public static final String THIS_SHOULD_ALSO_BE_UPPERCASE = "This should also be uppercase";
    public static final String HREF = "href";
    public static final String ANCHOR_TAG = "a";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private static final String DOCUMENT = """
            = Test document
            """;

    private static final String BLOCK_MACRO_DOCUMENT = """
            = Test document

            testmacro::target[]

            """;

    private static final String BLOCK_DOCUMENT = """
            = Test document

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

            """;

    private static final String BLOCK_DOCUMENT_2 = """
            = Test document

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

            """;

    private static final String INLINE_MACRO_DOCUMENT = """
            = Test document

            You can find infos on man:gittutorial[7] or man:git[8, 1].

            """;

    private static final String INLINE_MACRO_REGEXP_DOCUMENT = """
            = Test document

            You can find infos on man:gittutorial[7].

            And even more infos on manpage:git[7].

            """;


    @Test
    @DisplayName("a docinfoprocessor should be configurable via the Location annotation")
    public void aDocinfoprocessorShouldBeConfigurableViaTheLocationAnnotation() {
        // when:
        asciidoctor.javaExtensionRegistry().docinfoProcessor(AnnotatedDocinfoProcessor.class);
        String result = asciidoctor.convert(DOCUMENT, options());

        // then:
        Document doc = Jsoup.parse(result, UTF8);
        Element footer = doc.getElementById(ELEMENTID_FOOTER);
        assertThat(footer.nextElementSibling().attr(ATTR_KEY_NAME)).isEqualTo(ATTR_VALUE_ROBOTS);
    }

    @Test
    @DisplayName("a docinfoprocessor instance should be configurable via the Location annotation")
    public void aDocinfoprocessorInstanceShouldBeConfigurableViaTheLocationAnnotation() {
        // when:
        asciidoctor.javaExtensionRegistry().docinfoProcessor(new AnnotatedDocinfoProcessor());
        String result = asciidoctor.convert(DOCUMENT, options());

        // then:
        Document doc = Jsoup.parse(result, UTF8);
        Element footer = doc.getElementById(ELEMENTID_FOOTER);
        assertThat(footer.nextElementSibling().attr(ATTR_KEY_NAME)).isEqualTo(ATTR_VALUE_ROBOTS);
    }

    @Disabled("Option for docinfo in header changed from :header to :head in AsciidoctorJRuby. Ignore as long as these tests have to run on both Asciidoctor 1.5.2 as well as 1.5.3.dev")
    @Test
    @DisplayName("a docinfoprocessor instance can override the annotation from footer to header")
    public void aDocinfoprocessorInstanceCanOverrideTheAnnotationFromFooterToHeader() {
        // when:
        asciidoctor.javaExtensionRegistry().docinfoProcessor(new AnnotatedDocinfoProcessor(LocationType.HEADER));
        String result = asciidoctor.convert(DOCUMENT, options());

        // then:
        Document doc = Jsoup.parse(result, UTF8);
        assertThat(doc.getElementsByTag(TAG_HEAD).get(0).getElementsByAttributeValueContaining(ATTR_KEY_NAME, ATTR_VALUE_ROBOTS)).isNotEmpty();
    }

    @Test
    @DisplayName("a docinfoprocessor instance can override the annotation from footer to footer")
    public void aDocinfoprocessorInstanceCanOverrideTheAnnotationFromFooterToFooter() {
        // when:
        asciidoctor.javaExtensionRegistry().docinfoProcessor(new AnnotatedDocinfoProcessor(LocationType.FOOTER));
        String result = asciidoctor.convert(DOCUMENT, options());

        // then:
        Document doc = Jsoup.parse(result, UTF8);
        Element footer = doc.getElementById(ELEMENTID_FOOTER);
        assertThat(footer.nextElementSibling().attr(ATTR_KEY_NAME)).isEqualTo(ATTR_VALUE_ROBOTS);
    }

    @Test
    @DisplayName("when registering a BlockMacroProcessor class it should be configurable via annotations")
    public void whenRegisteringABlockMacroProcessorClassItShouldBeConfigurableViaAnnotations() {

        // when:
        asciidoctor.javaExtensionRegistry().blockMacro(AnnotatedBlockMacroProcessor.class);
        String result = asciidoctor.convert(BLOCK_MACRO_DOCUMENT, disableStandaloneOption());

        // then:
        assertThat(result).contains(AnnotatedBlockMacroProcessor.RESULT);
    }

    // TODO: What about the macro name when passing an instance? Do we really need BlockMacroProcessor.getName()?

    @Test
    @DisplayName("when registering a BlockProcessor class it should be configurable via annotations")
    public void whenRegisteringABlockProcessorClassItShouldBeConfigurableViaAnnotations() {

        // when:
        asciidoctor.javaExtensionRegistry().block(AnnotatedBlockProcessor.class);
        String result = asciidoctor.convert(BLOCK_DOCUMENT, disableStandaloneOption());

        // then:
        assertThat(result).contains(DO_NOT_TOUCH_THIS);
        assertThat(result).contains(THIS_SHOULD_BE_UPPERCASE.toUpperCase());
        assertThat(result).contains(THIS_SHOULD_ALSO_BE_UPPERCASE.toUpperCase());
    }

    // TODO: What about the block name when passing an instance? Do we really need BlockMacroProcessor.getName()?

    @Test
    @DisplayName("when registering a BlockProcessor instance it should be configurable via annotations")
    public void whenRegisteringABlockProcessorInstanceItShouldBeConfigurableViaAnnotations() {

        // when:
        asciidoctor.javaExtensionRegistry().block(new AnnotatedBlockProcessor("dummy", "yell2"));
        String result = asciidoctor.convert(BLOCK_DOCUMENT_2, disableStandaloneOption());

        // then:
        assertThat(result).contains(DO_NOT_TOUCH_THIS);
        assertThat(result).contains(THIS_SHOULD_BE_UPPERCASE.toUpperCase());
        assertThat(result).contains(THIS_SHOULD_ALSO_BE_UPPERCASE.toUpperCase());
    }

    @Test
    @DisplayName("when registering an InlineMacroProcessor class with long format it should be configurable via annotations")
    public void whenRegisteringAnInlineMacroProcessorClassWithLongFormatItShouldBeConfigurableViaAnnotations() {
        // when:
        asciidoctor.javaExtensionRegistry().inlineMacro(AnnotatedLongInlineMacroProcessor.class);
        String result = asciidoctor.convert(INLINE_MACRO_DOCUMENT, disableStandaloneOption());

        // then:
        Document doc = Jsoup.parse(result, UTF8);
        Element link = doc.getElementsByTag(ANCHOR_TAG).first();
        assertThat(link.attr(HREF)).isEqualTo("gittutorial.html");
    }

    @Test
    @DisplayName("when registering an InlineMacroProcessor class with regexp it should be configurable via annotations")
    public void whenRegisteringAnInlineMacroProcessorClassWithRegexpItShouldBeConfigurableViaAnnotations() {
        // when:
        asciidoctor.javaExtensionRegistry().inlineMacro(AnnotatedRegexpInlineMacroProcessor.class);
        String result = asciidoctor.convert(INLINE_MACRO_REGEXP_DOCUMENT, disableStandaloneOption());

        // then:
        Document doc = Jsoup.parse(result, UTF8);
        Element link = doc.getElementsByTag(ANCHOR_TAG).first();
        assertThat(link.attr(HREF)).isEqualTo("git.html");
    }

    private OptionsBuilder standaloneBuilder(boolean enable) {
        return Options.builder().standalone(enable);
    }

    private Options disableStandaloneOption() {
        return standaloneBuilder(false).build();
    }

    private Options options() {
        return standaloneBuilder(true).safe(SafeMode.SERVER).build();
    }
}
