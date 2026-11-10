package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

import org.asciidoctor.core.converter.AbstractConverter;
import org.asciidoctor.core.converter.BackendTraits;
import org.asciidoctor.core.converter.Converter;
import org.asciidoctor.core.converter.ConverterFactory;

/**
 * Port of converter_test.rb (ported by hand since it relies on Ruby-specific features such as mixins and singleton
 * classes; the tests of the template converter are disabled since it requires the Tilt gem).
 */
@DisplayName("Converter")
public class ConverterTest extends TestHelper {
    static final String TEMPLATES = "custom converter templates are not supported (they require the Tilt gem)";

    @Nested
    @DisplayName("View options")
    class ViewOptions {

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should set Haml format to html5 for html5 backend")
        void shouldSetHamlFormatToHtml5ForHtml5Backend() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should set Haml format to xhtml for docbook backend")
        void shouldSetHamlFormatToXhtmlForDocbookBackend() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should configure Slim to resolve includes in specified template dirs")
        void shouldConfigureSlimToResolveIncludesInSpecifiedTemplateDirs() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should coerce template_dirs option to an Array")
        void shouldCoerceTemplate_dirsOptionToAnArray() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should set Slim format to html for html5 backend")
        void shouldSetSlimFormatToHtmlForHtml5Backend() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should set Slim format to nil for docbook backend")
        void shouldSetSlimFormatToNilForDocbookBackend() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should set safe mode of Slim AsciiDoc engine to match document safe mode when Slim >= 3")
        void shouldSetSafeModeOfSlimAsciiDocEngineToMatchDocumentSafeModeWhenSlim3() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should support custom template engine options for known engine")
        void shouldSupportCustomTemplateEngineOptionsForKnownEngine() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should support custom template engine options")
        void shouldSupportCustomTemplateEngineOptions() {
        }
    }

    @Nested
    @DisplayName("Custom backends")
    class CustomBackends {

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should load Haml templates for default backend")
        void shouldLoadHamlTemplatesForDefaultBackend() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should set outfilesuffix according to backend info")
        void shouldSetOutfilesuffixAccordingToBackendInfo() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should not override outfilesuffix attribute if locked")
        void shouldNotOverrideOutfilesuffixAttributeIfLocked() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should load Haml templates for docbook5 backend")
        void shouldLoadHamlTemplatesForDocbook5Backend() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should use Haml templates in place of built-in templates")
        void shouldUseHamlTemplatesInPlaceOfBuiltInTemplates() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should allow custom backend to emulate a known backend")
        void shouldAllowCustomBackendToEmulateAKnownBackend() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should create template converter even when a converter is not registered for the specified backend")
        void shouldCreateTemplateConverterEvenWhenAConverterIsNotRegisteredForTheSpecifiedBackend() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should use built-in global cache to cache templates")
        void shouldUseBuiltInGlobalCacheToCacheTemplates() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should use custom cache to cache templates")
        void shouldUseCustomCacheToCacheTemplates() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should be able to disable template cache")
        void shouldBeAbleToDisableTemplateCache() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should load ERB templates using ERBTemplate if eruby is not set")
        void shouldLoadERBTemplatesUsingERBTemplateIfErubyIsNotSet() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should load ERB templates using ErubiTemplate if eruby is set to erubi")
        void shouldLoadERBTemplatesUsingErubiTemplateIfErubyIsSetToErubi() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should load Slim templates for default backend")
        void shouldLoadSlimTemplatesForDefaultBackend() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should load Slim templates for docbook5 backend")
        void shouldLoadSlimTemplatesForDocbook5Backend() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should use Slim templates in place of built-in templates")
        void shouldUseSlimTemplatesInPlaceOfBuiltInTemplates() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should be able to override the outline using a custom template")
        void shouldBeAbleToOverrideTheOutlineUsingACustomTemplate() {
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("resolves templates from classloader when using JRuby")
        void resolvesTemplatesFromClassloaderWhenUsingJRuby() {
        }
    }

