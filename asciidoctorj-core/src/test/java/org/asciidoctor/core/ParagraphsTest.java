package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

@DisplayName("Paragraphs")
public class ParagraphsTest extends TestHelper {

    @Nested
    @DisplayName("Normal")
    class Normal {

        @Test
        @DisplayName("should treat plain text separated by blank lines as paragraphs")
        void shouldTreatPlainTextSeparatedByBlankLinesAsParagraphs() throws Exception {
            var input = """
                    Plain text for the win!

                    Yep. Text. Plain and simple.
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("p", output, 2);
            assertXpath("(//p)[1][text() = \"Plain text for the win!\"]", output, 1);
            assertXpath("(//p)[2][text() = \"Yep. Text. Plain and simple.\"]", output, 1);
        }

        @Test
        @DisplayName("should associate block title with paragraph")
        void shouldAssociateBlockTitleWithParagraph() throws Exception {
            var input = """
                    .Titled
                    Paragraph.

                    Winning.
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("p", output, 2);
            assertXpath("(//p)[1]/preceding-sibling::*[@class = \"title\"]", output, 1);
            assertXpath("(//p)[1]/preceding-sibling::*[@class = \"title\"][text() = \"Titled\"]", output, 1);
            assertXpath("(//p)[2]/preceding-sibling::*[@class = \"title\"]", output, 0);
        }

        @Test
        @DisplayName("no duplicate block before next section")
        void noDuplicateBlockBeforeNextSection() throws Exception {
            var input = """
                    = Title

                    Preamble

                    == First Section

                    Paragraph 1

                    Paragraph 2

                    == Second Section

                    Last words
                    """;
            var output = convertString(input);
            assertXpath("//p[text() = \"Paragraph 2\"]", output, 1);
        }

        @Test
        @DisplayName("does not treat wrapped line as a list item")
        void doesNotTreatWrappedLineAsAListItem() throws Exception {
            var input = """
                    paragraph
                    . wrapped line
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("p", output, 1);
            assertXpath("//p[text()=\"paragraph\n. wrapped line\"]", output, 1);
        }

        @Test
        @DisplayName("does not treat wrapped line as a block title")
        void doesNotTreatWrappedLineAsABlockTitle() throws Exception {
            var input = """
                    paragraph
                    .wrapped line
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("p", output, 1);
            assertXpath("//p[text()=\"paragraph\n.wrapped line\"]", output, 1);
        }

        @Test
        @DisplayName("interprets normal paragraph style as normal paragraph")
        void interpretsNormalParagraphStyleAsNormalParagraph() throws Exception {
            var input = """
                    [normal]
                    Normal paragraph.
                    Nothing special.
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("p", output, 1);
        }

        @Test
        @DisplayName("removes indentation from literal paragraph marked as normal")
        void removesIndentationFromLiteralParagraphMarkedAsNormal() throws Exception {
            var input = """
                    [normal]
                      Normal paragraph.
                        Nothing special.
                      Last line.
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("p", output, 1);
            assertXpath("//p[text()=\"Normal paragraph.\n  Nothing special.\nLast line.\"]", output, 1);
        }

        @Test
        @DisplayName("normal paragraph terminates at block attribute list")
        void normalParagraphTerminatesAtBlockAttributeList() throws Exception {
            var input = """
                    normal text
                    [literal]
                    literal text
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".paragraph:root", output, 1);
            assertCss(".literalblock:root", output, 1);
        }

        @Test
        @DisplayName("normal paragraph terminates at block delimiter")
        void normalParagraphTerminatesAtBlockDelimiter() throws Exception {
            var input = """
                    normal text
                    --
                    text in open block
                    --
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".paragraph:root", output, 1);
            assertCss(".openblock:root", output, 1);
        }

