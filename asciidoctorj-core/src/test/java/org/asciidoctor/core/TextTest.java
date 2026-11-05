package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

@DisplayName("Text")
public class TextTest extends TestHelper {
    String output;

    @Test
    @DisplayName("proper encoding to handle utf8 characters in document using html backend")
    void properEncodingToHandleUtf8CharactersInDocumentUsingHtmlBackend() throws Exception {
        var output = exampleDocument("encoding").convert();
        assertXpath("//p", output, 4);
        assertXpath("//a", output, 1);
    }

    @Test
    @DisplayName("proper encoding to handle utf8 characters in embedded document using html backend")
    void properEncodingToHandleUtf8CharactersInEmbeddedDocumentUsingHtmlBackend() throws Exception {
        var output = exampleDocument("encoding", Options.of("standalone", false)).convert();
        assertXpath("//p", output, 4);
        assertXpath("//a", output, 1);
    }

    @Test
    @DisplayName("proper encoding to handle utf8 characters in document using docbook backend")
    void properEncodingToHandleUtf8CharactersInDocumentUsingDocbookBackend() throws Exception {
        var output = exampleDocument("encoding", Options.of("attributes", Options.of("backend", "docbook", "xmlns", ""))).convert();
        assertXpath("//xmlns:simpara", output, 4);
        assertXpath("//xmlns:link", output, 1);
    }

    @Test
    @DisplayName("proper encoding to handle utf8 characters in embedded document using docbook backend")
    void properEncodingToHandleUtf8CharactersInEmbeddedDocumentUsingDocbookBackend() throws Exception {
        var output = exampleDocument("encoding", Options.of("standalone", false, "attributes", Options.of("backend", "docbook"))).convert();
        assertXpath("//simpara", output, 4);
        assertXpath("//link", output, 1);
    }

    @Test
    @DisplayName("proper encoding to handle utf8 characters in arbitrary block")
    void properEncodingToHandleUtf8CharactersInArbitraryBlock() throws Exception {
        var input = new ArrayList<String>();
        input.add("[verse]\n");
        input.addAll(lines(readFile(sampleDocPath("encoding"))));
        var doc = emptyDocument();
        var reader = new PreprocessorReader(doc, input, null, true);
        var block = Parser.nextBlock(reader, doc);
        assertXpath("//pre", gsub(block.convert(), rx("^\\s*\\n"), ""), 1);
    }

    @Test
    @DisplayName("proper encoding to handle utf8 characters from included file")
    void properEncodingToHandleUtf8CharactersFromIncludedFile() throws Exception {
        var input = "include::fixtures/encoding.adoc[tags=romé]";
        var doc = emptySafeDocument(Options.of("base_dir", testdir()));
        var reader = new PreprocessorReader(doc, input, null, true);
        var block = Parser.nextBlock(reader, doc);
        var output = block.convert();
        assertCss(".paragraph", output, 1);
    }

    @Test
    @DisplayName("escaped text markup")
    void escapedTextMarkup() throws Exception {
        assertMatch(rx("All your &lt;em&gt;inline&lt;/em&gt; markup belongs to &lt;strong&gt;us&lt;/strong&gt;!"), convertString("All your <em>inline</em> markup belongs to <strong>us</strong>!"));
    }

    @Test
    @DisplayName("line breaks")
    void lineBreaks() throws Exception {
        assertXpath("//br", convertString("Well this is +\njust fine and dandy, isn't it?"), 1);
    }

    @Test
    @DisplayName("single- and double-quoted text")
    void singleAndDoubleQuotedText() throws Exception {
        var output = convertStringToEmbedded("``Where?,'' she said, flipping through her copy of `The New Yorker.'", Options.of("attributes", Options.of("compat-mode", "")));
        assertMatch(rx("&#8220;Where\\?,&#8221;"), output);
        assertMatch(rx("&#8216;The New Yorker.&#8217;"), output);
        output = convertStringToEmbedded("\"`Where?,`\" she said, flipping through her copy of '`The New Yorker.`'");
        assertMatch(rx("&#8220;Where\\?,&#8221;"), output);
        assertMatch(rx("&#8216;The New Yorker.&#8217;"), output);
    }

    @Test
    @DisplayName("multiple double-quoted text on a single line")
    void multipleDoubleQuotedTextOnASingleLine() throws Exception {
        assertEquals("&#8220;Our business is constantly changing&#8221; or &#8220;We need faster time to market.&#8221;", convertInlineString("``Our business is constantly changing'' or ``We need faster time to market.''", Options.of("attributes", Options.of("compat-mode", ""))));
        assertEquals("&#8220;Our business is constantly changing&#8221; or &#8220;We need faster time to market.&#8221;", convertInlineString("\"`Our business is constantly changing`\" or \"`We need faster time to market.`\""));
    }