    public static class CustomHtmlConverterA implements Converter<String> {
        public CustomHtmlConverterA(String backend, Map<String, Object> opts) {
        }

        @Override
        public String convert(AbstractNode node, String transform, Map<String, Object> opts) {
            return "document";
        }
    }

    public static class CustomTextConverterA extends CustomHtmlConverterA {
        public CustomTextConverterA(String backend, Map<String, Object> opts) {
            super(backend, opts);
        }
    }

    public static class MyConverterClass extends AbstractConverter<String> {
        public MyConverterClass(String backend, Map<String, Object> opts) {
            super(backend, opts);
            handle("document", AbstractNode.class, node -> "document");
        }
    }

    public static class CustomConverterB extends AbstractConverter<String> {
        public CustomConverterB(String backend, Map<String, Object> opts) {
            super(backend, opts);
            setBasebackend("text");
            setFiletype("text");
            setOutfilesuffix(".fb");
        }

        @Override
        public String convert(AbstractNode node, String transform, Map<String, Object> opts) {
            return "foobar content";
        }
    }

    public static class SlidesConverter extends AbstractConverter<String> {
        public SlidesConverter(String backend, Map<String, Object> opts) {
            super(backend, opts);
            setBasebackend("html");
        }
    }

    public static class CustomConverterC extends AbstractConverter<String> {
        public CustomConverterC(String backend) {
            super(backend);
            handle("paragraph", AbstractNode.class, node -> "paragraph");
        }
    }

    /** A converter that converts documents and paragraphs. */
    public static class SimpleHtmlConverter extends AbstractConverter<String> {
        public SimpleHtmlConverter(String backend, Map<String, Object> opts) {
            super(backend, opts);
            handle("document", AbstractBlock.class, node -> String.join("\n", "<!DOCTYPE html>", "<html>", "<body>",
                    node.getContent(), "</body>", "</html>"));
            handle("paragraph", AbstractBlock.class, node -> String.join("\n", "<div class=\"paragraph\">",
                    "<p>" + node.getContent() + "</p>", "</div>"));
        }
    }

    public static class CustomConverterWithMissingHandlers extends AbstractConverter<String> {
        public CustomConverterWithMissingHandlers(String backend, Map<String, Object> opts) {
            super(backend, opts);
        }
    }

    public static class CustomConverterF implements Converter<String> {
        @Override
        public String convert(AbstractNode node, String transform, Map<String, Object> opts) {
            return "foobaz content";
        }
    }

    public static class FooConverter extends AbstractConverter<String> {
        public FooConverter(String backend) {
            super(backend);
        }
    }

    public static class CatchAllConverter extends FooConverter {
        public CatchAllConverter(String backend) {
            super(backend);
        }
    }

    @Nested
    @DisplayName("Custom converters")
    class CustomConverters {

        @Test
        @Disabled("the included hook of a Ruby module has no equivalent in Java")
        @DisplayName("should not expose included method on Converter class")
        void shouldNotExposeIncludedMethodOnConverterClass() {
        }

