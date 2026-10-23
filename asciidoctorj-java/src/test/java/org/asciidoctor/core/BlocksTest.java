package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

@DisplayName("Blocks")
public class BlocksTest extends TestHelper {
    MemoryLogger logger;
    Logger defaultLogger = LoggerManager.getLogger();

    @BeforeEach
    void setup() throws Exception {
        LoggerManager.setLogger((logger = new MemoryLogger()));
    }

    @AfterEach
    void teardown() throws Exception {
        LoggerManager.setLogger(defaultLogger);
    }

    @Nested
    @DisplayName("Layout Breaks")
    class LayoutBreaks {

        @Test
        @DisplayName("horizontal rule")
        void horizontalRule() throws Exception {
            for (var line : list("'''", "''''", "''''''")) {
                var output = convertStringToEmbedded(line);
                assertIncludes(output, "<hr>");
            }
        }

        @Test
        @DisplayName("horizontal rule with markdown syntax disabled")
        void horizontalRuleWithMarkdownSyntaxDisabled() throws Exception {
            var oldMarkdownSyntax = Compliance.isMarkdownSyntax();
            try {
                Compliance.setMarkdownSyntax(false);
                for (var line : list("'''", "''''", "''''''")) {
                    var output = convertStringToEmbedded(line);
                    assertIncludes(output, "<hr>");
                }
                for (var line : list("---", "***", "___")) {
                    var output = convertStringToEmbedded(line);
                    refuteIncludes(output, "<hr>");
                }
            } finally {
                Compliance.setMarkdownSyntax(oldMarkdownSyntax);
            }
        }

        @Test
        @DisplayName("< 3 chars does not make horizontal rule")
        void test3CharsDoesNotMakeHorizontalRule() throws Exception {
            for (var line : list("'", "''")) {
                var output = convertStringToEmbedded(line);
                refuteIncludes(output, "<hr>");
                assertIncludes(output, ("<p>" + str(line) + "</p>"));
            }
        }

        @Test
        @DisplayName("mixed chars does not make horizontal rule")
        void mixedCharsDoesNotMakeHorizontalRule() throws Exception {
            for (var line : list("''<", "'''<", "' ' '")) {
                var output = convertStringToEmbedded(line);
                refuteIncludes(output, "<hr>");
                assertIncludes(output, ("<p>" + str(sub(line, "<", "&lt;")) + "</p>"));
            }
        }

        @Test
        @DisplayName("horizontal rule between blocks")
        void horizontalRuleBetweenBlocks() throws Exception {
            var output = convertStringToEmbedded("Block above\n\n'''\n\nBlock below");
            assertXpath("/hr", output, 1);
            assertXpath("/hr/preceding-sibling::*", output, 1);
            assertXpath("/hr/following-sibling::*", output, 1);
        }