    @Test
    @DisplayName("horizontal rule")
    void horizontalRule() throws Exception {
        var input = """
                This line is separated by a horizontal rule...

                '''

                ...from this line.
                """;
        var output = convertStringToEmbedded(input);
        assertXpath("//hr", output, 1);
        assertXpath("/*[@class=\"paragraph\"]", output, 2);
        assertXpath("(/*[@class=\"paragraph\"])[1]/following-sibling::hr", output, 1);
        assertXpath("/hr/following-sibling::*[@class=\"paragraph\"]", output, 1);
    }

    @Test
    @DisplayName("markdown horizontal rules")
    void markdownHorizontalRules() throws Exception {
        var variants = list("---", "- - -", "***", "* * *", "___", "_ _ _");
        var offsets = list("", " ", "  ", "   ");
        for (var variant : variants) {
            for (var offset : offsets) {
                var input = ("This line is separated by a horizontal rule...\n\n" + str(offset) + str(variant) + "\n\n...from this line.\n");
                var output = convertStringToEmbedded(input);
                assertXpath("//hr", output, 1);
                assertXpath("/*[@class=\"paragraph\"]", output, 2);
                assertXpath("(/*[@class=\"paragraph\"])[1]/following-sibling::hr", output, 1);
                assertXpath("/hr/following-sibling::*[@class=\"paragraph\"]", output, 1);
            }
        }
    }

    @Test
    @DisplayName("markdown horizontal rules negative case")
    void markdownHorizontalRulesNegativeCase() throws Exception {
        var badVariants = list("- - - -", "* * * *", "_ _ _ _");
        var goodOffsets = list("", " ", "  ", "   ");
        for (var variant : badVariants) {
            for (var offset : goodOffsets) {
                var input = ("This line is separated by something that is not a horizontal rule...\n\n" + str(offset) + str(variant) + "\n\n...from this line.\n");
                var output = convertStringToEmbedded(input);
                assertXpath("//hr", output, 0);
            }
        }
        var goodVariants = list("- - -", "* * *", "_ _ _");
        var badOffsets = list("\t", "    ");
        for (var variant : goodVariants) {
            for (var offset : badOffsets) {
                var input = ("This line is separated by something that is not a horizontal rule...\n\n" + str(offset) + str(variant) + "\n\n...from this line.\n");
                var output = convertStringToEmbedded(input);
                assertXpath("//hr", output, 0);
            }
        }
    }

    @Test
    @DisplayName("emphasized text using underscore characters")
    void emphasizedTextUsingUnderscoreCharacters() throws Exception {
        assertXpath("//em", convertString("An _emphatic_ no"));
    }

    @Test
    @DisplayName("emphasized text with single quote using apostrophe characters")
    void emphasizedTextWithSingleQuoteUsingApostropheCharacters() throws Exception {
        var rsquo = decodeChar(8217);
        assertXpath(("//em[text()=\"Johnny" + str(rsquo) + "s\"]"), convertString("It's 'Johnny's' phone", Options.of("attributes", Options.of("compat-mode", ""))));
        assertXpath(("//p[text()=\"It" + str(rsquo) + "s 'Johnny" + str(rsquo) + "s' phone\"]"), convertString("It's 'Johnny's' phone"));
    }

    @Test
    @DisplayName("emphasized text with escaped single quote using apostrophe characters")
    void emphasizedTextWithEscapedSingleQuoteUsingApostropheCharacters() throws Exception {
        assertXpath("//em[text()=\"Johnny's\"]", convertString("It's 'Johnny\\'s' phone", Options.of("attributes", Options.of("compat-mode", ""))));
        assertXpath("//p[text()=\"It's 'Johnny's' phone\"]", convertString("It\\'s 'Johnny\\'s' phone"));
    }

    @Test
    @DisplayName("escaped single quote is restored as single quote")
    void escapedSingleQuoteIsRestoredAsSingleQuote() throws Exception {
        assertXpath("//p[contains(text(), \"Let's do it!\")]", convertString("Let\\'s do it!"));
    }

    @Test
    @DisplayName("unescape escaped single quote emphasis in compat mode only")
    void unescapeEscapedSingleQuoteEmphasisInCompatModeOnly() throws Exception {
        assertXpath("//p[text()=\"A 'single quoted string' example\"]", convertStringToEmbedded("A \\'single quoted string' example", Options.of("attributes", Options.of("compat-mode", ""))));
        assertXpath("//p[text()=\"'single quoted string'\"]", convertStringToEmbedded("\\'single quoted string'", Options.of("attributes", Options.of("compat-mode", ""))));
        assertXpath("//p[text()=\"A \\'single quoted string' example\"]", convertStringToEmbedded("A \\'single quoted string' example"));
        assertXpath("//p[text()=\"\\'single quoted string'\"]", convertStringToEmbedded("\\'single quoted string'"));
    }

    @Test
    @DisplayName("emphasized text at end of line")
    void emphasizedTextAtEndOfLine() throws Exception {
        assertXpath("//em", convertString("This library is _awesome_"));
    }