        @Test
        @DisplayName("should derive backend traits for the given backend")
        void shouldDeriveBackendTraitsForTheGivenBackend() throws Exception {
            var expected = new BackendTraits.Values("dita", "dita", null, ".dita");
            var actual = BackendTraits.derive("dita2", null);
            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("should use specified converter for current backend")
        void shouldUseSpecifiedConverterForCurrentBackend() throws Exception {
            var input = """
                    = Document Title

                    preamble

                    == Section

                    content
                    """;
            var doc = documentFromString(input, Options.of("converter", CustomHtmlConverterA.class));
            assertInstanceOf(CustomHtmlConverterA.class, doc.getConverter());
            assertEquals("html", get(doc.getAttributes(), "filetype"));
            assertEquals("document", doc.convert());
        }

        @Test
        @DisplayName("should use specified converter for specified backend")
        void shouldUseSpecifiedConverterForSpecifiedBackend() throws Exception {
            var input = """
                    = Document Title

                    preamble

                    == Section

                    content
                    """;
            var doc = documentFromString(input, Options.of("backend", "text", "converter", CustomTextConverterA.class));
            assertInstanceOf(CustomTextConverterA.class, doc.getConverter());
            assertEquals("text", get(doc.getAttributes(), "filetype"));
            assertEquals("document", doc.convert());
        }

        @Test
        @DisplayName("should get converter from specified converter factory")
        void shouldGetConverterFromSpecifiedConverterFactory() throws Exception {
            var input = """
                    = Document Title

                    preamble

                    == Section

                    content
                    """;
            var converterFactory = ConverterFactory.custom(Map.of("html5", MyConverterClass.class));
            var doc = documentFromString(input, Options.of("converter_factory", converterFactory));
            assertInstanceOf(MyConverterClass.class, doc.getConverter());
            assertEquals("html", get(doc.getAttributes(), "filetype"));
            assertEquals("document", doc.convert());
        }

        @Test
        @DisplayName("should allow converter to set htmlsyntax when basebackend is html")
        void shouldAllowConverterToSetHtmlsyntaxWhenBasebackendIsHtml() throws Exception {
            var input = "image::sunset.jpg[]";
            var converter = Converter.create("html5", Options.of("htmlsyntax", "xml"));
            var doc = documentFromString(input, Options.of("converter", converter));
            assertEquals(converter, doc.getConverter());
            assertEquals("xml", doc.getAttribute("htmlsyntax"));
            var output = doc.convert(Options.of("standalone", false));
            assertIncludes(output, "<img src=\"sunset.jpg\" alt=\"sunset\"/>");
        }

        @Test
        @DisplayName("should use converter registered for backend")
        void shouldUseConverterRegisteredForBackend() throws Exception {
            try {
                var convertersBefore = Converter.getFactory().getConverters();
                Converter.register(CustomConverterB.class, "foobar");
                var input = "content";
                assertEquals(CustomConverterB.class, Converter.forBackend("foobar"));
                var converters = Converter.getFactory().getConverters();
                assertEquals(size(convertersBefore) + 1, size(converters));
                assertEquals(CustomConverterB.class, get(converters, "foobar"));
                var output = convertString(input, Options.of("backend", "foobar"));
                assertEquals("foobar content", output);
            } finally {
                Converter.getFactory().unregisterAll();
            }
        }

        @Test
        @DisplayName("should be able to register converter using symbol")
        void shouldBeAbleToRegisterConverterUsingSymbol() throws Exception {
            try {
                Converter.register(CustomConverterB.class, "foobaz");
                assertEquals(CustomConverterB.class, Converter.forBackend("foobaz"));
            } finally {
                Converter.getFactory().unregisterAll();
            }
        }

        @Test
        @DisplayName("should use basebackend to compute filetype and outfilesuffix")
        void shouldUseBasebackendToComputeFiletypeAndOutfilesuffix() throws Exception {
            try {
                assertNull(Converter.forBackend("slides"));
                Converter.register(SlidesConverter.class, "slides");
                var doc = documentFromString("content", Options.of("backend", "slides"));
                assertEquals(".html", doc.getOutfilesuffix());
                var converter = (AbstractConverter) doc.getConverter();
                var expectedTraits = new BackendTraits.Values("html", "html", "html", ".html");
                assertEquals(expectedTraits, new BackendTraits.Values(converter.getBasebackend(), converter.getFiletype(),
                        converter.getHtmlsyntax(), converter.getOutfilesuffix()));
            } finally {
                Converter.getFactory().unregisterAll();
            }
        }

        @Test
        @DisplayName("should be able to register converter from converter class itself")
        void shouldBeAbleToRegisterConverterFromConverterClassItself() throws Exception {
            try {
                assertNull(Converter.forBackend("foobar"));
                Converter.register(CustomConverterF.class, "foobar");
                assertEquals(CustomConverterF.class, Converter.forBackend("foobar"));
            } finally {
                Converter.getFactory().unregisterAll();
            }
        }

        @Test
        @DisplayName("should map handles? method on converter to respond_to? implementation by default")
        void shouldMapHandlesMethodOnConverterToRespondToImplementationByDefault() throws Exception {
            var converter = new CustomConverterC("myhtml");
            assertTruthy(converter.handles("paragraph"));
            assertFalsy(converter.handles("sentence"));
        }

        @Test
        @DisplayName("should not configure converter to support templates by default")
        void shouldNotConfigureConverterToSupportTemplatesByDefault() throws Exception {
            try {
                Converter.register(SimpleHtmlConverter.class, "myhtml");
                var input = "paragraph";
                var doc = documentFromString(input, Options.of("backend", "myhtml"));
                assertInstanceOf(SimpleHtmlConverter.class, doc.getConverter());
                assertFalsy(((AbstractConverter) doc.getConverter()).isSupportsTemplates());
                var output = doc.convert();
                assertXpath("//*[@class=\"paragraph\"]/p[text()=\"paragraph\"]", output, 1);
            } finally {
                Converter.getFactory().unregisterAll();
            }
        }

        @Test
        @Disabled(TEMPLATES)
        @DisplayName("should wrap converter in composite converter with template converter if it declares that it supports templates")
        void shouldWrapConverterInCompositeConverterWithTemplateConverterIfItDeclaresThatItSupportsTemplates() {
        }

        @Test
        @DisplayName("should map Factory.new to DefaultFactoryProxy constructor by default")
        void shouldMapFactoryNewToDefaultFactoryProxyConstructorByDefault() throws Exception {
            assertEquals(Converter.forBackend("html5"), ConverterFactory.create(Map.of(), true).forBackend("html5"));
        }

        @Test
        @DisplayName("should map Factory.new to CustomFactory constructor if proxy keyword arg is false")
        void shouldMapFactoryNewToCustomFactoryConstructorIfProxyKeywordArgIsFalse() throws Exception {
            assertNull(ConverterFactory.create(Map.of(), false).forBackend("html5"));
        }

        @Test
        @DisplayName("should default to catch all converter")
        void shouldDefaultToCatchAllConverter() throws Exception {
            try {
                Converter.register(CustomConverterF.class, "*");
                var input = "content";
                assertEquals(CustomConverterF.class, Converter.forBackend("all"));
                assertEquals(CustomConverterF.class, Converter.forBackend("whatever"));
                assertNotEquals(CustomConverterF.class, Converter.forBackend("html5"));
                var converters = Converter.getFactory().getConverters();
                assertNull(get(converters, "*"));
                var output = convertString(input, Options.of("backend", "foobaz"));
                assertEquals("foobaz content", output);
            } finally {
                Converter.getFactory().unregisterAll();
            }
        }

        @Test
        @DisplayName("should use catch all converter from custom factory only if no other converter matches")
        void shouldUseCatchAllConverterFromCustomFactoryOnlyIfNoOtherConverterMatches() throws Exception {
            var factory = ConverterFactory.custom(Map.of("foo", FooConverter.class, "*", CatchAllConverter.class));
            assertEquals(FooConverter.class, factory.forBackend("foo"));
            assertEquals(CatchAllConverter.class, factory.forBackend("nada"));
            assertEquals(CatchAllConverter.class, factory.forBackend("html5"));
        }

        @Test
        @DisplayName("should prefer catch all converter from proxy over statically registered catch all converter")
        void shouldPreferCatchAllConverterFromProxyOverStaticallyRegisteredCatchAllConverter() throws Exception {
            try {
                Converter.register(CatchAllConverter.class, "*");
                var factory = ConverterFactory.proxy(Map.of("*", FooConverter.class));
                assertEquals(FooConverter.class, factory.forBackend("foobar"));
                assertNotEquals(FooConverter.class, factory.forBackend("html5"));
                assertNotEquals(CatchAllConverter.class, factory.forBackend("html5"));
            } finally {
                Converter.getFactory().unregisterAll();
            }
        }

        @Test
        @DisplayName("should prefer converter in proxy with same name as provided converter")
        void shouldPreferConverterInProxyWithSameNameAsProvidedConverter() throws Exception {
            var factory = ConverterFactory.proxy(Map.of("html5", FooConverter.class));
            assertEquals(FooConverter.class, factory.forBackend("html5"));
        }

        @Test
        @DisplayName("should allow nil to be registered as converter")
        void shouldAllowNilToBeRegisteredAsConverter() throws Exception {
            var factory = ConverterFactory.proxy(Options.of("html5", null));
            assertNull(factory.forBackend("html5"));
        }

        @Test
        @DisplayName("should create a new custom factory when Converter::Factory.new is invoked")
        void shouldCreateANewCustomFactoryWhenConverterFactoryNewIsInvoked() throws Exception {
            var converters = Map.of("mine", FooConverter.class);
            var factory = ConverterFactory.create(converters, false);
            assertInstanceOf(ConverterFactory.CustomFactory.class, factory);
            assertEquals(FooConverter.class, factory.forBackend("mine"));
        }

        @Test
        @DisplayName("should delegate to method on HTML 5 converter with convert_ prefix if called without prefix")
        void shouldDelegateToMethodOnHTML5ConverterWithConvert_PrefixIfCalledWithoutPrefix() throws Exception {
            var doc = documentFromString("paragraph");
            assertTruthy(doc.getConverter().handles("paragraph"));
            var result = doc.getConverter().convert(doc.getBlocks().get(0), "paragraph");
            assertCss("p", result, 1);
        }

        @Test
        @DisplayName("should not delegate unprefixed method on HTML 5 converter if converter does not handle transform")
        void shouldNotDelegateUnprefixedMethodOnHTML5ConverterIfConverterDoesNotHandleTransform() throws Exception {
            var doc = documentFromString("paragraph");
            assertFalsy(doc.getConverter().handles("sentence"));
            // NOTE in Java, the converter logs a warning and returns null (there's no method to call directly)
            try (var logger = usingMemoryLogger()) {
                assertNull(doc.getConverter().convert(doc.getBlocks().get(0), "sentence"));
                assertMessage(logger, "WARN", "~missing convert handler for sentence node in html5 backend");
            }
        }

        @Test
        @DisplayName("should warn when convert method for node is missing")
        void shouldWarnWhenConvertMethodForNodeIsMissing() throws Exception {
            try {
                Converter.register(CustomConverterWithMissingHandlers.class, "fizzbuzz");
                var doc = documentFromString("word", Options.of("backend", "fizzbuzz"));
                try (var logger = usingMemoryLogger()) {
                    assertNull(doc.convert());
                    assertMessage(logger, "WARN", "~missing convert handler for document node in fizzbuzz backend (" + CustomConverterWithMissingHandlers.class.getName() + ")");
                }
            } finally {
                Converter.getFactory().unregisterAll();
            }
        }

        @Test
        @DisplayName("can call read_svg_contents on built-in HTML5 converter; should remove markup prior the root svg element")
        void canCallRead_svg_contentsOnBuiltInHTML5ConverterShouldRemoveMarkupPriorTheRootSvgElement() throws Exception {
            var doc = documentFromString("image::circle.svg[]", Options.of("base_dir", fixturedir()));
            var result = ((org.asciidoctor.core.converter.Html5Converter) doc.getConverter()).readSvgContents(doc.getBlocks().get(0), "circle.svg");
            assertNotNull(result);
            assertTruthy(result.startsWith("<svg"));
        }
    }
}
