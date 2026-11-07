package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.*;
import org.asciidoctor.core.syntaxhighlighter.SyntaxHighlighter;
import org.asciidoctor.core.syntaxhighlighter.AbstractSyntaxHighlighter;

import org.junit.jupiter.api.*;

@DisplayName("Syntax Highlighter")
public class SyntaxHighlighterTest extends TestHelper {

    @Test
    @Disabled("the CodeRay adapter is not ported (it requires a gem)")
    @DisplayName("should set syntax_highlighter property on document if source highlighter is set and basebackend is html")
    void shouldSetSyntax_highlighterPropertyOnDocumentIfSourceHighlighterIsSetAndBasebackendIsHtml() throws Exception {
    }

    @Test
    @DisplayName("should not set syntax_highlighter property on document if source highlighter is set and basebackend is not html")
    void shouldNotSetSyntax_highlighterPropertyOnDocumentIfSourceHighlighterIsSetAndBasebackendIsNotHtml() throws Exception {
        var input = """
                :source-highlighter: coderay

                [source, ruby]
                ----
                puts 'Hello, World!'
                ----
                """;
        var doc = documentFromString(input, Options.of("safe", "safe", "backend", "docbook", "parse", true));
        assertFalsy(doc.isBasebackend("html"));
        assertNull(doc.getSyntaxHighlighter());
    }

    @Test
    @DisplayName("should not set syntax_highlighter property on document if source highlighter is not set")
    void shouldNotSetSyntax_highlighterPropertyOnDocumentIfSourceHighlighterIsNotSet() throws Exception {
        var input = """
                [source, ruby]
                ----
                puts 'Hello, World!'
                ----
                """;
        var doc = documentFromString(input, Options.of("safe", "safe", "parse", true));
        assertNull(doc.getSyntaxHighlighter());
    }

    @Test
    @DisplayName("should not set syntax_highlighter property on document if syntax highlighter cannot be resolved")
    void shouldNotSetSyntax_highlighterPropertyOnDocumentIfSyntaxHighlighterCannotBeResolved() throws Exception {
        var input = """
                :source-highlighter: unknown

                [source, ruby]
                ----
                puts 'Hello, World!'
                ----
                """;
        var doc = documentFromString(input, Options.of("safe", "safe", "parse", true));
        assertNull(doc.getSyntaxHighlighter());
    }

    @Test
    @DisplayName("should not allow document to enable syntax highlighter if safe mode is at least SERVER")
    void shouldNotAllowDocumentToEnableSyntaxHighlighterIfSafeModeIsAtLeastSERVER() throws Exception {
        var input = ":source-highlighter: coderay";
        var doc = documentFromString(input, Options.of("safe", SafeMode.SERVER.getLevel(), "parse", true));
        assertNull(get(doc.getAttributes(), "source-highlighter"));
        assertNull(doc.getSyntaxHighlighter());
    }

    @Test
    @DisplayName("should not invoke highlight method on syntax highlighter if highlight? is false")
    void shouldNotInvokeHighlightMethodOnSyntaxHighlighterIfHighlightIsFalse() throws Exception {
        class SampleSyntaxHighlighter extends AbstractSyntaxHighlighter {
            public SampleSyntaxHighlighter(String name, String backend, Map<String, Object> opts) {
                super(name, backend, opts);
            }

            @Override
            public String format(AbstractBlock node, String language, Map<String, Object> opts) {
                return "<pre class=\"highlight\"><code class=\"language-" + language + "\" data-lang=\"" + language + "\">"
                        + node.getContent() + "</code></pre>";
            }

            @Override
            public boolean isHighlight() {
                return false;
            }
        }

        SyntaxHighlighter.register((SyntaxHighlighter.Constructor) SampleSyntaxHighlighter::new, "unavailable");
        var input = """
                [source,ruby]
                ----
                puts 'Hello, World!'
                ----
                """;
        var doc = documentFromString(input, Options.of("attributes", Options.of("source-highlighter", "unavailable")));
        var output = doc.convert();
        assertCss("pre.highlight > code.language-ruby", output, 1);
        var sourceBlock = (Block) doc.findBy(filter(candidate -> Objects.equals(candidate.getStyle(), "source"))).get(0);
        assertThrows(UnsupportedOperationException.class, () -> {
            doc.getSyntaxHighlighter().highlight(sourceBlock, sourceBlock.getSource(), sourceBlock.getStringAttribute("language"), Options.of());
        });
    }