        @Test
        @DisplayName("normal paragraph terminates at list continuation")
        void normalParagraphTerminatesAtListContinuation() throws Exception {
            var input = """
                    normal text
                    +
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".paragraph:root", output, 2);
            assertXpath("(/*[@class=\"paragraph\"])[1]/p[text() = \"normal text\"]", output, 1);
            assertXpath("(/*[@class=\"paragraph\"])[2]/p[text() = \"+\"]", output, 1);
        }

        @Test
        @DisplayName("normal style turns literal paragraph into normal paragraph")
        void normalStyleTurnsLiteralParagraphIntoNormalParagraph() throws Exception {
            var input = """
                    [normal]
                     normal paragraph,
                     despite the leading indent
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".paragraph:root > p", output, 1);
        }

        @Test
        @DisplayName("automatically promotes index terms in DocBook output if indexterm-promotion-option is set")
        void automaticallyPromotesIndexTermsInDocBookOutputIfIndextermPromotionOptionIsSet() throws Exception {
            var input = """
                    Here is an index entry for ((tigers)).
                    indexterm:[Big cats,Tigers,Siberian Tiger]
                    Here is an index entry for indexterm2:[Linux].
                    (((Operating Systems,Linux)))
                    Note that multi-entry terms generate separate index entries.
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook", "attributes", Options.of("indexterm-promotion-option", "")));
            assertXpath("/simpara", output, 1);
            var term1 = xmlnodesAtXpath("(//indexterm)[1]", output, 1);
            assertEquals("<indexterm>\n<primary>tigers</primary>\n</indexterm>", str(term1));
            assertTruthy(startsWith(term1.getNext().getContent(), "tigers"));
            var term2 = xmlnodesAtXpath("(//indexterm)[2]", output, 1);
            var term2Elements = term2.getElements();
            assertEquals(3, size(term2Elements));
            assertEquals("<primary>Big cats</primary>", str(term2Elements.get(0)));
            assertEquals("<secondary>Tigers</secondary>", str(term2Elements.get(1)));
            assertEquals("<tertiary>Siberian Tiger</tertiary>", str(term2Elements.get(2)));
            var term3 = xmlnodesAtXpath("(//indexterm)[3]", output, 1);
            var term3Elements = term3.getElements();
            assertEquals(2, size(term3Elements));
            assertEquals("<primary>Tigers</primary>", str(term3Elements.get(0)));
            assertEquals("<secondary>Siberian Tiger</secondary>", str(term3Elements.get(1)));
            var term4 = xmlnodesAtXpath("(//indexterm)[4]", output, 1);
            var term4Elements = term4.getElements();
            assertEquals(1, size(term4Elements));
            assertEquals("<primary>Siberian Tiger</primary>", str(term4Elements.get(0)));
            var term5 = xmlnodesAtXpath("(//indexterm)[5]", output, 1);
            assertEquals("<indexterm>\n<primary>Linux</primary>\n</indexterm>", str(term5));
            assertTruthy(startsWith(term5.getNext().getContent(), "Linux"));
            assertXpath("(//indexterm)[6]/*", output, 2);
            assertXpath("(//indexterm)[7]/*", output, 1);
        }

        @Test
        @DisplayName("does not automatically promote index terms in DocBook output if indexterm-promotion-option is not set")
        void doesNotAutomaticallyPromoteIndexTermsInDocBookOutputIfIndextermPromotionOptionIsNotSet() throws Exception {
            var input = """
                    The Siberian Tiger is one of the biggest living cats.
                    indexterm:[Big cats,Tigers,Siberian Tiger]
                    Note that multi-entry terms generate separate index entries.
                    (((Operating Systems,Linux)))
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertCss("indexterm", output, 2);
            var terms = xmlnodesAtCss("indexterm", output);
            var term1 = terms.get(0);
            var term1Elements = term1.getElements();
            assertEquals(3, size(term1Elements));
            assertEquals("<primary>Big cats</primary>", str(term1Elements.get(0)));
            assertEquals("<secondary>Tigers</secondary>", str(term1Elements.get(1)));
            assertEquals("<tertiary>Siberian Tiger</tertiary>", str(term1Elements.get(2)));
            var term2 = terms.get(1);
            var term2Elements = term2.getElements();
            assertEquals(2, size(term2Elements));
            assertEquals("<primary>Operating Systems</primary>", str(term2Elements.get(0)));
            assertEquals("<secondary>Linux</secondary>", str(term2Elements.get(1)));
        }

        @Test
        @DisplayName("normal paragraph should honor explicit subs list")
        void normalParagraphShouldHonorExplicitSubsList() throws Exception {
            var input = """
                    [subs="specialcharacters"]
                    *<Hey Jude>*
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "*&lt;Hey Jude&gt;*");
        }

        @Test
        @DisplayName("normal paragraph should honor specialchars shorthand")
        void normalParagraphShouldHonorSpecialcharsShorthand() throws Exception {
            var input = """
                    [subs="specialchars"]
                    *<Hey Jude>*
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "*&lt;Hey Jude&gt;*");
        }

        @Test
        @DisplayName("should add a hardbreak at end of each line when hardbreaks option is set")
        void shouldAddAHardbreakAtEndOfEachLineWhenHardbreaksOptionIsSet() throws Exception {
            var input = """
                    [%hardbreaks]
                    read
                    my
                    lips
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("br", output, 2);
            assertXpath("//p", output, 1);
            assertIncludes(output, "<p>read<br>\nmy<br>\nlips</p>");
        }

        @Test
        @DisplayName("should be able to toggle hardbreaks by setting hardbreaks-option on document")
        void shouldBeAbleToToggleHardbreaksBySettingHardbreaksOptionOnDocument() throws Exception {
            var input = """
                    :hardbreaks-option:

                    make
                    it
                    so

                    :!hardbreaks:

                    roll it back
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("(//p)[1]/br", output, 2);
            assertXpath("(//p)[2]/br", output, 0);
        }
    }

    @Nested
    @DisplayName("Literal")
    class Literal {

        @Test
        @DisplayName("single-line literal paragraphs")
        void singleLineLiteralParagraphs() throws Exception {
            var input = """
                    you know what?

                     LITERALS

                     ARE LITERALLY

                     AWESOME!
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//pre", output, 3);
        }

        @Test
        @DisplayName("multi-line literal paragraph")
        void multiLineLiteralParagraph() throws Exception {
            var input = """
                    Install instructions:

                     yum install ruby rubygems
                     gem install asciidoctor

                    You're good to go!
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//pre", output, 1);
            assertXpath("//pre[text() = \"yum install ruby rubygems\ngem install asciidoctor\"]", output, 1);
        }

        @Test
        @DisplayName("literal paragraph")
        void literalParagraph() throws Exception {
            var input = """
                    [literal]
                    this text is literally literal
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"literalblock\"]//pre[text()=\"this text is literally literal\"]", output, 1);
        }

        @Test
        @DisplayName("should read content below literal style verbatim")
        void shouldReadContentBelowLiteralStyleVerbatim() throws Exception {
            var input = """
                    [literal]
                    image::not-an-image-block[]
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"literalblock\"]//pre[text()=\"image::not-an-image-block[]\"]", output, 1);
            assertCss("img", output, 0);
        }

        @Test
        @DisplayName("listing paragraph")
        void listingParagraph() throws Exception {
            var input = """
                    [listing]
                    this text is a listing
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"listingblock\"]//pre[text()=\"this text is a listing\"]", output, 1);
        }

        @Test
        @DisplayName("source paragraph")
        void sourceParagraph() throws Exception {
            var input = """
                    [source]
                    use the source, luke!
                    """;
            var block = blockFromString(input);
            assertEquals("listing", block.getContext());
            assertEquals("source", block.getAttribute("style"));
            assertEquals("paragraph", block.getAttribute("cloaked-context"));
            assertNull(block.getAttribute("language"));
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"listingblock\"]//pre[@class=\"highlight\"]/code[text()=\"use the source, luke!\"]", output, 1);
        }

        @Test
        @DisplayName("source code paragraph with language")
        void sourceCodeParagraphWithLanguage() throws Exception {
            var input = """
                    [source, perl]
                    die 'zomg perl is tough';
                    """;
            var block = blockFromString(input);
            assertEquals("listing", block.getContext());
            assertEquals("source", block.getAttribute("style"));
            assertEquals("paragraph", block.getAttribute("cloaked-context"));
            assertEquals("perl", block.getAttribute("language"));
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"listingblock\"]//pre[@class=\"highlight\"]/code[@class=\"language-perl\"][@data-lang=\"perl\"][text()=\"die 'zomg perl is tough';\"]", output, 1);
        }

        @Test
        @DisplayName("literal paragraph terminates at block attribute list")
        void literalParagraphTerminatesAtBlockAttributeList() throws Exception {
            var input = """
                     literal text
                    [normal]
                    normal text
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"literalblock\"]", output, 1);
            assertXpath("/*[@class=\"paragraph\"]", output, 1);
        }

        @Test
        @DisplayName("literal paragraph terminates at block delimiter")
        void literalParagraphTerminatesAtBlockDelimiter() throws Exception {
            var input = """
                     literal text
                    --
                    normal text
                    --
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"literalblock\"]", output, 1);
            assertXpath("/*[@class=\"openblock\"]", output, 1);
        }

        @Test
        @DisplayName("literal paragraph terminates at list continuation")
        void literalParagraphTerminatesAtListContinuation() throws Exception {
            var input = """
                     literal text
                    +
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"literalblock\"]", output, 1);
            assertXpath("/*[@class=\"literalblock\"]//pre[text() = \"literal text\"]", output, 1);
            assertXpath("/*[@class=\"paragraph\"]", output, 1);
            assertXpath("/*[@class=\"paragraph\"]/p[text() = \"+\"]", output, 1);
        }
    }

    @Nested
    @DisplayName("Quote")
    class Quote {

        @Test
        @DisplayName("single-line quote paragraph")
        void singleLineQuoteParagraph() throws Exception {
            var input = """
                    [quote]
                    Famous quote.
                    """;
            var output = convertString(input);
            assertXpath("//*[@class = \"quoteblock\"]", output, 1);
            assertXpath("//*[@class = \"quoteblock\"]//p", output, 0);
            assertXpath("//*[@class = \"quoteblock\"]//*[contains(text(), \"Famous quote.\")]", output, 1);
        }

        @Test
        @DisplayName("quote paragraph terminates at list continuation")
        void quoteParagraphTerminatesAtListContinuation() throws Exception {
            var input = """
                    [quote]
                    A famouse quote.
                    +
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".quoteblock:root", output, 1);
            assertCss(".paragraph:root", output, 1);
            assertXpath("/*[@class=\"paragraph\"]/p[text() = \"+\"]", output, 1);
        }

        @Test
        @DisplayName("verse paragraph")
        void verseParagraph() throws Exception {
            var output = convertString("[verse]\nFamous verse.");
            assertXpath("//*[@class = \"verseblock\"]", output, 1);
            assertXpath("//*[@class = \"verseblock\"]/pre", output, 1);
            assertXpath("//*[@class = \"verseblock\"]//p", output, 0);
            assertXpath("//*[@class = \"verseblock\"]/pre[normalize-space(text()) = \"Famous verse.\"]", output, 1);
        }

        @Test
        @DisplayName("should perform normal subs on a verse paragraph")
        void shouldPerformNormalSubsOnAVerseParagraph() throws Exception {
            var input = """
                    [verse]
                    _GET /groups/link:#group-id[\\{group-id\\}]_
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "<pre class=\"content\"><em>GET /groups/<a href=\"#group-id\">{group-id}</a></em></pre>");
        }

        @Test
        @DisplayName("quote paragraph should honor explicit subs list")
        void quoteParagraphShouldHonorExplicitSubsList() throws Exception {
            var input = """
                    [subs="specialcharacters"]
                    [quote]
                    *Hey Jude*
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "*Hey Jude*");
        }
    }

    @Nested
    @DisplayName("special")
    class Special {

        @Test
        @DisplayName("note multiline syntax")
        void noteMultilineSyntax() throws Exception {
            for (var style : Asciidoctor.ADMONITION_STYLES) {
                assertXpath(("//div[@class='admonitionblock " + str(style.toLowerCase()) + "']"), convertString(("[" + str(style) + "]\nThis is a winner.")));
            }
        }

        @Test
        @DisplayName("note block syntax")
        void noteBlockSyntax() throws Exception {
            for (var style : Asciidoctor.ADMONITION_STYLES) {
                assertXpath(("//div[@class='admonitionblock " + str(style.toLowerCase()) + "']"), convertString(("[" + str(style) + "]\n====\nThis is a winner.\n====")));
            }
        }

        @Test
        @DisplayName("note inline syntax")
        void noteInlineSyntax() throws Exception {
            for (var style : Asciidoctor.ADMONITION_STYLES) {
                assertXpath(("//div[@class='admonitionblock " + str(style.toLowerCase()) + "']"), convertString((str(style) + ": This is important, fool!")));
            }
        }

        @Test
        @DisplayName("should process preprocessor conditional in paragraph content")
        void shouldProcessPreprocessorConditionalInParagraphContent() throws Exception {
            var input = """
                    ifdef::asciidoctor-version[]
                    [sidebar]
                    First line of sidebar.
                    ifdef::backend[The backend is {backend}.]
                    Last line of sidebar.
                    endif::[]
                    """;
            var expected = Strings.chop("""
                    <div class="sidebarblock">
                    <div class="content">
                    First line of sidebar.
                    The backend is html5.
                    Last line of sidebar.
                    </div>
                    </div>
                    """);
            var result = convertStringToEmbedded(input);
            assertEquals(expected, result);
        }

        @Nested
        @DisplayName("Styled Paragraphs")
        class StyledParagraphs {

            @Test
            @DisplayName("should wrap text in simpara for styled paragraphs when converted to DocBook")
            void shouldWrapTextInSimparaForStyledParagraphsWhenConvertedToDocBook() throws Exception {
                var input = """
                        = Book
                        :doctype: book

                        [preface]
                        = About this book

                        [abstract]
                        An abstract for the book.

                        = Part 1

                        [partintro]
                        An intro to this part.

                        == Chapter 1

                        [sidebar]
                        Just a side note.

                        [example]
                        As you can see here.

                        [quote]
                        Wise words from a wise person.

                        [open]
                        Make it what you want.
                        """;
                var output = convertString(input, Options.of("backend", "docbook"));
                assertCss("abstract > simpara", output, 1);
                assertCss("partintro > simpara", output, 1);
                assertCss("sidebar > simpara", output, 1);
                assertCss("informalexample > simpara", output, 1);
                assertCss("blockquote > simpara", output, 1);
                assertCss("chapter > simpara", output, 1);
            }

            @Test
            @DisplayName("should convert open paragraph to open block")
            void shouldConvertOpenParagraphToOpenBlock() throws Exception {
                var input = """
                        [open]
                        Make it what you want.
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".openblock", output, 1);
                assertCss(".openblock p", output, 0);
            }

            @Test
            @DisplayName("should wrap text in simpara for styled paragraphs with title when converted to DocBook")
            void shouldWrapTextInSimparaForStyledParagraphsWithTitleWhenConvertedToDocBook() throws Exception {
                var input = """
                        = Book
                        :doctype: book

                        [preface]
                        = About this book

                        [abstract]
                        .Abstract title
                        An abstract for the book.

                        = Part 1

                        [partintro]
                        .Part intro title
                        An intro to this part.

                        == Chapter 1

                        [sidebar]
                        .Sidebar title
                        Just a side note.

                        [example]
                        .Example title
                        As you can see here.

                        [quote]
                        .Quote title
                        Wise words from a wise person.
                        """;
                var output = convertString(input, Options.of("backend", "docbook"));
                assertCss("abstract > title", output, 1);
                assertXpath("//abstract/title[text() = \"Abstract title\"]", output, 1);
                assertCss("abstract > title + simpara", output, 1);
                assertCss("partintro > title", output, 1);
                assertXpath("//partintro/title[text() = \"Part intro title\"]", output, 1);
                assertCss("partintro > title + simpara", output, 1);
                assertCss("sidebar > title", output, 1);
                assertXpath("//sidebar/title[text() = \"Sidebar title\"]", output, 1);
                assertCss("sidebar > title + simpara", output, 1);
                assertCss("example > title", output, 1);
                assertXpath("//example/title[text() = \"Example title\"]", output, 1);
                assertCss("example > title + simpara", output, 1);
                assertCss("blockquote > title", output, 1);
                assertXpath("//blockquote/title[text() = \"Quote title\"]", output, 1);
                assertCss("blockquote > title + simpara", output, 1);
            }
        }

        @Nested
        @DisplayName("Inline doctype")
        class InlineDoctype {

            @Test
            @DisplayName("should only format and output text in first paragraph when doctype is inline")
            void shouldOnlyFormatAndOutputTextInFirstParagraphWhenDoctypeIsInline() throws Exception {
                var input = "http://asciidoc.org[AsciiDoc] is a _lightweight_ markup language...\n\nignored";
                var output = convertString(input, Options.of("doctype", "inline"));
                assertEquals("<a href=\"http://asciidoc.org\">AsciiDoc</a> is a <em>lightweight</em> markup language&#8230;&#8203;", output);
            }

            @Test
            @DisplayName("should output nil and warn if first block is not a paragraph")
            void shouldOutputNilAndWarnIfFirstBlockIsNotAParagraph() throws Exception {
                var input = "* bullet";
                try (var logger = usingMemoryLogger()) {
                    var output = convertString(input, Options.of("doctype", "inline"));
                    assertNull(output);
                    assertMessage(logger, "WARN", "~no inline candidate");
                }
            }
        }
    }

    @Nested
    @DisplayName("Custom")
    class Custom {

        @Test
        @DisplayName("should not warn if paragraph style is unregisted")
        void shouldNotWarnIfParagraphStyleIsUnregisted() throws Exception {
            var input = """
                    [foo]
                    bar
                    """;
            try (var logger = usingMemoryLogger()) {
                convertStringToEmbedded(input);
                assertEmpty(logger.getMessages());
            }
        }

        @Test
        @DisplayName("should log debug message if paragraph style is unknown and debug level is enabled")
        void shouldLogDebugMessageIfParagraphStyleIsUnknownAndDebugLevelIsEnabled() throws Exception {
            var input = """
                    [foo]
                    bar
                    """;
            try (var logger = usingMemoryLogger(Logger.Severity.DEBUG)) {
                convertStringToEmbedded(input);
                assertMessage(logger, "DEBUG", "<stdin>: line 2: unknown style for paragraph: foo", LogMessage.class);
            }
        }
    }
}