    @Test
    @DisplayName("emphasized text at beginning of line")
    void emphasizedTextAtBeginningOfLine() throws Exception {
        assertXpath("//em", convertString("_drop_ it"));
    }

    @Test
    @DisplayName("emphasized text across line")
    void emphasizedTextAcrossLine() throws Exception {
        assertXpath("//em", convertString("_check it_"));
    }

    @Test
    @DisplayName("unquoted text")
    void unquotedText() throws Exception {
        refuteInclude("#", convertString("An #unquoted# word"));
    }

    @Test
    @DisplayName("backticks and straight quotes in text")
    void backticksAndStraightQuotesInText() throws Exception {
        var backslash = "\\";
        assertEquals("run <code>foo</code> <em>dog</em>", convertInlineString("run `foo` 'dog'", Options.of("attributes", Options.of("compat-mode", ""))));
        assertEquals("run <code>foo</code> 'dog'", convertInlineString("run `foo` 'dog'"));
        assertEquals("run `foo` 'dog'", convertInlineString(("run " + str(backslash) + "`foo` 'dog'")));
        assertEquals("run &#8216;foo` 'dog&#8217;", convertInlineString("run '`foo` 'dog`'"));
        assertEquals("run '`foo` 'dog`'", convertInlineString(("run " + str(backslash) + "'`foo` 'dog" + str(backslash) + "`'")));
    }

    @Test
    @DisplayName("plus characters inside single plus passthrough")
    void plusCharactersInsideSinglePlusPassthrough() throws Exception {
        assertXpath("//p[text()=\"+\"]", convertStringToEmbedded("+++"));
        assertXpath("//p[text()=\"+=\"]", convertStringToEmbedded("++=+"));
    }

    @Test
    @DisplayName("plus passthrough escapes entity reference")
    void plusPassthroughEscapesEntityReference() throws Exception {
        assertMatch(rx("&amp;#44;"), convertStringToEmbedded("+&#44;+"));
        assertMatch(rx("one&amp;#44;two"), convertStringToEmbedded("one++&#44;++two"));
    }

    @Nested
    @DisplayName("basic styling")
    class BasicStyling {

        @BeforeEach
        void setup() throws Exception {
            output = convertString("A *BOLD* word.  An _italic_ word.  A `mono` word.  ^superscript!^ and some ~subscript~.");
        }

        @Test
        @DisplayName("strong")
        void strong() throws Exception {
            assertXpath("//strong", output, 1);
        }

        @Test
        @DisplayName("italic")
        void italic() throws Exception {
            assertXpath("//em", output, 1);
        }

        @Test
        @DisplayName("monospaced")
        void monospaced() throws Exception {
            assertXpath("//code", output, 1);
        }

        @Test
        @DisplayName("superscript")
        void superscript() throws Exception {
            assertXpath("//sup", output, 1);
        }

        @Test
        @DisplayName("subscript")
        void subscript() throws Exception {
            assertXpath("//sub", output, 1);
        }

        @Test
        @DisplayName("passthrough")
        void passthrough() throws Exception {
            assertXpath("//code", convertString("This is +passed through+."), 0);
            assertXpath("//code", convertString("This is +passed through and monospaced+.", Options.of("attributes", Options.of("compat-mode", ""))), 1);
        }

        @Test
        @DisplayName("nested styles")
        void nestedStyles() throws Exception {
            var output = convertString("Winning *big _time_* in the +city *boyeeee*+.", Options.of("attributes", Options.of("compat-mode", "")));
            assertXpath("//strong/em", output);
            assertXpath("//code/strong", output);
            output = convertString("Winning *big _time_* in the `city *boyeeee*`.");
            assertXpath("//strong/em", output);
            assertXpath("//code/strong", output);
        }

        @Test
        @DisplayName("unconstrained quotes")
        void unconstrainedQuotes() throws Exception {
            var output = convertString("**B**__I__++M++[role]++M++", Options.of("attributes", Options.of("compat-mode", "")));
            assertXpath("//strong", output, 1);
            assertXpath("//em", output, 1);
            assertXpath("//code[not(@class)]", output, 1);
            assertXpath("//code[@class=\"role\"]", output, 1);
            output = convertString("**B**__I__``M``[role]``M``");
            assertXpath("//strong", output, 1);
            assertXpath("//em", output, 1);
            assertXpath("//code[not(@class)]", output, 1);
            assertXpath("//code[@class=\"role\"]", output, 1);
        }
    }

    @Test
    @DisplayName("should format Asian characters as words")
    void shouldFormatAsianCharactersAsWords() throws Exception {
        assertXpath("//strong", convertStringToEmbedded("bold *要* bold"));
        assertXpath("//strong", convertStringToEmbedded("bold *素* bold"));
        assertXpath("//strong", convertStringToEmbedded("bold *要素* bold"));
    }
}