    @Test
    @DisplayName("should be able to register syntax highlighter from syntax highlighter class itself")
    void shouldBeAbleToRegisterSyntaxHighlighterFromSyntaxHighlighterClassItself() throws Exception {
        class SampleSyntaxHighlighter extends AbstractSyntaxHighlighter {
            public SampleSyntaxHighlighter(String name, String backend, Map<String, Object> opts) {
                super(name, backend, opts);
            }

            @Override
            public String format(AbstractBlock node, String language, Map<String, Object> opts) {
                return "<pre class=\"highlight\"><code class=\"language-" + language + "\" data-lang=\"" + language + "\">"
                        + node.getContent() + "</code></pre>";
            }

            @Override
            public boolean isHighlight() {
                return false;
            }
        }

        SyntaxHighlighter.register(SampleSyntaxHighlighter.class, "foobar");
        assertEquals(SampleSyntaxHighlighter.class, SyntaxHighlighter.forName("foobar"));
    }

    @Test
    @DisplayName("should be able to register syntax highlighter using symbol")
    void shouldBeAbleToRegisterSyntaxHighlighterUsingSymbol() throws Exception {
        class SampleSyntaxHighlighter extends AbstractSyntaxHighlighter {
            public SampleSyntaxHighlighter(String name, String backend, Map<String, Object> opts) {
                super(name, backend, opts);
            }

            @Override
            public String format(AbstractBlock node, String language, Map<String, Object> opts) {
                return "<pre class=\"highlight\"><code class=\"language-" + language + "\" data-lang=\"" + language + "\">"
                        + node.getContent() + "</code></pre>";
            }

            @Override
            public boolean isHighlight() {
                return false;
            }
        }

        SyntaxHighlighter.register(SampleSyntaxHighlighter.class, "foobaz");
        assertEquals(SampleSyntaxHighlighter.class, SyntaxHighlighter.forName("foobaz"));
    }

