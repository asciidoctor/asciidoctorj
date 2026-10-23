package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.*;
import org.asciidoctor.core.converter.Html5Converter;
import org.asciidoctor.core.converter.DocBook5Converter;
import java.nio.charset.StandardCharsets;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.*;

@DisplayName("Document")
public class DocumentTest extends TestHelper {
    static final java.util.List<String> BUILT_IN_ELEMENTS = list("admonition", "audio", "colist", "dlist", "document", "embedded", "example", "floating_title", "image", "inline_anchor", "inline_break", "inline_button", "inline_callout", "inline_footnote", "inline_image", "inline_indexterm", "inline_kbd", "inline_menu", "inline_quoted", "listing", "literal", "stem", "olist", "open", "page_break", "paragraph", "pass", "preamble", "quote", "section", "sidebar", "table", "thematic_break", "toc", "ulist", "verse", "video");

    @Nested
    @DisplayName("Example document")
    class ExampleDocument {

        @Test
        @DisplayName("document title")
        void documentTitle() throws Exception {
            var doc = exampleDocument("asciidoc_index");
            assertEquals("AsciiDoc Home Page", doc.getDoctitle());
            assertEquals("AsciiDoc Home Page", doc.getName());
            assertNotNull(doc.getHeader());
            assertEquals("section", doc.getHeader().getContext());
            assertEquals("header", doc.getHeader().getSectionName());
            assertEquals(14, size(doc.getBlocks()));
            assertEquals("preamble", doc.getBlocks().get(0).getContext());
            assertEquals("section", doc.getBlocks().get(1).getContext());
            var result = doc.getBlocks().get(0).convert();
            assertXpath("//em[text()=\"Stuart Rackham\"]", result, 1);
        }
    }

    @Nested
    @DisplayName("Default settings")
    class DefaultSettings {

        @Test
        @DisplayName("safe mode level set to SECURE by default")
        void safeModeLevelSetToSECUREByDefault() throws Exception {
            var doc = emptyDocument();
            assertEquals(SafeMode.SECURE.getLevel(), doc.getSafe());
        }

        @Test
        @DisplayName("safe mode level set using string")
        void safeModeLevelSetUsingString() throws Exception {
            var doc = emptyDocument(Options.of("safe", "server"));
            assertEquals(SafeMode.SERVER.getLevel(), doc.getSafe());
            doc = emptyDocument(Options.of("safe", "foo"));
            assertEquals(SafeMode.SECURE.getLevel(), doc.getSafe());
        }

        @Test
        @DisplayName("safe mode level set using symbol")
        void safeModeLevelSetUsingSymbol() throws Exception {
            var doc = emptyDocument(Options.of("safe", "server"));
            assertEquals(SafeMode.SERVER.getLevel(), doc.getSafe());
            doc = emptyDocument(Options.of("safe", "foo"));
            assertEquals(SafeMode.SECURE.getLevel(), doc.getSafe());
        }

        @Test
        @DisplayName("safe mode level set using integer")
        void safeModeLevelSetUsingInteger() throws Exception {
            var doc = emptyDocument(Options.of("safe", 10));
            assertEquals(SafeMode.SERVER.getLevel(), doc.getSafe());
            doc = emptyDocument(Options.of("safe", 100));
            assertEquals(100, doc.getSafe());
        }

        @Test
        @DisplayName("safe mode attributes are set on document")
        void safeModeAttributesAreSetOnDocument() throws Exception {
            var doc = emptyDocument();
            assertEquals(SafeMode.SECURE.getLevel(), doc.getAttribute("safe-mode-level"));
            assertEquals("secure", doc.getAttribute("safe-mode-name"));
            assertTruthy(doc.hasAttribute("safe-mode-secure"));
            assertFalsy(doc.hasAttribute("safe-mode-unsafe"));
            assertFalsy(doc.hasAttribute("safe-mode-safe"));
            assertFalsy(doc.hasAttribute("safe-mode-server"));
        }

        @Test
        @DisplayName("safe mode level can be set in the constructor")
        void safeModeLevelCanBeSetInTheConstructor() throws Exception {
            var doc = new Document(new ArrayList<String>(), Options.of("safe", SafeMode.SAFE.getLevel()));
            assertEquals(SafeMode.SAFE.getLevel(), doc.getSafe());
        }

        @Test
        @DisplayName("safe model level cannot be modified")
        void safeModelLevelCannotBeModified() throws Exception {
            assertThrows(NoSuchMethodException.class, () -> Document.class.getMethod("setSafe", int.class),
                    "safe mode property of Document should not be writable!");
        }

        @Test
        @DisplayName("toc and sectnums should be enabled by default in DocBook backend")
        void tocAndSectnumsShouldBeEnabledByDefaultInDocBookBackend() throws Exception {
            var doc = documentFromString("content", Options.of("backend", "docbook", "parse", true));
            assertTruthy(doc.hasAttribute("toc"));
            assertTruthy(doc.hasAttribute("sectnums"));
            var result = doc.convert();
            assertMatch("<?asciidoc-toc?>", result);
            assertMatch("<?asciidoc-numbered?>", result);
        }

        @Test
        @DisplayName("maxdepth attribute should be set on asciidoc-toc and asciidoc-numbered processing instructions in DocBook backend")
        void maxdepthAttributeShouldBeSetOnAsciidocTocAndAsciidocNumberedProcessingInstructionsInDocBookBackend() throws Exception {
            var doc = documentFromString("content", Options.of("backend", "docbook", "parse", true, "attributes", Options.of("toclevels", "1", "sectnumlevels", "1")));
            assertTruthy(doc.hasAttribute("toc"));
            assertTruthy(doc.hasAttribute("sectnums"));
            var result = doc.convert();
            assertMatch("<?asciidoc-toc maxdepth=\"1\"?>", result);
            assertMatch("<?asciidoc-numbered maxdepth=\"1\"?>", result);
        }

        @Test
        @DisplayName("should be able to disable toc and sectnums in document header in DocBook backend")
        void shouldBeAbleToDisableTocAndSectnumsInDocumentHeaderInDocBookBackend() throws Exception {
            var input = """
                    = Document Title
                    :toc!:
                    :sectnums!:
                    """;
            var doc = documentFromString(input, Options.of("backend", "docbook"));
            assertFalsy(doc.hasAttribute("toc"));
            assertFalsy(doc.hasAttribute("sectnums"));
        }

        @Test
        @DisplayName("noheader attribute should suppress info element when converting to DocBook")
        void noheaderAttributeShouldSuppressInfoElementWhenConvertingToDocBook() throws Exception {
            var input = """
                    = Document Title
                    :noheader:

                    content
                    """;
            var result = convertString(input, Options.of("backend", "docbook"));
            assertXpath("/article", result, 1);
            assertXpath("/article/info", result, 0);
        }

        @Test
        @DisplayName("should be able to disable section numbering using numbered attribute in document header in DocBook backend")
        void shouldBeAbleToDisableSectionNumberingUsingNumberedAttributeInDocumentHeaderInDocBookBackend() throws Exception {
            var input = """
                    = Document Title
                    :numbered!:
                    """;
            var doc = documentFromString(input, Options.of("backend", "docbook"));
            assertFalsy(doc.hasAttribute("sectnums"));
        }
    }

    @Nested
    @DisplayName("Docinfo files")
    class DocinfoFiles {

        @Test
        @DisplayName("should include docinfo files for html backend")
        void shouldIncludeDocinfoFilesForHtmlBackend() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var cases = Options.of("docinfo", Options.of("head_script", 1, "meta", 0, "top_link", 0, "footer_script", 1, "navbar", 1), "docinfo=private", Options.of("head_script", 1, "meta", 0, "top_link", 0, "footer_script", 1, "navbar", 1), "docinfo1", Options.of("head_script", 0, "meta", 1, "top_link", 1, "footer_script", 0, "navbar", 0), "docinfo=shared", Options.of("head_script", 0, "meta", 1, "top_link", 1, "footer_script", 0, "navbar", 0), "docinfo2", Options.of("head_script", 1, "meta", 1, "top_link", 1, "footer_script", 1, "navbar", 1), "docinfo docinfo2", Options.of("head_script", 1, "meta", 1, "top_link", 1, "footer_script", 1, "navbar", 1), "docinfo=private,shared", Options.of("head_script", 1, "meta", 1, "top_link", 1, "footer_script", 1, "navbar", 1), "docinfo=private-head", Options.of("head_script", 1, "meta", 0, "top_link", 0, "footer_script", 0, "navbar", 0), "docinfo=private-header", Options.of("head_script", 0, "meta", 0, "top_link", 0, "footer_script", 0, "navbar", 1), "docinfo=shared-head", Options.of("head_script", 0, "meta", 1, "top_link", 0, "footer_script", 0, "navbar", 0), "docinfo=private-footer", Options.of("head_script", 0, "meta", 0, "top_link", 0, "footer_script", 1, "navbar", 0), "docinfo=shared-footer", Options.of("head_script", 0, "meta", 0, "top_link", 1, "footer_script", 0, "navbar", 0), "docinfo=private-head\\ ,\\ shared-footer", Options.of("head_script", 1, "meta", 0, "top_link", 1, "footer_script", 0, "navbar", 0));
            for (var entry : entries(cases)) {
                var attrVal = entry.getKey();
                var markup = entry.getValue();
                var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", SafeMode.SERVER.getLevel(), "attributes", ("linkcss copycss! " + str(attrVal))));
                refuteEmpty(output);
                assertCss("script[src=\"modernizr.js\"]", output, get(markup, "head_script"));
                assertCss("meta[http-equiv=\"imagetoolbar\"]", output, get(markup, "meta"));
                assertCss("body > a#top", output, get(markup, "top_link"));
                assertCss("body > script", output, get(markup, "footer_script"));
                assertCss("body > nav.navbar", output, get(markup, "navbar"));
                assertCss("body > nav.navbar + #header", output, get(markup, "navbar"));
            }
        }