        @Test
        @DisplayName("horizontal rule with role")
        void horizontalRuleWithRole() throws Exception {
            var input = """
                    [.fancy]
                    '''
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("hr", output, 1);
            assertCss("hr.fancy", output, 1);
        }

        @Test
        @DisplayName("page break")
        void pageBreak() throws Exception {
            var output = convertStringToEmbedded("page 1\n\n<<<\n\npage 2");
            assertXpath("/*[@class=\"page-break\"]", output, 1);
            assertXpath("/*[@class=\"page-break\"]/preceding-sibling::div/p[text()=\"page 1\"]", output, 1);
            assertXpath("/*[@class=\"page-break\"]/following-sibling::div/p[text()=\"page 2\"]", output, 1);
        }
    }

    @Nested
    @DisplayName("Comments")
    class Comments {

        @Test
        @DisplayName("line comment between paragraphs offset by blank lines")
        void lineCommentBetweenParagraphsOffsetByBlankLines() throws Exception {
            var input = """
                    first paragraph

                    // line comment

                    second paragraph
                    """;
            var output = convertStringToEmbedded(input);
            refuteMatch(rx("line comment"), output);
            assertXpath("//p", output, 2);
        }

        @Test
        @DisplayName("adjacent line comment between paragraphs")
        void adjacentLineCommentBetweenParagraphs() throws Exception {
            var input = """
                    first line
                    // line comment
                    second line
                    """;
            var output = convertStringToEmbedded(input);
            refuteMatch(rx("line comment"), output);
            assertXpath("//p", output, 1);
            assertXpath("//p[1][text()='first line\nsecond line']", output, 1);
        }

        @Test
        @DisplayName("comment block between paragraphs offset by blank lines")
        void commentBlockBetweenParagraphsOffsetByBlankLines() throws Exception {
            var input = """
                    first paragraph

                    ////
                    block comment
                    ////

                    second paragraph
                    """;
            var output = convertStringToEmbedded(input);
            refuteMatch(rx("block comment"), output);
            assertXpath("//p", output, 2);
        }

        @Test
        @DisplayName("comment block between paragraphs offset by blank lines inside delimited block")
        void commentBlockBetweenParagraphsOffsetByBlankLinesInsideDelimitedBlock() throws Exception {
            var input = """
                    ====
                    first paragraph

                    ////
                    block comment
                    ////

                    second paragraph
                    ====
                    """;
            var output = convertStringToEmbedded(input);
            refuteMatch(rx("block comment"), output);
            assertXpath("//p", output, 2);
        }

        @Test
        @DisplayName("adjacent comment block between paragraphs")
        void adjacentCommentBlockBetweenParagraphs() throws Exception {
            var input = """
                    first paragraph
                    ////
                    block comment
                    ////
                    second paragraph
                    """;
            var output = convertStringToEmbedded(input);
            refuteMatch(rx("block comment"), output);
            assertXpath("//p", output, 2);
        }

        @Test
        @DisplayName("can convert with block comment at end of document with trailing newlines")
        void canConvertWithBlockCommentAtEndOfDocumentWithTrailingNewlines() throws Exception {
            var input = """
                    paragraph

                    ////
                    block comment
                    ////


                    """;
            var output = convertStringToEmbedded(input);
            refuteMatch(rx("block comment"), output);
        }

        @Test
        @DisplayName("trailing newlines after block comment at end of document does not create paragraph")
        void trailingNewlinesAfterBlockCommentAtEndOfDocumentDoesNotCreateParagraph() throws Exception {
            var input = """
                    paragraph

                    ////
                    block comment
                    ////


                    """;
            var d = documentFromString(input);
            assertEquals(1, size(d.getBlocks()));
            assertXpath("//p", d.convert(), 1);
        }

        @Test
        @DisplayName("line starting with three slashes should not be line comment")
        void lineStartingWithThreeSlashesShouldNotBeLineComment() throws Exception {
            var input = "/// not a line comment";
            var output = convertStringToEmbedded(input);
            refuteEmpty(Strings.strip(output), ("Line should be emitted => " + str(Strings.rstrip(input))));
        }

        @Test
        @DisplayName("preprocessor directives should not be processed within comment block within block metadata")
        void preprocessorDirectivesShouldNotBeProcessedWithinCommentBlockWithinBlockMetadata() throws Exception {
            var input = """
                    .sample title
                    ////
                    ifdef::asciidoctor[////]
                    ////
                    line should be shown
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//p[text()=\"line should be shown\"]", output, 1);
        }

        @Test
        @DisplayName("preprocessor directives should not be processed within comment block")
        void preprocessorDirectivesShouldNotBeProcessedWithinCommentBlock() throws Exception {
            var input = """
                    dummy line

                    ////
                    ifdef::asciidoctor[////]
                    ////

                    line should be shown
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//p[text()=\"line should be shown\"]", output, 1);
        }

        @Test
        @DisplayName("should warn if unterminated comment block is detected in body")
        void shouldWarnIfUnterminatedCommentBlockIsDetectedInBody() throws Exception {
            var input = """
                    before comment block

                    ////
                    content that has been disabled

                    supposed to be after comment block, except it got swallowed by block comment
                    """;
            convertStringToEmbedded(input);
            assertMessage(logger, "WARN", "<stdin>: line 3: unterminated comment block", LogMessage.class);
        }

        @Test
        @DisplayName("should warn if unterminated comment block is detected inside another block")
        void shouldWarnIfUnterminatedCommentBlockIsDetectedInsideAnotherBlock() throws Exception {
            var input = """
                    before sidebar block

                    ****
                    ////
                    content that has been disabled
                    ****

                    supposed to be after sidebar block, except it got swallowed by block comment
                    """;
            convertStringToEmbedded(input);
            assertMessage(logger, "WARN", "<stdin>: line 4: unterminated comment block", LogMessage.class);
        }

        @Test
        @DisplayName("preprocessor directives should not be processed within comment open block")
        void preprocessorDirectivesShouldNotBeProcessedWithinCommentOpenBlock() throws Exception {
            var input = """
                    [comment]
                    --
                    first line of comment
                    ifdef::asciidoctor[--]
                    line should not be shown
                    --

                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//p", output, 0);
        }

        @Test
        @DisplayName("preprocessor directives should not be processed on subsequent lines of a comment paragraph")
        void preprocessorDirectivesShouldNotBeProcessedOnSubsequentLinesOfACommentParagraph() throws Exception {
            var input = """
                    [comment]
                    first line of content
                    ifdef::asciidoctor[////]

                    this line should be shown
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//p[text()=\"this line should be shown\"]", output, 1);
        }

        @Test
        @DisplayName("comment style on open block should only skip block")
        void commentStyleOnOpenBlockShouldOnlySkipBlock() throws Exception {
            var input = """
                    [comment]
                    --
                    skip

                    this block
                    --

                    not this text
                    """;
            var result = convertStringToEmbedded(input);
            assertXpath("//p", result, 1);
            assertXpath("//p[text()=\"not this text\"]", result, 1);
        }

        @Test
        @DisplayName("comment style on paragraph should only skip paragraph")
        void commentStyleOnParagraphShouldOnlySkipParagraph() throws Exception {
            var input = """
                    [comment]
                    skip
                    this paragraph

                    not this text
                    """;
            var result = convertStringToEmbedded(input);
            assertXpath("//p", result, 1);
            assertXpath("//p[text()=\"not this text\"]", result, 1);
        }

        @Test
        @DisplayName("comment style on paragraph should not cause adjacent block to be skipped")
        void commentStyleOnParagraphShouldNotCauseAdjacentBlockToBeSkipped() throws Exception {
            var input = """
                    [comment]
                    skip
                    this paragraph
                    [example]
                    not this text
                    """;
            var result = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"exampleblock\"]", result, 1);
            assertXpath("/*[@class=\"exampleblock\"]//*[normalize-space(text())=\"not this text\"]", result, 1);
        }

        @Test
        @DisplayName("should not drop content that follows skipped content inside a delimited block")
        void shouldNotDropContentThatFollowsSkippedContentInsideADelimitedBlock() throws Exception {
            var input = """
                    ====
                    paragraph

                    [comment#idname]
                    skip

                    paragraph
                    ====
                    """;
            var result = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"exampleblock\"]", result, 1);
            assertXpath("/*[@class=\"exampleblock\"]//*[@class=\"paragraph\"]", result, 2);
            assertXpath("//*[@class=\"paragraph\"][@id=\"idname\"]", result, 0);
        }
    }

    @Nested
    @DisplayName("Sidebar Blocks")
    class SidebarBlocks {

        @Test
        @DisplayName("should parse sidebar block")
        void shouldParseSidebarBlock() throws Exception {
            var input = """
                    == Section

                    .Sidebar
                    ****
                    Content goes here
                    ****
                    """;
            var result = convertString(input);
            assertXpath("//*[@class=\"sidebarblock\"]//p", result, 1);
        }
    }

    @Nested
    @DisplayName("Quote and Verse Blocks")
    class QuoteAndVerseBlocks {

        @Test
        @DisplayName("quote block with no attribution")
        void quoteBlockWithNoAttribution() throws Exception {
            var input = """
                    ____
                    A famous quote.
                    ____
                    """;
            var output = convertString(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock > blockquote", output, 1);
            assertCss(".quoteblock > blockquote > .paragraph > p", output, 1);
            assertCss(".quoteblock > .attribution", output, 0);
            assertXpath("//*[@class=\"quoteblock\"]//p[text()=\"A famous quote.\"]", output, 1);
        }

        @Test
        @DisplayName("quote block with attribution")
        void quoteBlockWithAttribution() throws Exception {
            var input = """
                    [quote, Famous Person, Famous Book (1999)]
                    ____
                    A famous quote.
                    ____
                    """;
            var output = convertString(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock > blockquote", output, 1);
            assertCss(".quoteblock > blockquote > .paragraph > p", output, 1);
            assertCss(".quoteblock > .attribution", output, 1);
            assertCss(".quoteblock > .attribution > cite", output, 1);
            assertCss(".quoteblock > .attribution > br + cite", output, 1);
            assertXpath("//*[@class=\"quoteblock\"]/*[@class=\"attribution\"]/cite[text()=\"Famous Book (1999)\"]", output, 1);
            var attribution = xmlnodesAtXpath("//*[@class=\"quoteblock\"]/*[@class=\"attribution\"]", output, 1);
            var author = first(attribution.getChildren());
            assertEquals((str(decodeChar(8212)) + " Famous Person"), Strings.strip(author.getText()));
        }

        @Test
        @DisplayName("quote block with attribute and id and role shorthand")
        void quoteBlockWithAttributeAndIdAndRoleShorthand() throws Exception {
            var input = """
                    [quote#justice-to-all.solidarity, Martin Luther King, Jr.]
                    ____
                    Injustice anywhere is a threat to justice everywhere.
                    ____
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".quoteblock", output, 1);
            assertCss("#justice-to-all.quoteblock.solidarity", output, 1);
            assertCss(".quoteblock > .attribution", output, 1);
        }

        @Test
        @DisplayName("setting ID using style shorthand should not reset block style")
        void settingIDUsingStyleShorthandShouldNotResetBlockStyle() throws Exception {
            var input = """
                    [quote]
                    [#justice-to-all.solidarity, Martin Luther King, Jr.]
                    ____
                    Injustice anywhere is a threat to justice everywhere.
                    ____
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".quoteblock", output, 1);
            assertCss("#justice-to-all.quoteblock.solidarity", output, 1);
            assertCss(".quoteblock > .attribution", output, 1);
        }

        @Test
        @DisplayName("quote block with complex content")
        void quoteBlockWithComplexContent() throws Exception {
            var input = """
                    ____
                    A famous quote.

                    NOTE: _That_ was inspiring.
                    ____
                    """;
            var output = convertString(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock > blockquote", output, 1);
            assertCss(".quoteblock > blockquote > .paragraph", output, 1);
            assertCss(".quoteblock > blockquote > .paragraph + .admonitionblock", output, 1);
        }

        @Test
        @DisplayName("quote block with attribution converted to DocBook")
        void quoteBlockWithAttributionConvertedToDocBook() throws Exception {
            var input = """
                    [quote, Famous Person, Famous Book (1999)]
                    ____
                    A famous quote.
                    ____
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("blockquote", output, 1);
            assertCss("blockquote > simpara", output, 1);
            assertCss("blockquote > attribution", output, 1);
            assertCss("blockquote > attribution > citetitle", output, 1);
            assertXpath("//blockquote/attribution/citetitle[text()=\"Famous Book (1999)\"]", output, 1);
            var attribution = xmlnodesAtXpath("//blockquote/attribution", output, 1);
            var author = first(attribution.getChildren());
            assertEquals("Famous Person", Strings.strip(author.getText()));
        }

        @Test
        @DisplayName("epigraph quote block with attribution converted to DocBook")
        void epigraphQuoteBlockWithAttributionConvertedToDocBook() throws Exception {
            var input = """
                    [.epigraph, Famous Person, Famous Book (1999)]
                    ____
                    A famous quote.
                    ____
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("epigraph", output, 1);
            assertCss("epigraph > simpara", output, 1);
            assertCss("epigraph > attribution", output, 1);
            assertCss("epigraph > attribution > citetitle", output, 1);
            assertXpath("//epigraph/attribution/citetitle[text()=\"Famous Book (1999)\"]", output, 1);
            var attribution = xmlnodesAtXpath("//epigraph/attribution", output, 1);
            var author = first(attribution.getChildren());
            assertEquals("Famous Person", Strings.strip(author.getText()));
        }

        @Test
        @DisplayName("markdown-style quote block with single paragraph and no attribution")
        void markdownStyleQuoteBlockWithSingleParagraphAndNoAttribution() throws Exception {
            var input = """
                    > A famous quote.
                    > Some more inspiring words.
                    """;
            var output = convertString(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock > blockquote", output, 1);
            assertCss(".quoteblock > blockquote > .paragraph > p", output, 1);
            assertCss(".quoteblock > .attribution", output, 0);
            assertXpath("//*[@class=\"quoteblock\"]//p[text()=\"A famous quote.\nSome more inspiring words.\"]", output, 1);
        }

        @Test
        @DisplayName("lazy markdown-style quote block with single paragraph and no attribution")
        void lazyMarkdownStyleQuoteBlockWithSingleParagraphAndNoAttribution() throws Exception {
            var input = """
                    > A famous quote.
                    Some more inspiring words.
                    """;
            var output = convertString(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock > blockquote", output, 1);
            assertCss(".quoteblock > blockquote > .paragraph > p", output, 1);
            assertCss(".quoteblock > .attribution", output, 0);
            assertXpath("//*[@class=\"quoteblock\"]//p[text()=\"A famous quote.\nSome more inspiring words.\"]", output, 1);
        }

        @Test
        @DisplayName("markdown-style quote block with multiple paragraphs and no attribution")
        void markdownStyleQuoteBlockWithMultipleParagraphsAndNoAttribution() throws Exception {
            var input = """
                    > A famous quote.
                    >
                    > Some more inspiring words.
                    """;
            var output = convertString(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock > blockquote", output, 1);
            assertCss(".quoteblock > blockquote > .paragraph > p", output, 2);
            assertCss(".quoteblock > .attribution", output, 0);
            assertXpath("(//*[@class=\"quoteblock\"]//p)[1][text()=\"A famous quote.\"]", output, 1);
            assertXpath("(//*[@class=\"quoteblock\"]//p)[2][text()=\"Some more inspiring words.\"]", output, 1);
        }

        @Test
        @DisplayName("markdown-style quote block with multiple blocks and no attribution")
        void markdownStyleQuoteBlockWithMultipleBlocksAndNoAttribution() throws Exception {
            var input = """
                    > A famous quote.
                    >
                    > NOTE: Some more inspiring words.
                    """;
            var output = convertString(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock > blockquote", output, 1);
            assertCss(".quoteblock > blockquote > .paragraph > p", output, 1);
            assertCss(".quoteblock > blockquote > .admonitionblock", output, 1);
            assertCss(".quoteblock > .attribution", output, 0);
            assertXpath("(//*[@class=\"quoteblock\"]//p)[1][text()=\"A famous quote.\"]", output, 1);
            assertXpath("(//*[@class=\"quoteblock\"]//*[@class=\"admonitionblock note\"]//*[@class=\"content\"])[1][normalize-space(text())=\"Some more inspiring words.\"]", output, 1);
        }

        @Test
        @DisplayName("markdown-style quote block with single paragraph and attribution")
        void markdownStyleQuoteBlockWithSingleParagraphAndAttribution() throws Exception {
            var input = """
                    > A famous quote.
                    > Some more inspiring words.
                    > -- Famous Person, Famous Source, Volume 1 (1999)
                    """;
            var output = convertString(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock > blockquote", output, 1);
            assertCss(".quoteblock > blockquote > .paragraph > p", output, 1);
            assertXpath("//*[@class=\"quoteblock\"]//p[text()=\"A famous quote.\nSome more inspiring words.\"]", output, 1);
            assertCss(".quoteblock > .attribution", output, 1);
            assertCss(".quoteblock > .attribution > cite", output, 1);
            assertCss(".quoteblock > .attribution > br + cite", output, 1);
            assertXpath("//*[@class=\"quoteblock\"]/*[@class=\"attribution\"]/cite[text()=\"Famous Source, Volume 1 (1999)\"]", output, 1);
            var attribution = xmlnodesAtXpath("//*[@class=\"quoteblock\"]/*[@class=\"attribution\"]", output, 1);
            var author = first(attribution.getChildren());
            assertEquals((str(decodeChar(8212)) + " Famous Person"), Strings.strip(author.getText()));
        }

        @Test
        @DisplayName("markdown-style quote block with only attribution")
        void markdownStyleQuoteBlockWithOnlyAttribution() throws Exception {
            var input = "> -- Anonymous";
            var output = convertString(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock > blockquote", output, 1);
            assertCss(".quoteblock > blockquote > *", output, 0);
            assertCss(".quoteblock > .attribution", output, 1);
            assertXpath("//*[@class=\"quoteblock\"]//*[@class=\"attribution\"][contains(text(),\"Anonymous\")]", output, 1);
        }

        @Test
        @DisplayName("should parse credit line in markdown-style quote block like positional block attributes")
        void shouldParseCreditLineInMarkdownStyleQuoteBlockLikePositionalBlockAttributes() throws Exception {
            var input = """
                    > I hold it that a little rebellion now and then is a good thing,
                    > and as necessary in the political world as storms in the physical.
                    -- Thomas Jefferson, https://jeffersonpapers.princeton.edu/selected-documents/james-madison-1[The Papers of Thomas Jefferson, Volume 11]
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock cite a[href=\"https://jeffersonpapers.princeton.edu/selected-documents/james-madison-1\"]", output, 1);
        }

        @Test
        @DisplayName("quoted paragraph-style quote block with attribution")
        void quotedParagraphStyleQuoteBlockWithAttribution() throws Exception {
            var input = """
                    "A famous quote.
                    Some more inspiring words."
                    -- Famous Person, Famous Source, Volume 1 (1999)
                    """;
            var output = convertString(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock > blockquote", output, 1);
            assertXpath("//*[@class=\"quoteblock\"]/blockquote[normalize-space(text())=\"A famous quote. Some more inspiring words.\"]", output, 1);
            assertCss(".quoteblock > .attribution", output, 1);
            assertCss(".quoteblock > .attribution > cite", output, 1);
            assertCss(".quoteblock > .attribution > br + cite", output, 1);
            assertXpath("//*[@class=\"quoteblock\"]/*[@class=\"attribution\"]/cite[text()=\"Famous Source, Volume 1 (1999)\"]", output, 1);
            var attribution = xmlnodesAtXpath("//*[@class=\"quoteblock\"]/*[@class=\"attribution\"]", output, 1);
            var author = first(attribution.getChildren());
            assertEquals((str(decodeChar(8212)) + " Famous Person"), Strings.strip(author.getText()));
        }

        @Test
        @DisplayName("should parse credit line in quoted paragraph-style quote block like positional block attributes")
        void shouldParseCreditLineInQuotedParagraphStyleQuoteBlockLikePositionalBlockAttributes() throws Exception {
            var input = """
                    "I hold it that a little rebellion now and then is a good thing,
                    and as necessary in the political world as storms in the physical."
                    -- Thomas Jefferson, https://jeffersonpapers.princeton.edu/selected-documents/james-madison-1[The Papers of Thomas Jefferson, Volume 11]
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock cite a[href=\"https://jeffersonpapers.princeton.edu/selected-documents/james-madison-1\"]", output, 1);
        }

        @Test
        @DisplayName("single-line verse block without attribution")
        void singleLineVerseBlockWithoutAttribution() throws Exception {
            var input = """
                    [verse]
                    ____
                    A famous verse.
                    ____
                    """;
            var output = convertString(input);
            assertCss(".verseblock", output, 1);
            assertCss(".verseblock > pre", output, 1);
            assertCss(".verseblock > .attribution", output, 0);
            assertCss(".verseblock p", output, 0);
            assertXpath("//*[@class=\"verseblock\"]/pre[normalize-space(text())=\"A famous verse.\"]", output, 1);
        }

        @Test
        @DisplayName("single-line verse block with attribution")
        void singleLineVerseBlockWithAttribution() throws Exception {
            var input = """
                    [verse, Famous Poet, Famous Poem]
                    ____
                    A famous verse.
                    ____
                    """;
            var output = convertString(input);
            assertCss(".verseblock", output, 1);
            assertCss(".verseblock p", output, 0);
            assertCss(".verseblock > pre", output, 1);
            assertCss(".verseblock > .attribution", output, 1);
            assertCss(".verseblock > .attribution > cite", output, 1);
            assertCss(".verseblock > .attribution > br + cite", output, 1);
            assertXpath("//*[@class=\"verseblock\"]/*[@class=\"attribution\"]/cite[text()=\"Famous Poem\"]", output, 1);
            var attribution = xmlnodesAtXpath("//*[@class=\"verseblock\"]/*[@class=\"attribution\"]", output, 1);
            var author = first(attribution.getChildren());
            assertEquals((str(decodeChar(8212)) + " Famous Poet"), Strings.strip(author.getText()));
        }

        @Test
        @DisplayName("single-line verse block with attribution converted to DocBook")
        void singleLineVerseBlockWithAttributionConvertedToDocBook() throws Exception {
            var input = """
                    [verse, Famous Poet, Famous Poem]
                    ____
                    A famous verse.
                    ____
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("blockquote", output, 1);
            assertCss("blockquote simpara", output, 0);
            assertCss("blockquote > literallayout", output, 1);
            assertCss("blockquote > attribution", output, 1);
            assertCss("blockquote > attribution > citetitle", output, 1);
            assertXpath("//blockquote/attribution/citetitle[text()=\"Famous Poem\"]", output, 1);
            var attribution = xmlnodesAtXpath("//blockquote/attribution", output, 1);
            var author = first(attribution.getChildren());
            assertEquals("Famous Poet", Strings.strip(author.getText()));
        }

        @Test
        @DisplayName("single-line epigraph verse block with attribution converted to DocBook")
        void singleLineEpigraphVerseBlockWithAttributionConvertedToDocBook() throws Exception {
            var input = """
                    [verse.epigraph, Famous Poet, Famous Poem]
                    ____
                    A famous verse.
                    ____
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("epigraph", output, 1);
            assertCss("epigraph simpara", output, 0);
            assertCss("epigraph > literallayout", output, 1);
            assertCss("epigraph > attribution", output, 1);
            assertCss("epigraph > attribution > citetitle", output, 1);
            assertXpath("//epigraph/attribution/citetitle[text()=\"Famous Poem\"]", output, 1);
            var attribution = xmlnodesAtXpath("//epigraph/attribution", output, 1);
            var author = first(attribution.getChildren());
            assertEquals("Famous Poet", Strings.strip(author.getText()));
        }

        @Test
        @DisplayName("multi-stanza verse block")
        void multiStanzaVerseBlock() throws Exception {
            var input = """
                    [verse]
                    ____
                    A famous verse.

                    Stanza two.
                    ____
                    """;
            var output = convertString(input);
            assertXpath("//*[@class=\"verseblock\"]", output, 1);
            assertXpath("//*[@class=\"verseblock\"]/pre", output, 1);
            assertXpath("//*[@class=\"verseblock\"]//p", output, 0);
            assertXpath("//*[@class=\"verseblock\"]/pre[contains(text(), \"A famous verse.\")]", output, 1);
            assertXpath("//*[@class=\"verseblock\"]/pre[contains(text(), \"Stanza two.\")]", output, 1);
        }

        @Test
        @DisplayName("verse block does not contain block elements")
        void verseBlockDoesNotContainBlockElements() throws Exception {
            var input = """
                    [verse]
                    ____
                    A famous verse.

                    ....
                    not a literal
                    ....
                    ____
                    """;
            var output = convertString(input);
            assertCss(".verseblock", output, 1);
            assertCss(".verseblock > pre", output, 1);
            assertCss(".verseblock p", output, 0);
            assertCss(".verseblock .literalblock", output, 0);
        }

        @Test
        @DisplayName("verse should have normal subs")
        void verseShouldHaveNormalSubs() throws Exception {
            var input = """
                    [verse]
                    ____
                    A famous verse
                    ____
                    """;
            var verse = blockFromString(input);
            assertEquals(Substitutors.NORMAL_SUBS, verse.getSubs());
        }

        @Test
        @DisplayName("should not recognize callouts in a verse")
        void shouldNotRecognizeCalloutsInAVerse() throws Exception {
            var input = """
                    [verse]
                    ____
                    La la la <1>
                    ____
                    <1> Not pointing to a callout
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//pre[text()=\"La la la <1>\"]", output, 1);
            assertMessage(logger, "WARN", "<stdin>: line 5: no callout found for <1>", LogMessage.class);
        }

        @Test
        @DisplayName("should perform normal subs on a verse block")
        void shouldPerformNormalSubsOnAVerseBlock() throws Exception {
            var input = """
                    [verse]
                    ____
                    _GET /groups/link:#group-id[\\{group-id\\}]_
                    ____
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "<pre class=\"content\"><em>GET /groups/<a href=\"#group-id\">{group-id}</a></em></pre>");
        }
    }

    @Nested
    @DisplayName("Example Blocks")
    class ExampleBlocks {

        @Test
        @DisplayName("can convert example block")
        void canConvertExampleBlock() throws Exception {
            var input = """
                    ====
                    This is an example of an example block.

                    How crazy is that?
                    ====
                    """;
            var output = convertString(input);
            assertXpath("//*[@class=\"exampleblock\"]//p", output, 2);
        }

        @Test
        @DisplayName("assigns sequential numbered caption to example block with title")
        void assignsSequentialNumberedCaptionToExampleBlockWithTitle() throws Exception {
            var input = """
                    .Writing Docs with AsciiDoc
                    ====
                    Here's how you write AsciiDoc.

                    You just write.
                    ====

                    .Writing Docs with DocBook
                    ====
                    Here's how you write DocBook.

                    You futz with XML.
                    ====
                    """;
            var doc = documentFromString(input);
            assertEquals("1", doc.getBlocks().get(0).getNumeral());
            assertEquals("1", doc.getBlocks().get(0).getNumeral());
            assertEquals("2", doc.getBlocks().get(1).getNumeral());
            assertEquals("2", doc.getBlocks().get(1).getNumeral());
            var output = doc.convert();
            assertXpath("(//*[@class=\"exampleblock\"])[1]/*[@class=\"title\"][text()=\"Example 1. Writing Docs with AsciiDoc\"]", output, 1);
            assertXpath("(//*[@class=\"exampleblock\"])[2]/*[@class=\"title\"][text()=\"Example 2. Writing Docs with DocBook\"]", output, 1);
            assertEquals(2, get(doc.getAttributes(), "example-number"));
        }

        @Test
        @DisplayName("assigns sequential character caption to example block with title")
        void assignsSequentialCharacterCaptionToExampleBlockWithTitle() throws Exception {
            var input = """
                    :example-number: @

                    .Writing Docs with AsciiDoc
                    ====
                    Here's how you write AsciiDoc.

                    You just write.
                    ====

                    .Writing Docs with DocBook
                    ====
                    Here's how you write DocBook.

                    You futz with XML.
                    ====
                    """;
            var doc = documentFromString(input);
            assertEquals("A", doc.getBlocks().get(0).getNumeral());
            assertEquals("A", doc.getBlocks().get(0).getNumeral());
            assertEquals("B", doc.getBlocks().get(1).getNumeral());
            assertEquals("B", doc.getBlocks().get(1).getNumeral());
            var output = doc.convert();
            assertXpath("(//*[@class=\"exampleblock\"])[1]/*[@class=\"title\"][text()=\"Example A. Writing Docs with AsciiDoc\"]", output, 1);
            assertXpath("(//*[@class=\"exampleblock\"])[2]/*[@class=\"title\"][text()=\"Example B. Writing Docs with DocBook\"]", output, 1);
            assertEquals("B", get(doc.getAttributes(), "example-number"));
        }

        @Test
        @DisplayName("should increment counter for example even when example-number is locked by the API")
        void shouldIncrementCounterForExampleEvenWhenExampleNumberIsLockedByTheAPI() throws Exception {
            var input = """
                    .Writing Docs with AsciiDoc
                    ====
                    Here's how you write AsciiDoc.

                    You just write.
                    ====

                    .Writing Docs with DocBook
                    ====
                    Here's how you write DocBook.

                    You futz with XML.
                    ====
                    """;
            var doc = documentFromString(input, Options.of("attributes", Options.of("example-number", "`")));
            var output = doc.convert();
            assertXpath("(//*[@class=\"exampleblock\"])[1]/*[@class=\"title\"][text()=\"Example a. Writing Docs with AsciiDoc\"]", output, 1);
            assertXpath("(//*[@class=\"exampleblock\"])[2]/*[@class=\"title\"][text()=\"Example b. Writing Docs with DocBook\"]", output, 1);
            assertEquals("b", get(doc.getAttributes(), "example-number"));
        }

        @Test
        @DisplayName("should use explicit caption if specified")
        void shouldUseExplicitCaptionIfSpecified() throws Exception {
            var input = """
                    [caption="Look! "]
                    .Writing Docs with AsciiDoc
                    ====
                    Here's how you write AsciiDoc.

                    You just write.
                    ====
                    """;
            var doc = documentFromString(input);
            assertNull(doc.getBlocks().get(0).getNumeral());
            var output = doc.convert();
            assertXpath("(//*[@class=\"exampleblock\"])[1]/*[@class=\"title\"][text()=\"Look! Writing Docs with AsciiDoc\"]", output, 1);
            assertFalsy(doc.getAttributes().containsKey("example-number"));
        }

        @Test
        @DisplayName("automatic caption can be turned off and on and modified")
        void automaticCaptionCanBeTurnedOffAndOnAndModified() throws Exception {
            var input = """
                    .first example
                    ====
                    an example
                    ====

                    :caption:

                    .second example
                    ====
                    another example
                    ====

                    :caption!:
                    :example-caption: Exhibit

                    .third example
                    ====
                    yet another example
                    ====
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"exampleblock\"]", output, 3);
            assertXpath("(/*[@class=\"exampleblock\"])[1]/*[@class=\"title\"][starts-with(text(), \"Example \")]", output, 1);
            assertXpath("(/*[@class=\"exampleblock\"])[2]/*[@class=\"title\"][text()=\"second example\"]", output, 1);
            assertXpath("(/*[@class=\"exampleblock\"])[3]/*[@class=\"title\"][starts-with(text(), \"Exhibit \")]", output, 1);
        }

        @Test
        @DisplayName("should use explicit caption if specified even if block-specific global caption is disabled")
        void shouldUseExplicitCaptionIfSpecifiedEvenIfBlockSpecificGlobalCaptionIsDisabled() throws Exception {
            var input = """
                    :!example-caption:

                    [caption="Look! "]
                    .Writing Docs with AsciiDoc
                    ====
                    Here's how you write AsciiDoc.

                    You just write.
                    ====
                    """;
            var doc = documentFromString(input);
            assertNull(doc.getBlocks().get(0).getNumeral());
            var output = doc.convert();
            assertXpath("(//*[@class=\"exampleblock\"])[1]/*[@class=\"title\"][text()=\"Look! Writing Docs with AsciiDoc\"]", output, 1);
            assertFalsy(doc.getAttributes().containsKey("example-number"));
        }

        @Test
        @DisplayName("should use global caption if specified even if block-specific global caption is disabled")
        void shouldUseGlobalCaptionIfSpecifiedEvenIfBlockSpecificGlobalCaptionIsDisabled() throws Exception {
            var input = """
                    :!example-caption:
                    :caption: Look!{sp}

                    .Writing Docs with AsciiDoc
                    ====
                    Here's how you write AsciiDoc.

                    You just write.
                    ====
                    """;
            var doc = documentFromString(input);
            assertNull(doc.getBlocks().get(0).getNumeral());
            var output = doc.convert();
            assertXpath("(//*[@class=\"exampleblock\"])[1]/*[@class=\"title\"][text()=\"Look! Writing Docs with AsciiDoc\"]", output, 1);
            assertFalsy(doc.getAttributes().containsKey("example-number"));
        }

        @Test
        @DisplayName("should not process caption attribute on block that does not support a caption")
        void shouldNotProcessCaptionAttributeOnBlockThatDoesNotSupportACaption() throws Exception {
            var input = """
                    [caption="Look! "]
                    .No caption here
                    --
                    content
                    --
                    """;
            var doc = documentFromString(input);
            assertNull(doc.getBlocks().get(0).getCaption());
            assertEquals("Look! ", doc.getBlocks().get(0).getAttribute("caption"));
            var output = doc.convert();
            assertXpath("(//*[@class=\"openblock\"])[1]/*[@class=\"title\"][text()=\"No caption here\"]", output, 1);
        }

        @Test
        @DisplayName("should create details/summary set if collapsible option is set")
        void shouldCreateDetailsSummarySetIfCollapsibleOptionIsSet() throws Exception {
            var input = """
                    .Toggle Me
                    [%collapsible]
                    ====
                    This content is revealed when the user clicks the words "Toggle Me".
                    ====
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("details", output, 1);
            assertCss("details[open]", output, 0);
            assertCss("details > summary.title", output, 1);
            assertXpath("//details/summary[text()=\"Toggle Me\"]", output, 1);
            assertCss("details > summary.title + .content", output, 1);
            assertCss("details > summary.title + .content p", output, 1);
        }

        @Test
        @DisplayName("should open details/summary set if collapsible and open options are set")
        void shouldOpenDetailsSummarySetIfCollapsibleAndOpenOptionsAreSet() throws Exception {
            var input = """
                    .Toggle Me
                    [%collapsible%open]
                    ====
                    This content is revealed when the user clicks the words "Toggle Me".
                    ====
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("details", output, 1);
            assertCss("details[open]", output, 1);
            assertCss("details > summary.title", output, 1);
            assertXpath("//details/summary[text()=\"Toggle Me\"]", output, 1);
        }

        @Test
        @DisplayName("should add default summary element if collapsible option is set and title is not specifed")
        void shouldAddDefaultSummaryElementIfCollapsibleOptionIsSetAndTitleIsNotSpecifed() throws Exception {
            var input = """
                    [%collapsible]
                    ====
                    This content is revealed when the user clicks the words "Details".
                    ====
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("details", output, 1);
            assertCss("details > summary.title", output, 1);
            assertXpath("//details/summary[text()=\"Details\"]", output, 1);
        }

        @Test
        @DisplayName("should not allow collapsible block to increment example number")
        void shouldNotAllowCollapsibleBlockToIncrementExampleNumber() throws Exception {
            var input = """
                    .Before
                    ====
                    before
                    ====

                    .Show Me The Goods
                    [%collapsible]
                    ====
                    This content is revealed when the user clicks the words "Show Me The Goods".
                    ====

                    .After
                    ====
                    after
                    ====
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//*[@class=\"title\"][text()=\"Example 1. Before\"]", output, 1);
            assertXpath("//*[@class=\"title\"][text()=\"Example 2. After\"]", output, 1);
            assertCss("details", output, 1);
            assertCss("details > summary.title", output, 1);
            assertXpath("//details/summary[text()=\"Show Me The Goods\"]", output, 1);
        }

        @Test
        @DisplayName("should warn if example block is not terminated")
        void shouldWarnIfExampleBlockIsNotTerminated() throws Exception {
            var input = """
                    outside

                    ====
                    inside

                    still inside

                    eof
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"exampleblock\"]", output, 1);
            assertMessage(logger, "WARN", "<stdin>: line 3: unterminated example block", LogMessage.class);
        }
    }

    @Nested
    @DisplayName("Admonition Blocks")
    class AdmonitionBlocks {

        @Test
        @DisplayName("caption block-level attribute should be used as caption")
        void captionBlockLevelAttributeShouldBeUsedAsCaption() throws Exception {
            var input = """
                    :tip-caption: Pro Tip

                    [caption="Pro Tip"]
                    TIP: Override the caption of an admonition block using an attribute entry
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/*[@class=\"title\"][text()=\"Pro Tip\"]", output, 1);
        }

        @Test
        @DisplayName("can override caption of admonition block using document attribute")
        void canOverrideCaptionOfAdmonitionBlockUsingDocumentAttribute() throws Exception {
            var input = """
                    :tip-caption: Pro Tip

                    TIP: Override the caption of an admonition block using an attribute entry
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/*[@class=\"title\"][text()=\"Pro Tip\"]", output, 1);
        }

        @Test
        @DisplayName("blank caption document attribute should not blank admonition block caption")
        void blankCaptionDocumentAttributeShouldNotBlankAdmonitionBlockCaption() throws Exception {
            var input = """
                    :caption:

                    TIP: Override the caption of an admonition block using an attribute entry
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/*[@class=\"title\"][text()=\"Tip\"]", output, 1);
        }

        @Test
        @DisplayName("should generate appropriate tag based on admonition type in DocBook output")
        void shouldGenerateAppropriateTagBasedOnAdmonitionTypeInDocBookOutput() throws Exception {
            var input = """
                    NOTE: Remember the oat milk.

                    IMPORTANT: Don't forget the children!

                    [caption=Pro Tip]
                    TIP: Look for the warp under the bridge.

                    CAUTION: Slippery when wet.

                    WARNING: The software you're about to use has *not* been tested.
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            for (var type : list("note", "important", "tip", "caution", "warning")) {
                assertCss(type, output, 1);
                assertCss((str(type) + " > simpara"), output, 1);
            }
            assertXpath("/tip/simpara[text()=\"Look for the warp under the bridge.\"]", output, 1);
            refuteIncludes(output, "Pro Tip");
        }
    }

    @Nested
    @DisplayName("Preformatted Blocks")
    class PreformattedBlocks {

        @Test
        @DisplayName("should separate adjacent paragraphs and listing into blocks")
        void shouldSeparateAdjacentParagraphsAndListingIntoBlocks() throws Exception {
            var input = """
                    paragraph 1
                    ----
                    listing content
                    ----
                    paragraph 2
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"paragraph\"]/p", output, 2);
            assertXpath("/*[@class=\"listingblock\"]", output, 1);
            assertXpath("(/*[@class=\"paragraph\"]/following-sibling::*)[1][@class=\"listingblock\"]", output, 1);
        }

        @Test
        @DisplayName("should warn if listing block is not terminated")
        void shouldWarnIfListingBlockIsNotTerminated() throws Exception {
            var input = """
                    outside

                    ----
                    inside

                    still inside

                    eof
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"listingblock\"]", output, 1);
            assertMessage(logger, "WARN", "<stdin>: line 3: unterminated listing block", LogMessage.class);
        }

        @Test
        @DisplayName("should not crash when converting verbatim block that has no lines")
        void shouldNotCrashWhenConvertingVerbatimBlockThatHasNoLines() throws Exception {
            for (var input : list("----\n----", "....\n....")) {
                var output = convertStringToEmbedded(input);
                assertCss("pre", output, 1);
                assertCss("pre:empty", output, 1);
            }
        }

        @Test
        @DisplayName("should return content as empty string for verbatim or raw block that has no lines")
        void shouldReturnContentAsEmptyStringForVerbatimOrRawBlockThatHasNoLines() throws Exception {
            for (var input : list("----\n----", "....\n....")) {
                var doc = documentFromString(input);
                assertEquals("", doc.getBlocks().get(0).getContent());
            }
        }

        @Test
        @DisplayName("should preserve newlines in literal block")
        void shouldPreserveNewlinesInLiteralBlock() throws Exception {
            var input = """
                    ....
                    line one

                    line two

                    line three
                    ....
                    """;
            for (var standalone : list(true, false)) {
                var output = convertString(input, Options.of("standalone", standalone));
                assertXpath("//pre", output, 1);
                assertXpath("//pre/text()", output, 1);
                var text = xmlnodesAtXpath("//pre/text()", output, 1).getText();
                var lines = lines(text);
                assertEquals(5, size(lines));
                var expected = lines("line one\n\nline two\n\nline three");
                assertEquals(expected, lines);
                var blankLines = size(scan(output, rx("\\n[ \\t]*\\n")));
                assertTruthy((blankLines >= 2));
            }
        }

        @Test
        @DisplayName("should preserve newlines in listing block")
        void shouldPreserveNewlinesInListingBlock() throws Exception {
            var input = """
                    ----
                    line one

                    line two

                    line three
                    ----
                    """;
            for (var standalone : list(true, false)) {
                var output = convertString(input, Options.of("standalone", standalone));
                assertXpath("//pre", output, 1);
                assertXpath("//pre/text()", output, 1);
                var text = xmlnodesAtXpath("//pre/text()", output, 1).getText();
                var lines = lines(text);
                assertEquals(5, size(lines));
                var expected = lines("line one\n\nline two\n\nline three");
                assertEquals(expected, lines);
                var blankLines = size(scan(output, rx("\\n[ \\t]*\\n")));
                assertTruthy((blankLines >= 2));
            }
        }

        @Test
        @DisplayName("should preserve newlines in verse block")
        void shouldPreserveNewlinesInVerseBlock() throws Exception {
            var input = """
                    --
                    [verse]
                    ____
                    line one

                    line two

                    line three
                    ____
                    --
                    """;
            for (var standalone : list(true, false)) {
                var output = convertString(input, Options.of("standalone", standalone));
                assertXpath("//*[@class=\"verseblock\"]/pre", output, 1);
                assertXpath("//*[@class=\"verseblock\"]/pre/text()", output, 1);
                var text = xmlnodesAtXpath("//*[@class=\"verseblock\"]/pre/text()", output, 1).getText();
                var lines = lines(text);
                assertEquals(5, size(lines));
                var expected = lines("line one\n\nline two\n\nline three");
                assertEquals(expected, lines);
                var blankLines = size(scan(output, rx("\\n[ \\t]*\\n")));
                assertTruthy((blankLines >= 2));
            }
        }

        @Test
        @DisplayName("should strip leading and trailing blank lines when converting verbatim block")
        void shouldStripLeadingAndTrailingBlankLinesWhenConvertingVerbatimBlock() throws Exception {
            var input = """
                    [subs=attributes+]
                    ....


                      first line

                    last line

                    {empty}

                    ....
                    """;
            var doc = documentFromString(input, Options.of("standalone", false));
            var block = first(doc.getBlocks());
            assertEquals(list("", "", "  first line", "", "last line", "", "{empty}", ""), lines(block));
            var result = doc.convert();
            assertXpath("//pre[text()=\"  first line\n\nlast line\"]", result, 1);
        }

        @Test
        @DisplayName("should process block with CRLF line endings")
        void shouldProcessBlockWithCRLFLineEndings() throws Exception {
            var input = """
                    ----\r
                    source line 1\r
                    source line 2\r
                    ----\r
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"listingblock\"]//pre", output, 1);
            assertXpath("/*[@class=\"listingblock\"]//pre[text()=\"source line 1\nsource line 2\"]", output, 1);
        }

        @Test
        @DisplayName("should remove block indent if indent attribute is 0")
        void shouldRemoveBlockIndentIfIndentAttributeIs0() throws Exception {
            var input = """
                    [indent="0"]
                    ----
                        def names

                          @names.split

                        end
                    ----
                    """;
            var expected = Strings.chop("""
                    def names

                      @names.split

                    end
                    """);
            var output = convertStringToEmbedded(input);
            assertCss("pre", output, 1);
            assertCss(".listingblock pre", output, 1);
            var result = xmlnodesAtXpath("//pre", output, 1).getText();
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("should not remove block indent if indent attribute is -1")
        void shouldNotRemoveBlockIndentIfIndentAttributeIs1() throws Exception {
            var input = """
                    [indent="-1"]
                    ----
                        def names

                          @names.split

                        end
                    ----
                    """;
            var expected = Strings.chop(join(slice(lines(input), 2, 5)));
            var output = convertStringToEmbedded(input);
            assertCss("pre", output, 1);
            assertCss(".listingblock pre", output, 1);
            var result = xmlnodesAtXpath("//pre", output, 1).getText();
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("should set block indent to value specified by indent attribute")
        void shouldSetBlockIndentToValueSpecifiedByIndentAttribute() throws Exception {
            var input = """
                    [indent="1"]
                    ----
                        def names

                          @names.split

                        end
                    ----
                    """;
            var expected = Strings.chop(join(map(slice(lines(input), 2, 5), l -> sub(l, "    ", " "))));
            var output = convertStringToEmbedded(input);
            assertCss("pre", output, 1);
            assertCss(".listingblock pre", output, 1);
            var result = xmlnodesAtXpath("//pre", output, 1).getText();
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("should set block indent to value specified by indent document attribute")
        void shouldSetBlockIndentToValueSpecifiedByIndentDocumentAttribute() throws Exception {
            var input = """
                    :source-indent: 1

                    [source,ruby]
                    ----
                        def names

                          @names.split

                        end
                    ----
                    """;
            var expected = Strings.chop(join(map(slice(lines(input), 4, 5), l -> sub(l, "    ", " "))));
            var output = convertStringToEmbedded(input);
            assertCss("pre", output, 1);
            assertCss(".listingblock pre", output, 1);
            var result = xmlnodesAtXpath("//pre", output, 1).getText();
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("should expand tabs if tabsize attribute is positive")
        void shouldExpandTabsIfTabsizeAttributeIsPositive() throws Exception {
            var input = """
                    :tabsize: 4

                    [indent=0]
                    ----
                    	def names

                    		@names.split

                    	end
                    ----
                    """;
            var expected = Strings.chop("""
                    def names

                        @names.split

                    end
                    """);
            var output = convertStringToEmbedded(input);
            assertCss("pre", output, 1);
            assertCss(".listingblock pre", output, 1);
            var result = xmlnodesAtXpath("//pre", output, 1).getText();
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("literal block should honor nowrap option")
        void literalBlockShouldHonorNowrapOption() throws Exception {
            var input = """
                    [options="nowrap"]
                    ----
                    Do not wrap me if I get too long.
                    ----
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("pre.nowrap", output, 1);
        }

        @Test
        @DisplayName("literal block should set nowrap class if prewrap document attribute is disabled")
        void literalBlockShouldSetNowrapClassIfPrewrapDocumentAttributeIsDisabled() throws Exception {
            var input = """
                    :prewrap!:

                    ----
                    Do not wrap me if I get too long.
                    ----
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("pre.nowrap", output, 1);
        }

        @Test
        @DisplayName("should preserve guard in front of callout if icons are not enabled")
        void shouldPreserveGuardInFrontOfCalloutIfIconsAreNotEnabled() throws Exception {
            var input = """
                    ----
                    puts 'Hello, World!' # <1>
                    puts 'Goodbye, World ;(' # <2>
                    ----
                    """;
            var result = convertStringToEmbedded(input);
            assertInclude(" # <b class=\"conum\">(1)</b>", result);
            assertInclude(" # <b class=\"conum\">(2)</b>", result);
        }

        @Test
        @DisplayName("should preserve guard around callout if icons are not enabled")
        void shouldPreserveGuardAroundCalloutIfIconsAreNotEnabled() throws Exception {
            var input = """
                    ----
                    <parent> <!--1-->
                      <child/> <!--2-->
                    </parent>
                    ----
                    """;
            var result = convertStringToEmbedded(input);
            assertInclude(" &lt;!--<b class=\"conum\">(1)</b>--&gt;", result);
            assertInclude(" &lt;!--<b class=\"conum\">(2)</b>--&gt;", result);
        }

        @Test
        @DisplayName("literal block should honor explicit subs list")
        void literalBlockShouldHonorExplicitSubsList() throws Exception {
            var input = """
                    [subs="verbatim,quotes"]
                    ----
                    Map<String, String> *attributes*; //<1>
                    ----
                    """;
            var block = blockFromString(input);
            assertEquals(list(Substitution.SPECIAL_CHARACTERS, Substitution.CALLOUTS, Substitution.QUOTES), block.getSubs());
            var output = block.convert();
            assertIncludes(output, "Map&lt;String, String&gt; <strong>attributes</strong>;");
            assertXpath("//pre/b[text()=\"(1)\"]", output, 1);
        }

        @Test
        @DisplayName("should be able to disable callouts for literal block")
        void shouldBeAbleToDisableCalloutsForLiteralBlock() throws Exception {
            var input = """
                    [subs="specialcharacters"]
                    ----
                    No callout here <1>
                    ----
                    """;
            var block = blockFromString(input);
            assertEquals(list(Substitution.SPECIAL_CHARACTERS), block.getSubs());
            var output = block.convert();
            assertXpath("//pre/b[text()=\"(1)\"]", output, 0);
        }

        @Test
        @DisplayName("listing block should honor explicit subs list")
        void listingBlockShouldHonorExplicitSubsList() throws Exception {
            var input = """
                    [subs="specialcharacters,quotes"]
                    ----
                    $ *python functional_tests.py*
                    Traceback (most recent call last):
                      File "functional_tests.py", line 4, in <module>
                        assert 'Django' in browser.title
                    AssertionError
                    ----
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".listingblock pre", output, 1);
            assertCss(".listingblock pre strong", output, 1);
            assertCss(".listingblock pre em", output, 0);
            var input2 = """
                    [subs="specialcharacters,macros"]
                    ----
                    $ pass:quotes[*python functional_tests.py*]
                    Traceback (most recent call last):
                      File "functional_tests.py", line 4, in <module>
                        assert pass:quotes['Django'] in browser.title
                    AssertionError
                    ----
                    """;
            var output2 = convertStringToEmbedded(input2);
            assertEquals(output, output2);
        }

        @Test
        @DisplayName("should not mangle array that contains formatted text with role in listing block with quotes sub enabled")
        void shouldNotMangleArrayThatContainsFormattedTextWithRoleInListingBlockWithQuotesSubEnabled() throws Exception {
            var input = """
                    [,ruby,subs=+quotes]
                    ----
                    nums = [1, 2, 3, [.added]#4#]
                    ----
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "nums = [1, 2, 3, <span class=\"added\">4</span>]");
        }

        @Test
        @DisplayName("first character of block title may be a period if not followed by space")
        void firstCharacterOfBlockTitleMayBeAPeriodIfNotFollowedBySpace() throws Exception {
            var input = """
                    ..gitignore
                    ----
                    /.bundle/
                    /build/
                    /Gemfile.lock
                    ----
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//*[@class=\"title\"][text()=\".gitignore\"]", output);
        }

        @Test
        @DisplayName("listing block without title should generate screen element in docbook")
        void listingBlockWithoutTitleShouldGenerateScreenElementInDocbook() throws Exception {
            var input = """
                    ----
                    listing block
                    ----
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertXpath("/screen[text()=\"listing block\"]", output, 1);
        }

        @Test
        @DisplayName("listing block with title should generate screen element inside formalpara element in docbook")
        void listingBlockWithTitleShouldGenerateScreenElementInsideFormalparaElementInDocbook() throws Exception {
            var input = """
                    .title
                    ----
                    listing block
                    ----
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertXpath("/formalpara", output, 1);
            assertXpath("/formalpara/title[text()=\"title\"]", output, 1);
            assertXpath("/formalpara/para/screen[text()=\"listing block\"]", output, 1);
        }

        @Test
        @DisplayName("should not prepend caption to title of listing block with title if listing-caption attribute is not set")
        void shouldNotPrependCaptionToTitleOfListingBlockWithTitleIfListingCaptionAttributeIsNotSet() throws Exception {
            var input = """
                    .title
                    ----
                    listing block content
                    ----
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"listingblock\"][1]/*[@class=\"title\"][text()=\"title\"]", output, 1);
        }

        @Test
        @DisplayName("should prepend caption specified by listing-caption attribute and number to title of listing block with title")
        void shouldPrependCaptionSpecifiedByListingCaptionAttributeAndNumberToTitleOfListingBlockWithTitle() throws Exception {
            var input = """
                    :listing-caption: Listing

                    .title
                    ----
                    listing block content
                    ----
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"listingblock\"][1]/*[@class=\"title\"][text()=\"Listing 1. title\"]", output, 1);
        }

        @Test
        @DisplayName("should prepend caption specified by caption attribute on listing block even if listing-caption attribute is not set")
        void shouldPrependCaptionSpecifiedByCaptionAttributeOnListingBlockEvenIfListingCaptionAttributeIsNotSet() throws Exception {
            var input = """
                    [caption="Listing {counter:listing-number}. "]
                    .Behold!
                    ----
                    listing block content
                    ----
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"listingblock\"][1]/*[@class=\"title\"][text()=\"Listing 1. Behold!\"]", output, 1);
        }

        @Test
        @DisplayName("listing block without an explicit style and with a second positional argument should be promoted to a source block")
        void listingBlockWithoutAnExplicitStyleAndWithASecondPositionalArgumentShouldBePromotedToASourceBlock() throws Exception {
            var input = """
                    [,ruby]
                    ----
                    puts 'Hello, Ruby!'
                    ----
                    """;
            var matches = documentFromString(input).findBy(AbstractBlock.Selector.any().withContext("listing").withStyle("source"));
            assertEquals(1, size(matches));
            assertEquals("ruby", matches.get(0).getAttribute("language"));
        }

        @Test
        @DisplayName("listing block without an explicit style should be promoted to a source block if source-language is set")
        void listingBlockWithoutAnExplicitStyleShouldBePromotedToASourceBlockIfSourceLanguageIsSet() throws Exception {
            var input = """
                    :source-language: ruby

                    ----
                    puts 'Hello, Ruby!'
                    ----
                    """;
            var matches = documentFromString(input).findBy(AbstractBlock.Selector.any().withContext("listing").withStyle("source"));
            assertEquals(1, size(matches));
            assertEquals("ruby", matches.get(0).getAttribute("language"));
        }

        @Test
        @DisplayName("listing block with an explicit style and a second positional argument should not be promoted to a source block")
        void listingBlockWithAnExplicitStyleAndASecondPositionalArgumentShouldNotBePromotedToASourceBlock() throws Exception {
            var input = """
                    [listing,ruby]
                    ----
                    puts 'Hello, Ruby!'
                    ----
                    """;
            var matches = documentFromString(input).findBy(AbstractBlock.Selector.any().withContext("listing"));
            assertEquals(1, size(matches));
            assertEquals("listing", matches.get(0).getStyle());
            assertNull(matches.get(0).getAttribute("language"));
        }

        @Test
        @DisplayName("listing block with an explicit style should not be promoted to a source block if source-language is set")
        void listingBlockWithAnExplicitStyleShouldNotBePromotedToASourceBlockIfSourceLanguageIsSet() throws Exception {
            var input = """
                    :source-language: ruby

                    [listing]
                    ----
                    puts 'Hello, Ruby!'
                    ----
                    """;
            var matches = documentFromString(input).findBy(AbstractBlock.Selector.any().withContext("listing"));
            assertEquals(1, size(matches));
            assertEquals("listing", matches.get(0).getStyle());
            assertNull(matches.get(0).getAttribute("language"));
        }

        @Test
        @DisplayName("source block with no title or language should generate screen element in docbook")
        void sourceBlockWithNoTitleOrLanguageShouldGenerateScreenElementInDocbook() throws Exception {
            var input = """
                    [source]
                    ----
                    source block
                    ----
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertXpath("/screen[@linenumbering=\"unnumbered\"][text()=\"source block\"]", output, 1);
        }

        @Test
        @DisplayName("source block with title and no language should generate screen element inside formalpara element for docbook")
        void sourceBlockWithTitleAndNoLanguageShouldGenerateScreenElementInsideFormalparaElementForDocbook() throws Exception {
            var input = """
                    [source]
                    .title
                    ----
                    source block
                    ----
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertXpath("/formalpara", output, 1);
            assertXpath("/formalpara/title[text()=\"title\"]", output, 1);
            assertXpath("/formalpara/para/screen[@linenumbering=\"unnumbered\"][text()=\"source block\"]", output, 1);
        }
    }

    @Nested
    @DisplayName("Open Blocks")
    class OpenBlocks {

        @Test
        @DisplayName("can convert open block")
        void canConvertOpenBlock() throws Exception {
            var input = """
                    --
                    This is an open block.

                    It can span multiple lines.
                    --
                    """;
            var output = convertString(input);
            assertXpath("//*[@class=\"openblock\"]//p", output, 2);
        }

        @Test
        @DisplayName("open block can contain another block")
        void openBlockCanContainAnotherBlock() throws Exception {
            var input = """
                    --
                    This is an open block.

                    It can span multiple lines.

                    ____
                    It can hold great quotes like this one.
                    ____
                    --
                    """;
            var output = convertString(input);
            assertXpath("//*[@class=\"openblock\"]//p", output, 3);
            assertXpath("//*[@class=\"openblock\"]//*[@class=\"quoteblock\"]", output, 1);
        }

        @Test
        @DisplayName("can nest open blocks using ~ variant")
        void canNestOpenBlocksUsingVariant() throws Exception {
            var input = """
                    [.tabs]
                    ~~~~
                    [.tab]
                    ~~~~~~
                    tab one
                    ~~~~~~

                    [.tab]
                    ~~~~~~
                    tab two
                    ~~~~~~
                    ~~~~
                    """;
            var output = convertString(input);
            assertCss(".openblock", output, 3);
            assertCss(".openblock .openblock", output, 2);
        }

        @Test
        @DisplayName("open block ~ variant cannot masquerade as another block context")
        void openBlockVariantCannotMasqueradeAsAnotherBlockContext() throws Exception {
            var input = """
                    [sidebar]
                    ~~~~
                    This is just an open block.
                    ~~~~
                    """;
            var output = convertString(input);
            assertCss(".openblock", output, 1);
            assertCss(".sidebarblock", output, 0);
        }

        @Test
        @DisplayName("should transfer id and reftext on open block to DocBook output")
        void shouldTransferIdAndReftextOnOpenBlockToDocBookOutput() throws Exception {
            var input = """
                    Check out that <<open>>!

                    [[open,Open Block]]
                    --
                    This is an open block.

                    TIP: An open block can have other blocks inside of it.
                    --

                    Back to our regularly scheduled programming.
                    """;
            var output = convertString(input, Options.of("backend", "docbook", "keep_namespaces", true));
            assertCss("article:root > para[xml|id=\"open\"]", output, 1);
            assertCss("article:root > para[xreflabel=\"Open Block\"]", output, 1);
            assertCss("article:root > simpara", output, 2);
            assertCss("article:root > para", output, 1);
            assertCss("article:root > para > simpara", output, 1);
            assertCss("article:root > para > tip", output, 1);
        }

        @Test
        @DisplayName("should transfer id and reftext on open paragraph to DocBook output")
        void shouldTransferIdAndReftextOnOpenParagraphToDocBookOutput() throws Exception {
            var input = """
                    [open#openpara,reftext="Open Paragraph"]
                    This is an open paragraph.
                    """;
            var output = convertString(input, Options.of("backend", "docbook", "keep_namespaces", true));
            assertCss("article:root > simpara", output, 1);
            assertCss("article:root > simpara[xml|id=\"openpara\"]", output, 1);
            assertCss("article:root > simpara[xreflabel=\"Open Paragraph\"]", output, 1);
        }

        @Test
        @DisplayName("should transfer title on open block to DocBook output")
        void shouldTransferTitleOnOpenBlockToDocBookOutput() throws Exception {
            var input = """
                    .Behold the open
                    --
                    This is an open block with a title.
                    --
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("article > formalpara", output, 1);
            assertCss("article > formalpara > *", output, 2);
            assertCss("article > formalpara > title", output, 1);
            assertXpath("/article/formalpara/title[text()=\"Behold the open\"]", output, 1);
            assertCss("article > formalpara > para", output, 1);
            assertCss("article > formalpara > para > simpara", output, 1);
        }

        @Test
        @DisplayName("should transfer title on open paragraph to DocBook output")
        void shouldTransferTitleOnOpenParagraphToDocBookOutput() throws Exception {
            var input = """
                    .Behold the open
                    This is an open paragraph with a title.
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("article > formalpara", output, 1);
            assertCss("article > formalpara > *", output, 2);
            assertCss("article > formalpara > title", output, 1);
            assertXpath("/article/formalpara/title[text()=\"Behold the open\"]", output, 1);
            assertCss("article > formalpara > para", output, 1);
            assertCss("article > formalpara > para[text()=\"This is an open paragraph with a title.\"]", output, 1);
        }

        @Test
        @DisplayName("should transfer role on open block to DocBook output")
        void shouldTransferRoleOnOpenBlockToDocBookOutput() throws Exception {
            var input = """
                    [.container]
                    --
                    This is an open block.
                    It holds stuff.
                    --
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("article > para[role=container]", output, 1);
            assertCss("article > para[role=container] > simpara", output, 1);
        }

        @Test
        @DisplayName("should transfer role on open paragraph to DocBook output")
        void shouldTransferRoleOnOpenParagraphToDocBookOutput() throws Exception {
            var input = """
                    [.container]
                    This is an open block.
                    It holds stuff.
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("article > simpara[role=container]", output, 1);
        }
    }

    @Nested
    @DisplayName("Passthrough Blocks")
    class PassthroughBlocks {

        @Test
        @DisplayName("can parse a passthrough block")
        void canParseAPassthroughBlock() throws Exception {
            var input = """
                    ++++
                    This is a passthrough block.
                    ++++
                    """;
            var block = blockFromString(input);
            assertNotNull(block);
            assertEquals(1, size(lines(block)));
            assertEquals("This is a passthrough block.", ((Block) block).getSource());
        }

        @Test
        @DisplayName("does not perform subs on a passthrough block by default")
        void doesNotPerformSubsOnAPassthroughBlockByDefault() throws Exception {
            var input = """
                    :type: passthrough

                    ++++
                    This is a '{type}' block.
                    http://asciidoc.org
                    image:tiger.png[]
                    ++++
                    """;
            var expected = "This is a '{type}' block.\nhttp://asciidoc.org\nimage:tiger.png[]";
            var output = convertStringToEmbedded(input);
            assertEquals(expected, Strings.strip(output));
        }

        @Test
        @DisplayName("does not perform subs on a passthrough block with pass style by default")
        void doesNotPerformSubsOnAPassthroughBlockWithPassStyleByDefault() throws Exception {
            var input = """
                    :type: passthrough

                    [pass]
                    ++++
                    This is a '{type}' block.
                    http://asciidoc.org
                    image:tiger.png[]
                    ++++
                    """;
            var expected = "This is a '{type}' block.\nhttp://asciidoc.org\nimage:tiger.png[]";
            var output = convertStringToEmbedded(input);
            assertEquals(expected, Strings.strip(output));
        }

        @Test
        @DisplayName("passthrough block honors explicit subs list")
        void passthroughBlockHonorsExplicitSubsList() throws Exception {
            var input = """
                    :type: passthrough

                    [subs="attributes,quotes,macros"]
                    ++++
                    This is a _{type}_ block.
                    http://asciidoc.org
                    ++++
                    """;
            var expected = "This is a <em>passthrough</em> block.\n<a href=\"http://asciidoc.org\" class=\"bare\">http://asciidoc.org</a>";
            var output = convertStringToEmbedded(input);
            assertEquals(expected, Strings.strip(output));
        }

        @Test
        @DisplayName("should strip leading and trailing blank lines when converting raw block")
        void shouldStripLeadingAndTrailingBlankLinesWhenConvertingRawBlock() throws Exception {
            var input = """
                    ++++
                    line above
                    ++++

                    ++++


                      first line

                    last line


                    ++++

                    ++++
                    line below
                    ++++
                    """;
            var doc = documentFromString(input, Options.of("standalone", false));
            var block = doc.getBlocks().get(1);
            assertEquals(list("", "", "  first line", "", "last line", "", ""), lines(block));
            var result = doc.convert();
            assertEquals("line above\n  first line\n\nlast line\nline below", result, str(1));
        }
    }

    @Nested
    @DisplayName("Math blocks")
    class MathBlocks {

        @Test
        @DisplayName("should not crash when converting stem block that has no lines")
        void shouldNotCrashWhenConvertingStemBlockThatHasNoLines() throws Exception {
            var input = """
                    [stem]
                    ++++
                    ++++
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".stemblock", output, 1);
        }

        @Test
        @DisplayName("should return content as empty string for stem or pass block that has no lines")
        void shouldReturnContentAsEmptyStringForStemOrPassBlockThatHasNoLines() throws Exception {
            for (var input : list("++++\n++++", "[stem]\n++++\n++++")) {
                var doc = documentFromString(input);
                assertEquals("", doc.getBlocks().get(0).getContent());
            }
        }

        @Test
        @DisplayName("should add LaTeX math delimiters around latexmath block content")
        void shouldAddLaTeXMathDelimitersAroundLatexmathBlockContent() throws Exception {
            var input = """
                    [latexmath]
                    ++++
                    \\sqrt{3x-1}+(1+x)^2 < y
                    ++++
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".stemblock", output, 1);
            var nodes = xmlnodesAtXpath("//*[@class=\"content\"]/child::text()", output);
            assertEquals("\\[\\sqrt{3x-1}+(1+x)^2 &lt; y\\]", Strings.strip(str(first(nodes))));
        }

        @Test
        @DisplayName("should not add LaTeX math delimiters around latexmath block content if already present")
        void shouldNotAddLaTeXMathDelimitersAroundLatexmathBlockContentIfAlreadyPresent() throws Exception {
            var input = """
                    [latexmath]
                    ++++
                    \\[\\sqrt{3x-1}+(1+x)^2 < y\\]
                    ++++
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".stemblock", output, 1);
            var nodes = xmlnodesAtXpath("//*[@class=\"content\"]/child::text()", output);
            assertEquals("\\[\\sqrt{3x-1}+(1+x)^2 &lt; y\\]", Strings.strip(str(first(nodes))));
        }

        @Test
        @DisplayName("should display latexmath block in alt of equation in DocBook backend")
        void shouldDisplayLatexmathBlockInAltOfEquationInDocBookBackend() throws Exception {
            var input = """
                    [latexmath]
                    ++++
                    \\sqrt{3x-1}+(1+x)^2 < y
                    ++++
                    """;
            var expect = """
                    <informalequation>
                    <alt><![CDATA[\\sqrt{3x-1}+(1+x)^2 < y]]></alt>
                    <mathphrase><![CDATA[\\sqrt{3x-1}+(1+x)^2 < y]]></mathphrase>
                    </informalequation>
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertEquals(Strings.strip(expect), Strings.strip(output));
        }

        @Test
        @DisplayName("should set autoNumber option for latexmath to none by default")
        void shouldSetAutoNumberOptionForLatexmathToNoneByDefault() throws Exception {
            var input = """
                    :stem: latexmath

                    [stem]
                    ++++
                    y = x^2
                    ++++
                    """;
            var output = convertString(input);
            assertIncludes(output, "TeX: { equationNumbers: { autoNumber: \"none\" } }");
        }

        @Test
        @DisplayName("should set autoNumber option for latexmath to none if eqnums is set to none")
        void shouldSetAutoNumberOptionForLatexmathToNoneIfEqnumsIsSetToNone() throws Exception {
            var input = """
                    :stem: latexmath
                    :eqnums: none

                    [stem]
                    ++++
                    y = x^2
                    ++++
                    """;
            var output = convertString(input);
            assertIncludes(output, "TeX: { equationNumbers: { autoNumber: \"none\" } }");
        }

        @Test
        @DisplayName("should set autoNumber option for latexmath to AMS if eqnums is set")
        void shouldSetAutoNumberOptionForLatexmathToAMSIfEqnumsIsSet() throws Exception {
            var input = """
                    :stem: latexmath
                    :eqnums:

                    [stem]
                    ++++
                    \\begin{equation}
                    y = x^2
                    \\end{equation}
                    ++++
                    """;
            var output = convertString(input);
            assertIncludes(output, "TeX: { equationNumbers: { autoNumber: \"AMS\" } }");
        }

        @Test
        @DisplayName("should set autoNumber option for latexmath to all if eqnums is set to all")
        void shouldSetAutoNumberOptionForLatexmathToAllIfEqnumsIsSetToAll() throws Exception {
            var input = """
                    :stem: latexmath
                    :eqnums: all

                    [stem]
                    ++++
                    y = x^2
                    ++++
                    """;
            var output = convertString(input);
            assertIncludes(output, "TeX: { equationNumbers: { autoNumber: \"all\" } }");
        }

        @Test
        @DisplayName("should not split equation in AsciiMath block at single newline")
        void shouldNotSplitEquationInAsciiMathBlockAtSingleNewline() throws Exception {
            var input = """
                    [asciimath]
                    ++++
                    f: bbb"N" -> bbb"N"
                    f: x |-> x + 1
                    ++++
                    """;
            var expected = Strings.chop("""
                    \\$f: bbb"N" -&gt; bbb"N"
                    f: x |-&gt; x + 1\\$
                    """);
            var output = convertStringToEmbedded(input);
            assertCss(".stemblock", output, 1);
            var nodes = xmlnodesAtXpath("//*[@class=\"content\"]", output);
            assertEquals(expected, Strings.strip(first(nodes).getInnerHtml()));
        }

        @Test
        @DisplayName("should split equation in AsciiMath block at escaped newline")
        void shouldSplitEquationInAsciiMathBlockAtEscapedNewline() throws Exception {
            var input = """
                    [asciimath]
                    ++++
                    f: bbb"N" -> bbb"N" \\
                    f: x |-> x + 1
                    ++++
                    """;
            var expected = Strings.chop("""
                    \\$f: bbb"N" -&gt; bbb"N"\\$
                    \\$f: x |-&gt; x + 1\\$
                    """);
            var output = convertStringToEmbedded(input);
            assertCss(".stemblock", output, 1);
            var nodes = xmlnodesAtXpath("//*[@class=\"content\"]", output);
            assertEquals(expected, Strings.strip(first(nodes).getInnerHtml()));
        }

        @Test
        @DisplayName("should split equation in AsciiMath block at sequence of escaped newlines")
        void shouldSplitEquationInAsciiMathBlockAtSequenceOfEscapedNewlines() throws Exception {
            var input = """
                    [asciimath]
                    ++++
                    f: bbb"N" -> bbb"N" \\
                    \\
                    f: x |-> x + 1
                    ++++
                    """;
            var expected = Strings.chop("""
                    \\$f: bbb"N" -&gt; bbb"N"\\$
                    <br>
                    \\$f: x |-&gt; x + 1\\$
                    """);
            var output = convertStringToEmbedded(input);
            assertCss(".stemblock", output, 1);
            var nodes = xmlnodesAtXpath("//*[@class=\"content\"]", output);
            assertEquals(expected, Strings.strip(first(nodes).getInnerHtml()));
        }

        @Test
        @DisplayName("should split equation in AsciiMath block at newline sequence and preserve breaks")
        void shouldSplitEquationInAsciiMathBlockAtNewlineSequenceAndPreserveBreaks() throws Exception {
            var input = """
                    [asciimath]
                    ++++
                    f: bbb"N" -> bbb"N"


                    f: x |-> x + 1
                    ++++
                    """;
            var expected = Strings.chop("""
                    \\$f: bbb"N" -&gt; bbb"N"\\$
                    <br>
                    <br>
                    \\$f: x |-&gt; x + 1\\$
                    """);
            var output = convertStringToEmbedded(input);
            assertCss(".stemblock", output, 1);
            var nodes = xmlnodesAtXpath("//*[@class=\"content\"]", output);
            assertEquals(expected, Strings.strip(first(nodes).getInnerHtml()));
        }

        @Test
        @DisplayName("should add AsciiMath delimiters around asciimath block content")
        void shouldAddAsciiMathDelimitersAroundAsciimathBlockContent() throws Exception {
            var input = """
                    [asciimath]
                    ++++
                    sqrt(3x-1)+(1+x)^2 < y
                    ++++
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".stemblock", output, 1);
            var nodes = xmlnodesAtXpath("//*[@class=\"content\"]/child::text()", output);
            assertEquals("\\$sqrt(3x-1)+(1+x)^2 &lt; y\\$", Strings.strip(str(first(nodes))));
        }

        @Test
        @DisplayName("should not add AsciiMath delimiters around asciimath block content if already present")
        void shouldNotAddAsciiMathDelimitersAroundAsciimathBlockContentIfAlreadyPresent() throws Exception {
            var input = """
                    [asciimath]
                    ++++
                    \\$sqrt(3x-1)+(1+x)^2 < y\\$
                    ++++
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".stemblock", output, 1);
            var nodes = xmlnodesAtXpath("//*[@class=\"content\"]/child::text()", output);
            assertEquals("\\$sqrt(3x-1)+(1+x)^2 &lt; y\\$", Strings.strip(str(first(nodes))));
        }

        @Test
        @DisplayName("should convert contents of asciimath block to MathML in DocBook output if asciimath gem is available")
        void shouldConvertContentsOfAsciimathBlockToMathMLInDocBookOutputIfAsciimathGemIsAvailable() throws Exception {
            var asciimathAvailable = false;
            var input = """
                    [asciimath]
                    ++++
                    x+b/(2a)<+-sqrt((b^2)/(4a^2)-c/a)
                    ++++

                    [asciimath]
                    ++++
                    ++++
                    """;
            var expect = Strings.chop("""
                    <informalequation>
                    <mml:math xmlns:mml="http://www.w3.org/1998/Math/MathML"><mml:mi>x</mml:mi><mml:mo>+</mml:mo><mml:mfrac><mml:mi>b</mml:mi><mml:mrow><mml:mn>2</mml:mn><mml:mi>a</mml:mi></mml:mrow></mml:mfrac><mml:mo>&lt;</mml:mo><mml:mo>&#xB1;</mml:mo><mml:msqrt><mml:mrow><mml:mfrac><mml:msup><mml:mi>b</mml:mi><mml:mn>2</mml:mn></mml:msup><mml:mrow><mml:mn>4</mml:mn><mml:msup><mml:mi>a</mml:mi><mml:mn>2</mml:mn></mml:msup></mml:mrow></mml:mfrac><mml:mo>&#x2212;</mml:mo><mml:mfrac><mml:mi>c</mml:mi><mml:mi>a</mml:mi></mml:mfrac></mml:mrow></mml:msqrt></mml:math>
                    </informalequation>
                    <informalequation>
                    <mml:math xmlns:mml="http://www.w3.org/1998/Math/MathML"></mml:math>
                    </informalequation>
                    """);
            try (var logger = usingMemoryLogger()) {
                var doc = documentFromString(input, Options.of("backend", "docbook", "standalone", false));
                var actual = doc.convert();
                if (isTruthy(asciimathAvailable)) {
                    assertEquals(expect, Strings.strip(actual));
                } else {
                    assertMessage(logger, "WARN", "optional gem 'asciimath' is not available. Functionality disabled.");
                }
            }
        }

        @Test
        @DisplayName("should output title for latexmath block if defined")
        void shouldOutputTitleForLatexmathBlockIfDefined() throws Exception {
            var input = """
                    .The Lorenz Equations
                    [latexmath]
                    ++++
                    \\begin{aligned}
                    \\dot{x} & = \\sigma(y-x) \\\\
                    \\dot{y} & = \\rho x - y - xz \\\\
                    \\dot{z} & = -\\beta z + xy
                    \\end{aligned}
                    ++++
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".stemblock", output, 1);
            assertCss(".stemblock .title", output, 1);
            assertXpath("//*[@class=\"title\"][text()=\"The Lorenz Equations\"]", output, 1);
        }

        @Test
        @DisplayName("should output title for asciimath block if defined")
        void shouldOutputTitleForAsciimathBlockIfDefined() throws Exception {
            var input = """
                    .Simple fraction
                    [asciimath]
                    ++++
                    a//b
                    ++++
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".stemblock", output, 1);
            assertCss(".stemblock .title", output, 1);
            assertXpath("//*[@class=\"title\"][text()=\"Simple fraction\"]", output, 1);
        }

        @Test
        @DisplayName("should add AsciiMath delimiters around stem block content if stem attribute is asciimath, empty, or not set")
        void shouldAddAsciiMathDelimitersAroundStemBlockContentIfStemAttributeIsAsciimathEmptyOrNotSet() throws Exception {
            var input = """
                    [stem]
                    ++++
                    sqrt(3x-1)+(1+x)^2 < y
                    ++++
                    """;
            for (var attributes : list(Options.of(), Options.of("stem", ""), Options.of("stem", "asciimath"), Options.of("stem", "bogus"))) {
                var output = convertStringToEmbedded(input, Options.of("attributes", attributes));
                assertCss(".stemblock", output, 1);
                var nodes = xmlnodesAtXpath("//*[@class=\"content\"]/child::text()", output);
                assertEquals("\\$sqrt(3x-1)+(1+x)^2 &lt; y\\$", Strings.strip(str(first(nodes))));
            }
        }

        @Test
        @DisplayName("should add LaTeX math delimiters around stem block content if stem attribute is latexmath, latex, or tex")
        void shouldAddLaTeXMathDelimitersAroundStemBlockContentIfStemAttributeIsLatexmathLatexOrTex() throws Exception {
            var input = """
                    [stem]
                    ++++
                    \\sqrt{3x-1}+(1+x)^2 < y
                    ++++
                    """;
            for (var attributes : list(Options.of("stem", "latexmath"), Options.of("stem", "latex"), Options.of("stem", "tex"))) {
                var output = convertStringToEmbedded(input, Options.of("attributes", attributes));
                assertCss(".stemblock", output, 1);
                var nodes = xmlnodesAtXpath("//*[@class=\"content\"]/child::text()", output);
                assertEquals("\\[\\sqrt{3x-1}+(1+x)^2 &lt; y\\]", Strings.strip(str(first(nodes))));
            }
        }

        @Test
        @DisplayName("should allow stem style to be set using second positional argument of block attributes")
        void shouldAllowStemStyleToBeSetUsingSecondPositionalArgumentOfBlockAttributes() throws Exception {
            var input = """
                    :stem: latexmath

                    [stem,asciimath]
                    ++++
                    sqrt(3x-1)+(1+x)^2 < y
                    ++++
                    """;
            var doc = documentFromString(input);
            var stemblock = doc.getBlocks().get(0);
            assertEquals("stem", stemblock.getContext());
            assertEquals("asciimath", get(stemblock.getAttributes(), "style"));
            var output = doc.convert(Options.of("standalone", false));
            assertCss(".stemblock", output, 1);
            var nodes = xmlnodesAtXpath("//*[@class=\"content\"]/child::text()", output);
            assertEquals("\\$sqrt(3x-1)+(1+x)^2 &lt; y\\$", Strings.strip(str(first(nodes))));
        }
    }

    @Nested
    @DisplayName("Custom Blocks")
    class CustomBlocks {

        @Test
        @DisplayName("should not warn if block style is unknown")
        void shouldNotWarnIfBlockStyleIsUnknown() throws Exception {
            var input = """
                    [foo]
                    --
                    bar
                    --
                    """;
            convertStringToEmbedded(input);
            assertEmpty(logger.getMessages());
        }

        @Test
        @DisplayName("should log debug message if block style is unknown and debug level is enabled")
        void shouldLogDebugMessageIfBlockStyleIsUnknownAndDebugLevelIsEnabled() throws Exception {
            var input = """
                    [foo]
                    --
                    bar
                    --
                    """;
            try (var logger = usingMemoryLogger(Logger.Severity.DEBUG)) {
                convertStringToEmbedded(input);
                assertMessage(logger, "DEBUG", "<stdin>: line 2: unknown style for open block: foo", LogMessage.class);
            }
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("block title above section gets carried over to first block in section")
        void blockTitleAboveSectionGetsCarriedOverToFirstBlockInSection() throws Exception {
            var input = """
                    .Title
                    == Section

                    paragraph
                    """;
            var output = convertString(input);
            assertXpath("//*[@class=\"paragraph\"]", output, 1);
            assertXpath("//*[@class=\"paragraph\"]/*[@class=\"title\"][text()=\"Title\"]", output, 1);
            assertXpath("//*[@class=\"paragraph\"]/p[text()=\"paragraph\"]", output, 1);
        }

        @Test
        @DisplayName("block title above document title demotes document title to a section title")
        void blockTitleAboveDocumentTitleDemotesDocumentTitleToASectionTitle() throws Exception {
            var input = """
                    .Block title
                    = Section Title

                    section paragraph
                    """;
            var output = convertString(input);
            assertXpath("//*[@id=\"header\"]/*", output, 0);
            assertXpath("//*[@id=\"preamble\"]/*", output, 0);
            assertXpath("//*[@id=\"content\"]/h1[text()=\"Section Title\"]", output, 1);
            assertXpath("//*[@class=\"paragraph\"]", output, 1);
            assertXpath("//*[@class=\"paragraph\"]/*[@class=\"title\"][text()=\"Block title\"]", output, 1);
            assertMessage(logger, "ERROR", "<stdin>: line 2: level 0 sections can only be used when doctype is book", LogMessage.class);
        }

        @Test
        @DisplayName("block title above document title gets carried over to first block in first section if no preamble")
        void blockTitleAboveDocumentTitleGetsCarriedOverToFirstBlockInFirstSectionIfNoPreamble() throws Exception {
            var input = """
                    :doctype: book
                    .Block title
                    = Document Title

                    == First Section

                    paragraph
                    """;
            var doc = documentFromString(input);
            assertFalsy(doc.hasHeader());
            var output = doc.convert();
            assertXpath("//*[@class=\"sect1\"]//*[@class=\"paragraph\"]/*[@class=\"title\"][text()=\"Block title\"]", output, 1);
        }

        @Test
        @DisplayName("should apply substitutions to a block title in normal order")
        void shouldApplySubstitutionsToABlockTitleInNormalOrder() throws Exception {
            var input = """
                    .{link-url}[{link-text}]{tm}
                    The one and only!
                    """;
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("link-url", "https://acme.com", "link-text", "ACME", "tm", "(TM)")));
            assertCss(".title", output, 1);
            assertCss(".title a[href=\"https://acme.com\"]", output, 1);
            assertXpath(("//*[@class=\"title\"][contains(text(),\"" + str(decodeChar(8482)) + "\")]"), output, 1);
        }

        @Test
        @DisplayName("empty attribute list should not appear in output")
        void emptyAttributeListShouldNotAppearInOutput() throws Exception {
            var input = """
                    []
                    --
                    Block content
                    --
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "Block content");
            refuteIncludes(output, "[]");
        }

        @Test
        @DisplayName("empty block anchor should not appear in output")
        void emptyBlockAnchorShouldNotAppearInOutput() throws Exception {
            var input = """
                    [[]]
                    --
                    Block content
                    --
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "Block content");
            refuteIncludes(output, "[[]]");
        }
    }

    @Nested
    @DisplayName("Images")
    class Images {

        @Test
        @DisplayName("can convert block image with alt text defined in macro")
        void canConvertBlockImageWithAltTextDefinedInMacro() throws Exception {
            var input = "image::images/tiger.png[Tiger]";
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//img[@src=\"images/tiger.png\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("converts SVG image using img element by default")
        void convertsSVGImageUsingImgElementByDefault() throws Exception {
            var input = "image::tiger.svg[Tiger]";
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel()));
            assertXpath("/*[@class=\"imageblock\"]//img[@src=\"tiger.svg\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("converts interactive SVG image with alt text using object element")
        void convertsInteractiveSVGImageWithAltTextUsingObjectElement() throws Exception {
            var input = """
                    :imagesdir: images

                    [%interactive]
                    image::tiger.svg[Tiger,100]
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel()));
            assertXpath("/*[@class=\"imageblock\"]//object[@type=\"image/svg+xml\"][@data=\"images/tiger.svg\"][@width=\"100\"]/span[@class=\"alt\"][text()=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("converts SVG image with alt text using img element when safe mode is secure")
        void convertsSVGImageWithAltTextUsingImgElementWhenSafeModeIsSecure() throws Exception {
            var input = """
                    [%interactive]
                    image::images/tiger.svg[Tiger,100]
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//img[@src=\"images/tiger.svg\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("inserts fallback image for SVG inside object element using same dimensions")
        void insertsFallbackImageForSVGInsideObjectElementUsingSameDimensions() throws Exception {
            var input = """
                    :imagesdir: images

                    [%interactive]
                    image::tiger.svg[Tiger,100,fallback=tiger.png]
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel()));
            assertXpath("/*[@class=\"imageblock\"]//object[@type=\"image/svg+xml\"][@data=\"images/tiger.svg\"][@width=\"100\"]/img[@src=\"images/tiger.png\"][@width=\"100\"]", output, 1);
        }

        @Test
        @DisplayName("detects SVG image URI that contains a query string")
        void detectsSVGImageURIThatContainsAQueryString() throws Exception {
            var input = """
                    :imagesdir: images

                    [%interactive]
                    image::http://example.org/tiger.svg?foo=bar[Tiger,100]
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel()));
            assertXpath("/*[@class=\"imageblock\"]//object[@type=\"image/svg+xml\"][@data=\"http://example.org/tiger.svg?foo=bar\"][@width=\"100\"]/span[@class=\"alt\"][text()=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("detects SVG image when format attribute is svg")
        void detectsSVGImageWhenFormatAttributeIsSvg() throws Exception {
            var input = """
                    :imagesdir: images

                    [%interactive]
                    image::http://example.org/tiger-svg[Tiger,100,format=svg]
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel()));
            assertXpath("/*[@class=\"imageblock\"]//object[@type=\"image/svg+xml\"][@data=\"http://example.org/tiger-svg\"][@width=\"100\"]/span[@class=\"alt\"][text()=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("converts to inline SVG image when inline option is set on block")
        void convertsToInlineSVGImageWhenInlineOptionIsSetOnBlock() throws Exception {
            var input = """
                    :imagesdir: fixtures

                    [%inline]
                    image::circle.svg[Tiger,100]
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertMatch(rx("<svg\\s[^>]*width=\"100\"[^>]*>"), output, 1);
            refuteMatch(rx("<svg\\s[^>]*width=\"500\"[^>]*>"), output);
            refuteMatch(rx("<svg\\s[^>]*height=\"500\"[^>]*>"), output);
            refuteMatch(rx("<svg\\s[^>]*style=\"[^>]*>"), output);
        }

        @Test
        @DisplayName("should ignore link attribute if value is self and image target is inline SVG")
        void shouldIgnoreLinkAttributeIfValueIsSelfAndImageTargetIsInlineSVG() throws Exception {
            var input = """
                    :imagesdir: fixtures

                    [%inline]
                    image::circle.svg[Tiger,100,link=self]
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertMatch(rx("<svg\\s[^>]*width=\"100\"[^>]*>"), output);
            refuteMatch(rx("<a href="), output);
        }

        @Test
        @DisplayName("should honor percentage width for SVG image with inline option")
        void shouldHonorPercentageWidthForSVGImageWithInlineOption() throws Exception {
            var input = """
                    :imagesdir: fixtures

                    image::circle.svg[Circle,50%,opts=inline]
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertMatch(rx("<svg\\s[^>]*width=\"50%\"[^>]*>"), output, 1);
        }

        @Test
        @DisplayName("should not crash if explicit width on SVG image block is an integer")
        void shouldNotCrashIfExplicitWidthOnSVGImageBlockIsAnInteger() throws Exception {
            var input = """
                    :imagesdir: fixtures

                    image::circle.svg[Circle,opts=inline]
                    """;
            var doc = documentFromString(input, Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docdir", testdir())));
            doc.getBlocks().get(0).setAttribute("width", 50);
            var output = doc.convert();
            assertMatch(rx("<svg\\s[^>]*width=\"50\"[^>]*>"), output, 1);
        }

        @Test
        @DisplayName("converts to inline SVG image when inline option is set on block and data-uri is set on document")
        void convertsToInlineSVGImageWhenInlineOptionIsSetOnBlockAndDataUriIsSetOnDocument() throws Exception {
            var input = """
                    :imagesdir: fixtures
                    :data-uri:

                    [%inline]
                    image::circle.svg[Tiger,100]
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertMatch(rx("<svg\\s[^>]*width=\"100\">"), output, 1);
        }

        @Test
        @DisplayName("should not throw exception if SVG to inline is empty")
        void shouldNotThrowExceptionIfSVGToInlineIsEmpty() throws Exception {
            var input = "image::empty.svg[nada,opts=inline]";
            var output = convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("docdir", testdir(), "imagesdir", "fixtures")));
            assertXpath("//svg", output, 0);
            assertXpath("//span[@class=\"alt\"][text()=\"nada\"]", output, 1);
            assertMessage(logger, "WARN", "~contents of SVG is empty:");
        }

        @Test
        @DisplayName("should not throw exception if SVG to inline contains an incomplete start tag and explicit width is specified")
        void shouldNotThrowExceptionIfSVGToInlineContainsAnIncompleteStartTagAndExplicitWidthIsSpecified() throws Exception {
            var input = "image::incomplete.svg[,200,opts=inline]";
            var output = convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("docdir", testdir(), "imagesdir", "fixtures")));
            assertXpath("//svg", output, 1);
            assertXpath("//span[@class=\"alt\"]", output, 0);
        }

        @Test
        @DisplayName("embeds remote SVG to inline when inline option is set on block and allow-uri-read is set on document")
        void embedsRemoteSVGToInlineWhenInlineOptionIsSetOnBlockAndAllowUriReadIsSetOnDocument() throws Exception {
            var input = ("image::http://" + str(resolveLocalhost()) + ":9876/fixtures/circle.svg[Circle,100,100,opts=inline]");
            var output = usingTestWebserver(() -> convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("allow-uri-read", ""))));
            assertCss("svg", output, 1);
            assertCss("svg[style]", output, 0);
            assertCss("svg[width=\"100\"]", output, 1);
            assertCss("svg[height=\"100\"]", output, 1);
            assertCss("svg circle", output, 1);
        }

        @Test
        @Disabled("OpenURI cache is specific to the Ruby implementation")
        @DisplayName("should cache remote SVG when allow-uri-read, cache-uri, and inline option are set")
        void shouldCacheRemoteSVGWhenAllowUriReadCacheUriAndInlineOptionAreSet() throws Exception {
        }

        @Test
        @DisplayName("converts to alt text for SVG with inline option set if SVG cannot be read")
        void convertsToAltTextForSVGWithInlineOptionSetIfSVGCannotBeRead() throws Exception {
            var input = """
                    [%inline]
                    image::no-such-image.svg[Alt Text]
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("docfile", "document.adoc")));
            assertXpath("//span[@class=\"alt\"][text()=\"Alt Text\"]", output, 1);
            assertMessage(logger, "WARN", "~document.adoc: SVG does not exist or cannot be read");
        }

        @Test
        @DisplayName("reports document as <stdin> if SVG to inline cannot be read and document is string")
        void reportsDocumentAsStdinIfSVGToInlineCannotBeReadAndDocumentIsString() throws Exception {
            var input = """
                    [%inline]
                    image::no-such-image.svg[Alt Text]
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel()));
            assertXpath("//span[@class=\"alt\"][text()=\"Alt Text\"]", output, 1);
            assertMessage(logger, "WARN", "~<stdin>: SVG does not exist or cannot be read");
        }

        @Test
        @DisplayName("can convert block image with alt text defined in macro containing square bracket")
        void canConvertBlockImageWithAltTextDefinedInMacroContainingSquareBracket() throws Exception {
            var input = "image::images/tiger.png[A [Bengal] Tiger]";
            var output = convertString(input);
            var img = xmlnodesAtXpath("//img", output, 1);
            assertEquals("A [Bengal] Tiger", img.getAttribute("alt"));
        }

        @Test
        @DisplayName("can convert block image with target containing spaces")
        void canConvertBlockImageWithTargetContainingSpaces() throws Exception {
            var input = "image::images/big tiger.png[A Big Tiger]";
            var output = convertString(input);
            var img = xmlnodesAtXpath("//img", output, 1);
            assertEquals("images/big%20tiger.png", img.getAttribute("src"));
            assertEquals("A Big Tiger", img.getAttribute("alt"));
        }

        @Test
        @DisplayName("should not recognize block image if target has leading or trailing spaces")
        void shouldNotRecognizeBlockImageIfTargetHasLeadingOrTrailingSpaces() throws Exception {
            for (var target : list(" tiger.png", "tiger.png ")) {
                var input = ("image::" + str(target) + "[Tiger]");
                var output = convertStringToEmbedded(input);
                assertXpath("//img", output, 0);
            }
        }

        @Test
        @DisplayName("can convert block image with alt text defined in block attribute above macro")
        void canConvertBlockImageWithAltTextDefinedInBlockAttributeAboveMacro() throws Exception {
            var input = """
                    [Tiger]
                    image::images/tiger.png[]
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//img[@src=\"images/tiger.png\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("alt text in macro overrides alt text above macro")
        void altTextInMacroOverridesAltTextAboveMacro() throws Exception {
            var input = """
                    [Alt Text]
                    image::images/tiger.png[Tiger]
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//img[@src=\"images/tiger.png\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("should substitute attribute references in alt text defined in image block macro")
        void shouldSubstituteAttributeReferencesInAltTextDefinedInImageBlockMacro() throws Exception {
            var input = """
                    :alt-text: Tiger

                    image::images/tiger.png[{alt-text}]
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//img[@src=\"images/tiger.png\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("should set direction CSS class on image if float attribute is set")
        void shouldSetDirectionCSSClassOnImageIfFloatAttributeIsSet() throws Exception {
            var input = """
                    [float=left]
                    image::images/tiger.png[Tiger]
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".imageblock.left", output, 1);
            assertCss(".imageblock[style]", output, 0);
        }

        @Test
        @DisplayName("should set text alignment CSS class on image if align attribute is set")
        void shouldSetTextAlignmentCSSClassOnImageIfAlignAttributeIsSet() throws Exception {
            var input = """
                    [align=center]
                    image::images/tiger.png[Tiger]
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".imageblock.text-center", output, 1);
            assertCss(".imageblock[style]", output, 0);
        }

        @Test
        @DisplayName("style attribute is dropped from image macro")
        void styleAttributeIsDroppedFromImageMacro() throws Exception {
            var input = """
                    [style=value]
                    image::images/tiger.png[Tiger]
                    """;
            var doc = documentFromString(input);
            var img = doc.getBlocks().get(0);
            assertFalsy(img.getAttributes().containsKey("style"));
            assertNull(img.getStyle());
        }

        @Test
        @DisplayName("should apply specialcharacters and replacement substitutions to alt text")
        void shouldApplySpecialcharactersAndReplacementSubstitutionsToAltText() throws Exception {
            var input = "A tiger's \"roar\" is < a bear's \"growl\"";
            var expected = "A tiger&#8217;s &quot;roar&quot; is &lt; a bear&#8217;s &quot;growl&quot;";
            var result = convertStringToEmbedded(("image::images/tiger-roar.png[" + str(input) + "]"));
            assertIncludes(result, ("alt=\"" + str(expected) + "\""));
        }

        @Test
        @DisplayName("should not encode double quotes in alt text when converting to DocBook")
        void shouldNotEncodeDoubleQuotesInAltTextWhenConvertingToDocBook() throws Exception {
            var input = "Select \"File > Open\"";
            var expected = "Select \"File &gt; Open\"";
            var result = convertStringToEmbedded(("image::images/open.png[" + str(input) + "]"), Options.of("backend", "docbook"));
            assertIncludes(result, ("<phrase>" + str(expected) + "</phrase>"));
        }

        @Test
        @DisplayName("should auto-generate alt text for block image if alt text is not specified")
        void shouldAutoGenerateAltTextForBlockImageIfAltTextIsNotSpecified() throws Exception {
            var input = "image::images/lions-and-tigers.png[]";
            var image = blockFromString(input);
            assertEquals("lions and tigers", image.getAttribute("alt"));
            assertEquals("lions and tigers", image.getAttribute("default-alt"));
            var output = image.convert();
            assertXpath("/*[@class=\"imageblock\"]//img[@src=\"images/lions-and-tigers.png\"][@alt=\"lions and tigers\"]", output, 1);
        }

        @Test
        @DisplayName("can convert block image with alt text and height and width")
        void canConvertBlockImageWithAltTextAndHeightAndWidth() throws Exception {
            var input = "image::images/tiger.png[Tiger, 200, 300]";
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//img[@src=\"images/tiger.png\"][@alt=\"Tiger\"][@width=\"200\"][@height=\"300\"]", output, 1);
        }

        @Test
        @DisplayName("should not output empty width attribute if positional width attribute is empty")
        void shouldNotOutputEmptyWidthAttributeIfPositionalWidthAttributeIsEmpty() throws Exception {
            var input = "image::images/tiger.png[Tiger,]";
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//img[@src=\"images/tiger.png\"]", output, 1);
            assertXpath("/*[@class=\"imageblock\"]//img[@src=\"images/tiger.png\"][@width]", output, 0);
        }

        @Test
        @DisplayName("can convert block image with link")
        void canConvertBlockImageWithLink() throws Exception {
            var input = "image::images/tiger.png[Tiger, link='http://en.wikipedia.org/wiki/Tiger']\n";
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//a[@class=\"image\"][@href=\"http://en.wikipedia.org/wiki/Tiger\"]/img[@src=\"images/tiger.png\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("can convert block image with link to self")
        void canConvertBlockImageWithLinkToSelf() throws Exception {
            var input = """
                    :imagesdir: img

                    image::tiger.png[Tiger, link=self]
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//a[@class=\"image\"][@href=\"img/tiger.png\"]/img[@src=\"img/tiger.png\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("adds rel=noopener attribute to block image with link that targets _blank window")
        void addsRelNoopenerAttributeToBlockImageWithLinkThatTargets_blankWindow() throws Exception {
            var input = "image::images/tiger.png[Tiger,link=http://en.wikipedia.org/wiki/Tiger,window=_blank]";
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//a[@class=\"image\"][@href=\"http://en.wikipedia.org/wiki/Tiger\"][@target=\"_blank\"][@rel=\"noopener\"]/img[@src=\"images/tiger.png\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("adds rel=noopener attribute to block image with link that targets name window when the noopener option is set")
        void addsRelNoopenerAttributeToBlockImageWithLinkThatTargetsNameWindowWhenTheNoopenerOptionIsSet() throws Exception {
            var input = "image::images/tiger.png[Tiger,link=http://en.wikipedia.org/wiki/Tiger,window=name,opts=noopener]";
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//a[@class=\"image\"][@href=\"http://en.wikipedia.org/wiki/Tiger\"][@target=\"name\"][@rel=\"noopener\"]/img[@src=\"images/tiger.png\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("adds rel=nofollow attribute to block image with a link when the nofollow option is set")
        void addsRelNofollowAttributeToBlockImageWithALinkWhenTheNofollowOptionIsSet() throws Exception {
            var input = "image::images/tiger.png[Tiger,link=http://en.wikipedia.org/wiki/Tiger,opts=nofollow]";
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//a[@class=\"image\"][@href=\"http://en.wikipedia.org/wiki/Tiger\"][@rel=\"nofollow\"]/img[@src=\"images/tiger.png\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("can convert block image with caption")
        void canConvertBlockImageWithCaption() throws Exception {
            var input = """
                    .The AsciiDoc Tiger
                    image::images/tiger.png[Tiger]
                    """;
            var doc = documentFromString(input);
            assertEquals("1", doc.getBlocks().get(0).getNumeral());
            var output = doc.convert();
            assertXpath("//*[@class=\"imageblock\"]//img[@src=\"images/tiger.png\"][@alt=\"Tiger\"]", output, 1);
            assertXpath("//*[@class=\"imageblock\"]/*[@class=\"title\"][text()=\"Figure 1. The AsciiDoc Tiger\"]", output, 1);
            assertEquals(1, get(doc.getAttributes(), "figure-number"));
        }

        @Test
        @DisplayName("can convert block image with explicit caption")
        void canConvertBlockImageWithExplicitCaption() throws Exception {
            var input = """
                    [caption="Voila! "]
                    .The AsciiDoc Tiger
                    image::images/tiger.png[Tiger]
                    """;
            var doc = documentFromString(input);
            assertNull(doc.getBlocks().get(0).getNumeral());
            var output = doc.convert();
            assertXpath("//*[@class=\"imageblock\"]//img[@src=\"images/tiger.png\"][@alt=\"Tiger\"]", output, 1);
            assertXpath("//*[@class=\"imageblock\"]/*[@class=\"title\"][text()=\"Voila! The AsciiDoc Tiger\"]", output, 1);
            assertFalsy(doc.getAttributes().containsKey("figure-number"));
        }

        @Test
        @DisplayName("can align image in DocBook backend")
        void canAlignImageInDocBookBackend() throws Exception {
            var input = "image::images/sunset.jpg[Sunset,align=right]";
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertXpath("//imagedata", output, 1);
            assertXpath("//imagedata[@align=\"right\"]", output, 1);
        }

        @Test
        @DisplayName("can convert block image with link in DocBook backend")
        void canConvertBlockImageWithLinkInDocBookBackend() throws Exception {
            var input = "image::images/tiger.png[Tiger, link=\"http://en.wikipedia.org/wiki/Tiger\"]";
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertMatch("<link xl:href=\"http://en.wikipedia.org/wiki/Tiger\"><mediaobject>", output);
        }

        @Test
        @DisplayName("can convert block image with link to self in DocBook backend")
        void canConvertBlockImageWithLinkToSelfInDocBookBackend() throws Exception {
            var input = """
                    :imagesdir: img

                    image::tiger.png[Tiger, link=self]
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertMatch("<link xl:href=\"img/tiger.png\"><mediaobject>", output);
        }

        @Test
        @DisplayName("should set content width and depth in DocBook backend if no scaling")
        void shouldSetContentWidthAndDepthInDocBookBackendIfNoScaling() throws Exception {
            var input = "image::images/sunset.jpg[Sunset,500,332]";
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertXpath("//imagedata", output, 1);
            assertXpath("//imagedata[@contentwidth=\"500\"]", output, 1);
            assertXpath("//imagedata[@contentdepth=\"332\"]", output, 1);
            assertXpath("//imagedata[@width]", output, 0);
            assertXpath("//imagedata[@depth]", output, 0);
        }

        @Test
        @DisplayName("can scale image in DocBook backend")
        void canScaleImageInDocBookBackend() throws Exception {
            var input = "image::images/sunset.jpg[Sunset,500,332,scale=200]";
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertXpath("//imagedata", output, 1);
            assertXpath("//imagedata[@scale=\"200\"]", output, 1);
            assertXpath("//imagedata[@width]", output, 0);
            assertXpath("//imagedata[@depth]", output, 0);
            assertXpath("//imagedata[@contentwidth]", output, 0);
            assertXpath("//imagedata[@contentdepth]", output, 0);
        }

        @Test
        @DisplayName("scale image width in DocBook backend")
        void scaleImageWidthInDocBookBackend() throws Exception {
            var input = "image::images/sunset.jpg[Sunset,500,332,scaledwidth=25%]";
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertXpath("//imagedata", output, 1);
            assertXpath("//imagedata[@width=\"25%\"]", output, 1);
            assertXpath("//imagedata[@depth]", output, 0);
            assertXpath("//imagedata[@contentwidth]", output, 0);
            assertXpath("//imagedata[@contentdepth]", output, 0);
        }

        @Test
        @DisplayName("adds % to scaled width if no units given in DocBook backend ")
        void addsToScaledWidthIfNoUnitsGivenInDocBookBackend() throws Exception {
            var input = "image::images/sunset.jpg[Sunset,scaledwidth=25]";
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertXpath("//imagedata", output, 1);
            assertXpath("//imagedata[@width=\"25%\"]", output, 1);
        }

        @Test
        @DisplayName("keeps attribute reference unprocessed if image target is missing attribute reference and attribute-missing is skip")
        void keepsAttributeReferenceUnprocessedIfImageTargetIsMissingAttributeReferenceAndAttributeMissingIsSkip() throws Exception {
            var input = """
                    :attribute-missing: skip

                    image::{bogus}[]
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("img[src=\"{bogus}\"]", output, 1);
            assertEmpty(logger);
        }

        @Test
        @DisplayName("should not drop line if image target is missing attribute reference and attribute-missing is drop")
        void shouldNotDropLineIfImageTargetIsMissingAttributeReferenceAndAttributeMissingIsDrop() throws Exception {
            var input = """
                    :attribute-missing: drop

                    image::{bogus}/photo.jpg[]
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("img[src=\"/photo.jpg\"]", output, 1);
            assertEmpty(logger);
        }

        @Test
        @DisplayName("drops line if image target is missing attribute reference and attribute-missing is drop-line")
        void dropsLineIfImageTargetIsMissingAttributeReferenceAndAttributeMissingIsDropLine() throws Exception {
            var input = """
                    :attribute-missing: drop-line

                    image::{bogus}[]
                    """;
            var output = convertStringToEmbedded(input);
            assertEmpty(Strings.strip(output));
            assertMessage(logger, "INFO", "dropping line containing reference to missing attribute: bogus");
        }

        @Test
        @DisplayName("should not drop line if image target resolves to blank and attribute-missing is drop-line")
        void shouldNotDropLineIfImageTargetResolvesToBlankAndAttributeMissingIsDropLine() throws Exception {
            var input = """
                    :attribute-missing: drop-line

                    image::{blank}[]
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("img[src=\"\"]", output, 1);
            assertEmpty(logger);
        }

        @Test
        @DisplayName("dropped image does not break processing of following section and attribute-missing is drop-line")
        void droppedImageDoesNotBreakProcessingOfFollowingSectionAndAttributeMissingIsDropLine() throws Exception {
            var input = """
                    :attribute-missing: drop-line

                    image::{bogus}[]

                    == Section Title
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("img", output, 0);
            assertCss("h2", output, 1);
            refuteIncludes(output, "== Section Title");
            assertMessage(logger, "INFO", "dropping line containing reference to missing attribute: bogus");
        }

        @Test
        @DisplayName("should pass through image that references uri")
        void shouldPassThroughImageThatReferencesUri() throws Exception {
            var input = """
                    :imagesdir: images

                    image::http://asciidoc.org/images/tiger.png[Tiger]
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//img[@src=\"http://asciidoc.org/images/tiger.png\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("should encode spaces in image target if value is a URI")
        void shouldEncodeSpacesInImageTargetIfValueIsAURI() throws Exception {
            var input = "image::http://example.org/svg?digraph=digraph G { a -> b; }[diagram]";
            var output = convertStringToEmbedded(input);
            assertXpath(("/*[@class=\"imageblock\"]//img[@src=\"http://example.org/svg?digraph=digraph%20G%20{%20a%20-" + str(decodeChar(62)) + "%20b;%20}\"]"), output, 1);
        }

        @Test
        @DisplayName("can resolve image relative to imagesdir")
        void canResolveImageRelativeToImagesdir() throws Exception {
            var input = """
                    :imagesdir: images

                    image::tiger.png[Tiger]
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@class=\"imageblock\"]//img[@src=\"images/tiger.png\"][@alt=\"Tiger\"]", output, 1);
        }

        @Test
        @DisplayName("embeds base64-encoded data uri for image when data-uri attribute is set")
        void embedsBase64EncodedDataUriForImageWhenDataUriAttributeIsSet() throws Exception {
            var input = """
                    :data-uri:
                    :imagesdir: fixtures

                    image::dot.gif[Dot]
                    """;
            var doc = documentFromString(input, Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertEquals("fixtures", get(doc.getAttributes(), "imagesdir"));
            var output = doc.convert();
            assertXpath("//img[@src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=\"][@alt=\"Dot\"]", output, 1);
        }

        @Test
        @Disabled("uri:classloader is specific to JRuby")
        @DisplayName("embeds base64-encoded data uri for image in classloader when data-uri attribute is set")
        void embedsBase64EncodedDataUriForImageInClassloaderWhenDataUriAttributeIsSet() throws Exception {
        }

        @Test
        @DisplayName("embeds SVG image with image/svg+xml mimetype when file extension is .svg")
        void embedsSVGImageWithImageSvgXmlMimetypeWhenFileExtensionIsSvg() throws Exception {
            var input = """
                    :imagesdir: fixtures
                    :data-uri:

                    image::circle.svg[Tiger,100]
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertXpath("//img[starts-with(@src,\"data:image/svg+xml;base64,\")]", output, 1);
        }

        @Test
        @DisplayName("should link to data URI if value of link attribute is self and image is embedded")
        void shouldLinkToDataURIIfValueOfLinkAttributeIsSelfAndImageIsEmbedded() throws Exception {
            var input = """
                    :imagesdir: fixtures
                    :data-uri:

                    image::circle.svg[Tiger,100,link=self]
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertXpath("//img[starts-with(@src,\"data:image/svg+xml;base64,\")]", output, 1);
            assertXpath("//a[starts-with(@href,\"data:image/svg+xml;base64,\")]", output, 1);
        }

        @Test
        @DisplayName("embeds empty base64-encoded data uri for unreadable image when data-uri attribute is set")
        void embedsEmptyBase64EncodedDataUriForUnreadableImageWhenDataUriAttributeIsSet() throws Exception {
            var input = """
                    :data-uri:
                    :imagesdir: fixtures

                    image::unreadable.gif[Dot]
                    """;
            var doc = documentFromString(input, Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertEquals("fixtures", get(doc.getAttributes(), "imagesdir"));
            var output = doc.convert();
            assertXpath("//img[@src=\"data:image/gif;base64,\"]", output, 1);
            assertMessage(logger, "WARN", "~image to embed not found or not readable");
        }

        @Test
        @DisplayName("embeds base64-encoded data uri with application/octet-stream mimetype when file extension is missing")
        void embedsBase64EncodedDataUriWithApplicationOctetStreamMimetypeWhenFileExtensionIsMissing() throws Exception {
            var input = """
                    :data-uri:
                    :imagesdir: fixtures

                    image::dot[Dot]
                    """;
            var doc = documentFromString(input, Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertEquals("fixtures", get(doc.getAttributes(), "imagesdir"));
            var output = doc.convert();
            assertXpath("//img[starts-with(@src,\"data:application/octet-stream;base64,\")]", output, 1);
        }

        @Test
        @DisplayName("embeds base64-encoded data uri for remote image when data-uri attribute is set")
        void embedsBase64EncodedDataUriForRemoteImageWhenDataUriAttributeIsSet() throws Exception {
            var input = (":data-uri:\n\nimage::http://" + str(resolveLocalhost()) + ":9876/fixtures/dot.gif[Dot]\n");
            var output = usingTestWebserver(() -> convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("allow-uri-read", ""))));
            assertXpath("//img[@src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=\"][@alt=\"Dot\"]", output, 1);
        }

        @Test
        @DisplayName("embeds base64-encoded data uri for remote image when imagesdir is a URI and data-uri attribute is set")
        void embedsBase64EncodedDataUriForRemoteImageWhenImagesdirIsAURIAndDataUriAttributeIsSet() throws Exception {
            var input = (":data-uri:\n:imagesdir: http://" + str(resolveLocalhost()) + ":9876/fixtures\n\nimage::dot.gif[Dot]\n");
            var output = usingTestWebserver(() -> convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("allow-uri-read", ""))));
            assertXpath("//img[@src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=\"][@alt=\"Dot\"]", output, 1);
        }

        @Test
        @Disabled("OpenURI cache is specific to the Ruby implementation")
        @DisplayName("should cache remote image when allow-uri-read, cache-uri, and data-uri are set")
        void shouldCacheRemoteImageWhenAllowUriReadCacheUriAndDataUriAreSet() throws Exception {
        }

        @Test
        @DisplayName("uses remote image uri when data-uri attribute is set and image cannot be retrieved")
        void usesRemoteImageUriWhenDataUriAttributeIsSetAndImageCannotBeRetrieved() throws Exception {
            var imageUri = ("http://" + str(resolveLocalhost()) + ":9876/fixtures/missing-image.gif");
            var input = (":data-uri:\n\nimage::" + str(imageUri) + "[Missing image]\n");
            var output = usingTestWebserver(() -> convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("allow-uri-read", ""))));
            assertXpath(("/*[@class=\"imageblock\"]//img[@src=\"" + str(imageUri) + "\"][@alt=\"Missing image\"]"), output, 1);
            assertMessage(logger, "WARN", "~could not retrieve image data from URI");
        }

        @Test
        @DisplayName("uses remote image uri when data-uri attribute is set and allow-uri-read is not set")
        void usesRemoteImageUriWhenDataUriAttributeIsSetAndAllowUriReadIsNotSet() throws Exception {
            var imageUri = ("http://" + str(resolveLocalhost()) + ":9876/fixtures/dot.gif");
            var input = (":data-uri:\n\nimage::" + str(imageUri) + "[Dot]\n");
            var output = usingTestWebserver(() -> convertStringToEmbedded(input, Options.of("safe", "safe")));
            assertXpath(("/*[@class=\"imageblock\"]//img[@src=\"" + str(imageUri) + "\"][@alt=\"Dot\"]"), output, 1);
        }

        @Test
        @DisplayName("can handle embedded data uri images")
        void canHandleEmbeddedDataUriImages() throws Exception {
            var input = "image::data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=[Dot]";
            var output = convertStringToEmbedded(input);
            assertXpath("//img[@src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=\"][@alt=\"Dot\"]", output, 1);
        }

        @Test
        @DisplayName("can handle embedded data uri images when data-uri attribute is set")
        void canHandleEmbeddedDataUriImagesWhenDataUriAttributeIsSet() throws Exception {
            var input = """
                    :data-uri:

                    image::data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=[Dot]
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//img[@src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=\"][@alt=\"Dot\"]", output, 1);
        }

        @Test
        @DisplayName("cleans reference to ancestor directories in imagesdir before reading image if safe mode level is at least SAFE")
        void cleansReferenceToAncestorDirectoriesInImagesdirBeforeReadingImageIfSafeModeLevelIsAtLeastSAFE() throws Exception {
            var input = """
                    :data-uri:
                    :imagesdir: ../..//fixtures/./../../fixtures

                    image::dot.gif[Dot]
                    """;
            var doc = documentFromString(input, Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertEquals("../..//fixtures/./../../fixtures", get(doc.getAttributes(), "imagesdir"));
            var output = doc.convert();
            assertXpath("//img[@src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=\"][@alt=\"Dot\"]", output, 1);
            assertMessage(logger, "WARN", "image has illegal reference to ancestor of jail; recovering automatically");
        }

        @Test
        @DisplayName("cleans reference to ancestor directories in target before reading image if safe mode level is at least SAFE")
        void cleansReferenceToAncestorDirectoriesInTargetBeforeReadingImageIfSafeModeLevelIsAtLeastSAFE() throws Exception {
            var input = """
                    :data-uri:
                    :imagesdir: ./

                    image::../..//fixtures/./../../fixtures/dot.gif[Dot]
                    """;
            var doc = documentFromString(input, Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertEquals("./", get(doc.getAttributes(), "imagesdir"));
            var output = doc.convert();
            assertXpath("//img[@src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=\"][@alt=\"Dot\"]", output, 1);
            assertMessage(logger, "WARN", "image has illegal reference to ancestor of jail; recovering automatically");
        }

        @Test
        @DisplayName("should use the imagesdir attribute set on the node when resolving the image path")
        void shouldUseTheImagesdirAttributeSetOnTheNodeWhenResolvingTheImagePath() throws Exception {
            var image = blockFromString("image::rainbow.png[]", Options.of("attributes", Options.of("imagesdir", "images")));
            image.setAttribute("imagesdir", "chapter-1/images");
            var imageUri = image.imageUri(image.getStringAttribute("target"));
            assertEquals("chapter-1/images/rainbow.png", imageUri);
        }

        @Test
        @DisplayName("should use the imagesdir attribute defined on image macro when resolving image path")
        void shouldUseTheImagesdirAttributeDefinedOnImageMacroWhenResolvingImagePath() throws Exception {
            var input = """
                    :imagesdir: images

                    image::rainbow.png[imagesdir=chapter-1/images]
                    '''
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "src=\"chapter-1/images/rainbow.png\"");
        }
    }

    @Nested
    @DisplayName("Media")
    class Media {

        @Test
        @DisplayName("should detect and convert video macro")
        void shouldDetectAndConvertVideoMacro() throws Exception {
            var input = "video::cats-vs-dogs.avi[]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 1);
            assertCss("video[src=\"cats-vs-dogs.avi\"]", output, 1);
        }

        @Test
        @DisplayName("should detect and convert video macro with positional attributes for poster and dimensions")
        void shouldDetectAndConvertVideoMacroWithPositionalAttributesForPosterAndDimensions() throws Exception {
            var input = "video::cats-vs-dogs.avi[cats-and-dogs.png, 200, 300]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 1);
            assertCss("video[src=\"cats-vs-dogs.avi\"]", output, 1);
            assertCss("video[poster=\"cats-and-dogs.png\"]", output, 1);
            assertCss("video[width=\"200\"]", output, 1);
            assertCss("video[height=\"300\"]", output, 1);
        }

        @Test
        @DisplayName("should set direction CSS class on video block if float attribute is set")
        void shouldSetDirectionCSSClassOnVideoBlockIfFloatAttributeIsSet() throws Exception {
            var input = "video::cats-vs-dogs.avi[cats-and-dogs.png,float=right]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 1);
            assertCss("video[src=\"cats-vs-dogs.avi\"]", output, 1);
            assertCss(".videoblock.right", output, 1);
        }

        @Test
        @DisplayName("should set text alignment CSS class on video block if align attribute is set")
        void shouldSetTextAlignmentCSSClassOnVideoBlockIfAlignAttributeIsSet() throws Exception {
            var input = "video::cats-vs-dogs.avi[cats-and-dogs.png,align=center]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 1);
            assertCss("video[src=\"cats-vs-dogs.avi\"]", output, 1);
            assertCss(".videoblock.text-center", output, 1);
        }

        @Test
        @DisplayName("video macro should honor all options")
        void videoMacroShouldHonorAllOptions() throws Exception {
            var input = "video::cats-vs-dogs.avi[options=\"autoplay,muted,nocontrols,loop\",preload=\"metadata\"]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 1);
            assertCss("video[autoplay]", output, 1);
            assertCss("video[muted]", output, 1);
            assertCss("video:not([controls])", output, 1);
            assertCss("video[loop]", output, 1);
            assertCss("video[preload=metadata]", output, 1);
        }

        @Test
        @DisplayName("video macro should add time range anchor with start time if start attribute is set")
        void videoMacroShouldAddTimeRangeAnchorWithStartTimeIfStartAttributeIsSet() throws Exception {
            var input = "video::cats-vs-dogs.avi[start=\"30\"]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 1);
            assertXpath("//video[@src=\"cats-vs-dogs.avi#t=30\"]", output, 1);
        }

        @Test
        @DisplayName("video macro should add time range anchor with end time if end attribute is set")
        void videoMacroShouldAddTimeRangeAnchorWithEndTimeIfEndAttributeIsSet() throws Exception {
            var input = "video::cats-vs-dogs.avi[end=\"30\"]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 1);
            assertXpath("//video[@src=\"cats-vs-dogs.avi#t=,30\"]", output, 1);
        }

        @Test
        @DisplayName("video macro should add time range anchor with start and end time if start and end attributes are set")
        void videoMacroShouldAddTimeRangeAnchorWithStartAndEndTimeIfStartAndEndAttributesAreSet() throws Exception {
            var input = "video::cats-vs-dogs.avi[start=\"30\",end=\"60\"]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 1);
            assertXpath("//video[@src=\"cats-vs-dogs.avi#t=30,60\"]", output, 1);
        }

        @Test
        @DisplayName("video macro should use imagesdir attribute to resolve target and poster")
        void videoMacroShouldUseImagesdirAttributeToResolveTargetAndPoster() throws Exception {
            var input = """
                    :imagesdir: assets

                    video::cats-vs-dogs.avi[cats-and-dogs.png, 200, 300]
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 1);
            assertCss("video[src=\"assets/cats-vs-dogs.avi\"]", output, 1);
            assertCss("video[poster=\"assets/cats-and-dogs.png\"]", output, 1);
            assertCss("video[width=\"200\"]", output, 1);
            assertCss("video[height=\"300\"]", output, 1);
        }

        @Test
        @DisplayName("video macro should not use imagesdir attribute to resolve target if target is a URL")
        void videoMacroShouldNotUseImagesdirAttributeToResolveTargetIfTargetIsAURL() throws Exception {
            var input = """
                    :imagesdir: assets

                    video::http://example.org/videos/cats-vs-dogs.avi[]
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 1);
            assertCss("video[src=\"http://example.org/videos/cats-vs-dogs.avi\"]", output, 1);
        }

        @Test
        @DisplayName("video macro should output custom HTML with iframe for vimeo service")
        void videoMacroShouldOutputCustomHTMLWithIframeForVimeoService() throws Exception {
            var input = "video::67480300[vimeo, 400, 300, start=60, options=\"autoplay,muted\"]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 0);
            assertCss("iframe", output, 1);
            assertCss("iframe[src=\"https://player.vimeo.com/video/67480300?autoplay=1&muted=1#at=60\"]", output, 1);
            assertCss("iframe[width=\"400\"]", output, 1);
            assertCss("iframe[height=\"300\"]", output, 1);
        }

        @Test
        @DisplayName("video macro should allow hash for vimeo video to be specified in video ID")
        void videoMacroShouldAllowHashForVimeoVideoToBeSpecifiedInVideoID() throws Exception {
            var input = "video::67480300/123456789[vimeo, 400, 300, options=loop]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 0);
            assertCss("iframe", output, 1);
            assertCss("iframe[src=\"https://player.vimeo.com/video/67480300?h=123456789&loop=1\"]", output, 1);
            assertCss("iframe[width=\"400\"]", output, 1);
            assertCss("iframe[height=\"300\"]", output, 1);
        }

        @Test
        @DisplayName("video macro should allow hash for vimeo video to be specified using hash attribute")
        void videoMacroShouldAllowHashForVimeoVideoToBeSpecifiedUsingHashAttribute() throws Exception {
            var input = "video::67480300[vimeo, 400, 300, options=loop, hash=123456789]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 0);
            assertCss("iframe", output, 1);
            assertCss("iframe[src=\"https://player.vimeo.com/video/67480300?h=123456789&loop=1\"]", output, 1);
            assertCss("iframe[width=\"400\"]", output, 1);
            assertCss("iframe[height=\"300\"]", output, 1);
        }

        @Test
        @DisplayName("video macro should output custom HTML with iframe for youtube service")
        void videoMacroShouldOutputCustomHTMLWithIframeForYoutubeService() throws Exception {
            var input = "video::U8GBXvdmHT4/PLg7s6cbtAD15Das5LK9mXt_g59DLWxKUe[youtube, 640, 360, start=60, options=\"autoplay,muted,modest\", theme=light]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 0);
            assertCss("iframe", output, 1);
            assertCss("iframe[src=\"https://www.youtube.com/embed/U8GBXvdmHT4?rel=0&start=60&autoplay=1&mute=1&list=PLg7s6cbtAD15Das5LK9mXt_g59DLWxKUe&modestbranding=1&theme=light\"]", output, 1);
            assertCss("iframe[width=\"640\"]", output, 1);
            assertCss("iframe[height=\"360\"]", output, 1);
        }

        @Test
        @DisplayName("video macro should output custom HTML with iframe for youtube service with dynamic playlist")
        void videoMacroShouldOutputCustomHTMLWithIframeForYoutubeServiceWithDynamicPlaylist() throws Exception {
            var input = "video::SCZF6I-Rc4I,AsKGOeonbIs,HwrPhOp6-aM[youtube, 640, 360, start=60, options=autoplay]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 0);
            assertCss("iframe", output, 1);
            assertCss("iframe[src=\"https://www.youtube.com/embed/SCZF6I-Rc4I?rel=0&start=60&autoplay=1&playlist=SCZF6I-Rc4I,AsKGOeonbIs,HwrPhOp6-aM\"]", output, 1);
            assertCss("iframe[width=\"640\"]", output, 1);
            assertCss("iframe[height=\"360\"]", output, 1);
        }

        @Test
        @DisplayName("video macro should output custom HTML with iframe for wistia service")
        void videoMacroShouldOutputCustomHTMLWithIframeForWistiaService() throws Exception {
            var input = "video::be5gtsbaco[wistia,640,360,start=60,options=\"autoplay,loop,muted\"]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 0);
            assertCss("iframe", output, 1);
            assertCss("iframe[src=\"https://fast.wistia.com/embed/iframe/be5gtsbaco?time=60&autoPlay=true&endVideoBehavior=loop&muted=true\"]", output, 1);
            assertCss("iframe[width=\"640\"]", output, 1);
            assertCss("iframe[height=\"360\"]", output, 1);
        }

        @Test
        @DisplayName("video macro should output custom HTML with iframe for wistia service with loop behavior set")
        void videoMacroShouldOutputCustomHTMLWithIframeForWistiaServiceWithLoopBehaviorSet() throws Exception {
            var input = "video::be5gtsbaco[wistia,640,360,start=60,options=\"autoplay,reset,muted\"]";
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 0);
            assertCss("iframe", output, 1);
            assertCss("iframe[src=\"https://fast.wistia.com/embed/iframe/be5gtsbaco?time=60&autoPlay=true&endVideoBehavior=reset&muted=true\"]", output, 1);
            assertCss("iframe[width=\"640\"]", output, 1);
            assertCss("iframe[height=\"360\"]", output, 1);
        }

        @Test
        @DisplayName("should detect and convert audio macro")
        void shouldDetectAndConvertAudioMacro() throws Exception {
            var input = "audio::podcast.mp3[]";
            var output = convertStringToEmbedded(input);
            assertCss("audio", output, 1);
            assertCss("audio[src=\"podcast.mp3\"]", output, 1);
        }

        @Test
        @DisplayName("audio macro should use imagesdir attribute to resolve target")
        void audioMacroShouldUseImagesdirAttributeToResolveTarget() throws Exception {
            var input = """
                    :imagesdir: assets

                    audio::podcast.mp3[]
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("audio", output, 1);
            assertCss("audio[src=\"assets/podcast.mp3\"]", output, 1);
        }

        @Test
        @DisplayName("audio macro should not use imagesdir attribute to resolve target if target is a URL")
        void audioMacroShouldNotUseImagesdirAttributeToResolveTargetIfTargetIsAURL() throws Exception {
            var input = """
                    :imagesdir: assets

                    video::http://example.org/podcast.mp3[]
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("video", output, 1);
            assertCss("video[src=\"http://example.org/podcast.mp3\"]", output, 1);
        }

        @Test
        @DisplayName("audio macro should honor all options")
        void audioMacroShouldHonorAllOptions() throws Exception {
            var input = "audio::podcast.mp3[options=\"autoplay,nocontrols,loop\"]";
            var output = convertStringToEmbedded(input);
            assertCss("audio", output, 1);
            assertCss("audio[autoplay]", output, 1);
            assertCss("audio:not([controls])", output, 1);
            assertCss("audio[loop]", output, 1);
        }

        @Test
        @DisplayName("audio macro should support start and end time")
        void audioMacroShouldSupportStartAndEndTime() throws Exception {
            var input = "audio::podcast.mp3[start=1,end=2]";
            var output = convertStringToEmbedded(input);
            assertCss("audio", output, 1);
            assertCss("audio[controls]", output, 1);
            assertCss("audio[src=\"podcast.mp3#t=1,2\"]", output, 1);
        }

        @Test
        @DisplayName("should use the imagesdir attribute on the node when resolving the video path")
        void shouldUseTheImagesdirAttributeOnTheNodeWhenResolvingTheVideoPath() throws Exception {
            var video = blockFromString("video::promo.mp4[]", Options.of("attributes", Options.of("imagesdir", "images")));
            video.setAttribute("imagesdir", "chapter-1/videos");
            var videoUri = video.mediaUri(video.getStringAttribute("target"));
            assertEquals("chapter-1/videos/promo.mp4", videoUri);
        }

        @Test
        @DisplayName("should use the imagesdir attribute defined on video macro when resolving image path")
        void shouldUseTheImagesdirAttributeDefinedOnVideoMacroWhenResolvingImagePath() throws Exception {
            var input = """
                    :imagesdir: images

                    video::promo.mp4[imagesdir=chapter-1/videos]
                    '''
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "src=\"chapter-1/videos/promo.mp4\"");
        }
    }

    @Nested
    @DisplayName("Admonition icons")
    class AdmonitionIcons {

        @Test
        @DisplayName("can resolve icon relative to default iconsdir")
        void canResolveIconRelativeToDefaultIconsdir() throws Exception {
            var input = """
                    :icons:

                    [TIP]
                    You can use icons for admonitions by setting the 'icons' attribute.
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SERVER.getLevel()));
            assertXpath("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/img[@src=\"./images/icons/tip.png\"][@alt=\"Tip\"]", output, 1);
        }

        @Test
        @DisplayName("can resolve icon relative to custom iconsdir")
        void canResolveIconRelativeToCustomIconsdir() throws Exception {
            var input = """
                    :icons:
                    :iconsdir: icons

                    [TIP]
                    You can use icons for admonitions by setting the 'icons' attribute.
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SERVER.getLevel()));
            assertXpath("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/img[@src=\"icons/tip.png\"][@alt=\"Tip\"]", output, 1);
        }

        @Test
        @DisplayName("should use iconsdir attribute set on admonition block in document to resolve icon path")
        void shouldUseIconsdirAttributeSetOnAdmonitionBlockInDocumentToResolveIconPath() throws Exception {
            var input = """
                    :icons:

                    [TIP,iconsdir=icons]
                    You can use icons for admonitions by setting the 'icons' attribute.
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SERVER.getLevel()));
            assertXpath("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/img[@src=\"icons/tip.png\"][@alt=\"Tip\"]", output, 1);
        }

        @Test
        @DisplayName("should add file extension to custom icon if not specified")
        void shouldAddFileExtensionToCustomIconIfNotSpecified() throws Exception {
            var input = """
                    :icons: font
                    :iconsdir: images/icons

                    [TIP,icon=a]
                    Override the icon of an admonition block using an attribute
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SERVER.getLevel()));
            assertXpath("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/img[@src=\"images/icons/a.png\"]", output, 1);
        }

        @Test
        @DisplayName("should allow icontype to be specified when using built-in admonition icon")
        void shouldAllowIcontypeToBeSpecifiedWhenUsingBuiltInAdmonitionIcon() throws Exception {
            var input = "TIP: Set the icontype using either the icontype attribute on the icons attribute.";
            for (var attributes : list(Options.of("icons", "", "ext", "png"), Options.of("icons", "", "icontype", "jpg", "ext", "jpg"), Options.of("icons", "jpg", "ext", "jpg"), Options.of("icons", "image", "ext", "png"))) {
                var expectedSrc = ("./images/icons/tip." + str(attributes.remove("ext")));
                var output = convertString(input, Options.of("attributes", attributes));
                assertXpath(("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/img[@src=\"" + str(expectedSrc) + "\"]"), output, 1);
            }
        }

        @Test
        @DisplayName("should allow icontype to be specified when using custom admonition icon")
        void shouldAllowIcontypeToBeSpecifiedWhenUsingCustomAdmonitionIcon() throws Exception {
            var input = """
                    [TIP,icon=hint]
                    Set the icontype using either the icontype attribute on the icons attribute.
                    """;
            for (var attributes : list(Options.of("icons", "", "ext", "png"), Options.of("icons", "", "icontype", "jpg", "ext", "jpg"), Options.of("icons", "jpg", "ext", "jpg"), Options.of("icons", "image", "ext", "png"))) {
                var expectedSrc = ("./images/icons/hint." + str(attributes.remove("ext")));
                var output = convertString(input, Options.of("attributes", attributes));
                assertXpath(("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/img[@src=\"" + str(expectedSrc) + "\"]"), output, 1);
            }
        }

        @Test
        @DisplayName("embeds base64-encoded data uri of icon when data-uri attribute is set and safe mode level is less than SECURE")
        void embedsBase64EncodedDataUriOfIconWhenDataUriAttributeIsSetAndSafeModeLevelIsLessThanSECURE() throws Exception {
            var input = """
                    :icons:
                    :iconsdir: fixtures
                    :icontype: gif
                    :data-uri:

                    [TIP]
                    You can use icons for admonitions by setting the 'icons' attribute.
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertXpath("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/img[@src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=\"][@alt=\"Tip\"]", output, 1);
        }

        @Test
        @DisplayName("should embed base64-encoded data uri of custom icon when data-uri attribute is set")
        void shouldEmbedBase64EncodedDataUriOfCustomIconWhenDataUriAttributeIsSet() throws Exception {
            var input = """
                    :icons:
                    :iconsdir: fixtures
                    :icontype: gif
                    :data-uri:

                    [TIP,icon=tip]
                    You can set a custom icon using the icon attribute on the block.
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertXpath("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/img[@src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=\"][@alt=\"Tip\"]", output, 1);
        }

        @Test
        @DisplayName("does not embed base64-encoded data uri of icon when safe mode level is SECURE or greater")
        void doesNotEmbedBase64EncodedDataUriOfIconWhenSafeModeLevelIsSECUREOrGreater() throws Exception {
            var input = """
                    :icons:
                    :iconsdir: fixtures
                    :icontype: gif
                    :data-uri:

                    [TIP]
                    You can use icons for admonitions by setting the 'icons' attribute.
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("icons", "")));
            assertXpath("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/img[@src=\"fixtures/tip.gif\"][@alt=\"Tip\"]", output, 1);
        }

        @Test
        @DisplayName("cleans reference to ancestor directories before reading icon if safe mode level is at least SAFE")
        void cleansReferenceToAncestorDirectoriesBeforeReadingIconIfSafeModeLevelIsAtLeastSAFE() throws Exception {
            var input = """
                    :icons:
                    :iconsdir: ../fixtures
                    :icontype: gif
                    :data-uri:

                    [TIP]
                    You can use icons for admonitions by setting the 'icons' attribute.
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("docdir", testdir())));
            assertXpath("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/img[@src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=\"][@alt=\"Tip\"]", output, 1);
            assertMessage(logger, "WARN", "image has illegal reference to ancestor of jail; recovering automatically");
        }

        @Test
        @DisplayName("should import Font Awesome and use font-based icons when value of icons attribute is font")
        void shouldImportFontAwesomeAndUseFontBasedIconsWhenValueOfIconsAttributeIsFont() throws Exception {
            var input = """
                    :icons: font

                    [TIP]
                    You can use icons for admonitions by setting the 'icons' attribute.
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SERVER.getLevel()));
            assertCss(("html > head > link[rel=\"stylesheet\"][href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/" + str(Asciidoctor.FONT_AWESOME_VERSION) + "/css/font-awesome.min.css\"]"), output, 1);
            assertXpath("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/i[@class=\"fa icon-tip\"]", output, 1);
        }

        @Test
        @DisplayName("font-based icon should not override icon specified on admonition")
        void fontBasedIconShouldNotOverrideIconSpecifiedOnAdmonition() throws Exception {
            var input = """
                    :icons: font
                    :iconsdir: images/icons

                    [TIP,icon=a.png]
                    Override the icon of an admonition block using an attribute
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SERVER.getLevel()));
            assertXpath("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/i[@class=\"fa icon-tip\"]", output, 0);
            assertXpath("//*[@class=\"admonitionblock tip\"]//*[@class=\"icon\"]/img[@src=\"images/icons/a.png\"]", output, 1);
        }

        @Test
        @DisplayName("should use http uri scheme for assets when asset-uri-scheme is http")
        void shouldUseHttpUriSchemeForAssetsWhenAssetUriSchemeIsHttp() throws Exception {
            var input = """
                    :asset-uri-scheme: http
                    :icons: font
                    :source-highlighter: highlightjs

                    TIP: You can control the URI scheme used for assets with the asset-uri-scheme attribute

                    [source,ruby]
                    puts "AsciiDoc, FTW!"
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel()));
            assertCss(("html > head > link[rel=\"stylesheet\"][href=\"http://cdnjs.cloudflare.com/ajax/libs/font-awesome/" + str(Asciidoctor.FONT_AWESOME_VERSION) + "/css/font-awesome.min.css\"]"), output, 1);
            assertCss(("html > body > script[src=\"http://cdnjs.cloudflare.com/ajax/libs/highlight.js/" + str(Asciidoctor.HIGHLIGHT_JS_VERSION) + "/highlight.min.js\"]"), output, 1);
        }

        @Test
        @DisplayName("should use no uri scheme for assets when asset-uri-scheme is blank")
        void shouldUseNoUriSchemeForAssetsWhenAssetUriSchemeIsBlank() throws Exception {
            var input = """
                    :asset-uri-scheme:
                    :icons: font
                    :source-highlighter: highlightjs

                    TIP: You can control the URI scheme used for assets with the asset-uri-scheme attribute

                    [source,ruby]
                    puts "AsciiDoc, FTW!"
                    """;
            var output = convertString(input, Options.of("safe", SafeMode.SAFE.getLevel()));
            assertCss(("html > head > link[rel=\"stylesheet\"][href=\"//cdnjs.cloudflare.com/ajax/libs/font-awesome/" + str(Asciidoctor.FONT_AWESOME_VERSION) + "/css/font-awesome.min.css\"]"), output, 1);
            assertCss(("html > body > script[src=\"//cdnjs.cloudflare.com/ajax/libs/highlight.js/" + str(Asciidoctor.HIGHLIGHT_JS_VERSION) + "/highlight.min.js\"]"), output, 1);
        }
    }

    @Nested
    @DisplayName("Image paths")
    class ImagePaths {

        @Test
        @DisplayName("restricts access to ancestor directories when safe mode level is at least SAFE")
        void restrictsAccessToAncestorDirectoriesWhenSafeModeLevelIsAtLeastSAFE() throws Exception {
            var input = "image::asciidoctor.png[Asciidoctor]";
            var basedir = testdir();
            var block = blockFromString(input, Options.of("attributes", Options.of("docdir", basedir)));
            var doc = block.getDocument();
            assertTruthy((doc.getSafe() >= SafeMode.SAFE.getLevel()));
            assertEquals(joinPath(basedir, "images"), block.normalizeAssetPath("images"));
            assertEquals(joinPath(basedir, "etc/images"), block.normalizeAssetPath((str(diskRoot()) + "etc/images")));
            assertEquals(joinPath(basedir, "images"), block.normalizeAssetPath("../../images"));
        }

        @Test
        @DisplayName("does not restrict access to ancestor directories when safe mode is disabled")
        void doesNotRestrictAccessToAncestorDirectoriesWhenSafeModeIsDisabled() throws Exception {
            var input = "image::asciidoctor.png[Asciidoctor]";
            var basedir = testdir();
            var block = blockFromString(input, Options.of("safe", SafeMode.UNSAFE.getLevel(), "attributes", Options.of("docdir", basedir)));
            var doc = block.getDocument();
            assertEquals(SafeMode.UNSAFE.getLevel(), doc.getSafe());
            assertEquals(joinPath(basedir, "images"), block.normalizeAssetPath("images"));
            var absolutePath = (str(diskRoot()) + "etc/images");
            assertEquals(absolutePath, block.normalizeAssetPath(absolutePath));
            assertEquals(fileExpandPath(joinPath(basedir, "../../images")), block.normalizeAssetPath("../../images"));
        }
    }

    @Nested
    @DisplayName("Source code")
    class SourceCode {

        @Test
        @DisplayName("should support fenced code block using backticks")
        void shouldSupportFencedCodeBlockUsingBackticks() throws Exception {
            var input = """
                    ```
                    puts "Hello, World!"
                    ```
                    """;
            var block = blockFromString(input);
            assertEquals("listing", block.getContext());
            assertEquals("source", block.getAttribute("style"));
            assertEquals("fenced_code", block.getAttribute("cloaked-context"));
            assertNull(block.getAttribute("language"));
            var output = convertStringToEmbedded(input);
            assertCss(".listingblock", output, 1);
            assertCss(".listingblock pre code", output, 1);
            assertCss(".listingblock pre code:not([class])", output, 1);
        }

        @Test
        @DisplayName("should not recognize fenced code blocks with more than three delimiters")
        void shouldNotRecognizeFencedCodeBlocksWithMoreThanThreeDelimiters() throws Exception {
            var input = """
                    ````ruby
                    puts "Hello, World!"
                    ````

                    ~~~~ javascript
                    alert("Hello, World!")
                    ~~~~
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".listingblock", output, 0);
        }

        @Test
        @DisplayName("should support fenced code blocks with languages")
        void shouldSupportFencedCodeBlocksWithLanguages() throws Exception {
            var input = """
                    ```ruby
                    puts "Hello, World!"
                    ```

                    ``` javascript
                    alert("Hello, World!")
                    ```
                    """;
            var block = documentFromString(input).getBlocks().get(0);
            assertEquals("listing", block.getContext());
            assertEquals("source", block.getAttribute("style"));
            assertEquals("fenced_code", block.getAttribute("cloaked-context"));
            assertEquals("ruby", block.getAttribute("language"));
            var output = convertStringToEmbedded(input);
            assertCss(".listingblock", output, 2);
            assertCss(".listingblock pre code.language-ruby[data-lang=ruby]", output, 1);
            assertCss(".listingblock pre code.language-javascript[data-lang=javascript]", output, 1);
        }

        @Test
        @DisplayName("should support fenced code blocks with languages and numbering")
        void shouldSupportFencedCodeBlocksWithLanguagesAndNumbering() throws Exception {
            var input = """
                    ```ruby,numbered
                    puts "Hello, World!"
                    ```

                    ``` javascript, numbered
                    alert("Hello, World!")
                    ```
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".listingblock", output, 2);
            assertCss(".listingblock pre code.language-ruby[data-lang=ruby]", output, 1);
            assertCss(".listingblock pre code.language-javascript[data-lang=javascript]", output, 1);
        }

        @Test
        @DisplayName("should allow source style to be specified on literal block")
        void shouldAllowSourceStyleToBeSpecifiedOnLiteralBlock() throws Exception {
            var input = """
                    [source]
                    ....
                    console.log('Hello, World!')
                    ....
                    """;
            var block = blockFromString(input);
            assertEquals("listing", block.getContext());
            assertEquals("source", block.getAttribute("style"));
            assertEquals("literal", block.getAttribute("cloaked-context"));
            assertNull(block.getAttribute("language"));
            var output = convertStringToEmbedded(input);
            assertCss(".listingblock", output, 1);
            assertCss(".listingblock pre", output, 1);
            assertCss(".listingblock pre code", output, 1);
            assertCss(".listingblock pre code[data-lang]", output, 0);
        }

        @Test
        @DisplayName("should allow source style and language to be specified on literal block")
        void shouldAllowSourceStyleAndLanguageToBeSpecifiedOnLiteralBlock() throws Exception {
            var input = """
                    [source,js]
                    ....
                    console.log('Hello, World!')
                    ....
                    """;
            var block = blockFromString(input);
            assertEquals("listing", block.getContext());
            assertEquals("source", block.getAttribute("style"));
            assertEquals("literal", block.getAttribute("cloaked-context"));
            assertEquals("js", block.getAttribute("language"));
            var output = convertStringToEmbedded(input);
            assertCss(".listingblock", output, 1);
            assertCss(".listingblock pre", output, 1);
            assertCss(".listingblock pre code", output, 1);
            assertCss(".listingblock pre code[data-lang]", output, 1);
        }
    }

    @Nested
    @DisplayName("Abstract and Part Intro")
    class AbstractAndPartIntro {

        @Test
        @DisplayName("should make abstract on open block without title a quote block for article")
        void shouldMakeAbstractOnOpenBlockWithoutTitleAQuoteBlockForArticle() throws Exception {
            var input = """
                    = Article

                    [abstract]
                    --
                    This article is about stuff.

                    And other stuff.
                    --

                    == Section One

                    content
                    """;
            var output = convertString(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock.abstract", output, 1);
            assertCss("#preamble .quoteblock", output, 1);
            assertCss(".quoteblock > blockquote", output, 1);
            assertCss(".quoteblock > blockquote > .paragraph", output, 2);
        }

        @Test
        @DisplayName("should make abstract on open block with title a quote block with title for article")
        void shouldMakeAbstractOnOpenBlockWithTitleAQuoteBlockWithTitleForArticle() throws Exception {
            var input = """
                    = Article

                    .My abstract
                    [abstract]
                    --
                    This article is about stuff.
                    --

                    == Section One

                    content
                    """;
            var output = convertString(input);
            assertCss(".quoteblock", output, 1);
            assertCss(".quoteblock.abstract", output, 1);
            assertCss("#preamble .quoteblock", output, 1);
            assertCss(".quoteblock > .title", output, 1);
            assertCss(".quoteblock > .title + blockquote", output, 1);
            assertCss(".quoteblock > .title + blockquote > .paragraph", output, 1);
        }

        @Test
        @DisplayName("should allow abstract in document with title if doctype is book")
        void shouldAllowAbstractInDocumentWithTitleIfDoctypeIsBook() throws Exception {
            var input = """
                    = Book
                    :doctype: book

                    [abstract]
                    Abstract for book with title is valid
                    """;
            var output = convertString(input);
            assertCss(".abstract", output, 1);
        }

        @Test
        @DisplayName("should not allow abstract as direct child of document if doctype is book")
        void shouldNotAllowAbstractAsDirectChildOfDocumentIfDoctypeIsBook() throws Exception {
            var input = """
                    :doctype: book

                    [abstract]
                    Abstract for book without title is invalid.
                    """;
            var output = convertString(input);
            assertCss(".abstract", output, 0);
            assertMessage(logger, "WARN", "abstract block cannot be used in a document without a doctitle when doctype is book. Excluding block content.");
        }

        @Test
        @DisplayName("should make abstract on open block without title converted to DocBook")
        void shouldMakeAbstractOnOpenBlockWithoutTitleConvertedToDocBook() throws Exception {
            var input = """
                    = Article

                    [abstract]
                    --
                    This article is about stuff.

                    And other stuff.
                    --
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("info > abstract", output, 1);
            assertCss("info > abstract > simpara", output, 2);
        }

        @Test
        @DisplayName("should make abstract on open block with title converted to DocBook")
        void shouldMakeAbstractOnOpenBlockWithTitleConvertedToDocBook() throws Exception {
            var input = """
                    = Article

                    .My abstract
                    [abstract]
                    --
                    This article is about stuff.
                    --
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("info > abstract", output, 1);
            assertCss("info > abstract > title", output, 1);
            assertCss("info > abstract > title + simpara", output, 1);
        }

        @Test
        @DisplayName("should allow abstract in document with title if doctype is book converted to DocBook")
        void shouldAllowAbstractInDocumentWithTitleIfDoctypeIsBookConvertedToDocBook() throws Exception {
            var input = """
                    = Book
                    :doctype: book

                    [abstract]
                    Abstract for book with title is valid
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("info > abstract", output, 1);
            assertCss("preface", output, 0);
        }

        @Test
        @DisplayName("should not allow abstract as direct child of document if doctype is book converted to DocBook")
        void shouldNotAllowAbstractAsDirectChildOfDocumentIfDoctypeIsBookConvertedToDocBook() throws Exception {
            var input = """
                    :doctype: book

                    [abstract]
                    Abstract for book is invalid.
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("abstract", output, 0);
            assertMessage(logger, "WARN", "abstract block cannot be used in a document without a doctitle when doctype is book. Excluding block content.");
        }

        @Test
        @DisplayName("should accept partintro on open block without title")
        void shouldAcceptPartintroOnOpenBlockWithoutTitle() throws Exception {
            var input = """
                    = Book
                    :doctype: book

                    = Part 1

                    [partintro]
                    --
                    This is a part intro.

                    It can have multiple paragraphs.
                    --

                    == Chapter 1

                    content
                    """;
            var output = convertString(input);
            assertCss(".openblock", output, 1);
            assertCss(".openblock.partintro", output, 1);
            assertCss(".openblock .title", output, 0);
            assertCss(".openblock .content", output, 1);
            assertXpath(("//h1[@id=\"_part_1\"]/following-sibling::*[" + str(containsClass("openblock")) + "]"), output, 1);
            assertXpath(("//*[" + str(containsClass("openblock")) + "]/*[@class=\"content\"]/*[@class=\"paragraph\"]"), output, 2);
        }

        @Test
        @DisplayName("should accept partintro on open block with title")
        void shouldAcceptPartintroOnOpenBlockWithTitle() throws Exception {
            var input = """
                    = Book
                    :doctype: book

                    = Part 1

                    .Intro title
                    [partintro]
                    --
                    This is a part intro with a title.
                    --

                    == Chapter 1

                    content
                    """;
            var output = convertString(input);
            assertCss(".openblock", output, 1);
            assertCss(".openblock.partintro", output, 1);
            assertCss(".openblock .title", output, 1);
            assertCss(".openblock .content", output, 1);
            assertXpath(("//h1[@id=\"_part_1\"]/following-sibling::*[" + str(containsClass("openblock")) + "]"), output, 1);
            assertXpath(("//*[" + str(containsClass("openblock")) + "]/*[@class=\"title\"][text()=\"Intro title\"]"), output, 1);
            assertXpath(("//*[" + str(containsClass("openblock")) + "]/*[@class=\"content\"]/*[@class=\"paragraph\"]"), output, 1);
        }

        @Test
        @DisplayName("should exclude partintro if not a child of part")
        void shouldExcludePartintroIfNotAChildOfPart() throws Exception {
            var input = """
                    = Book
                    :doctype: book

                    [partintro]
                    part intro paragraph
                    """;
            var output = convertString(input);
            assertCss(".partintro", output, 0);
            assertMessage(logger, "ERROR", "partintro block can only be used when doctype is book and must be a child of a book part. Excluding block content.");
        }

        @Test
        @DisplayName("should not allow partintro unless doctype is book")
        void shouldNotAllowPartintroUnlessDoctypeIsBook() throws Exception {
            var input = """
                    [partintro]
                    part intro paragraph
                    """;
            var output = convertString(input);
            assertCss(".partintro", output, 0);
            assertMessage(logger, "ERROR", "partintro block can only be used when doctype is book and must be a child of a book part. Excluding block content.");
        }

        @Test
        @DisplayName("should accept partintro on open block without title converted to DocBook")
        void shouldAcceptPartintroOnOpenBlockWithoutTitleConvertedToDocBook() throws Exception {
            var input = """
                    = Book
                    :doctype: book

                    = Part 1

                    [partintro]
                    --
                    This is a part intro.

                    It can have multiple paragraphs.
                    --

                    == Chapter 1

                    content
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("partintro", output, 1);
            assertCss("part[xml|id=\"_part_1\"] > partintro", output, 1);
            assertCss("partintro > simpara", output, 2);
        }

        @Test
        @DisplayName("should accept partintro on open block with title converted to DocBook")
        void shouldAcceptPartintroOnOpenBlockWithTitleConvertedToDocBook() throws Exception {
            var input = """
                    = Book
                    :doctype: book

                    = Part 1

                    .Intro title
                    [partintro]
                    --
                    This is a part intro with a title.
                    --

                    == Chapter 1

                    content
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("partintro", output, 1);
            assertCss("part[xml|id=\"_part_1\"] > partintro", output, 1);
            assertCss("partintro > title", output, 1);
            assertCss("partintro > title + simpara", output, 1);
        }

        @Test
        @DisplayName("should exclude partintro if not a child of part converted to DocBook")
        void shouldExcludePartintroIfNotAChildOfPartConvertedToDocBook() throws Exception {
            var input = """
                    = Book
                    :doctype: book

                    [partintro]
                    part intro paragraph
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("partintro", output, 0);
            assertMessage(logger, "ERROR", "partintro block can only be used when doctype is book and must be a child of a book part. Excluding block content.");
        }

        @Test
        @DisplayName("should not allow partintro unless doctype is book converted to DocBook")
        void shouldNotAllowPartintroUnlessDoctypeIsBookConvertedToDocBook() throws Exception {
            var input = """
                    [partintro]
                    part intro paragraph
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("partintro", output, 0);
            assertMessage(logger, "ERROR", "partintro block can only be used when doctype is book and must be a child of a book part. Excluding block content.");
        }
    }

    @Nested
    @DisplayName("Substitutions")
    class Substitutions {

        @Test
        @DisplayName("processor should not crash if subs are empty")
        void processorShouldNotCrashIfSubsAreEmpty() throws Exception {
            var input = """
                    [subs=","]
                    ....
                    content
                    ....
                    """;
            var doc = documentFromString(input);
            var block = first(doc.getBlocks());
            assertEmpty(block.getSubs());
        }

        @Test
        @DisplayName("should be able to append subs to default block substitution list")
        void shouldBeAbleToAppendSubsToDefaultBlockSubstitutionList() throws Exception {
            var input = """
                    :application: Asciidoctor

                    [subs="+attributes,+macros"]
                    ....
                    {application}
                    ....
                    """;
            var doc = documentFromString(input);
            var block = first(doc.getBlocks());
            assertEquals(list(Substitution.SPECIAL_CHARACTERS, Substitution.ATTRIBUTES, Substitution.MACROS), block.getSubs());
        }

        @Test
        @DisplayName("should be able to prepend subs to default block substitution list")
        void shouldBeAbleToPrependSubsToDefaultBlockSubstitutionList() throws Exception {
            var input = """
                    :application: Asciidoctor

                    [subs="attributes+"]
                    ....
                    {application}
                    ....
                    """;
            var doc = documentFromString(input);
            var block = first(doc.getBlocks());
            assertEquals(list(Substitution.ATTRIBUTES, Substitution.SPECIAL_CHARACTERS), block.getSubs());
        }

        @Test
        @DisplayName("should be able to remove subs to default block substitution list")
        void shouldBeAbleToRemoveSubsToDefaultBlockSubstitutionList() throws Exception {
            var input = """
                    [subs="-quotes,-replacements"]
                    content
                    """;
            var doc = documentFromString(input);
            var block = first(doc.getBlocks());
            assertEquals(list(Substitution.SPECIAL_CHARACTERS, Substitution.ATTRIBUTES, Substitution.MACROS, Substitution.POST_REPLACEMENTS), block.getSubs());
        }

        @Test
        @DisplayName("should be able to prepend, append and remove subs from default block substitution list")
        void shouldBeAbleToPrependAppendAndRemoveSubsFromDefaultBlockSubstitutionList() throws Exception {
            var input = """
                    :application: asciidoctor

                    [subs="attributes+,-verbatim,+specialcharacters,+macros"]
                    ....
                    https://{application}.org[{gt}{gt}] <1>
                    ....
                    """;
            var doc = documentFromString(input, Options.of("standalone", false));
            var block = first(doc.getBlocks());
            assertEquals(list(Substitution.ATTRIBUTES, Substitution.SPECIAL_CHARACTERS, Substitution.MACROS), block.getSubs());
            var result = doc.convert();
            assertIncludes(result, "<pre><a href=\"https://asciidoctor.org\">&gt;&gt;</a> &lt;1&gt;</pre>");
        }

        @Test
        @DisplayName("should be able to set subs then modify them")
        void shouldBeAbleToSetSubsThenModifyThem() throws Exception {
            var input = """
                    [subs="verbatim,-callouts"]
                    _hey now_ <1>
                    """;
            var doc = documentFromString(input, Options.of("standalone", false));
            var block = first(doc.getBlocks());
            assertEquals(list(Substitution.SPECIAL_CHARACTERS), block.getSubs());
            var result = doc.convert();
            assertIncludes(result, "_hey now_ &lt;1&gt;");
        }
    }

    @Nested
    @DisplayName("References")
    class References {

        @Test
        @DisplayName("should not recognize block anchor with illegal id characters")
        void shouldNotRecognizeBlockAnchorWithIllegalIdCharacters() throws Exception {
            var input = """
                    [[illegal$id,Reference Text]]
                    ----
                    content
                    ----
                    """;
            var doc = documentFromString(input);
            var block = first(doc.getBlocks());
            assertNull(block.getId());
            assertNull(block.getAttribute("reftext"));
            assertFalsy(doc.getCatalog().getRefs().containsKey("illegal$id"));
        }

        @Test
        @DisplayName("should not recognize block anchor that starts with digit")
        void shouldNotRecognizeBlockAnchorThatStartsWithDigit() throws Exception {
            var input = """
                    [[3-blind-mice]]
                    --
                    see how they run
                    --
                    """;
            var output = convertStringToEmbedded(input);
            assertIncludes(output, "[[3-blind-mice]]");
            assertXpath("/*[@id=\":3-blind-mice\"]", output, 0);
        }

        @Test
        @DisplayName("should recognize block anchor that starts with colon")
        void shouldRecognizeBlockAnchorThatStartsWithColon() throws Exception {
            var input = """
                    [[:idname]]
                    --
                    content
                    --
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/*[@id=\":idname\"]", output, 1);
        }

        @Test
        @DisplayName("should use specified id and reftext when registering block reference")
        void shouldUseSpecifiedIdAndReftextWhenRegisteringBlockReference() throws Exception {
            var input = """
                    [[debian,Debian Install]]
                    .Installation on Debian
                    ----
                    $ apt-get install asciidoctor
                    ----
                    """;
            var doc = documentFromString(input);
            var ref = (AbstractBlock) get(doc.getCatalog().getRefs(), "debian");
            assertNotNull(ref);
            assertEquals("Debian Install", ref.getReftext());
            assertEquals("debian", doc.resolveId("Debian Install"));
        }

        @Test
        @DisplayName("should allow square brackets in block reference text")
        void shouldAllowSquareBracketsInBlockReferenceText() throws Exception {
            var input = """
                    [[debian,[Debian] Install]]
                    .Installation on Debian
                    ----
                    $ apt-get install asciidoctor
                    ----
                    """;
            var doc = documentFromString(input);
            var ref = (AbstractBlock) get(doc.getCatalog().getRefs(), "debian");
            assertNotNull(ref);
            assertEquals("[Debian] Install", ref.getReftext());
            assertEquals("debian", doc.resolveId("[Debian] Install"));
        }

        @Test
        @DisplayName("should allow comma in block reference text")
        void shouldAllowCommaInBlockReferenceText() throws Exception {
            var input = """
                    [[debian, Debian, Ubuntu]]
                    .Installation on Debian
                    ----
                    $ apt-get install asciidoctor
                    ----
                    """;
            var doc = documentFromString(input);
            var ref = (AbstractBlock) get(doc.getCatalog().getRefs(), "debian");
            assertNotNull(ref);
            assertEquals("Debian, Ubuntu", ref.getReftext());
            assertEquals("debian", doc.resolveId("Debian, Ubuntu"));
        }

        @Test
        @DisplayName("should resolve attribute reference in title using attribute defined at location of block")
        void shouldResolveAttributeReferenceInTitleUsingAttributeDefinedAtLocationOfBlock() throws Exception {
            var input = """
                    = Document Title
                    :foo: baz

                    intro paragraph. see <<free-standing>>.

                    :foo: bar

                    .foo is {foo}
                    [#formal-para]
                    paragraph with title

                    [discrete#free-standing]
                    == foo is still {foo}
                    """;
            var doc = documentFromString(input);
            var ref = (AbstractBlock) get(doc.getCatalog().getRefs(), "formal-para");
            assertNotNull(ref);
            assertEquals("foo is bar", ref.getTitle());
            assertEquals("formal-para", doc.resolveId("foo is bar"));
            var output = doc.convert(Options.of("standalone", false));
            assertInclude("<a href=\"#free-standing\">foo is still bar</a>", output);
            assertInclude("<h2 id=\"free-standing\" class=\"discrete\">foo is still bar</h2>", output);
        }

        @Test
        @DisplayName("should substitute attribute references in reftext when registering block reference")
        void shouldSubstituteAttributeReferencesInReftextWhenRegisteringBlockReference() throws Exception {
            var input = """
                    :label-tiger: Tiger

                    [[tiger-evolution,Evolution of the {label-tiger}]]
                    ****
                    Information about the evolution of the tiger.
                    ****
                    """;
            var doc = documentFromString(input);
            var ref = (AbstractBlock) get(doc.getCatalog().getRefs(), "tiger-evolution");
            assertNotNull(ref);
            assertEquals("Evolution of the Tiger", get(ref.getAttributes(), "reftext"));
            assertEquals("tiger-evolution", doc.resolveId("Evolution of the Tiger"));
        }

        @Test
        @DisplayName("should use specified reftext when registering block reference")
        void shouldUseSpecifiedReftextWhenRegisteringBlockReference() throws Exception {
            var input = """
                    [[debian]]
                    [reftext="Debian Install"]
                    .Installation on Debian
                    ----
                    $ apt-get install asciidoctor
                    ----
                    """;
            var doc = documentFromString(input);
            var ref = (AbstractBlock) get(doc.getCatalog().getRefs(), "debian");
            assertNotNull(ref);
            assertEquals("Debian Install", ref.getReftext());
            assertEquals("debian", doc.resolveId("Debian Install"));
        }
    }
}