    @Test
    @DisplayName("should set language on output of source block when source-highlighter attribute is not set")
    void shouldSetLanguageOnOutputOfSourceBlockWhenSourceHighlighterAttributeIsNotSet() throws Exception {
        var input = """
                [source, ruby]
                ----
                puts 'Hello, World!'
                ----
                """;
        var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel()));
        assertCss("pre.highlight", output, 1);
        assertCss("pre.highlight > code.language-ruby", output, 1);
        assertCss("pre.highlight > code.language-ruby[data-lang=\"ruby\"]", output, 1);
    }

    @Test
    @DisplayName("should set language on output of source block when source-highlighter attribute is not recognized")
    void shouldSetLanguageOnOutputOfSourceBlockWhenSourceHighlighterAttributeIsNotRecognized() throws Exception {
        var input = """
                :source-highlighter: unknown

                [source, ruby]
                ----
                puts 'Hello, World!'
                ----
                """;
        var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel()));
        assertCss("pre.highlight", output, 1);
        assertCss("pre.highlight > code.language-ruby", output, 1);
        assertCss("pre.highlight > code.language-ruby[data-lang=\"ruby\"]", output, 1);
    }

    @Test
    @Disabled("the CodeRay adapter is not ported (it requires a gem)")
    @DisplayName("should highlight source if source highlighter is set even if language is not set")
    void shouldHighlightSourceIfSourceHighlighterIsSetEvenIfLanguageIsNotSet() throws Exception {
    }

    @Test
    @Disabled("the CodeRay adapter is not ported (it requires a gem)")
    @DisplayName("should not crash if source block has no lines and source highlighter is set")
    void shouldNotCrashIfSourceBlockHasNoLinesAndSourceHighlighterIsSet() throws Exception {
    }

    @Test
    @Disabled("the CodeRay adapter is not ported (it requires a gem)")
    @DisplayName("should highlight source inside AsciiDoc table cell if source-highlighter attribute is set")
    void shouldHighlightSourceInsideAsciiDocTableCellIfSourceHighlighterAttributeIsSet() throws Exception {
    }

    @Test
    @DisplayName("should set starting line number in DocBook output if linenums option is enabled and start attribute is set")
    void shouldSetStartingLineNumberInDocBookOutputIfLinenumsOptionIsEnabledAndStartAttributeIsSet() throws Exception {
        var input = """
                [source%linenums,java,start=3]
                ----
                public class HelloWorld {
                  public static void main(String[] args) {
                    out.println("Hello, World!");
                  }
                }
                ----
                """;
        var output = convertStringToEmbedded(input, Options.of("backend", "docbook", "safe", SafeMode.SAFE.getLevel()));
        assertCss("programlisting[startinglinenumber]", output, 1);
        assertCss("programlisting[startinglinenumber=\"3\"]", output, 1);
    }

    @Test
    @Disabled("the CodeRay adapter is not ported (it requires a gem)")
    @DisplayName("should read source language from source-language document attribute if not specified on source block")
    void shouldReadSourceLanguageFromSourceLanguageDocumentAttributeIfNotSpecifiedOnSourceBlock() throws Exception {
    }

    @Test
    @DisplayName("should rename document attribute named language to source-language when compat-mode is enabled")
    void shouldRenameDocumentAttributeNamedLanguageToSourceLanguageWhenCompatModeIsEnabled() throws Exception {
        var input = """
                :language: ruby

                {source-language}
                """;
        assertEquals("ruby", convertInlineString(input, Options.of("attributes", Options.of("compat-mode", ""))));
        input = """
                :language: ruby

                {source-language}
                """;
        assertEquals("{source-language}", convertInlineString(input));
    }

    @Nested
    @Disabled("the CodeRay, Rouge and Pygments adapters are not ported (they require a gem)")
    @DisplayName("CodeRay")
    class CodeRay {
    }

    @Nested
    @DisplayName("Highlight.js")
    class HighlightJs {

        @Test
        @DisplayName("should add data-lang as last attribute on code tag when source-highlighter is highlight.js")
        void shouldAddDataLangAsLastAttributeOnCodeTagWhenSourceHighlighterIsHighlightJs() throws Exception {
            var input = """
                    :source-highlighter: highlight.js

                    [source,ruby]
                    ----
                    puts 'Hello, World!'
                    ----
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", "safe"));
            assertIncludes(output, "<code class=\"language-ruby hljs\" data-lang=\"ruby\">");
        }

        @Test
        @DisplayName("should include remote highlight.js assets if source-highlighter attribute is highlight.js")
        void shouldIncludeRemoteHighlightJsAssetsIfSourceHighlighterAttributeIsHighlightJs() throws Exception {
            var input = """
                    :source-highlighter: highlight.js

                    [source,html]
                    ----
                    <p>Highlight me!</p>
                    ----
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel()));
            assertCss("pre.highlightjs.highlight", output, 1);
            assertCss("pre.highlightjs.highlight > code.language-html.hljs[data-lang=\"html\"]", output, 1);
            assertIncludes(output, "&lt;p&gt;Highlight me!&lt;/p&gt;");
            assertCss("head > link[href*=\"highlight.js\"]", output, 1);
            assertCss("#footer ~ script[src*=\"highlight.min.js\"]", output, 1);
            assertXpath("//script[contains(text(), \"hljs.highlightBlock(el)\")]", output, 1);
        }

        @Test
        @DisplayName("should add language-none class to source block when source-highlighter is highlight.js and language is not set")
        void shouldAddLanguageNoneClassToSourceBlockWhenSourceHighlighterIsHighlightJsAndLanguageIsNotSet() throws Exception {
            var input = """
                    :source-highlighter: highlight.js

                    [source]
                    ----
                    [numbers]
                    one
                    two
                    three
                    ----
                    """;
            var output = convertString(input, Options.of("safe", "safe"));
            assertCss("code.language-none", output, 1);
        }

        @Test
        @DisplayName("should not add highlight class if nohighlight option is set")
        void shouldNotAddHighlightClassIfNohighlightOptionIsSet() throws Exception {
            var input = """
                    :source-highlighter: highlight.js

                    [%nohighlight,ruby]
                    ----
                    puts "hi"
                    ----
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", "safe"));
            assertCss("pre.highlightjs", output, 1);
            assertCss("pre.highlight", output, 0);
        }

        @Test
        @DisplayName("should load additional languages specified by highlightjs-languages")
        void shouldLoadAdditionalLanguagesSpecifiedByHighlightjsLanguages() throws Exception {
            var input = """
                    :source-highlighter: highlight.js
                    :highlightjs-languages: yaml, scilab

                    [source,yaml]
                    ----
                    key: value
                    ----
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel()));
            assertCss("#footer ~ script[src*=\"languages/yaml.min.js\"]", output, 1);
            assertCss("#footer ~ script[src*=\"languages/scilab.min.js\"]", output, 1);
        }
    }

    @Nested
    @DisplayName("Prettify")
    class Prettify {

        @Test
        @DisplayName("should add language classes to child code element when source-highlighter is prettify")
        void shouldAddLanguageClassesToChildCodeElementWhenSourceHighlighterIsPrettify() throws Exception {
            var input = """
                    [source,ruby]
                    ----
                    puts "foo"
                    ----
                    """;
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("source-highlighter", "prettify")));
            assertCss("pre[class=\"prettyprint highlight\"]", output, 1);
            assertCss("pre > code[data-lang=\"ruby\"]", output, 1);
        }

        @Test
        @DisplayName("should set linenums start if linenums are enabled and start attribute is set when source-highlighter is prettify")
        void shouldSetLinenumsStartIfLinenumsAreEnabledAndStartAttributeIsSetWhenSourceHighlighterIsPrettify() throws Exception {
            var input = """
                    [source%linenums,ruby,start=5]
                    ----
                    puts "foo"
                    ----
                    """;
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("source-highlighter", "prettify")));
            assertCss("pre[class=\"prettyprint highlight linenums:5\"]", output, 1);
            assertCss("pre > code[data-lang=\"ruby\"]", output, 1);
        }
    }

    @Nested
    @DisplayName("HTML Pipeline")
    class HTMLPipeline {

        @Test
        @DisplayName("should set lang attribute on pre when source-highlighter is html-pipeline")
        void shouldSetLangAttributeOnPreWhenSourceHighlighterIsHtmlPipeline() throws Exception {
            var input = """
                    [source,ruby]
                    ----
                    filters = [
                    HTML::Pipeline::AsciiDocFilter,
                    HTML::Pipeline::SanitizationFilter,
                    HTML::Pipeline::SyntaxHighlightFilter
                    ]

                    puts HTML::Pipeline.new(filters, {}).call(input)[:output]
                    ----
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("source-highlighter", "html-pipeline")));
            assertCss("pre[lang=\"ruby\"]", output, 1);
            assertCss("pre[lang=\"ruby\"] > code", output, 1);
            assertCss("pre[class]", output, 0);
            assertCss("code[class]", output, 0);
        }
    }

    @Nested
    @Disabled("the CodeRay, Rouge and Pygments adapters are not ported (they require a gem)")
    @DisplayName("Rouge")
    class Rouge {
    }

    @Nested
    @Disabled("the CodeRay, Rouge and Pygments adapters are not ported (they require a gem)")
    @DisplayName("Pygments")
    class Pygments {
    }
}