        @Test
        @DisplayName("should include docinfo header even if noheader attribute is set")
        void shouldIncludeDocinfoHeaderEvenIfNoheaderAttributeIsSet() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo", "private-header", "noheader", "")));
            refuteEmpty(output);
            assertCss("body > nav.navbar", output, 1);
            assertCss("body > nav.navbar + #content", output, 1);
        }

        @Test
        @DisplayName("should include docinfo footer even if nofooter attribute is set")
        void shouldIncludeDocinfoFooterEvenIfNofooterAttributeIsSet() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo1", "", "nofooter", "")));
            refuteEmpty(output);
            assertCss("body > a#top", output, 1);
        }

        @Test
        @DisplayName("should include user docinfo after built-in docinfo")
        void shouldIncludeUserDocinfoAfterBuiltInDocinfo() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var attrs = Options.of("docinfo", "shared", "source-highlighter", "highlight.js", "linkcss", "", "copycss", null);
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", "safe", "attributes", attrs));
            assertCss("link[rel=stylesheet] + meta[http-equiv=imagetoolbar]", output, 1);
            assertCss("meta[http-equiv=imagetoolbar] + *", output, 0);
            assertCss("script + a#top", output, 1);
            assertCss("a#top + *", output, 0);
        }

        @Test
        @DisplayName("should include docinfo files for html backend with custom docinfodir")
        void shouldIncludeDocinfoFilesForHtmlBackendWithCustomDocinfodir() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo", "", "docinfodir", "custom-docinfodir")));
            refuteEmpty(output);
            assertCss("script[src=\"bootstrap.js\"]", output, 1);
            assertCss("meta[name=\"robots\"]", output, 0);
            output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo1", "", "docinfodir", "custom-docinfodir")));
            refuteEmpty(output);
            assertCss("script[src=\"bootstrap.js\"]", output, 0);
            assertCss("meta[name=\"robots\"]", output, 1);
            output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo2", "", "docinfodir", "./custom-docinfodir")));
            refuteEmpty(output);
            assertCss("script[src=\"bootstrap.js\"]", output, 1);
            assertCss("meta[name=\"robots\"]", output, 1);
            output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo2", "", "docinfodir", "custom-docinfodir/subfolder")));
            refuteEmpty(output);
            assertCss("script[src=\"bootstrap.js\"]", output, 0);
            assertCss("meta[name=\"robots\"]", output, 0);
        }

        @Test
        @DisplayName("should include docinfo files in docbook backend")
        void shouldIncludeDocinfoFilesInDocbookBackend() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "backend", "docbook", "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo", "")));
            refuteEmpty(output);
            assertCss("productname", output, 0);
            assertCss("copyright", output, 1);
            output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "backend", "docbook", "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo1", "")));
            refuteEmpty(output);
            assertCss("productname", output, 1);
            assertXpath("//xmlns:productname[text()=\"Asciidoctor™\"]", output, 1);
            assertCss("edition", output, 1);
            assertXpath("//xmlns:edition[text()=\"1.0\"]", output, 1);
            assertCss("copyright", output, 0);
            output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "backend", "docbook", "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo2", "")));
            refuteEmpty(output);
            assertCss("productname", output, 1);
            assertXpath("//xmlns:productname[text()=\"Asciidoctor™\"]", output, 1);
            assertCss("edition", output, 1);
            assertXpath("//xmlns:edition[text()=\"1.0\"]", output, 1);
            assertCss("copyright", output, 1);
        }

        @Test
        @DisplayName("should use header docinfo in place of default header")
        void shouldUseHeaderDocinfoInPlaceOfDefaultHeader() throws Exception {
            var output = Asciidoctor.convertFile(fixturePath("sample.adoc"), Options.of("to_file", false, "standalone", true, "backend", "docbook", "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo", "private-header", "noheader", "")));
            refuteEmpty(output);
            assertCss("article > info", output, 1);
            assertCss("article > info > title", output, 1);
            assertCss("article > info > revhistory", output, 1);
            assertCss("article > info > revhistory > revision", output, 2);
        }

        @Test
        @DisplayName("should include docinfo footer files for html backend")
        void shouldIncludeDocinfoFooterFilesForHtmlBackend() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo", "")));
            refuteEmpty(output);
            assertCss("body script", output, 1);
            assertCss("a#top", output, 0);
            output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo1", "")));
            refuteEmpty(output);
            assertCss("body script", output, 0);
            assertCss("a#top", output, 1);
            output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo2", "")));
            refuteEmpty(output);
            assertCss("body script", output, 1);
            assertCss("a#top", output, 1);
        }

        @Test
        @DisplayName("should include docinfo footer files in DocBook backend")
        void shouldIncludeDocinfoFooterFilesInDocBookBackend() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "backend", "docbook", "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo", "")));
            refuteEmpty(output);
            assertCss("article > revhistory", output, 1);
            assertXpath("/xmlns:article/xmlns:revhistory/xmlns:revision/xmlns:revnumber[text()=\"1.0\"]", output, 1);
            assertCss("glossary", output, 0);
            output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "backend", "docbook", "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo1", "")));
            refuteEmpty(output);
            assertCss("article > revhistory", output, 0);
            assertCss("glossary[xml|id=\"_glossary\"]", output, 1);
            output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "backend", "docbook", "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo2", "")));
            refuteEmpty(output);
            assertCss("article > revhistory", output, 1);
            assertXpath("/xmlns:article/xmlns:revhistory/xmlns:revision/xmlns:revnumber[text()=\"1.0\"]", output, 1);
            assertCss("glossary[xml|id=\"_glossary\"]", output, 1);
        }

        @Test
        @DisplayName("should force encoding of docinfo files to UTF-8")
        void shouldForceEncodingOfDocinfoFilesToUTF8() throws Exception {
            // NOTE Java does not have a default internal/external encoding; docinfo files are always read as UTF-8
            var sampleInputPath = fixturePath("basic.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "backend", "docbook", "safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docinfo", "private,shared")));
            refuteEmpty(output);
            assertCss("productname", output, 1);
            assertIncludes(output, "<productname>Asciidoctor™</productname>");
            assertCss("edition", output, 1);
            assertXpath("//xmlns:edition[text()=\"1.0\"]", output, 1);
            assertCss("copyright", output, 1);
        }

        @Test
        @DisplayName("should not include docinfo files by default")
        void shouldNotIncludeDocinfoFilesByDefault() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", SafeMode.SERVER.getLevel()));
            refuteEmpty(output);
            assertCss("script[src=\"modernizr.js\"]", output, 0);
            assertCss("meta[http-equiv=\"imagetoolbar\"]", output, 0);
            output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "backend", "docbook", "safe", SafeMode.SERVER.getLevel()));
            refuteEmpty(output);
            assertCss("productname", output, 0);
            assertCss("copyright", output, 0);
        }

        @Test
        @DisplayName("should not include docinfo files if safe mode is SECURE or greater")
        void shouldNotIncludeDocinfoFilesIfSafeModeIsSECUREOrGreater() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "attributes", Options.of("docinfo2", "")));
            refuteEmpty(output);
            assertCss("script[src=\"modernizr.js\"]", output, 0);
            assertCss("meta[http-equiv=\"imagetoolbar\"]", output, 0);
            output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "backend", "docbook", "attributes", Options.of("docinfo2", "")));
            refuteEmpty(output);
            assertCss("productname", output, 0);
            assertCss("copyright", output, 0);
        }

        @Test
        @DisplayName("should substitute attributes in docinfo files by default")
        void shouldSubstituteAttributesInDocinfoFilesByDefault() throws Exception {
            var sampleInputPath = fixturePath("subs.adoc");
            try (var logger = usingMemoryLogger()) {
                var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", "server", "attributes", Options.of("docinfo", "", "bootstrap-version", null, "linkcss", "", "attribute-missing", "drop-line")));
                refuteEmpty(output);
                assertCss("script", output, 0);
                assertXpath("//meta[@name=\"copyright\"][@content=\"(C) OpenDevise\"]", output, 1);
                assertMessage(logger, "INFO", "dropping line containing reference to missing attribute: bootstrap-version");
            }
        }

        @Test
        @DisplayName("should apply explicit substitutions to docinfo files")
        void shouldApplyExplicitSubstitutionsToDocinfoFiles() throws Exception {
            var sampleInputPath = fixturePath("subs.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", false, "standalone", true, "safe", "server", "attributes", Options.of("docinfo", "", "docinfosubs", "attributes,replacements", "linkcss", "")));
            refuteEmpty(output);
            assertCss("script[src=\"bootstrap.3.2.0.min.js\"]", output, 1);
            assertXpath(("//meta[@name=\"copyright\"][@content=\"" + str(decodeChar(169)) + " OpenDevise\"]"), output, 1);
        }
    }

    @Nested
    @DisplayName("MathJax")
    class MathJax {

        @Test
        @DisplayName("should add MathJax script to HTML head if stem attribute is set")
        void shouldAddMathJaxScriptToHTMLHeadIfStemAttributeIsSet() throws Exception {
            var output = convertString("", Options.of("attributes", Options.of("stem", "")));
            assertMatch("<script type=\"text/x-mathjax-config\">", output);
            assertMatch("inlineMath: [[\"\\\\(\", \"\\\\)\"]]", output);
            assertMatch("displayMath: [[\"\\\\[\", \"\\\\]\"]]", output);
            assertMatch("delimiters: [[\"\\\\$\", \"\\\\$\"]]", output);
        }
    }

    @Nested
    @DisplayName("Converter")
    class ConverterTests {

        @Test
        @DisplayName("convert methods on built-in converter are registered by default")
        void convertMethodsOnBuiltInConverterAreRegisteredByDefault() throws Exception {
            var doc = documentFromString("");
            assertEquals("html5", get(doc.getAttributes(), "backend"));
            assertTruthy(doc.getAttributes().containsKey("backend-html5"));
            assertEquals("html", get(doc.getAttributes(), "basebackend"));
            assertTruthy(doc.getAttributes().containsKey("basebackend-html"));
            var converter = doc.getConverter();
            assertInstanceOf(Html5Converter.class, converter);
            for (var element : BUILT_IN_ELEMENTS) {
                assertRespondTo(converter, ("convert_" + str(element)));
            }
        }

        @Test
        @DisplayName("convert methods on built-in converter are registered when backend is docbook5")
        void convertMethodsOnBuiltInConverterAreRegisteredWhenBackendIsDocbook5() throws Exception {
            var doc = documentFromString("", Options.of("attributes", Options.of("backend", "docbook5")));
            assertEquals("docbook5", get(doc.getAttributes(), "backend"));
            assertTruthy(doc.getAttributes().containsKey("backend-docbook5"));
            assertEquals("docbook", get(doc.getAttributes(), "basebackend"));
            assertTruthy(doc.getAttributes().containsKey("basebackend-docbook"));
            var converter = doc.getConverter();
            assertInstanceOf(DocBook5Converter.class, converter);
            for (var element : BUILT_IN_ELEMENTS) {
                assertRespondTo(converter, ("convert_" + str(element)));
            }
        }

        @Test
        @DisplayName("should add favicon if favicon attribute is set")
        void shouldAddFaviconIfFaviconAttributeIsSet() throws Exception {
            var cases = list(list("", "favicon.ico", "image/x-icon"), list("/favicon.ico", "/favicon.ico", "image/x-icon"),
                    list("/img/favicon.png", "/img/favicon.png", "image/png"));
            for (var it : cases) {
                var val = it.get(0);
                var href = it.get(1);
                var type = it.get(2);
                var result = convertString("= Untitled", Options.of("attributes", Options.of("favicon", val)));
                assertCss("link[rel=\"icon\"]", result, 1);
                assertCss("link[rel=\"icon\"][href=\"" + href + "\"]", result, 1);
                assertCss("link[rel=\"icon\"][type=\"" + type + "\"]", result, 1);
            }
        }
    }

    @Nested
    @DisplayName("Structure")
    class Structure {

        @Test
        @DisplayName("document with no doctitle")
        void documentWithNoDoctitle() throws Exception {
            var doc = documentFromString("Snorf");
            assertNull(doc.getDoctitle());
            assertNull(doc.getName());
            assertFalsy(doc.hasHeader());
            assertNull(doc.getHeader());
        }

        @Test
        @DisplayName("should enable compat mode for document with legacy doctitle")
        void shouldEnableCompatModeForDocumentWithLegacyDoctitle() throws Exception {
            var input = """
                    Document Title
                    ==============

                    +content+
                    """;
            var doc = documentFromString(input);
            assertTruthy(doc.hasAttribute("compat-mode"));
            var result = doc.convert();
            assertXpath("//code[text()=\"content\"]", result, 1);
        }

        @Test
        @DisplayName("should not enable compat mode for document with legacy doctitle if compat mode disable by header")
        void shouldNotEnableCompatModeForDocumentWithLegacyDoctitleIfCompatModeDisableByHeader() throws Exception {
            var input = """
                    Document Title
                    ==============
                    :compat-mode!:

                    +content+
                    """;
            var doc = documentFromString(input);
            assertNull(doc.getAttribute("compat-mode"));
            var result = doc.convert();
            assertXpath("//code[text()=\"content\"]", result, 0);
        }

        @Test
        @DisplayName("should not enable compat mode for document with legacy doctitle if compat mode is locked by API")
        void shouldNotEnableCompatModeForDocumentWithLegacyDoctitleIfCompatModeIsLockedByAPI() throws Exception {
            var input = """
                    Document Title
                    ==============

                    +content+
                    """;
            var doc = documentFromString(input, Options.of("attributes", Options.of("compat-mode", null)));
            assertTruthy(doc.isAttributeLocked("compat-mode"));
            assertNull(doc.getAttribute("compat-mode"));
            var result = doc.convert();
            assertXpath("//code[text()=\"content\"]", result, 0);
        }

        @Test
        @DisplayName("should apply max-width to each top-level container")
        void shouldApplyMaxWidthToEachTopLevelContainer() throws Exception {
            var input = """
                    = Document Title

                    contentfootnote:[placeholder]
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("max-width", "70em")));
            assertCss("body[style]", output, 0);
            assertCss("#header[style=\"max-width: 70em;\"]", output, 1);
            assertCss("#content[style=\"max-width: 70em;\"]", output, 1);
            assertCss("#footnotes[style=\"max-width: 70em;\"]", output, 1);
            assertCss("#footer[style=\"max-width: 70em;\"]", output, 1);
        }

        @Test
        @DisplayName("title partition API with default separator")
        void titlePartitionAPIWithDefaultSeparator() throws Exception {
            var title = new Document.Title("Main Title: And More: Subtitle");
            assertEquals("Main Title: And More", title.getMain());
            assertEquals("Subtitle", title.getSubtitle());
        }

        @Test
        @DisplayName("title partition API with custom separator")
        void titlePartitionAPIWithCustomSeparator() throws Exception {
            var title = new Document.Title("Main Title:: And More:: Subtitle", false, "::");
            assertEquals("Main Title:: And More", title.getMain());
            assertEquals("Subtitle", title.getSubtitle());
        }

        @Test
        @DisplayName("document with subtitle")
        void documentWithSubtitle() throws Exception {
            var input = """
                    = Main Title: *Subtitle*
                    Author Name

                    content
                    """;
            var doc = documentFromString(input);
            var title = doc.getPartitionedDoctitle(null, true, false);
            assertTruthy(title.hasSubtitle());
            assertTruthy(title.isSanitized());
            assertEquals("Main Title", title.getMain());
            assertEquals("Subtitle", title.getSubtitle());
        }

        @Test
        @DisplayName("document with subtitle and custom separator")
        void documentWithSubtitleAndCustomSeparator() throws Exception {
            var input = """
                    [separator=::]
                    = Main Title:: *Subtitle*
                    Author Name

                    content
                    """;
            var doc = documentFromString(input);
            var title = doc.getPartitionedDoctitle(null, true, false);
            assertTruthy(title.hasSubtitle());
            assertTruthy(title.isSanitized());
            assertEquals("Main Title", title.getMain());
            assertEquals("Subtitle", title.getSubtitle());
        }

        @Test
        @DisplayName("should not honor custom separator for doctitle if attribute is locked by API")
        void shouldNotHonorCustomSeparatorForDoctitleIfAttributeIsLockedByAPI() throws Exception {
            var input = """
                    [separator=::]
                    = Main Title - *Subtitle*
                    Author Name

                    content
                    """;
            var doc = documentFromString(input, Options.of("attributes", Options.of("title-separator", " -")));
            var title = doc.getPartitionedDoctitle(null, true, false);
            assertTruthy(title.hasSubtitle());
            assertTruthy(title.isSanitized());
            assertEquals("Main Title", title.getMain());
            assertEquals("Subtitle", title.getSubtitle());
        }

        @Test
        @DisplayName("document with doctitle defined as attribute entry")
        void documentWithDoctitleDefinedAsAttributeEntry() throws Exception {
            var input = """
                    :doctitle: Document Title

                    preamble

                    == First Section
                    """;
            var doc = documentFromString(input);
            assertEquals("Document Title", doc.getDoctitle());
            assertTruthy(doc.hasHeader());
            assertEquals("Document Title", doc.getHeader().getTitle());
            assertEquals("Document Title", doc.getFirstSection().getTitle());
        }

        @Test
        @DisplayName("document with doctitle defined as attribute entry followed by block with title")
        void documentWithDoctitleDefinedAsAttributeEntryFollowedByBlockWithTitle() throws Exception {
            var input = """
                    :doctitle: Document Title

                    .Block title
                    Block content
                    """;
            var doc = documentFromString(input);
            assertEquals("Document Title", doc.getDoctitle());
            assertTruthy(doc.hasHeader());
            assertEquals(1, size(doc.getBlocks()));
            assertEquals("paragraph", doc.getBlocks().get(0).getContext());
            assertEquals("Block title", doc.getBlocks().get(0).getTitle());
        }

        @Test
        @DisplayName("document with title attribute entry overrides doctitle")
        void documentWithTitleAttributeEntryOverridesDoctitle() throws Exception {
            var input = """
                    = Document Title
                    :title: Override

                    {doctitle}

                    == First Section
                    """;
            var doc = documentFromString(input);
            assertEquals("Override", doc.getDoctitle());
            assertEquals("Override", doc.getTitle());
            assertTruthy(doc.hasHeader());
            assertEquals("Document Title", doc.getHeader().getTitle());
            assertEquals("Document Title", doc.getFirstSection().getTitle());
            assertXpath("//*[@id=\"preamble\"]//p[text()=\"Document Title\"]", doc.convert(), 1);
        }

        @Test
        @DisplayName("document with blank title attribute entry overrides doctitle")
        void documentWithBlankTitleAttributeEntryOverridesDoctitle() throws Exception {
            var input = """
                    = Document Title
                    :title:

                    {doctitle}

                    == First Section
                    """;
            var doc = documentFromString(input);
            assertEquals("", doc.getDoctitle());
            assertEquals("", doc.getTitle());
            assertTruthy(doc.hasHeader());
            assertEquals("Document Title", doc.getHeader().getTitle());
            assertEquals("Document Title", doc.getFirstSection().getTitle());
            assertXpath("//*[@id=\"preamble\"]//p[text()=\"Document Title\"]", doc.convert(), 1);
        }

        @Test
        @DisplayName("document header can reference intrinsic doctitle attribute")
        void documentHeaderCanReferenceIntrinsicDoctitleAttribute() throws Exception {
            var input = """
                    = ACME Documentation
                    :intro: Welcome to the {doctitle}!

                    {intro}
                    """;
            var doc = documentFromString(input);
            assertEquals("Welcome to the ACME Documentation!", doc.getAttribute("intro"));
            assertXpath("//p[text()=\"Welcome to the ACME Documentation!\"]", doc.convert(), 1);
        }

        @Test
        @DisplayName("document with title attribute entry overrides doctitle attribute entry")
        void documentWithTitleAttributeEntryOverridesDoctitleAttributeEntry() throws Exception {
            var input = """
                    = Document Title
                    :snapshot: {doctitle}
                    :doctitle: doctitle
                    :title: Override

                    {snapshot}, {doctitle}

                    == First Section
                    """;
            var doc = documentFromString(input);
            assertEquals("Override", doc.getDoctitle());
            assertEquals("Override", doc.getTitle());
            assertTruthy(doc.hasHeader());
            assertEquals("doctitle", doc.getHeader().getTitle());
            assertEquals("doctitle", doc.getFirstSection().getTitle());
            assertXpath("//*[@id=\"preamble\"]//p[text()=\"Document Title, doctitle\"]", doc.convert(), 1);
        }

        @Test
        @DisplayName("document with doctitle attribute entry overrides implicit doctitle")
        void documentWithDoctitleAttributeEntryOverridesImplicitDoctitle() throws Exception {
            var input = """
                    = Document Title
                    :snapshot: {doctitle}
                    :doctitle: Override

                    {snapshot}, {doctitle}

                    == First Section
                    """;
            var doc = documentFromString(input);
            assertEquals("Override", doc.getDoctitle());
            assertNull(get(doc.getAttributes(), "title"));
            assertTruthy(doc.hasHeader());
            assertEquals("Override", doc.getHeader().getTitle());
            assertEquals("Override", doc.getFirstSection().getTitle());
            assertXpath("//*[@id=\"preamble\"]//p[text()=\"Document Title, Override\"]", doc.convert(), 1);
        }

        @Test
        @DisplayName("doctitle attribute entry above header overrides implicit doctitle")
        void doctitleAttributeEntryAboveHeaderOverridesImplicitDoctitle() throws Exception {
            var input = """
                    :doctitle: Override
                    = Document Title

                    {doctitle}

                    == First Section
                    """;
            var doc = documentFromString(input);
            assertEquals("Override", doc.getDoctitle());
            assertNull(get(doc.getAttributes(), "title"));
            assertTruthy(doc.hasHeader());
            assertEquals("Override", doc.getHeader().getTitle());
            assertEquals("Override", doc.getFirstSection().getTitle());
            assertXpath("//*[@id=\"preamble\"]//p[text()=\"Override\"]", doc.convert(), 1);
        }

        @Test
        @DisplayName("should apply header substitutions to value of the doctitle attribute assigned from implicit doctitle")
        void shouldApplyHeaderSubstitutionsToValueOfTheDoctitleAttributeAssignedFromImplicitDoctitle() throws Exception {
            var input = """
                    = <Foo> {plus} <Bar>

                    The name of the game is {doctitle}.
                    """;
            var doc = documentFromString(input);
            assertEquals("&lt;Foo&gt; &#43; &lt;Bar&gt;", doc.getAttribute("doctitle"));
            assertIncludes(doc.getBlocks().get(0).getContent(), "&lt;Foo&gt; &#43; &lt;Bar&gt;");
        }

        @Test
        @DisplayName("should substitute attribute reference in implicit document title for attribute defined earlier in header")
        void shouldSubstituteAttributeReferenceInImplicitDocumentTitleForAttributeDefinedEarlierInHeader() throws Exception {
            try (var logger = usingMemoryLogger()) {
                var input = """
                        :project-name: ACME
                        = {project-name} Docs

                        {doctitle}
                        """;
                var doc = documentFromString(input, Options.of("attributes", Options.of("attribute-missing", "warn")));
                assertEmpty(logger);
                assertEquals("ACME Docs", doc.getAttribute("doctitle"));
                assertEquals("ACME Docs", doc.getDoctitle());
                assertXpath("//p[text()=\"ACME Docs\"]", doc.convert(), 1);
            }
        }

        @Test
        @DisplayName("should not warn if implicit document title contains attribute reference for attribute defined later in header")
        void shouldNotWarnIfImplicitDocumentTitleContainsAttributeReferenceForAttributeDefinedLaterInHeader() throws Exception {
            try (var logger = usingMemoryLogger()) {
                var input = """
                        = {project-name} Docs
                        :project-name: ACME

                        {doctitle}
                        """;
                var doc = documentFromString(input, Options.of("attributes", Options.of("attribute-missing", "warn")));
                assertEmpty(logger);
                assertEquals("{project-name} Docs", doc.getAttribute("doctitle"));
                assertEquals("ACME Docs", doc.getDoctitle());
                assertXpath("//p[text()=\"{project-name} Docs\"]", doc.convert(), 1);
            }
        }

        @Test
        @DisplayName("should recognize document title when preceded by blank lines")
        void shouldRecognizeDocumentTitleWhenPrecededByBlankLines() throws Exception {
            var input = """

                    = Title

                    preamble

                    == Section 1

                    text
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel()));
            assertCss("#header h1", output, 1);
            assertCss("#content h1", output, 0);
        }

        @Test
        @DisplayName("should recognize document title when preceded by blank lines introduced by a preprocessor conditional")
        void shouldRecognizeDocumentTitleWhenPrecededByBlankLinesIntroducedByAPreprocessorConditional() throws Exception {
            var input = """
                    ifdef::sectids[]

                    :foo: bar
                    endif::[]
                    = Title

                    preamble

                    == Section 1

                    text
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel()));
            assertCss("#header h1", output, 1);
            assertCss("#content h1", output, 0);
        }

        @Test
        @DisplayName("should recognize document title when preceded by blank lines after an attribute entry")
        void shouldRecognizeDocumentTitleWhenPrecededByBlankLinesAfterAnAttributeEntry() throws Exception {
            var input = """
                    :doctype: book

                    = Title

                    preamble

                    == Section 1

                    text
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel()));
            assertCss("#header h1", output, 1);
            assertCss("#content h1", output, 0);
        }

        @Test
        @DisplayName("should recognize document title in include file when preceded by blank lines")
        void shouldRecognizeDocumentTitleInIncludeFileWhenPrecededByBlankLines() throws Exception {
            var input = "include::fixtures/include-with-leading-blank-line.adoc[]\n";
            var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertXpath("//h1[text()=\"Document Title\"]", output, 1);
            assertCss("#toc", output, 1);
        }

        @Test
        @DisplayName("should include specified lines even when leading lines are skipped")
        void shouldIncludeSpecifiedLinesEvenWhenLeadingLinesAreSkipped() throws Exception {
            var input = "include::fixtures/include-with-leading-blank-line.adoc[lines=6]\n";
            var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertXpath("//h2[text()=\"Section\"]", output, 1);
        }

        @Test
        @DisplayName("document with multiline attribute entry but only one line should not crash")
        void documentWithMultilineAttributeEntryButOnlyOneLineShouldNotCrash() throws Exception {
            var input = (":foo: bar" + Asciidoctor.LINE_CONTINUATION);
            var doc = documentFromString(input);
            assertEquals("bar", get(doc.getAttributes(), "foo"));
        }

        @Test
        @DisplayName("should sanitize contents of HTML title element")
        void shouldSanitizeContentsOfHTMLTitleElement() throws Exception {
            var input = """
                    = *Document* image:logo.png[] _Title_ image:another-logo.png[another logo]

                    content
                    """;
            var output = convertString(input);
            assertXpath("/html/head/title[text()=\"Document Title\"]", output, 1);
            var nodes = xmlnodesAtXpath("//*[@id=\"header\"]/h1", output);
            assertEquals(1, size(nodes));
            assertMatch("<h1><strong>Document</strong> <span class=\"image\"><img src=\"logo.png\" alt=\"logo\"></span> <em>Title</em> <span class=\"image\"><img src=\"another-logo.png\" alt=\"another logo\"></span></h1>", output);
        }

        @Test
        @DisplayName("should not choke on empty source")
        void shouldNotChokeOnEmptySource() throws Exception {
            var doc = new Document("");
            assertEmpty(doc.getBlocks());
            assertNull(doc.getDoctitle());
            assertFalsy(doc.hasHeader());
            assertNull(doc.getHeader());
        }

        @Test
        @DisplayName("should not choke on nil source")
        void shouldNotChokeOnNilSource() throws Exception {
            var doc = new Document(null);
            assertEmpty(doc.getBlocks());
            assertNull(doc.getDoctitle());
            assertFalsy(doc.hasHeader());
            assertNull(doc.getHeader());
        }

        @Test
        @DisplayName("with metadata")
        void withMetadata() throws Exception {
            var input = """
                    = AsciiDoc
                    Stuart Rackham <founder@asciidoc.org>
                    v8.6.8, 2012-07-12: See changelog.
                    :description: AsciiDoc user guide
                    :keywords: asciidoc,documentation
                    :copyright: Stuart Rackham

                    == Version 8.6.8

                    more info...
                    """;
            var output = convertString(input);
            assertXpath("//meta[@name=\"author\"][@content=\"Stuart Rackham\"]", output, 1);
            assertXpath("//meta[@name=\"description\"][@content=\"AsciiDoc user guide\"]", output, 1);
            assertXpath("//meta[@name=\"keywords\"][@content=\"asciidoc,documentation\"]", output, 1);
            assertXpath("//meta[@name=\"copyright\"][@content=\"Stuart Rackham\"]", output, 1);
            assertXpath("//*[@id=\"header\"]/*[@class=\"details\"]/span[@id=\"author\"][text()=\"Stuart Rackham\"]", output, 1);
            assertXpath("//*[@id=\"header\"]/*[@class=\"details\"]/span[@id=\"email\"]/a[@href=\"mailto:founder@asciidoc.org\"][text()=\"founder@asciidoc.org\"]", output, 1);
            assertXpath("//*[@id=\"header\"]/*[@class=\"details\"]/span[@id=\"revnumber\"][text()=\"version 8.6.8,\"]", output, 1);
            assertXpath("//*[@id=\"header\"]/*[@class=\"details\"]/span[@id=\"revdate\"][text()=\"2012-07-12\"]", output, 1);
            assertXpath("//*[@id=\"header\"]/*[@class=\"details\"]/span[@id=\"revremark\"][text()=\"See changelog.\"]", output, 1);
        }

        @Test
        @DisplayName("should parse revision line if date is empty")
        void shouldParseRevisionLineIfDateIsEmpty() throws Exception {
            var input = """
                    = Document Title
                    Author Name
                    v1.0.0,:remark

                    content
                    """;
            var doc = documentFromString(input);
            assertEquals("1.0.0", get(doc.getAttributes(), "revnumber"));
            assertNull(get(doc.getAttributes(), "revdate"));
            assertEquals("remark", get(doc.getAttributes(), "revremark"));
        }

        @Test
        @DisplayName("should include revision history in DocBook output if revdate and revnumber is set")
        void shouldIncludeRevisionHistoryInDocBookOutputIfRevdateAndRevnumberIsSet() throws Exception {
            var input = """
                    = Document Title
                    Author Name
                    :revdate: 2011-11-11
                    :revnumber: 1.0

                    content
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("revhistory", output, 1);
            assertCss("revhistory > revision", output, 1);
            assertCss("revhistory > revision > date", output, 1);
            assertCss("revhistory > revision > revnumber", output, 1);
        }

        @Test
        @DisplayName("should include revision history in DocBook output if revdate and revremark is set")
        void shouldIncludeRevisionHistoryInDocBookOutputIfRevdateAndRevremarkIsSet() throws Exception {
            var input = """
                    = Document Title
                    Author Name
                    :revdate: 2011-11-11
                    :revremark: features!

                    content
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("revhistory", output, 1);
            assertCss("revhistory > revision", output, 1);
            assertCss("revhistory > revision > date", output, 1);
            assertCss("revhistory > revision > revremark", output, 1);
        }

        @Test
        @DisplayName("should not include revision history in DocBook output if revdate is not set")
        void shouldNotIncludeRevisionHistoryInDocBookOutputIfRevdateIsNotSet() throws Exception {
            var input = """
                    = Document Title
                    Author Name
                    :revnumber: 1.0

                    content
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("revhistory", output, 0);
        }

        @Test
        @DisplayName("with metadata to DocBook 5")
        void withMetadataToDocBook5() throws Exception {
            var input = """
                    = AsciiDoc
                    Stuart Rackham <founder@asciidoc.org>

                    == Version 8.6.8

                    more info...
                    """;
            var output = convertString(input, Options.of("backend", "docbook5"));
            assertXpath("/article/info", output, 1);
            assertXpath("/article/info/title[text()=\"AsciiDoc\"]", output, 1);
            assertXpath("/article/info/author/personname", output, 1);
            assertXpath("/article/info/author/personname/firstname[text()=\"Stuart\"]", output, 1);
            assertXpath("/article/info/author/personname/surname[text()=\"Rackham\"]", output, 1);
            assertXpath("/article/info/author/email[text()=\"founder@asciidoc.org\"]", output, 1);
            assertCss("article:root:not([xml|id])", output, 1);
            assertCss("article:root[xml|lang=\"en\"]", output, 1);
        }

        @Test
        @DisplayName("with document ID to Docbook 5")
        void withDocumentIDToDocbook5() throws Exception {
            var input = """
                    [[document-id]]
                    = Document Title

                    more info...
                    """;
            var output = convertString(input, Options.of("backend", "docbook", "keep_namespaces", true));
            assertCss("article:root[xml|id=\"document-id\"]", output, 1);
        }

        @Test
        @DisplayName("with author defined using attribute entry to DocBook")
        void withAuthorDefinedUsingAttributeEntryToDocBook() throws Exception {
            var input = """
                    = Document Title
                    :author: Doc Writer
                    :email: thedoctor@asciidoc.org

                    content
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertXpath("/article/info/author", output, 1);
            assertXpath("/article/info/author/personname/firstname[text()=\"Doc\"]", output, 1);
            assertXpath("/article/info/author/personname/surname[text()=\"Writer\"]", output, 1);
            assertXpath("/article/info/author/email[text()=\"thedoctor@asciidoc.org\"]", output, 1);
            assertXpath("/article/info/authorinitials[text()=\"DW\"]", output, 1);
        }

        @Test
        @DisplayName("should substitute replacements in author names in HTML output")
        void shouldSubstituteReplacementsInAuthorNamesInHTMLOutput() throws Exception {
            var input = """
                    = Document Title
                    Stephen O'Grady <founder@redmonk.com>

                    content
                    """;
            var output = convertString(input);
            assertXpath(("//meta[@name=\"author\"][@content=\"Stephen O" + str(decodeChar(8217)) + "Grady\"]"), output, 1);
            assertXpath(("//span[@id=\"author\"][text()=\"Stephen O" + str(decodeChar(8217)) + "Grady\"]"), output, 1);
        }

        @Test
        @DisplayName("should substitute replacements in author names in DocBook output")
        void shouldSubstituteReplacementsInAuthorNamesInDocBookOutput() throws Exception {
            var input = """
                    = Document Title
                    Stephen O'Grady <founder@redmonk.com>

                    content
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertXpath("//author", output, 1);
            assertXpath(("//author/personname/surname[text()=\"O" + str(decodeChar(8217)) + "Grady\"]"), output, 1);
        }

        @Test
        @DisplayName("should sanitize content of HTML meta authors tag")
        void shouldSanitizeContentOfHTMLMetaAuthorsTag() throws Exception {
            var input = """
                    = Document Title
                    :author: pass:n[http://example.org/community/team.html[Ze *Product* team]]

                    content
                    """;
            var output = convertString(input);
            assertXpath("//meta[@name=\"author\"][@content=\"Ze Product team\"]", output, 1);
        }

        @Test
        @DisplayName("should not double escape ampersand in author attribute")
        void shouldNotDoubleEscapeAmpersandInAuthorAttribute() throws Exception {
            var input = """
                    = Document Title
                    R&D Lab

                    {author}
                    """;
            var output = convertString(input);
            assertIncludes(output, "R&amp;D Lab", 2);
        }

        @Test
        @DisplayName("should include multiple authors in HTML output")
        void shouldIncludeMultipleAuthorsInHTMLOutput() throws Exception {
            var input = """
                    = Document Title
                    Doc Writer <thedoctor@asciidoc.org>; Junior Writer <junior@asciidoctor.org>

                    content
                    """;
            var output = convertString(input);
            assertXpath("//span[@id=\"author\"]", output, 1);
            assertXpath("//span[@id=\"author\"][text()=\"Doc Writer\"]", output, 1);
            assertXpath("//span[@id=\"email\"]", output, 1);
            assertXpath("//span[@id=\"email\"]/a", output, 1);
            assertXpath("//span[@id=\"email\"]/a[@href=\"mailto:thedoctor@asciidoc.org\"][text()=\"thedoctor@asciidoc.org\"]", output, 1);
            assertXpath("//span[@id=\"author2\"]", output, 1);
            assertXpath("//span[@id=\"author2\"][text()=\"Junior Writer\"]", output, 1);
            assertXpath("//span[@id=\"email2\"]", output, 1);
            assertXpath("//span[@id=\"email2\"]/a", output, 1);
            assertXpath("//span[@id=\"email2\"]/a[@href=\"mailto:junior@asciidoctor.org\"][text()=\"junior@asciidoctor.org\"]", output, 1);
        }

        @Test
        @DisplayName("should create authorgroup in DocBook when multiple authors")
        void shouldCreateAuthorgroupInDocBookWhenMultipleAuthors() throws Exception {
            var input = """
                    = Document Title
                    Doc Writer <thedoctor@asciidoc.org>; Junior Writer <junior@asciidoctor.org>

                    content
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertXpath("/article/info/author", output, 0);
            assertXpath("/article/info/authorgroup", output, 1);
            assertXpath("/article/info/authorgroup/author", output, 2);
            assertXpath("(/article/info/authorgroup/author)[1]/personname/firstname[text()=\"Doc\"]", output, 1);
            assertXpath("(/article/info/authorgroup/author)[2]/personname/firstname[text()=\"Junior\"]", output, 1);
        }

        @Test
        @DisplayName("should process author defined by attribute when implicit doctitle is absent")
        void shouldProcessAuthorDefinedByAttributeWhenImplicitDoctitleIsAbsent() throws Exception {
            var input = """
                    :author: Doc Writer

                    {lastname}, {firstname} ({authorinitials})
                    """;
            var doc = documentFromString(input, Options.of("standalone", false));
            assertEquals("Doc Writer", doc.getAttribute("author"));
            assertNull(doc.getAttribute("author_1"));
            assertEquals("Writer", doc.getAttribute("lastname"));
            assertEquals("Doc", doc.getAttribute("firstname"));
            assertEquals("DW", doc.getAttribute("authorinitials"));
            assertEquals(1, doc.getAttribute("authorcount"));
            var output = doc.convert();
            assertXpath("//p[text()=\"Writer, Doc (DW)\"]", output, 1);
        }

        @Test
        @DisplayName("should process author and authorinitials defined by attribute when implicit doctitle is absent")
        void shouldProcessAuthorAndAuthorinitialsDefinedByAttributeWhenImplicitDoctitleIsAbsent() throws Exception {
            var input = """
                    :authorinitials: DOC
                    :author: Doc Writer

                    {lastname}, {firstname} ({authorinitials})
                    """;
            var doc = documentFromString(input, Options.of("standalone", false));
            assertEquals("Doc Writer", doc.getAttribute("author"));
            assertEquals("DOC", doc.getAttribute("authorinitials"));
            assertEquals(1, doc.getAttribute("authorcount"));
            var output = doc.convert();
            assertXpath("//p[text()=\"Writer, Doc (DOC)\"]", output, 1);
        }

        @Test
        @DisplayName("should process authors defined by attribute when implicit doctitle is absent")
        void shouldProcessAuthorsDefinedByAttributeWhenImplicitDoctitleIsAbsent() throws Exception {
            var input = """
                    :authors: Doc Writer; Other Author

                    {lastname}, {firstname} ({authorinitials})
                    """;
            var doc = documentFromString(input, Options.of("standalone", false));
            assertEquals("Doc Writer", doc.getAttribute("author"));
            assertEquals("Doc Writer, Other Author", doc.getAttribute("authors"));
            assertEquals("Doc Writer", doc.getAttribute("author_1"));
            assertEquals("Writer", doc.getAttribute("lastname"));
            assertEquals("Writer", doc.getAttribute("lastname_1"));
            assertEquals("Doc", doc.getAttribute("firstname"));
            assertEquals("Doc", doc.getAttribute("firstname_1"));
            assertEquals("DW", doc.getAttribute("authorinitials"));
            assertEquals("DW", doc.getAttribute("authorinitials_1"));
            assertEquals("Other Author", doc.getAttribute("author_2"));
            assertEquals("OA", doc.getAttribute("authorinitials_2"));
            assertEquals(2, doc.getAttribute("authorcount"));
            var output = doc.convert();
            assertXpath("//p[text()=\"Writer, Doc (DW)\"]", output, 1);
        }

        @Test
        @DisplayName("should process authors and authorinitials defined by attribute when implicit doctitle is absent")
        void shouldProcessAuthorsAndAuthorinitialsDefinedByAttributeWhenImplicitDoctitleIsAbsent() throws Exception {
            var input = """
                    :authorinitials: DOC
                    :authors: Doc Writer; Other Author

                    {lastname}, {firstname} ({authorinitials})
                    """;
            var doc = documentFromString(input, Options.of("standalone", false));
            assertEquals("Doc Writer", doc.getAttribute("author"));
            assertEquals("Doc Writer", doc.getAttribute("author_1"));
            assertEquals("DW", doc.getAttribute("authorinitials"));
            assertEquals("Other Author", doc.getAttribute("author_2"));
            assertEquals(2, doc.getAttribute("authorcount"));
            var output = doc.convert();
            assertXpath("//p[text()=\"Writer, Doc (DW)\"]", output, 1);
        }

        @Test
        @DisplayName("should set authorcount to 0 if document has no header")
        void shouldSetAuthorcountTo0IfDocumentHasNoHeader() throws Exception {
            var doc = documentFromString("content");
            assertEquals(0, doc.getAttribute("authorcount"));
        }

        @Test
        @DisplayName("should set authorcount to 0 if author not set by attribute and implicit doctitle is missing")
        void shouldSetAuthorcountTo0IfAuthorNotSetByAttributeAndImplicitDoctitleIsMissing() throws Exception {
            var input = """
                    :idprefix:

                    == Section Title

                    content
                    """;
            var doc = documentFromString(input);
            assertEquals(0, doc.getAttribute("authorcount"));
        }

        @Test
        @DisplayName("should set authorcount to 0 if author not set by attribute and document starts with level-0 section with style")
        void shouldSetAuthorcountTo0IfAuthorNotSetByAttributeAndDocumentStartsWithLevel0SectionWithStyle() throws Exception {
            var input = """
                    :doctype: book

                    [preface]
                    = Preface

                    content

                    = Part

                    == Chapter

                    content
                    """;
            var doc = documentFromString(input);
            assertEquals(0, doc.getAttribute("authorcount"));
        }

        @Test
        @DisplayName("with author defined by indexed attribute name")
        void withAuthorDefinedByIndexedAttributeName() throws Exception {
            var input = """
                    = Document Title
                    :author_1: Doc Writer

                    {author}
                    """;
            var doc = documentFromString(input);
            assertEquals("Doc Writer", doc.getAttribute("author"));
            assertEquals("Doc Writer", doc.getAttribute("author_1"));
        }

        @Test
        @DisplayName("with authors defined using attribute entry to DocBook")
        void withAuthorsDefinedUsingAttributeEntryToDocBook() throws Exception {
            var input = """
                    = Document Title
                    :authors: Doc Writer; Junior Writer
                    :email_1: thedoctor@asciidoc.org
                    :email_2: junior@asciidoc.org

                    content
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertXpath("/article/info/author", output, 0);
            assertXpath("/article/info/authorgroup", output, 1);
            assertXpath("/article/info/authorgroup/author", output, 2);
            assertXpath("(/article/info/authorgroup/author)[1]/personname/firstname[text()=\"Doc\"]", output, 1);
            assertXpath("(/article/info/authorgroup/author)[1]/email[text()=\"thedoctor@asciidoc.org\"]", output, 1);
            assertXpath("(/article/info/authorgroup/author)[2]/personname/firstname[text()=\"Junior\"]", output, 1);
            assertXpath("(/article/info/authorgroup/author)[2]/email[text()=\"junior@asciidoc.org\"]", output, 1);
        }

        @Test
        @DisplayName("should populate copyright element in DocBook output if copyright attribute is defined")
        void shouldPopulateCopyrightElementInDocBookOutputIfCopyrightAttributeIsDefined() throws Exception {
            var input = """
                    = Jet Bike
                    :copyright: ACME, Inc.

                    Essential for catching road runners.
                    """;
            var output = convertString(input, Options.of("backend", "docbook5"));
            assertXpath("/article/info/copyright", output, 1);
            assertXpath("/article/info/copyright/holder[text()=\"ACME, Inc.\"]", output, 1);
        }

        @Test
        @DisplayName("should populate copyright element in DocBook output if copyright attribute is defined with year")
        void shouldPopulateCopyrightElementInDocBookOutputIfCopyrightAttributeIsDefinedWithYear() throws Exception {
            var input = """
                    = Jet Bike
                    :copyright: ACME, Inc. 1956

                    Essential for catching road runners.
                    """;
            var output = convertString(input, Options.of("backend", "docbook5"));
            assertXpath("/article/info/copyright", output, 1);
            assertXpath("/article/info/copyright/holder[text()=\"ACME, Inc.\"]", output, 1);
            assertXpath("/article/info/copyright/year", output, 1);
            assertXpath("/article/info/copyright/year[text()=\"1956\"]", output, 1);
        }

        @Test
        @DisplayName("should populate copyright element in DocBook output if copyright attribute is defined with year range")
        void shouldPopulateCopyrightElementInDocBookOutputIfCopyrightAttributeIsDefinedWithYearRange() throws Exception {
            var input = """
                    = Jet Bike
                    :copyright: ACME, Inc. 1956-2018

                    Essential for catching road runners.
                    """;
            var output = convertString(input, Options.of("backend", "docbook5"));
            assertXpath("/article/info/copyright", output, 1);
            assertXpath("/article/info/copyright/holder[text()=\"ACME, Inc.\"]", output, 1);
            assertXpath("/article/info/copyright/year", output, 1);
            assertXpath("/article/info/copyright/year[text()=\"1956-2018\"]", output, 1);
        }

        @Test
        @DisplayName("with header footer")
        void withHeaderFooter() throws Exception {
            var doc = documentFromString("= Title\n\nparagraph");
            assertFalsy(doc.hasAttribute("embedded"));
            var result = doc.convert();
            assertXpath("/html", result, 1);
            assertXpath("//*[@id=\"header\"]", result, 1);
            assertXpath("//*[@id=\"header\"]/h1", result, 1);
            assertXpath("//*[@id=\"footer\"]", result, 1);
            assertXpath("//*[@id=\"content\"]", result, 1);
        }

        @Test
        @DisplayName("does not output footer if nofooter is set")
        void doesNotOutputFooterIfNofooterIsSet() throws Exception {
            var input = """
                    :nofooter:

                    content
                    """;
            var result = convertString(input);
            assertXpath("//*[@id=\"footer\"]", result, 0);
        }

        @Test
        @DisplayName("can disable last updated in footer")
        void canDisableLastUpdatedInFooter() throws Exception {
            var doc = documentFromString("= Document Title\n\npreamble", Options.of("attributes", Options.of("last-update-label!", "")));
            var result = doc.convert();
            assertXpath("//*[@id=\"footer-text\"]", result, 1);
            assertXpath("//*[@id=\"footer-text\"][normalize-space(text())=\"\"]", result, 1);
        }

        @Test
        @DisplayName("should create embedded document if standalone option passed to constructor is false")
        void shouldCreateEmbeddedDocumentIfStandaloneOptionPassedToConstructorIsFalse() throws Exception {
            var doc = new Document("= Document Title\n\ncontent", Options.of("standalone", false)).parse();
            assertTruthy(doc.hasAttribute("embedded"));
            var result = doc.convert();
            assertXpath("/html", result, 0);
            assertXpath("/h1", result, 0);
            assertXpath("/*[@id=\"header\"]", result, 0);
            assertXpath("/*[@id=\"footer\"]", result, 0);
            assertXpath("/*[@class=\"paragraph\"]", result, 1);
        }

        @Test
        @DisplayName("should create embedded document if standalone option passed to convert method is false")
        void shouldCreateEmbeddedDocumentIfStandaloneOptionPassedToConvertMethodIsFalse() throws Exception {
            var doc = new Document("= Document Title\n\ncontent", Options.of("standalone", true)).parse();
            assertFalsy(doc.hasAttribute("embedded"));
            var result = doc.convert(Options.of("standalone", false));
            assertXpath("/html", result, 0);
            assertXpath("/h1", result, 1);
            assertXpath("/*[@id=\"header\"]", result, 0);
            assertXpath("/*[@id=\"footer\"]", result, 0);
            assertXpath("/*[@class=\"paragraph\"]", result, 1);
        }

        @Test
        @DisplayName("should create embedded document if deprecated header_footer option is false")
        void shouldCreateEmbeddedDocumentIfDeprecatedHeader_footerOptionIsFalse() throws Exception {
            var doc = new Document("= Document Title\n\ncontent", Options.of("header_footer", false)).parse();
            assertTruthy(doc.hasAttribute("embedded"));
            var result = doc.convert();
            assertXpath("/html", result, 0);
            assertXpath("/h1", result, 0);
            assertXpath("/*[@id=\"header\"]", result, 0);
            assertXpath("/*[@id=\"footer\"]", result, 0);
            assertXpath("/*[@class=\"paragraph\"]", result, 1);
        }

        @Test
        @DisplayName("should create embedded document if header_footer option passed to convert method is false")
        void shouldCreateEmbeddedDocumentIfHeader_footerOptionPassedToConvertMethodIsFalse() throws Exception {
            var doc = new Document("= Document Title\n\ncontent", Options.of("header_footer", true)).parse();
            assertFalsy(doc.hasAttribute("embedded"));
            var result = doc.convert(Options.of("header_footer", false));
            assertXpath("/html", result, 0);
            assertXpath("/h1", result, 1);
            assertXpath("/*[@id=\"header\"]", result, 0);
            assertXpath("/*[@id=\"footer\"]", result, 0);
            assertXpath("/*[@class=\"paragraph\"]", result, 1);
        }

        @Test
        @DisplayName("enable title in embedded document by unassigning notitle attribute")
        void enableTitleInEmbeddedDocumentByUnassigningNotitleAttribute() throws Exception {
            var input = """
                    = Document Title

                    content
                    """;
            var result = convertStringToEmbedded(input, Options.of("attributes", Options.of("notitle!", "")));
            assertXpath("/html", result, 0);
            assertXpath("/h1", result, 1);
            assertXpath("/*[@id=\"header\"]", result, 0);
            assertXpath("/*[@id=\"footer\"]", result, 0);
            assertXpath("/*[@class=\"paragraph\"]", result, 1);
            assertXpath("(/*)[1]/self::h1", result, 1);
            assertXpath("(/*)[2]/self::*[@class=\"paragraph\"]", result, 1);
        }

        @Test
        @DisplayName("should be able to enable doctitle for embedded document")
        void shouldBeAbleToEnableDoctitleForEmbeddedDocument() throws Exception {
            for (var entry : entries(list(list(Options.of("notitle", null), null), list(Options.of("notitle", null), list(":!showtitle:")), list(Options.of("notitle", false), null), list(Options.of("notitle", "@"), list(":!notitle:")), list(Options.of("notitle", "@"), list(":showtitle:")), list(Options.of("showtitle", ""), list(":notitle:")), list(Options.of("showtitle", "@"), null), list(Options.of("showtitle", false), list(":!notitle:")), list(Options.of(), list(":!notitle:")), list(Options.of(), list(":notitle:", ":showtitle:")), list(Options.of(), list(":showtitle:")), list(Options.of(), list(":!showtitle:", ":!notitle:"))))) {
                var apiAttrs = entry.getKey();
                var attrEntries = entry.getValue();
                var input = ("= Document Title" + str((isTruthy(attrEntries) ? ("\n" + join(attrEntries, "\n")) : "")) + "\n\nifdef::showtitle[showtitle: set]\nifndef::showtitle[showtitle: not set]\nifdef::notitle[notitle: set]\nifndef::notitle[notitle: not set]\n");
                var result = convertStringToEmbedded(input, Options.of("attributes", apiAttrs));
                assertXpath("/html", result, 0);
                assertXpath("/h1", result, 1);
                assertXpath("(/*)[1]/self::h1", result, 1);
                assertXpath("(/*)[2]/self::*[@class=\"paragraph\"]", result, 1);
                assertIncludes(result, "notitle: not set");
            }
        }

        @Test
        @DisplayName("should be able to explicitly disable doctitle for embedded document")
        void shouldBeAbleToExplicitlyDisableDoctitleForEmbeddedDocument() throws Exception {
            for (var entry : entries(list(list(Options.of("notitle", ""), null), list(Options.of("notitle", "@"), null), list(Options.of("notitle", "@"), list(":!showtitle:")), list(Options.of("showtitle", null), null), list(Options.of("showtitle", false), null), list(Options.of("showtitle", "@"), list(":notitle:")), list(Options.of(), list(":notitle:")), list(Options.of(), list(":!showtitle:")), list(Options.of(), list(":!showtitle:", ":notitle:"))))) {
                var apiAttrs = entry.getKey();
                var attrEntries = entry.getValue();
                var input = ("= Document Title" + str((isTruthy(attrEntries) ? ("\n" + join(attrEntries, "\n")) : "")) + "\n\nifdef::showtitle[showtitle: set]\nifndef::showtitle[showtitle: not set]\nifdef::notitle[notitle: set]\nifndef::notitle[notitle: not set]\n");
                var result = convertStringToEmbedded(input, Options.of("attributes", apiAttrs));
                assertXpath("/html", result, 0);
                assertXpath("/h1", result, 0);
                assertXpath("/*[@class=\"paragraph\"]", result, 1);
                assertIncludes(result, "notitle: set");
            }
        }

        @Test
        @DisplayName("parse header only")
        void parseHeaderOnly() throws Exception {
            var input = """
                    = Document Title
                    Author Name
                    :foo: bar

                    preamble
                    """;
            var doc = documentFromString(input, Options.of("parse_header_only", true));
            assertEquals("Document Title", doc.getDoctitle());
            assertEquals("Author Name", doc.getAuthor());
            assertEquals("bar", get(doc.getAttributes(), "foo"));
            assertEquals(0, size(doc.getBlocks()));
        }

        @Test
        @DisplayName("should parse header only when docytpe is manpage")
        void shouldParseHeaderOnlyWhenDocytpeIsManpage() throws Exception {
            var input = """
                    = cmd(1)
                    Author Name
                    :doctype: manpage

                    == Name

                    cmd - does stuff
                    """;
            var doc = documentFromString(input, Options.of("parse_header_only", true));
            assertEquals("cmd(1)", doc.getDoctitle());
            assertEquals("Author Name", doc.getAuthor());
            assertEquals("cmd", get(doc.getAttributes(), "mantitle"));
            assertEquals("1", get(doc.getAttributes(), "manvolnum"));
            assertNull(get(doc.getAttributes(), "manname"));
            assertNull(get(doc.getAttributes(), "manpurpose"));
            assertEquals(0, size(doc.getBlocks()));
        }

        @Test
        @DisplayName("should not warn when parsing header only when docytpe is manpage and body is empty")
        void shouldNotWarnWhenParsingHeaderOnlyWhenDocytpeIsManpageAndBodyIsEmpty() throws Exception {
            var input = """
                    = cmd(1)
                    Author Name
                    :doctype: manpage
                    """;
            try (var logger = usingMemoryLogger()) {
                var doc = documentFromString(input, Options.of("parse_header_only", true));
                assertEmpty(logger.getMessages());
                assertEquals("cmd(1)", doc.getDoctitle());
                assertEquals("Author Name", doc.getAuthor());
                assertEquals("cmd", get(doc.getAttributes(), "mantitle"));
                assertEquals("1", get(doc.getAttributes(), "manvolnum"));
                assertNull(get(doc.getAttributes(), "manname"));
                assertNull(get(doc.getAttributes(), "manpurpose"));
                assertEquals(0, size(doc.getBlocks()));
            }
        }

        @Test
        @DisplayName("outputs footnotes in footer")
        void outputsFootnotesInFooter() throws Exception {
            var input = """
                    A footnote footnote:[An example footnote.];
                    a second footnote with a reference ID footnote:note2[Second footnote.];
                    and finally a reference to the second footnote footnote:note2[].
                    """;
            var output = convertString(input);
            assertCss("#footnotes", output, 1);
            assertCss("#footnotes .footnote", output, 2);
            assertCss("#footnotes .footnote#_footnotedef_1", output, 1);
            assertXpath("//div[@id=\"footnotes\"]/div[@id=\"_footnotedef_1\"]/a[@href=\"#_footnoteref_1\"][text()=\"1\"]", output, 1);
            var text = xmlnodesAtXpath("//div[@id=\"footnotes\"]/div[@id=\"_footnotedef_1\"]/text()", output);
            assertEquals(". An example footnote.", Strings.strip(text.getText()));
            assertCss("#footnotes .footnote#_footnotedef_2", output, 1);
            assertXpath("//div[@id=\"footnotes\"]/div[@id=\"_footnotedef_2\"]/a[@href=\"#_footnoteref_2\"][text()=\"2\"]", output, 1);
            text = xmlnodesAtXpath("//div[@id=\"footnotes\"]/div[@id=\"_footnotedef_2\"]/text()", output);
            assertEquals(". Second footnote.", Strings.strip(text.getText()));
        }

        @Test
        @DisplayName("outputs footnotes block in embedded document by default")
        void outputsFootnotesBlockInEmbeddedDocumentByDefault() throws Exception {
            var input = "Text that has supporting information{empty}footnote:[An example footnote.].";
            var output = convertStringToEmbedded(input);
            assertCss("#footnotes", output, 1);
            assertCss("#footnotes .footnote", output, 1);
            assertCss("#footnotes .footnote#_footnotedef_1", output, 1);
            assertXpath("/div[@id=\"footnotes\"]/div[@id=\"_footnotedef_1\"]/a[@href=\"#_footnoteref_1\"][text()=\"1\"]", output, 1);
            var text = xmlnodesAtXpath("/div[@id=\"footnotes\"]/div[@id=\"_footnotedef_1\"]/text()", output);
            assertEquals(". An example footnote.", Strings.strip(text.getText()));
        }

        @Test
        @DisplayName("does not output footnotes block in embedded document if nofootnotes attribute is set")
        void doesNotOutputFootnotesBlockInEmbeddedDocumentIfNofootnotesAttributeIsSet() throws Exception {
            var input = "Text that has supporting information{empty}footnote:[An example footnote.].";
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("nofootnotes", "")));
            assertCss("#footnotes", output, 0);
        }
    }

    @Nested
    @DisplayName("Catalog")
    class Catalog {

        @Test
        @DisplayName("should alias document catalog as document references")
        void shouldAliasDocumentCatalogAsDocumentReferences() throws Exception {
            var input = """
                    = Document Title

                    == Section A

                    Content

                    == Section B

                    Content.footnote:[commentary]
                    """;
            var doc = documentFromString(input);
            assertNotNull(doc.getCatalog());
            assertEquals(sorted(list("footnotes", "ids", "images", "includes", "links", "refs", "callouts")), sorted(keys(doc.getCatalog())));
            assertSame(doc.getCatalog(), doc.getReferences());
            assertSame(doc.getCatalog().getFootnotes(), doc.getReferences().getFootnotes());
            assertSame(doc.getCatalog().getRefs(), doc.getReferences().getRefs());
            assertEquals("_section_a", doc.resolveId("Section A"));
        }

        @Test
        @DisplayName("should return empty :ids table")
        void shouldReturnEmptyIdsTable() throws Exception {
            var doc = emptyDocument();
            assertNotNull(doc.getCatalog().getIds());
            assertEmpty(doc.getCatalog().getIds());
            assertNull(get(doc.getCatalog().getIds(), "foobar"));
        }

        @Test
        @DisplayName("should register entry in :refs table with reftext when request is made to register entry in :ids table")
        void shouldRegisterEntryInRefsTableWithReftextWhenRequestIsMadeToRegisterEntryInIdsTable() throws Exception {
            var doc = emptyDocument();
            doc.register("ids", list("foobar", "Foo Bar"));
            assertEmpty(doc.getCatalog().getIds());
            refuteEmpty(doc.getCatalog().getRefs());
            var ref = get(doc.getCatalog().getRefs(), "foobar");
            assertEquals("Foo Bar", ref.getReftext());
            assertEquals("foobar", doc.resolveId("Foo Bar"));
        }

        @Test
        @DisplayName("should return nil if there is already an entry for ID in the :refs table")
        void shouldReturnNilIfThereIsAlreadyAnEntryForIDInTheRefsTable() throws Exception {
            var doc = emptyDocument();
            var ref = list("tigers", new Inline(doc, "anchor", "[tigers]", Options.of("type", "ref", "target", "tigers")), "[tigers]");
            assertEquals(ref.get(1), doc.register("refs", ref));
            assertNull(doc.register("refs", ref));
        }

        @Test
        @DisplayName("should record imagesdir when image is registered with catalog")
        void shouldRecordImagesdirWhenImageIsRegisteredWithCatalog() throws Exception {
            var doc = emptyDocument(Options.of("attributes", Options.of("imagesdir", "img"), "catalog_assets", true));
            doc.register("images", "diagram.svg");
            assertEquals(1, size(doc.getCatalog().getImages()));
            assertEquals("diagram.svg", doc.getCatalog().getImages().get(0).getTarget());
            assertEquals("img", doc.getCatalog().getImages().get(0).getImagesdir());
        }

        @Test
        @DisplayName("should catalog assets inside nested document")
        void shouldCatalogAssetsInsideNestedDocument() throws Exception {
            var input = """
                    image::outer.png[]

                    |===
                    a|
                    image::inner.png[]
                    |===
                    """;
            var doc = documentFromString(input, Options.of("catalog_assets", true));
            var images = doc.getCatalog().getImages();
            refuteEmpty(images);
            assertEquals(2, size(images));
            assertEquals(list("outer.png", "inner.png"), map(images, it -> send(it, "target")));
        }
    }

    @Nested
    @DisplayName("Backends and Doctypes")
    class BackendsAndDoctypes {

        @Test
        @DisplayName("html5 backend doctype article")
        void html5BackendDoctypeArticle() throws Exception {
            var result = convertString("= Title\n\nparagraph", Options.of("attributes", Options.of("backend", "html5")));
            assertXpath("/html", result, 1);
            assertXpath("/html/body[@class=\"article\"]", result, 1);
            assertXpath("/html//*[@id=\"header\"]/h1[text()=\"Title\"]", result, 1);
            assertXpath("/html//*[@id=\"content\"]//p[text()=\"paragraph\"]", result, 1);
        }

        @Test
        @DisplayName("html5 backend doctype book")
        void html5BackendDoctypeBook() throws Exception {
            var result = convertString("= Title\n\nparagraph", Options.of("attributes", Options.of("backend", "html5", "doctype", "book")));
            assertXpath("/html", result, 1);
            assertXpath("/html/body[@class=\"book\"]", result, 1);
            assertXpath("/html//*[@id=\"header\"]/h1[text()=\"Title\"]", result, 1);
            assertXpath("/html//*[@id=\"content\"]//p[text()=\"paragraph\"]", result, 1);
        }

        @Test
        @DisplayName("xhtml5 backend should map to html5 and set htmlsyntax to xml")
        void xhtml5BackendShouldMapToHtml5AndSetHtmlsyntaxToXml() throws Exception {
            var input = "content";
            var doc = documentFromString(input, Options.of("backend", "xhtml5"));
            assertEquals("html5", doc.getBackend());
            assertEquals("xml", doc.getAttribute("htmlsyntax"));
        }

        @Test
        @DisplayName("xhtml backend should map to html5 and set htmlsyntax to xml")
        void xhtmlBackendShouldMapToHtml5AndSetHtmlsyntaxToXml() throws Exception {
            var input = "content";
            var doc = documentFromString(input, Options.of("backend", "xhtml"));
            assertEquals("html5", doc.getBackend());
            assertEquals("xml", doc.getAttribute("htmlsyntax"));
        }

        @Test
        @DisplayName("honor htmlsyntax attribute passed via API if backend is html")
        void honorHtmlsyntaxAttributePassedViaAPIIfBackendIsHtml() throws Exception {
            var input = "---";
            var doc = documentFromString(input, Options.of("safe", "safe", "attributes", Options.of("htmlsyntax", "xml")));
            assertEquals("html5", doc.getBackend());
            assertEquals("xml", doc.getAttribute("htmlsyntax"));
            var result = doc.convert(Options.of("standalone", false));
            assertEquals("<hr/>", result);
        }

        @Test
        @DisplayName("honor htmlsyntax attribute in document header if followed by backend attribute")
        void honorHtmlsyntaxAttributeInDocumentHeaderIfFollowedByBackendAttribute() throws Exception {
            var input = """
                    :htmlsyntax: xml
                    :backend: html5

                    ---
                    """;
            var doc = documentFromString(input, Options.of("safe", "safe"));
            assertEquals("html5", doc.getBackend());
            assertEquals("xml", doc.getAttribute("htmlsyntax"));
            var result = doc.convert(Options.of("standalone", false));
            assertEquals("<hr/>", result);
        }

        @Test
        @DisplayName("does not honor htmlsyntax attribute in document header if not followed by backend attribute")
        void doesNotHonorHtmlsyntaxAttributeInDocumentHeaderIfNotFollowedByBackendAttribute() throws Exception {
            var input = """
                    :backend: html5
                    :htmlsyntax: xml

                    ---
                    """;
            var result = convertStringToEmbedded(input, Options.of("safe", "safe"));
            assertEquals("<hr>", result);
        }

        @Test
        @DisplayName("should close all short tags when htmlsyntax is xml")
        void shouldCloseAllShortTagsWhenHtmlsyntaxIsXml() throws Exception {
            var input = """
                    = Document Title
                    Author Name
                    v1.0, 2001-01-01
                    :icons:
                    :favicon:

                    image:tiger.png[]

                    image::tiger.png[]

                    * [x] one
                    * [ ] two

                    |===
                    |A |B
                    |===

                    [horizontal, labelwidth="25%", itemwidth="75%"]
                    term:: description

                    NOTE: note

                    [quote,Author,Source]
                    ____
                    Quote me.
                    ____

                    [verse,Author,Source]
                    ____
                    A tall tale.
                    ____

                    [options="autoplay,loop"]
                    video::screencast.ogg[]

                    video::12345[vimeo]

                    [options="autoplay,loop"]
                    audio::podcast.ogg[]

                    one +
                    two

                    '''
                    """;
            var result = convertString(input, Options.of("safe", "safe", "backend", "xhtml"));
            try {
                assertWellFormedXml(result);
            } catch (Exception e) {
                flunk(("xhtml5 backend did not generate well-formed XML: " + str(e.getMessage()) + "\n" + str(result)));
            }
        }

        @Test
        @DisplayName("xhtml backend should emit elements in proper namespace")
        void xhtmlBackendShouldEmitElementsInProperNamespace() throws Exception {
            var input = "content";
            var result = convertString(input, Options.of("safe", "safe", "backend", "xhtml", "keep_namespaces", true));
            assertXpath("//*[not(namespace-uri()=\"http://www.w3.org/1999/xhtml\")]", result, 0);
        }

        @Test
        @DisplayName("should parse out subtitle when backend is DocBook")
        void shouldParseOutSubtitleWhenBackendIsDocBook() throws Exception {
            var input = """
                    = Document Title: Subtitle
                    :doctype: book

                    text
                    """;
            var result = convertString(input, Options.of("backend", "docbook5"));
            assertXpath("/book", result, 1);
            assertXpath("/book/info/title[text()=\"Document Title\"]", result, 1);
            assertXpath("/book/info/subtitle[text()=\"Subtitle\"]", result, 1);
        }

        @Test
        @DisplayName("should be able to set doctype to article when converting to DocBook")
        void shouldBeAbleToSetDoctypeToArticleWhenConvertingToDocBook() throws Exception {
            var input = """
                    = Title
                    Author Name

                    preamble

                    == First Section

                    section body
                    """;
            var result = convertString(input, Options.of("keep_namespaces", true, "attributes", Options.of("backend", "docbook5")));
            assertXpath("/xmlns:article", result, 1);
            var doc = xmlnodesAtXpath("/xmlns:article", result, 1);
            assertEquals("http://docbook.org/ns/docbook", get(doc.getNamespaces(), "xmlns"));
            assertEquals("http://www.w3.org/1999/xlink", get(doc.getNamespaces(), "xmlns:xl"));
            assertXpath("/xmlns:article[@version=\"5.0\"]", result, 1);
            assertXpath("/xmlns:article/xmlns:info/xmlns:title[text()=\"Title\"]", result, 1);
            assertXpath("/xmlns:article/xmlns:simpara[text()=\"preamble\"]", result, 1);
            assertXpath("/xmlns:article/xmlns:section", result, 1);
            assertCss("article:root > section[xml|id=\"_first_section\"]", result, 1);
        }

        @Test
        @DisplayName("should set doctype to article by default for document with no title when converting to DocBook")
        void shouldSetDoctypeToArticleByDefaultForDocumentWithNoTitleWhenConvertingToDocBook() throws Exception {
            var result = convertString("text", Options.of("attributes", Options.of("backend", "docbook")));
            assertXpath("/article", result, 1);
            assertXpath("/article/info/title", result, 1);
            assertXpath("/article/info/title[text()=\"Untitled\"]", result, 1);
            assertXpath("/article/info/date", result, 1);
        }

        @Test
        @DisplayName("should be able to convert DocBook manpage output when backend is DocBook and doctype is manpage")
        void shouldBeAbleToConvertDocBookManpageOutputWhenBackendIsDocBookAndDoctypeIsManpage() throws Exception {
            var input = """
                    = asciidoctor(1)
                    :mansource: Asciidoctor
                    :manmanual: Asciidoctor Manual

                    == NAME

                    asciidoctor - Process text

                    == SYNOPSIS

                    some text

                    == First Section

                    section body
                    """;
            var result = convertString(input, Options.of("keep_namespaces", true, "attributes", Options.of("backend", "docbook5", "doctype", "manpage")));
            assertXpath("/xmlns:article", result, 1);
            assertXpath("/xmlns:article/xmlns:refentry", result, 1);
            var doc = xmlnodesAtXpath("/xmlns:article", result, 1);
            assertEquals("http://docbook.org/ns/docbook", get(doc.getNamespaces(), "xmlns"));
            assertEquals("http://www.w3.org/1999/xlink", get(doc.getNamespaces(), "xmlns:xl"));
            assertEquals("5.0", doc.getAttribute("version"));
            assertXpath("/xmlns:article/xmlns:info/xmlns:title[text()=\"asciidoctor(1)\"]", result, 1);
            assertXpath("/xmlns:article/xmlns:refentry/xmlns:refmeta/xmlns:refentrytitle[text()=\"asciidoctor\"]", result, 1);
            assertXpath("/xmlns:article/xmlns:refentry/xmlns:refmeta/xmlns:manvolnum[text()=\"1\"]", result, 1);
            assertXpath("/xmlns:article/xmlns:refentry/xmlns:refmeta/xmlns:refmiscinfo[@class=\"source\"][text()=\"Asciidoctor\"]", result, 1);
            assertXpath("/xmlns:article/xmlns:refentry/xmlns:refmeta/xmlns:refmiscinfo[@class=\"manual\"][text()=\"Asciidoctor Manual\"]", result, 1);
            assertXpath("/xmlns:article/xmlns:refentry/xmlns:refnamediv/xmlns:refname[text()=\"asciidoctor\"]", result, 1);
            assertXpath("/xmlns:article/xmlns:refentry/xmlns:refnamediv/xmlns:refpurpose[text()=\"Process text\"]", result, 1);
            assertXpath("/xmlns:article/xmlns:refentry/xmlns:refsynopsisdiv", result, 1);
            assertXpath("/xmlns:article/xmlns:refentry/xmlns:refsynopsisdiv/xmlns:simpara[text()=\"some text\"]", result, 1);
            assertXpath("/xmlns:article/xmlns:refentry/xmlns:refsection", result, 1);
            assertCss("article:root > refentry > refsection[xml|id=\"_first_section\"]", result, 1);
        }

        @Test
        @DisplayName("should output non-breaking space for source and manual in docbook manpage output if absent from source")
        void shouldOutputNonBreakingSpaceForSourceAndManualInDocbookManpageOutputIfAbsentFromSource() throws Exception {
            var input = """
                    = asciidoctor(1)

                    == NAME

                    asciidoctor - Process text

                    == SYNOPSIS

                    some text
                    """;
            var result = convertString(input, Options.of("keep_namespaces", true, "attributes", Options.of("backend", "docbook5", "doctype", "manpage")));
            assertXpath(("/xmlns:article/xmlns:refentry/xmlns:refmeta/xmlns:refmiscinfo[@class=\"source\"][text()=\"" + str(decodeChar(160)) + "\"]"), result, 1);
            assertXpath(("/xmlns:article/xmlns:refentry/xmlns:refmeta/xmlns:refmiscinfo[@class=\"manual\"][text()=\"" + str(decodeChar(160)) + "\"]"), result, 1);
        }

        @Test
        @DisplayName("should apply replacements substitution to value of mantitle attribute used in DocBook output")
        void shouldApplyReplacementsSubstitutionToValueOfMantitleAttributeUsedInDocBookOutput() throws Exception {
            var input = """
                    = foo\\--bar(1)
                    Author Name
                    :doctype: manpage
                    :man manual: Foo Bar Manual
                    :man source: Foo Bar 1.0

                    == NAME

                    foo--bar - puts the foo in your bar
                    """;
            var doc = Asciidoctor.load(input, Options.of("backend", "docbook", "standalone", true));
            assertEquals("foo\\--bar", doc.getAttribute("mantitle"));
            var result = doc.convert();
            assertXpath("/xmlns:article/xmlns:info/xmlns:title[text()=\"foo--bar(1)\"]", result, 1);
            assertXpath("/xmlns:article/xmlns:refentry/xmlns:refmeta/xmlns:refentrytitle[text()=\"foo--bar\"]", result, 1);
        }

        @Test
        @DisplayName("should be able to set doctype to book when converting to DocBook")
        void shouldBeAbleToSetDoctypeToBookWhenConvertingToDocBook() throws Exception {
            var input = """
                    = Title
                    Author Name

                    preamble

                    == First Chapter

                    chapter body
                    """;
            var result = convertString(input, Options.of("keep_namespaces", true, "attributes", Options.of("backend", "docbook5", "doctype", "book")));
            assertXpath("/xmlns:book", result, 1);
            var doc = xmlnodesAtXpath("/xmlns:book", result, 1);
            assertEquals("http://docbook.org/ns/docbook", get(doc.getNamespaces(), "xmlns"));
            assertEquals("http://www.w3.org/1999/xlink", get(doc.getNamespaces(), "xmlns:xl"));
            assertXpath("/xmlns:book[@version=\"5.0\"]", result, 1);
            assertXpath("/xmlns:book/xmlns:info/xmlns:title[text()=\"Title\"]", result, 1);
            assertXpath("/xmlns:book/xmlns:preface/xmlns:simpara[text()=\"preamble\"]", result, 1);
            assertXpath("/xmlns:book/xmlns:chapter", result, 1);
            assertCss("book:root > chapter[xml|id=\"_first_chapter\"]", result, 1);
        }

        @Test
        @DisplayName("should be able to set doctype to book for document with no title when converting to DocBook")
        void shouldBeAbleToSetDoctypeToBookForDocumentWithNoTitleWhenConvertingToDocBook() throws Exception {
            var result = convertString("text", Options.of("attributes", Options.of("backend", "docbook5", "doctype", "book")));
            assertXpath("/book", result, 1);
            assertXpath("/book/info/date", result, 1);
            assertXpath("/book/preface/simpara[text()=\"text\"]", result, 1);
        }

        @Test
        @DisplayName("adds refname to DocBook output for each name defined in NAME section of manpage")
        void addsRefnameToDocBookOutputForEachNameDefinedInNAMESectionOfManpage() throws Exception {
            var input = """
                    = eve(1)
                    Andrew Stanton
                    v1.0.0
                    :doctype: manpage
                    :manmanual: EVE
                    :mansource: EVE

                    == NAME

                    eve, islifeform - analyzes an image to determine if it's a picture of a life form

                    == SYNOPSIS

                    *eve* ['OPTION']... 'FILE'...
                    """;
            var result = convertString(input, Options.of("backend", "docbook5"));
            assertXpath("/article/refentry/refnamediv/refname", result, 2);
            assertXpath("(/article/refentry/refnamediv/refname)[1][text()=\"eve\"]", result, 1);
            assertXpath("(/article/refentry/refnamediv/refname)[2][text()=\"islifeform\"]", result, 1);
        }

        @Test
        @DisplayName("adds a front and back cover image to DocBook 5 when doctype is book")
        void addsAFrontAndBackCoverImageToDocBook5WhenDoctypeIsBook() throws Exception {
            var input = """
                    = Title
                    :doctype: book
                    :imagesdir: images
                    :front-cover-image: image:front-cover.jpg[scaledwidth=210mm]
                    :back-cover-image: image:back-cover.jpg[]

                    preamble

                    == First Chapter

                    chapter body
                    """;
            var result = convertString(input, Options.of("attributes", Options.of("backend", "docbook5")));
            assertXpath("//info/cover[@role=\"front\"]", result, 1);
            assertXpath("//info/cover[@role=\"front\"]//imagedata[@fileref=\"images/front-cover.jpg\"]", result, 1);
            assertXpath("//info/cover[@role=\"back\"]", result, 1);
            assertXpath("//info/cover[@role=\"back\"]//imagedata[@fileref=\"images/back-cover.jpg\"]", result, 1);
        }

        @Test
        @DisplayName("should be able to set backend using :backend option key")
        void shouldBeAbleToSetBackendUsingBackendOptionKey() throws Exception {
            var doc = emptyDocument(Options.of("backend", "html5"));
            assertEquals("html5", get(doc.getAttributes(), "backend"));
        }

        @Test
        @DisplayName(":backend option should override backend attribute")
        void backendOptionShouldOverrideBackendAttribute() throws Exception {
            var doc = emptyDocument(Options.of("backend", "html5", "attributes", Options.of("backend", "docbook5")));
            assertEquals("html5", get(doc.getAttributes(), "backend"));
        }

        @Test
        @DisplayName("should be able to set doctype using :doctype option key")
        void shouldBeAbleToSetDoctypeUsingDoctypeOptionKey() throws Exception {
            var doc = emptyDocument(Options.of("doctype", "book"));
            assertEquals("book", get(doc.getAttributes(), "doctype"));
        }

        @Test
        @DisplayName(":doctype option should override doctype attribute")
        void doctypeOptionShouldOverrideDoctypeAttribute() throws Exception {
            var doc = emptyDocument(Options.of("doctype", "book", "attributes", Options.of("doctype", "article")));
            assertEquals("book", get(doc.getAttributes(), "doctype"));
        }

        @Test
        @DisplayName("do not override explicit author initials")
        void doNotOverrideExplicitAuthorInitials() throws Exception {
            var input = """
                    = AsciiDoc
                    Stuart Rackham <founder@asciidoc.org>
                    :Author Initials: SJR

                    more info...
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("backend", "docbook5")));
            assertXpath("/article/info/authorinitials[text()=\"SJR\"]", output, 1);
        }

        @Test
        @DisplayName("attribute entry can appear immediately after document title")
        void attributeEntryCanAppearImmediatelyAfterDocumentTitle() throws Exception {
            var input = """
                    Reference Guide
                    ===============
                    :toc:

                    preamble
                    """;
            var doc = documentFromString(input);
            assertTruthy(doc.hasAttribute("toc"));
            assertEquals("", doc.getAttribute("toc"));
        }

        @Test
        @DisplayName("attribute entry can appear before author line under document title")
        void attributeEntryCanAppearBeforeAuthorLineUnderDocumentTitle() throws Exception {
            var input = """
                    Reference Guide
                    ===============
                    :toc:
                    Dan Allen

                    preamble
                    """;
            var doc = documentFromString(input);
            assertTruthy(doc.hasAttribute("toc"));
            assertEquals("", doc.getAttribute("toc"));
            assertEquals("Dan Allen", doc.getAttribute("author"));
        }

        @Test
        @DisplayName("should parse mantitle and manvolnum from document title for manpage doctype")
        void shouldParseMantitleAndManvolnumFromDocumentTitleForManpageDoctype() throws Exception {
            var input = """
                    = asciidoctor ( 1 )
                    :doctype: manpage

                    == NAME

                    asciidoctor - converts AsciiDoc source files to HTML, DocBook and other formats
                    """;
            var doc = documentFromString(input);
            assertEquals("asciidoctor", doc.getAttribute("mantitle"));
            assertEquals("1", doc.getAttribute("manvolnum"));
        }

        @Test
        @DisplayName("should perform attribute substitution on mantitle in manpage doctype")
        void shouldPerformAttributeSubstitutionOnMantitleInManpageDoctype() throws Exception {
            var input = """
                    = {app}(1)
                    :doctype: manpage
                    :app: Asciidoctor

                    == NAME

                    asciidoctor - converts AsciiDoc source files to HTML, DocBook and other formats
                    """;
            var doc = documentFromString(input);
            assertEquals("asciidoctor", doc.getAttribute("mantitle"));
        }

        @Test
        @DisplayName("should consume name section as manname and manpurpose for manpage doctype")
        void shouldConsumeNameSectionAsMannameAndManpurposeForManpageDoctype() throws Exception {
            var input = """
                    = asciidoctor(1)
                    :doctype: manpage

                    == NAME

                    asciidoctor - converts AsciiDoc source files to HTML, DocBook and other formats
                    """;
            var doc = documentFromString(input);
            assertEquals("asciidoctor", doc.getAttribute("manname"));
            assertEquals("converts AsciiDoc source files to HTML, DocBook and other formats", doc.getAttribute("manpurpose"));
            assertEquals("_name", doc.getAttribute("manname-id"));
            assertEquals(0, size(doc.getBlocks()));
        }

        @Test
        @DisplayName("should set docname and outfilesuffix from manname and manvolnum for manpage backend and doctype")
        void shouldSetDocnameAndOutfilesuffixFromMannameAndManvolnumForManpageBackendAndDoctype() throws Exception {
            var input = """
                    = asciidoctor(1)
                    :doctype: manpage

                    == NAME

                    asciidoctor - converts AsciiDoc source files to HTML, DocBook and other formats
                    """;
            var doc = documentFromString(input, Options.of("backend", "manpage"));
            assertEquals("asciidoctor", get(doc.getAttributes(), "docname"));
            assertEquals(".1", get(doc.getAttributes(), "outfilesuffix"));
        }

        @Test
        @DisplayName("should mark synopsis as special section in manpage doctype")
        void shouldMarkSynopsisAsSpecialSectionInManpageDoctype() throws Exception {
            var input = """
                    = asciidoctor(1)
                    :doctype: manpage

                    == NAME

                    asciidoctor - converts AsciiDoc source files to HTML, DocBook and other formats

                    == SYNOPSIS

                    *asciidoctor* ['OPTION']... 'FILE'..
                    """;
            var doc = documentFromString(input);
            var synopsisSection = (Section) first(doc.getBlocks());
            assertNotNull(synopsisSection);
            assertEquals("section", synopsisSection.getContext());
            assertTruthy(synopsisSection.isSpecial());
            assertEquals("synopsis", synopsisSection.getSectionName());
        }

        @Test
        @DisplayName("should output special header block in HTML for manpage doctype")
        void shouldOutputSpecialHeaderBlockInHTMLForManpageDoctype() throws Exception {
            var input = """
                    = asciidoctor(1)
                    :doctype: manpage

                    == NAME

                    asciidoctor - converts AsciiDoc source files to HTML, DocBook and other formats

                    == SYNOPSIS

                    *asciidoctor* ['OPTION']... 'FILE'..
                    """;
            var output = convertString(input);
            assertCss("body.manpage", output, 1);
            assertXpath("//body/*[@id=\"header\"]/h1[text()=\"asciidoctor(1) Manual Page\"]", output, 1);
            assertXpath("//body/*[@id=\"header\"]/h1/following-sibling::h2[text()=\"NAME\"]", output, 1);
            assertXpath("//h2[@id=\"_name\"][text()=\"NAME\"]", output, 1);
            assertXpath("//h2[text()=\"NAME\"]/following-sibling::*[@class=\"sectionbody\"]", output, 1);
            assertXpath("//h2[text()=\"NAME\"]/following-sibling::*[@class=\"sectionbody\"]/p[text()=\"asciidoctor - converts AsciiDoc source files to HTML, DocBook and other formats\"]", output, 1);
            assertXpath("//*[@id=\"content\"]/*[@class=\"sect1\"]/h2[text()=\"SYNOPSIS\"]", output, 1);
        }

        @Test
        @DisplayName("should output special header block in embeddable HTML for manpage doctype")
        void shouldOutputSpecialHeaderBlockInEmbeddableHTMLForManpageDoctype() throws Exception {
            var input = """
                    = asciidoctor(1)
                    :doctype: manpage
                    :showtitle:

                    == NAME

                    asciidoctor - converts AsciiDoc source files to HTML, DocBook and other formats

                    == SYNOPSIS

                    *asciidoctor* ['OPTION']... 'FILE'..
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/h1[text()=\"asciidoctor(1) Manual Page\"]", output, 1);
            assertXpath("/h1/following-sibling::h2[text()=\"NAME\"]", output, 1);
            assertXpath("/h2[@id=\"_name\"][text()=\"NAME\"]", output, 1);
            assertXpath("/h2[text()=\"NAME\"]/following-sibling::*[@class=\"sectionbody\"]", output, 1);
            assertXpath("/h2[text()=\"NAME\"]/following-sibling::*[@class=\"sectionbody\"]/p[text()=\"asciidoctor - converts AsciiDoc source files to HTML, DocBook and other formats\"]", output, 1);
        }

        @Test
        @DisplayName("should output all mannames in name section in man page output")
        void shouldOutputAllMannamesInNameSectionInManPageOutput() throws Exception {
            var input = """
                    = eve(1)
                    :doctype: manpage

                    == NAME

                    eve, probe - analyzes an image to determine if it is a picture of a life form

                    == SYNOPSIS

                    *eve* [OPTION]... FILE...
                    """;
            var output = convertString(input);
            assertCss("body.manpage", output, 1);
            assertXpath("//h2[text()=\"NAME\"]/following-sibling::*[@class=\"sectionbody\"]/p[text()=\"eve, probe - analyzes an image to determine if it is a picture of a life form\"]", output, 1);
        }
    }

    @Nested
    @DisplayName("Secure Asset Path")
    class SecureAssetPath {

        @Test
        @DisplayName("allows us to specify a path relative to the current dir")
        void allowsUsToSpecifyAPathRelativeToTheCurrentDir() throws Exception {
            var doc = emptyDocument();
            var legitPath = (Helpers.workingDirectory() + "/foo");
            assertEquals(legitPath, doc.normalizeAssetPath(legitPath));
        }

        @Test
        @DisplayName("keeps naughty absolute paths from getting outside")
        void keepsNaughtyAbsolutePathsFromGettingOutside() throws Exception {
            var naughtyPath = (str(diskRoot()) + "etc/passwd");
            try (var logger = usingMemoryLogger()) {
                var doc = emptyDocument();
                var securePath = doc.normalizeAssetPath(naughtyPath);
                assertNotEquals(naughtyPath, securePath);
                assertEquals(joinPath(doc.getBaseDir(), "etc/passwd"), securePath);
                assertMessage(logger, "WARN", "path is outside of jail; recovering automatically");
            }
        }

        @Test
        @DisplayName("keeps naughty relative paths from getting outside")
        void keepsNaughtyRelativePathsFromGettingOutside() throws Exception {
            var naughtyPath = "safe/ok/../../../../../etc/passwd";
            try (var logger = usingMemoryLogger()) {
                var doc = emptyDocument();
                var securePath = doc.normalizeAssetPath(naughtyPath);
                assertNotEquals(naughtyPath, securePath);
                assertMatch(rx(("^" + str(doc.getBaseDir()))), securePath);
            }
        }

        @Test
        @DisplayName("should raise an exception when a converter cannot be resolved before conversion")
        void shouldRaiseAnExceptionWhenAConverterCannotBeResolvedBeforeConversion() throws Exception {
            var input = """
                    = Document Title

                    text
                    """;
            var exception = assertThrows(UnsupportedOperationException.class, () -> Asciidoctor.convert(input, Options.of("backend", "unknownBackend")));
            assertIncludes(exception.getMessage(), "missing converter for backend 'unknownBackend'");
        }

        @Test
        @DisplayName("should raise an exception when a converter cannot be resolved while parsing")
        void shouldRaiseAnExceptionWhenAConverterCannotBeResolvedWhileParsing() throws Exception {
            var input = """
                    = Document Title

                    == A _Big_ Section

                    text
                    """;
            var exception = assertThrows(UnsupportedOperationException.class, () -> Asciidoctor.convert(input, Options.of("backend", "unknownBackend")));
            assertIncludes(exception.getMessage(), "missing converter for backend 'unknownBackend'");
        }
    }

    @Nested
    @DisplayName("Timing report")
    class TimingReport {

        @Test
        @DisplayName("print_report does not lose precision")
        void print_reportDoesNotLosePrecision() throws Exception {
            var timings = new Timings();
            var log = timings.log;
            log.put("read", 1.0e-05);
            log.put("parse", 3.0e-05);
            log.put("convert", 5.0e-05);
            var sink = new ByteArrayOutputStream();
            timings.printReport(new PrintStream(sink, true, StandardCharsets.UTF_8));
            var expect = list("0.00004", "0.00005", "0.00009");
            var result = map(split(sink.toString(StandardCharsets.UTF_8), "\n"), l -> sub(l, rx(".*:\\s*([\\d.]+)"), "\\1"));
            assertEquals(expect, result);
        }

        @Test
        @DisplayName("print_report should print 0 for untimed phases")
        void print_reportShouldPrint0ForUntimedPhases() throws Exception {
            var sink = new ByteArrayOutputStream();
            new Timings().printReport(new PrintStream(sink, true, StandardCharsets.UTF_8));
            var expect = list("0.00000", "0.00000", "0.00000");
            var result = map(split(sink.toString(StandardCharsets.UTF_8), "\n"), l -> sub(l, rx(".*:\\s*([\\d.]+)"), "\\1"));
            assertEquals(expect, result);
        }
    }

    @Nested
    @DisplayName("Date time attributes")
    class DateTimeAttributes {

        @Test
        @DisplayName("should compute docyear and docdatetime from docdate and doctime")
        void shouldComputeDocyearAndDocdatetimeFromDocdateAndDoctime() throws Exception {
            var doc = new Document(new ArrayList<String>(), Options.of("attributes", Options.of("docdate", "2015-01-01", "doctime", "10:00:00-0700")));
            assertEquals("2015-01-01", doc.getAttribute("docdate"));
            assertEquals("2015", doc.getAttribute("docyear"));
            assertEquals("10:00:00-0700", doc.getAttribute("doctime"));
            assertEquals("2015-01-01 10:00:00-0700", doc.getAttribute("docdatetime"));
        }

        @Test
        @DisplayName("should allow docdate and doctime to be overridden")
        void shouldAllowDocdateAndDoctimeToBeOverridden() throws Exception {
            var doc = new Document(new ArrayList<String>(), Options.of("input_mtime", ZonedDateTime.now(), "attributes", Options.of("docdate", "2015-01-01", "doctime", "10:00:00-0700")));
            assertEquals("2015-01-01", doc.getAttribute("docdate"));
            assertEquals("2015", doc.getAttribute("docyear"));
            assertEquals("10:00:00-0700", doc.getAttribute("doctime"));
            assertEquals("2015-01-01 10:00:00-0700", doc.getAttribute("docdatetime"));
        }

        @Test
        @DisplayName("should compute docdatetime from doctime")
        void shouldComputeDocdatetimeFromDoctime() throws Exception {
            var doc = new Document(new ArrayList<String>(), Options.of("attributes", Options.of("doctime", "10:00:00-0700")));
            assertEquals("10:00:00-0700", doc.getAttribute("doctime"));
            assertTruthy(endsWith(doc.getAttribute("docdatetime"), " 10:00:00-0700"));
        }

        @Test
        @DisplayName("should compute docyear from docdate")
        void shouldComputeDocyearFromDocdate() throws Exception {
            var doc = new Document(new ArrayList<String>(), Options.of("attributes", Options.of("docdate", "2015-01-01")));
            assertEquals("2015", doc.getAttribute("docyear"));
            assertTruthy(startsWith(doc.getAttribute("docdatetime"), "2015-01-01 "));
        }

        @Test
        @DisplayName("should allow doctime to be overridden")
        void shouldAllowDoctimeToBeOverridden() throws Exception {
            assumeTrue(System.getenv("SOURCE_DATE_EPOCH") == null);
            var doc = new Document(new ArrayList<String>(), Options.of("input_mtime", ZonedDateTime.of(2019, 1, 2, 3, 4, 5, 0, ZoneOffset.of("+06:00")), "attributes", Options.of("doctime", "10:00:00-0700")));
            assertEquals("2019-01-02", doc.getAttribute("docdate"));
            assertEquals("2019", doc.getAttribute("docyear"));
            assertEquals("10:00:00-0700", doc.getAttribute("doctime"));
            assertEquals("2019-01-02 10:00:00-0700", doc.getAttribute("docdatetime"));
        }

        @Test
        @DisplayName("should allow docdate to be overridden")
        void shouldAllowDocdateToBeOverridden() throws Exception {
            assumeTrue(System.getenv("SOURCE_DATE_EPOCH") == null);
            var doc = new Document(new ArrayList<String>(), Options.of("input_mtime", ZonedDateTime.of(2019, 1, 2, 3, 4, 5, 0, ZoneOffset.of("+06:00")), "attributes", Options.of("docdate", "2015-01-01")));
            assertEquals("2015-01-01", doc.getAttribute("docdate"));
            assertEquals("2015", doc.getAttribute("docyear"));
            assertEquals("2015-01-01 03:04:05 +0600", doc.getAttribute("docdatetime"));
        }
    }
}
