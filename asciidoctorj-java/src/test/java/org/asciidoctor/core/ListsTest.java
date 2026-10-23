package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

public class ListsTest extends TestHelper {

    @Nested
    @DisplayName("Bulleted lists (:ulist)")
    class BulletedListsUlist {

        @Nested
        @DisplayName("Simple lists")
        class SimpleLists {

            @Test
            @DisplayName("dash elements with no blank lines")
            void dashElementsWithNoBlankLines() throws Exception {
                var input = """
                        List
                        ====

                        - Foo
                        - Boo
                        - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
            }

            @Test
            @DisplayName("indented dash elements using spaces")
            void indentedDashElementsUsingSpaces() throws Exception {
                var input = """
                         - Foo
                         - Boo
                         - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
            }

            @Test
            @DisplayName("indented dash elements using tabs")
            void indentedDashElementsUsingTabs() throws Exception {
                var input = """
                        	-	Foo
                        	-	Boo
                        	-	Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
            }

            @Test
            @DisplayName("dash elements separated by blank lines should merge lists")
            void dashElementsSeparatedByBlankLinesShouldMergeLists() throws Exception {
                var input = """
                        List
                        ====

                        - Foo

                        - Boo


                        - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
            }

            @Test
            @DisplayName("dash elements with interspersed line comments should be skipped and not break list")
            void dashElementsWithInterspersedLineCommentsShouldBeSkippedAndNotBreakList() throws Exception {
                var input = """
                        == List

                        - Foo
                        // line comment
                        // another line comment
                        - Boo
                        // line comment
                        more text
                        // another line comment
                        - Blech
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
                assertXpath("(//ul/li)[2]/p[text()=\"Boo\nmore text\"]", output, 1);
            }

            @Test
            @DisplayName("dash elements separated by a line comment offset by blank lines should not merge lists")
            void dashElementsSeparatedByALineCommentOffsetByBlankLinesShouldNotMergeLists() throws Exception {
                var input = """
                        List
                        ====

                        - Foo
                        - Boo

                        //

                        - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 2);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("(//ul)[2]/li", output, 1);
            }

            @Test
            @DisplayName("dash elements separated by a block title offset by a blank line should not merge lists")
            void dashElementsSeparatedByABlockTitleOffsetByABlankLineShouldNotMergeLists() throws Exception {
                var input = """
                        List
                        ====

                        - Foo
                        - Boo

                        .Also
                        - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 2);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("(//ul)[2]/li", output, 1);
                assertXpath("(//ul)[2]/preceding-sibling::*[@class = \"title\"][text() = \"Also\"]", output, 1);
            }

            @Test
            @DisplayName("dash elements separated by an attribute entry offset by a blank line should not merge lists")
            void dashElementsSeparatedByAnAttributeEntryOffsetByABlankLineShouldNotMergeLists() throws Exception {
                var input = """
                        == List

                        - Foo
                        - Boo

                        :foo: bar
                        - Blech
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//ul", output, 2);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("(//ul)[2]/li", output, 1);
            }

            @Test
            @DisplayName("a non-indented wrapped line is folded into text of list item")
            void aNonIndentedWrappedLineIsFoldedIntoTextOfListItem() throws Exception {
                var input = """
                        List
                        ====

                        - Foo
                        wrapped content
                        - Boo
                        - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li[1]/*", output, 1);
                assertXpath("//ul/li[1]/p[text() = 'Foo\nwrapped content']", output, 1);
            }

            @Test
            @DisplayName("a non-indented wrapped line that resembles a block title is folded into text of list item")
            void aNonIndentedWrappedLineThatResemblesABlockTitleIsFoldedIntoTextOfListItem() throws Exception {
                var input = """
                        == List

                        - Foo
                        .wrapped content
                        - Boo
                        - Blech
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li[1]/*", output, 1);
                assertXpath("//ul/li[1]/p[text() = 'Foo\n.wrapped content']", output, 1);
            }

            @Test
            @DisplayName("a non-indented wrapped line that resembles an attribute entry is folded into text of list item")
            void aNonIndentedWrappedLineThatResemblesAnAttributeEntryIsFoldedIntoTextOfListItem() throws Exception {
                var input = """
                        == List

                        - Foo
                        :foo: bar
                        - Boo
                        - Blech
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li[1]/*", output, 1);
                assertXpath("//ul/li[1]/p[text() = 'Foo\n:foo: bar']", output, 1);
            }

            @Test
            @DisplayName("a list item with a nested marker terminates non-indented paragraph for text of list item")
            void aListItemWithANestedMarkerTerminatesNonIndentedParagraphForTextOfListItem() throws Exception {
                var input = """
                        - Foo
                        Bar
                        * Foo
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul ul", output, 1);
                refuteIncludes(output, "* Foo");
            }

            @Test
            @DisplayName("a list item for a different list terminates non-indented paragraph for text of list item")
            void aListItemForADifferentListTerminatesNonIndentedParagraphForTextOfListItem() throws Exception {
                var input = """
                        == Example 1

                        - Foo
                        Bar
                        . Foo

                        == Example 2

                        * Item
                        text
                        term:: def
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul ol", output, 1);
                refuteIncludes(output, "* Foo");
                assertCss("ul dl", output, 1);
                refuteIncludes(output, "term:: def");
            }

            @Test
            @DisplayName("an indented wrapped line is unindented and folded into text of list item")
            void anIndentedWrappedLineIsUnindentedAndFoldedIntoTextOfListItem() throws Exception {
                var input = """
                        List
                        ====

                        - Foo
                          wrapped content
                        - Boo
                        - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li[1]/*", output, 1);
                assertXpath("//ul/li[1]/p[text() = 'Foo\nwrapped content']", output, 1);
            }

            @Test
            @DisplayName("wrapped list item with hanging indent followed by non-indented line")
            void wrappedListItemWithHangingIndentFollowedByNonIndentedLine() throws Exception {
                var input = """
                        == Lists

                        - list item 1
                          // not line comment
                        second wrapped line
                        - list item 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul", output, 1);
                assertCss("ul li", output, 2);
                var lines = lines(gsub(xmlnodesAtXpath("(//ul/li)[1]/p", output, 1).getText(), rx("\\n[[:space:]]*\\n"), "\n"));
                assertEquals(3, size(lines));
                assertEquals("list item 1", Strings.chomp(lines.get(0)));
                assertEquals("  // not line comment", Strings.chomp(lines.get(1)));
                assertEquals("second wrapped line", Strings.chomp(lines.get(2)));
            }

            @Test
            @DisplayName("a list item with a nested marker terminates indented paragraph for text of list item")
            void aListItemWithANestedMarkerTerminatesIndentedParagraphForTextOfListItem() throws Exception {
                var input = """
                        - Foo
                          Bar
                        * Foo
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul ul", output, 1);
                refuteIncludes(output, "* Foo");
            }

            @Test
            @DisplayName("a list item that starts with a sequence of list markers characters should not match a nested list")
            void aListItemThatStartsWithASequenceOfListMarkersCharactersShouldNotMatchANestedList() throws Exception {
                var input = """
                         * first item
                         *. normal text
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul", output, 1);
                assertCss("ul li", output, 1);
                assertXpath("//ul/li/p[text()='first item\n*. normal text']", output, 1);
            }

            @Test
            @DisplayName("a list item for a different list terminates indented paragraph for text of list item")
            void aListItemForADifferentListTerminatesIndentedParagraphForTextOfListItem() throws Exception {
                var input = """
                        == Example 1

                        - Foo
                          Bar
                        . Foo

                        == Example 2

                        * Item
                          text
                        term:: def
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul ol", output, 1);
                refuteIncludes(output, "* Foo");
                assertCss("ul dl", output, 1);
                refuteIncludes(output, "term:: def");
            }

            @Test
            @DisplayName("a literal paragraph offset by blank lines in list content is appended as a literal block")
            void aLiteralParagraphOffsetByBlankLinesInListContentIsAppendedAsALiteralBlock() throws Exception {
                var input = """
                        List
                        ====

                        - Foo

                          literal

                        - Boo
                        - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
                assertXpath("(//ul/li)[1]/p[text() = \"Foo\"]", output, 1);
                assertXpath("(//ul/li)[1]/*[@class=\"literalblock\"]", output, 1);
                assertXpath("(//ul/li)[1]/p/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("((//ul/li)[1]/*[@class=\"literalblock\"])[1]//pre[text() = \"literal\"]", output, 1);
            }

            @Test
            @DisplayName("should escape special characters in all literal paragraphs attached to list item")
            void shouldEscapeSpecialCharactersInAllLiteralParagraphsAttachedToListItem() throws Exception {
                var input = """
                        * first item

                          <code>text</code>

                          more <code>text</code>

                        * second item
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("li", output, 2);
                assertCss("code", output, 0);
                assertCss("li:first-of-type > *", output, 3);
                assertCss("li:first-of-type pre", output, 2);
                assertXpath("((//li)[1]//pre)[1][text()=\"<code>text</code>\"]", output, 1);
                assertXpath("((//li)[1]//pre)[2][text()=\"more <code>text</code>\"]", output, 1);
            }

            @Test
            @DisplayName("a literal paragraph offset by a blank line in list content followed by line with continuation is appended as two blocks")
            void aLiteralParagraphOffsetByABlankLineInListContentFollowedByLineWithContinuationIsAppendedAsTwoBlocks() throws Exception {
                var input = """
                        List
                        ====

                        - Foo

                          literal
                        +
                        para

                        - Boo
                        - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
                assertXpath("(//ul/li)[1]/p[text() = \"Foo\"]", output, 1);
                assertXpath("(//ul/li)[1]/*[@class=\"literalblock\"]", output, 1);
                assertXpath("(//ul/li)[1]/p/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("((//ul/li)[1]/*[@class=\"literalblock\"])[1]//pre[text() = \"literal\"]", output, 1);
                assertXpath("(//ul/li)[1]/*[@class=\"literalblock\"]/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("(//ul/li)[1]/*[@class=\"literalblock\"]/following-sibling::*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
            }

            @Test
            @DisplayName("an admonition paragraph attached by a line continuation to a list item with wrapped text should produce admonition")
            void anAdmonitionParagraphAttachedByALineContinuationToAListItemWithWrappedTextShouldProduceAdmonition() throws Exception {
                var input = """
                        - first-line text
                          wrapped text
                        +
                        NOTE: This is a note.
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul", output, 1);
                assertCss("ul > li", output, 1);
                assertCss("ul > li > p", output, 1);
                assertXpath("//ul/li/p[text()=\"first-line text\nwrapped text\"]", output, 1);
                assertCss("ul > li > p + .admonitionblock.note", output, 1);
                assertXpath("//ul/li/*[@class=\"admonitionblock note\"]//td[@class=\"content\"][normalize-space(text())=\"This is a note.\"]", output, 1);
            }

            @Test
            @DisplayName("paragraph-like blocks attached to an ancestor list item by a list continuation should produce blocks")
            void paragraphLikeBlocksAttachedToAnAncestorListItemByAListContinuationShouldProduceBlocks() throws Exception {
                var input = """
                        * parent
                         ** child

                        +
                        NOTE: This is a note.

                        * another parent
                         ** another child

                        +
                        '''
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul ul .admonitionblock.note", output, 0);
                assertXpath("(//ul)[1]/li/*[@class=\"admonitionblock note\"]", output, 1);
                assertCss("ul ul hr", output, 0);
                assertXpath("(//ul)[1]/li/hr", output, 1);
            }

            @Test
            @DisplayName("should not inherit block attributes from previous block when block is attached using a list continuation")
            void shouldNotInheritBlockAttributesFromPreviousBlockWhenBlockIsAttachedUsingAListContinuation() throws Exception {
                var input = """
                        * complex list item
                        +
                        [source,xml]
                        ----
                        <name>value</name> <!--1-->
                        ----
                        <1> a configuration value
                        """;
                var doc = documentFromString(input);
                var colist = last(((List) doc.getBlocks().get(0)).getItems().get(0).getBlocks());
                assertEquals("colist", colist.getContext());
                assertNotEquals("source", colist.getStyle());
                var output = doc.convert(Options.of("standalone", false));
                assertCss("ul", output, 1);
                assertCss("ul > li", output, 1);
                assertCss("ul > li > p", output, 1);
                assertCss("ul > li > .listingblock", output, 1);
                assertCss("ul > li > .colist", output, 1);
            }

            @Test
            @DisplayName("should continue to parse blocks attached by a list continuation after block is dropped")
            void shouldContinueToParseBlocksAttachedByAListContinuationAfterBlockIsDropped() throws Exception {
                var input = """
                        * item
                        +
                        paragraph
                        +
                        [comment]
                        comment
                        +
                        ====
                        example
                        ====
                        '''
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul > li > .paragraph", output, 1);
                assertCss("ul > li > .exampleblock", output, 1);
            }

            @Test
            @DisplayName("appends line as paragraph if attached by continuation following line comment")
            void appendsLineAsParagraphIfAttachedByContinuationFollowingLineComment() throws Exception {
                var input = """
                        - list item 1
                        // line comment
                        +
                        paragraph in list item 1

                        - list item 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul", output, 1);
                assertCss("ul li", output, 2);
                assertXpath("(//ul/li)[1]/p[text()=\"list item 1\"]", output, 1);
                assertXpath("(//ul/li)[1]/p/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("(//ul/li)[1]/p/following-sibling::*[@class=\"paragraph\"]/p[text()=\"paragraph in list item 1\"]", output, 1);
                assertXpath("(//ul/li)[2]/p[text()=\"list item 2\"]", output, 1);
            }

            @Test
            @DisplayName("a literal paragraph with a line that appears as a list item that is followed by a continuation should create two blocks")
            void aLiteralParagraphWithALineThatAppearsAsAListItemThatIsFollowedByAContinuationShouldCreateTwoBlocks() throws Exception {
                var input = """
                        * Foo
                        +
                          literal
                        . still literal
                        +
                        para

                        * Bar
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 2);
                assertXpath("(//ul/li)[1]/p[text() = \"Foo\"]", output, 1);
                assertXpath("(//ul/li)[1]/*[@class=\"literalblock\"]", output, 1);
                assertXpath("(//ul/li)[1]/p/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("((//ul/li)[1]/*[@class=\"literalblock\"])[1]//pre[text() = \"  literal\n. still literal\"]", output, 1);
                assertXpath("(//ul/li)[1]/*[@class=\"literalblock\"]/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("(//ul/li)[1]/*[@class=\"literalblock\"]/following-sibling::*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
            }

            @Test
            @DisplayName("consecutive literal paragraph offset by blank lines in list content are appended as a literal blocks")
            void consecutiveLiteralParagraphOffsetByBlankLinesInListContentAreAppendedAsALiteralBlocks() throws Exception {
                var input = """
                        List
                        ====

                        - Foo

                          literal

                          more
                          literal

                        - Boo
                        - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
                assertXpath("(//ul/li)[1]/p[text() = \"Foo\"]", output, 1);
                assertXpath("(//ul/li)[1]/*[@class=\"literalblock\"]", output, 2);
                assertXpath("(//ul/li)[1]/p/following-sibling::*[@class=\"literalblock\"]", output, 2);
                assertXpath("((//ul/li)[1]/*[@class=\"literalblock\"])[1]//pre[text()=\"literal\"]", output, 1);
                assertXpath("((//ul/li)[1]/*[@class='literalblock'])[2]//pre[text()='more\nliteral']", output, 1);
            }

            @Test
            @DisplayName("a literal paragraph without a trailing blank line consumes following list items")
            void aLiteralParagraphWithoutATrailingBlankLineConsumesFollowingListItems() throws Exception {
                var input = """
                        List
                        ====

                        - Foo

                          literal
                        - Boo
                        - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 1);
                assertXpath("(//ul/li)[1]/p[text() = \"Foo\"]", output, 1);
                assertXpath("(//ul/li)[1]/*[@class=\"literalblock\"]", output, 1);
                assertXpath("(//ul/li)[1]/p/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("((//ul/li)[1]/*[@class='literalblock'])[1]//pre[text() = '  literal\n- Boo\n- Blech']", output, 1);
            }

            @Test
            @DisplayName("asterisk elements with no blank lines")
            void asteriskElementsWithNoBlankLines() throws Exception {
                var input = """
                        List
                        ====

                        * Foo
                        * Boo
                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
            }

            @Test
            @DisplayName("indented asterisk elements using spaces")
            void indentedAsteriskElementsUsingSpaces() throws Exception {
                var input = """
                         * Foo
                         * Boo
                         * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
            }

            @Test
            @DisplayName("indented unicode bullet elements using spaces")
            void indentedUnicodeBulletElementsUsingSpaces() throws Exception {
                var input = """
                         • Foo
                         • Boo
                         • Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
            }

            @Test
            @DisplayName("indented asterisk elements using tabs")
            void indentedAsteriskElementsUsingTabs() throws Exception {
                var input = """
                        	*	Foo
                        	*	Boo
                        	*	Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
            }

            @Test
            @DisplayName("should represent block style as style class")
            void shouldRepresentBlockStyleAsStyleClass() throws Exception {
                for (var style : list("disc", "square", "circle")) {
                    var input = ("[" + str(style) + "]\n* a\n* b\n* c\n");
                    var output = convertStringToEmbedded(input);
                    assertCss((".ulist." + str(style)), output, 1);
                    assertCss((".ulist." + str(style) + " ul." + str(style)), output, 1);
                }
            }

            @Test
            @DisplayName("asterisk elements separated by blank lines should merge lists")
            void asteriskElementsSeparatedByBlankLinesShouldMergeLists() throws Exception {
                var input = """
                        List
                        ====

                        * Foo

                        * Boo


                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
            }

            @Test
            @DisplayName("asterisk elements with interspersed line comments should be skipped and not break list")
            void asteriskElementsWithInterspersedLineCommentsShouldBeSkippedAndNotBreakList() throws Exception {
                var input = """
                        == List

                        * Foo
                        // line comment
                        // another line comment
                        * Boo
                        // line comment
                        more text
                        // another line comment
                        * Blech
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
                assertXpath("(//ul/li)[2]/p[text()=\"Boo\nmore text\"]", output, 1);
            }

            @Test
            @DisplayName("asterisk elements separated by a line comment offset by blank lines should not merge lists")
            void asteriskElementsSeparatedByALineCommentOffsetByBlankLinesShouldNotMergeLists() throws Exception {
                var input = """
                        List
                        ====

                        * Foo
                        * Boo

                        //

                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 2);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("(//ul)[2]/li", output, 1);
            }

            @Test
            @DisplayName("asterisk elements separated by a block title offset by a blank line should not merge lists")
            void asteriskElementsSeparatedByABlockTitleOffsetByABlankLineShouldNotMergeLists() throws Exception {
                var input = """
                        List
                        ====

                        * Foo
                        * Boo

                        .Also
                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 2);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("(//ul)[2]/li", output, 1);
                assertXpath("(//ul)[2]/preceding-sibling::*[@class = \"title\"][text() = \"Also\"]", output, 1);
            }

            @Test
            @DisplayName("asterisk elements separated by an attribute entry offset by a blank line should not merge lists")
            void asteriskElementsSeparatedByAnAttributeEntryOffsetByABlankLineShouldNotMergeLists() throws Exception {
                var input = """
                        == List

                        * Foo
                        * Boo

                        :foo: bar
                        * Blech
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//ul", output, 2);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("(//ul)[2]/li", output, 1);
            }

            @Test
            @DisplayName("list should terminate before next lower section heading")
            void listShouldTerminateBeforeNextLowerSectionHeading() throws Exception {
                var input = """
                        List
                        ====

                        * first
                        item
                        * second
                        item

                        == Section
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 2);
                assertXpath("//h2[text() = \"Section\"]", output, 1);
            }

            @Test
            @DisplayName("list should terminate before next lower section heading with implicit id")
            void listShouldTerminateBeforeNextLowerSectionHeadingWithImplicitId() throws Exception {
                var input = """
                        List
                        ====

                        * first
                        item
                        * second
                        item

                        [[sec]]
                        == Section
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 2);
                assertXpath("//h2[@id = \"sec\"][text() = \"Section\"]", output, 1);
            }

            @Test
            @DisplayName("should not find section title immediately below last list item")
            void shouldNotFindSectionTitleImmediatelyBelowLastListItem() throws Exception {
                var input = """
                        * first
                        * second
                        == Not a section
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul", output, 1);
                assertCss("ul > li", output, 2);
                assertCss("h2", output, 0);
                assertIncludes(output, "== Not a section");
                assertXpath("(//li)[2]/p[text() = \"second\n== Not a section\"]", output, 1);
            }

            @Test
            @DisplayName("should match trailing line separator in text of list item")
            void shouldMatchTrailingLineSeparatorInTextOfListItem() throws Exception {
                var input = Strings.chop(("* a\n* b" + str(decodeChar(8232)) + "\n* c\n"));
                var output = convertString(input);
                assertCss("li", output, 3);
                assertXpath(("(//li)[2]/p[text()=\"b" + str(decodeChar(8232)) + "\"]"), output, 1);
            }

            @Test
            @DisplayName("should match line separator in text of list item")
            void shouldMatchLineSeparatorInTextOfListItem() throws Exception {
                var input = Strings.chop(("* a\n* b" + str(decodeChar(8232)) + "b\n* c\n"));
                var output = convertString(input);
                assertCss("li", output, 3);
                assertXpath(("(//li)[2]/p[text()=\"b" + str(decodeChar(8232)) + "b\"]"), output, 1);
            }
        }

        @Nested
        @DisplayName("Lists with inline markup")
        class ListsWithInlineMarkup {

            @Test
            @DisplayName("quoted text")
            void quotedText() throws Exception {
                var input = """
                        List
                        ====

                        - I am *strong*.
                        - I am _stressed_.
                        - I am `flexible`.
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
                assertXpath("(//ul/li)[1]//strong", output, 1);
                assertXpath("(//ul/li)[2]//em", output, 1);
                assertXpath("(//ul/li)[3]//code", output, 1);
            }

            @Test
            @DisplayName("attribute substitutions")
            void attributeSubstitutions() throws Exception {
                var input = """
                        List
                        ====
                        :foo: bar

                        - side a {vbar} side b
                        - Take me to a {foo}.
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 2);
                assertXpath("(//ul/li)[1]//p[text() = \"side a | side b\"]", output, 1);
                assertXpath("(//ul/li)[2]//p[text() = \"Take me to a bar.\"]", output, 1);
            }

            @Test
            @DisplayName("leading dot is treated as text not block title")
            void leadingDotIsTreatedAsTextNotBlockTitle() throws Exception {
                var input = """
                        * .first
                        * .second
                        * .third
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 3);
                var textList = list(".first", ".second", ".third");
                for (int index = 0; index < textList.size(); index++) {
                    var text = textList.get(index);
                    assertXpath(("(//ul/li)[" + str((index + 1)) + "]//p[text() = '" + str(text) + "']"), output, 1);
                }
            }

            @Test
            @DisplayName("word ending sentence on continuing line not treated as a list item")
            void wordEndingSentenceOnContinuingLineNotTreatedAsAListItem() throws Exception {
                var input = """
                        A. This is the story about
                           AsciiDoc. It begins here.
                        B. And it ends here.
                        """;
                var output = convertString(input);
                assertXpath("//ol", output, 1);
                assertXpath("//ol/li", output, 2);
            }

            @Test
            @DisplayName("should discover anchor at start of unordered list item text and register it as a reference")
            void shouldDiscoverAnchorAtStartOfUnorderedListItemTextAndRegisterItAsAReference() throws Exception {
                var input = """
                        The highest peak in the Front Range is <<grays-peak>>, which tops <<mount-evans>> by just a few feet.

                        * [[mount-evans,Mount Evans]]At 14,271 feet, Mount Evans is the highest summit of the Chicago Peaks in the Front Range of the Rocky Mountains.
                        * [[grays-peak,Grays Peak]]
                        Grays Peak rises to 14,278 feet, making it the highest summit in the Front Range of the Rocky Mountains.
                        * Longs Peak is a 14,259-foot high, prominent mountain summit in the northern Front Range of the Rocky Mountains.
                        * Pikes Peak is the highest summit of the southern Front Range of the Rocky Mountains at 14,115 feet.
                        """;
                var doc = documentFromString(input);
                var refs = doc.getCatalog().getRefs();
                assertTruthy(refs.containsKey("mount-evans"));
                assertTruthy(refs.containsKey("grays-peak"));
                var output = doc.convert(Options.of("standalone", false));
                assertXpath("(//p)[1]/a[@href=\"#grays-peak\"][text()=\"Grays Peak\"]", output, 1);
                assertXpath("(//p)[1]/a[@href=\"#mount-evans\"][text()=\"Mount Evans\"]", output, 1);
            }

            @Test
            @DisplayName("should discover anchor at start of ordered list item text and register it as a reference")
            void shouldDiscoverAnchorAtStartOfOrderedListItemTextAndRegisterItAsAReference() throws Exception {
                var input = """
                        This is a cross-reference to <<step-2>>.
                        This is a cross-reference to <<step-4>>.

                        . Ordered list, item 1, without anchor
                        . [[step-2,Step 2]]Ordered list, item 2, with anchor
                        . Ordered list, item 3, without anchor
                        . [[step-4,Step 4]]Ordered list, item 4, with anchor
                        """;
                var doc = documentFromString(input);
                var refs = doc.getCatalog().getRefs();
                assertTruthy(refs.containsKey("step-2"));
                assertTruthy(refs.containsKey("step-4"));
                var output = doc.convert(Options.of("standalone", false));
                assertXpath("(//p)[1]/a[@href=\"#step-2\"][text()=\"Step 2\"]", output, 1);
                assertXpath("(//p)[1]/a[@href=\"#step-4\"][text()=\"Step 4\"]", output, 1);
            }

            @Test
            @DisplayName("should discover anchor at start of callout list item text and register it as a reference")
            void shouldDiscoverAnchorAtStartOfCalloutListItemTextAndRegisterItAsAReference() throws Exception {
                var input = """
                        This is a cross-reference to <<url-mapping>>.

                        [source,ruby]
                        ----
                        require 'sinatra' <1>

                        get '/hi' do <2> <3>
                          "Hello World!"
                        end
                        ----
                        <1> Library import
                        <2> [[url-mapping,url mapping]]URL mapping
                        <3> Response block
                        """;
                var doc = documentFromString(input);
                var refs = doc.getCatalog().getRefs();
                assertTruthy(refs.containsKey("url-mapping"));
                var output = doc.convert(Options.of("standalone", false));
                assertXpath("(//p)[1]/a[@href=\"#url-mapping\"][text()=\"url mapping\"]", output, 1);
            }
        }

        @Nested
        @DisplayName("Nested lists")
        class NestedLists {

            @Test
            @DisplayName("asterisk element mixed with dash elements should be nested")
            void asteriskElementMixedWithDashElementsShouldBeNested() throws Exception {
                var input = """
                        List
                        ====

                        - Foo
                        * Boo
                        - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 2);
                assertXpath("//ul/li", output, 3);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("(//ul)[1]/li//ul/li", output, 1);
            }

            @Test
            @DisplayName("dash element mixed with asterisks elements should be nested")
            void dashElementMixedWithAsterisksElementsShouldBeNested() throws Exception {
                var input = """
                        List
                        ====

                        * Foo
                        - Boo
                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 2);
                assertXpath("//ul/li", output, 3);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("(//ul)[1]/li//ul/li", output, 1);
            }

            @Test
            @DisplayName("lines prefixed with alternating list markers separated by blank lines should be nested")
            void linesPrefixedWithAlternatingListMarkersSeparatedByBlankLinesShouldBeNested() throws Exception {
                var input = """
                        List
                        ====

                        - Foo

                        * Boo


                        - Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 2);
                assertXpath("//ul/li", output, 3);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("(//ul)[1]/li//ul/li", output, 1);
            }

            @Test
            @DisplayName("nested elements (2) with asterisks")
            void nestedElements2WithAsterisks() throws Exception {
                var input = """
                        List
                        ====

                        * Foo
                        ** Boo
                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 2);
                assertXpath("//ul/li", output, 3);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("(//ul)[1]/li//ul/li", output, 1);
            }

            @Test
            @DisplayName("nested elements (3) with asterisks")
            void nestedElements3WithAsterisks() throws Exception {
                var input = """
                        List
                        ====

                        * Foo
                        ** Boo
                        *** Snoo
                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 3);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("((//ul)[1]/li//ul)[1]/li", output, 1);
                assertXpath("(((//ul)[1]/li//ul)[1]/li//ul)[1]/li", output, 1);
            }

            @Test
            @DisplayName("nested elements (4) with asterisks")
            void nestedElements4WithAsterisks() throws Exception {
                var input = """
                        List
                        ====

                        * Foo
                        ** Boo
                        *** Snoo
                        **** Froo
                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 4);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("((//ul)[1]/li//ul)[1]/li", output, 1);
                assertXpath("(((//ul)[1]/li//ul)[1]/li//ul)[1]/li", output, 1);
                assertXpath("((((//ul)[1]/li//ul)[1]/li//ul)[1]/li//ul)[1]/li", output, 1);
            }

            @Test
            @DisplayName("nested elements (5) with asterisks")
            void nestedElements5WithAsterisks() throws Exception {
                var input = """
                        List
                        ====

                        * Foo
                        ** Boo
                        *** Snoo
                        **** Froo
                        ***** Groo
                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 5);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("((//ul)[1]/li//ul)[1]/li", output, 1);
                assertXpath("(((//ul)[1]/li//ul)[1]/li//ul)[1]/li", output, 1);
                assertXpath("((((//ul)[1]/li//ul)[1]/li//ul)[1]/li//ul)[1]/li", output, 1);
                assertXpath("(((((//ul)[1]/li//ul)[1]/li//ul)[1]/li//ul)[1]/li//ul)[1]/li", output, 1);
            }

            @Test
            @DisplayName("nested arbitrary depth with asterisks")
            void nestedArbitraryDepthWithAsterisks() throws Exception {
                var input = new ArrayList<String>();
                var chList = range("a", "z");
                for (int i = 0; i < chList.size(); i++) {
                    var ch = chList.get(i);
                    input.add((str("*".repeat((i + 1))) + " " + str(ch)));
                }
                var output = convertStringToEmbedded(join(input, "\n"));
                refuteIncludes(output, "*");
                assertCss("li", output, 26);
            }

            @Test
            @DisplayName("level of unordered list should match section level")
            void levelOfUnorderedListShouldMatchSectionLevel() throws Exception {
                var input = """
                        == Parent Section

                        * item 1.1
                         ** item 2.1
                          *** item 3.1
                         ** item 2.2
                        * item 1.2

                        === Nested Section

                        * item 1.1
                        """;
                var doc = documentFromString(input);
                var lists = doc.findBy(AbstractBlock.Selector.any().withContext("ulist"));
                assertEquals(1, lists.get(0).getLevel());
                assertEquals(1, lists.get(1).getLevel());
                assertEquals(1, lists.get(2).getLevel());
                assertEquals(2, lists.get(3).getLevel());
            }

            @Test
            @DisplayName("does not recognize lists with repeating unicode bullets")
            void doesNotRecognizeListsWithRepeatingUnicodeBullets() throws Exception {
                var input = "•• Boo";
                var output = convertString(input);
                assertXpath("//ul", output, 0);
                assertIncludes(output, "•");
            }

            @Test
            @DisplayName("nested ordered elements (2)")
            void nestedOrderedElements2() throws Exception {
                var input = """
                        List
                        ====

                        . Foo
                        .. Boo
                        . Blech
                        """;
                var output = convertString(input);
                assertXpath("//ol", output, 2);
                assertXpath("//ol/li", output, 3);
                assertXpath("(//ol)[1]/li", output, 2);
                assertXpath("(//ol)[1]/li//ol/li", output, 1);
            }

            @Test
            @DisplayName("nested ordered elements (3)")
            void nestedOrderedElements3() throws Exception {
                var input = """
                        List
                        ====

                        . Foo
                        .. Boo
                        ... Snoo
                        . Blech
                        """;
                var output = convertString(input);
                assertXpath("//ol", output, 3);
                assertXpath("(//ol)[1]/li", output, 2);
                assertXpath("((//ol)[1]/li//ol)[1]/li", output, 1);
                assertXpath("(((//ol)[1]/li//ol)[1]/li//ol)[1]/li", output, 1);
            }

            @Test
            @DisplayName("nested arbitrary depth with dot marker")
            void nestedArbitraryDepthWithDotMarker() throws Exception {
                var input = new ArrayList<String>();
                var chList = range("a", "z");
                for (int i = 0; i < chList.size(); i++) {
                    var ch = chList.get(i);
                    input.add((str(".".repeat((i + 1))) + " " + str(ch)));
                }
                var output = convertStringToEmbedded(join(input, "\n"));
                refuteIncludes(output, ".");
                assertCss("li", output, 26);
            }

            @Test
            @DisplayName("level of ordered list should match section level")
            void levelOfOrderedListShouldMatchSectionLevel() throws Exception {
                var input = """
                        == Parent Section

                        . item 1.1
                         .. item 2.1
                          ... item 3.1
                         .. item 2.2
                        . item 1.2

                        === Nested Section

                        . item 1.1
                        """;
                var doc = documentFromString(input);
                var lists = doc.findBy(AbstractBlock.Selector.any().withContext("olist"));
                assertEquals(1, lists.get(0).getLevel());
                assertEquals(1, lists.get(1).getLevel());
                assertEquals(1, lists.get(2).getLevel());
                assertEquals(2, lists.get(3).getLevel());
            }

            @Test
            @DisplayName("nested unordered inside ordered elements")
            void nestedUnorderedInsideOrderedElements() throws Exception {
                var input = """
                        List
                        ====

                        . Foo
                        * Boo
                        . Blech
                        """;
                var output = convertString(input);
                assertXpath("//ol", output, 1);
                assertXpath("//ul", output, 1);
                assertXpath("(//ol)[1]/li", output, 2);
                assertXpath("((//ol)[1]/li//ul)[1]/li", output, 1);
            }

            @Test
            @DisplayName("nested ordered inside unordered elements")
            void nestedOrderedInsideUnorderedElements() throws Exception {
                var input = """
                        List
                        ====

                        * Foo
                        . Boo
                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ol", output, 1);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("((//ul)[1]/li//ol)[1]/li", output, 1);
            }

            @Test
            @DisplayName("three levels of alternating unordered and ordered elements")
            void threeLevelsOfAlternatingUnorderedAndOrderedElements() throws Exception {
                var input = """
                        == Lists

                        * bullet 1
                        . numbered 1.1
                        ** bullet 1.1.1
                        * bullet 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".ulist", output, 2);
                assertCss(".olist", output, 1);
                assertCss(".ulist > ul > li > p", output, 3);
                assertCss(".ulist > ul > li > p + .olist", output, 1);
                assertCss(".ulist > ul > li > p + .olist > ol > li > p", output, 1);
                assertCss(".ulist > ul > li > p + .olist > ol > li > p + .ulist", output, 1);
                assertCss(".ulist > ul > li > p + .olist > ol > li > p + .ulist > ul > li > p", output, 1);
                assertCss(".ulist > ul > li + li > p", output, 1);
            }

            @Test
            @DisplayName("lines with alternating markers of unordered and ordered list types separated by blank lines should be nested")
            void linesWithAlternatingMarkersOfUnorderedAndOrderedListTypesSeparatedByBlankLinesShouldBeNested() throws Exception {
                var input = """
                        List
                        ====

                        * Foo

                        . Boo


                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ol", output, 1);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("((//ul)[1]/li//ol)[1]/li", output, 1);
            }

            @Test
            @DisplayName("list item with literal content should not consume nested list of different type")
            void listItemWithLiteralContentShouldNotConsumeNestedListOfDifferentType() throws Exception {
                var input = """
                        List
                        ====

                        - bullet

                          literal
                          but not
                          hungry

                        . numbered
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//li", output, 2);
                assertXpath("//ul//ol", output, 1);
                assertXpath("//ul/li/p", output, 1);
                assertXpath("//ul/li/p[text()=\"bullet\"]", output, 1);
                assertXpath("//ul/li/p/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("//ul/li/p/following-sibling::*[@class=\"literalblock\"]//pre[text()=\"literal\nbut not\nhungry\"]", output, 1);
                assertXpath("//*[@class=\"literalblock\"]/following-sibling::*[@class=\"olist arabic\"]", output, 1);
                assertXpath("//*[@class=\"literalblock\"]/following-sibling::*[@class=\"olist arabic\"]//p[text()=\"numbered\"]", output, 1);
            }

            @Test
            @DisplayName("nested list item does not eat the title of the following detached block")
            void nestedListItemDoesNotEatTheTitleOfTheFollowingDetachedBlock() throws Exception {
                var input = """
                        List
                        ====

                        - bullet
                          * nested bullet 1
                          * nested bullet 2

                        .Title
                        ....
                        literal
                        ....
                        """;
                var output = convertString(input);
                assertXpath("//*[@class=\"ulist\"]/ul", output, 2);
                assertXpath("(//*[@class=\"ulist\"])[1]/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("(//*[@class=\"ulist\"])[1]/following-sibling::*[@class=\"literalblock\"]/*[@class=\"title\"]", output, 1);
            }

            @Test
            @DisplayName("lines with alternating markers of bulleted and description list types separated by blank lines should be nested")
            void linesWithAlternatingMarkersOfBulletedAndDescriptionListTypesSeparatedByBlankLinesShouldBeNested() throws Exception {
                var input = """
                        List
                        ====

                        * Foo

                        term1:: def1

                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//dl", output, 1);
                assertXpath("//ul[1]/li", output, 2);
                assertXpath("//ul[1]/li//dl[1]/dt", output, 1);
                assertXpath("//ul[1]/li//dl[1]/dd", output, 1);
            }

            @Test
            @DisplayName("nested ordered with attribute inside unordered elements")
            void nestedOrderedWithAttributeInsideUnorderedElements() throws Exception {
                var input = """
                        Blah
                        ====

                        * Foo
                        [start=2]
                        . Boo
                        * Blech
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ol", output, 1);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("((//ul)[1]/li//ol)[1][@start = 2]/li", output, 1);
            }
        }

        @Nested
        @DisplayName("List continuations")
        class ListContinuations {

            @Test
            @DisplayName("adjacent list continuation line attaches following paragraph")
            void adjacentListContinuationLineAttachesFollowingParagraph() throws Exception {
                var input = """
                        Lists
                        =====

                        * Item one, paragraph one
                        +
                        Item one, paragraph two
                        +
                        * Item two
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 2);
                assertXpath("//ul/li[1]/p", output, 1);
                assertXpath("//ul/li[1]//p", output, 2);
                assertXpath("//ul/li[1]/p[text() = \"Item one, paragraph one\"]", output, 1);
                assertXpath("//ul/li[1]/*[@class = \"paragraph\"]/p[text() = \"Item one, paragraph two\"]", output, 1);
            }

            @Test
            @DisplayName("adjacent list continuation line attaches following block")
            void adjacentListContinuationLineAttachesFollowingBlock() throws Exception {
                var input = """
                        Lists
                        =====

                        * Item one, paragraph one
                        +
                        ....
                        Item one, literal block
                        ....
                        +
                        * Item two
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 2);
                assertXpath("//ul/li[1]/p", output, 1);
                assertXpath("(//ul/li[1]/p/following-sibling::*)[1][@class = \"literalblock\"]", output, 1);
            }

            @Test
            @DisplayName("adjacent list continuation line attaches following block with block attributes")
            void adjacentListContinuationLineAttachesFollowingBlockWithBlockAttributes() throws Exception {
                var input = """
                        Lists
                        =====

                        * Item one, paragraph one
                        +
                        :foo: bar
                        [[beck]]
                        .Read the following aloud to yourself
                        [source, ruby]
                        ----
                        5.times { print "Odelay!" }
                        ----

                        * Item two
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 2);
                assertXpath("//ul/li[1]/p", output, 1);
                assertXpath("(//ul/li[1]/p/following-sibling::*)[1][@id=\"beck\"][@class = \"listingblock\"]", output, 1);
                assertXpath("(//ul/li[1]/p/following-sibling::*)[1][@id=\"beck\"]/div[@class=\"title\"][starts-with(text(),\"Read\")]", output, 1);
                assertXpath("(//ul/li[1]/p/following-sibling::*)[1][@id=\"beck\"]//code[@data-lang=\"ruby\"][starts-with(text(),\"5.times\")]", output, 1);
            }

            @Test
            @DisplayName("trailing block attribute line attached by continuation should not create block")
            void trailingBlockAttributeLineAttachedByContinuationShouldNotCreateBlock() throws Exception {
                var input = """
                        Lists
                        =====

                        * Item one, paragraph one
                        +
                        [source]

                        * Item two
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 2);
                assertXpath("//ul/li[1]/*", output, 1);
                assertXpath("//ul/li//*[@class=\"listingblock\"]", output, 0);
            }

            @Test
            @DisplayName("trailing block title line attached by continuation should not create block")
            void trailingBlockTitleLineAttachedByContinuationShouldNotCreateBlock() throws Exception {
                var input = """
                        Lists
                        =====

                        * Item one, paragraph one
                        +
                        .Disappears into the ether

                        * Item two
                        """;
                var output = convertString(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 2);
                assertXpath("//ul/li[1]/*", output, 1);
            }

            @Test
            @DisplayName("consecutive blocks in list continuation attach to list item")
            void consecutiveBlocksInListContinuationAttachToListItem() throws Exception {
                var input = """
                        Lists
                        =====

                        * Item one, paragraph one
                        +
                        ....
                        Item one, literal block
                        ....
                        +
                        ____
                        Item one, quote block
                        ____
                        +
                        * Item two
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 2);
                assertXpath("//ul/li[1]/p", output, 1);
                assertXpath("(//ul/li[1]/p/following-sibling::*)[1][@class = \"literalblock\"]", output, 1);
                assertXpath("(//ul/li[1]/p/following-sibling::*)[2][@class = \"quoteblock\"]", output, 1);
            }

            @Test
            @DisplayName("list item with hanging indent followed by block attached by list continuation")
            void listItemWithHangingIndentFollowedByBlockAttachedByListContinuation() throws Exception {
                var input = """
                        == Lists

                        . list item 1
                          continued
                        +
                        --
                        open block in list item 1
                        --

                        . list item 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ol", output, 1);
                assertCss("ol li", output, 2);
                assertXpath("(//ol/li)[1]/p[text()=\"list item 1\ncontinued\"]", output, 1);
                assertXpath("(//ol/li)[1]/p/following-sibling::*[@class=\"openblock\"]", output, 1);
                assertXpath("(//ol/li)[1]/p/following-sibling::*[@class=\"openblock\"]//p[text()=\"open block in list item 1\"]", output, 1);
                assertXpath("(//ol/li)[2]/p[text()=\"list item 2\"]", output, 1);
            }

            @Test
            @DisplayName("list item paragraph in list item and nested list item")
            void listItemParagraphInListItemAndNestedListItem() throws Exception {
                var input = """
                        == Lists

                        . list item 1
                        +
                        list item 1 paragraph

                        * nested list item
                        +
                        nested list item paragraph

                        . list item 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".olist ol", output, 1);
                assertCss(".olist ol > li", output, 2);
                assertCss(".ulist ul", output, 1);
                assertCss(".ulist ul > li", output, 1);
                assertXpath("(//ol/li)[1]/*", output, 3);
                assertXpath("((//ol/li)[1]/*)[1]/self::p", output, 1);
                assertXpath("((//ol/li)[1]/*)[1]/self::p[text()=\"list item 1\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[2]/self::div[@class=\"paragraph\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[3]/self::div[@class=\"ulist\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[3]/self::div[@class=\"ulist\"]/ul/li", output, 1);
                assertXpath("((//ol/li)[1]/*)[3]/self::div[@class=\"ulist\"]/ul/li/p[text()=\"nested list item\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[3]/self::div[@class=\"ulist\"]/ul/li/p/following-sibling::div[@class=\"paragraph\"]", output, 1);
            }

            @Test
            @DisplayName("trailing list continuations should attach to list items at respective levels")
            void trailingListContinuationsShouldAttachToListItemsAtRespectiveLevels() throws Exception {
                var input = """
                        == Lists

                        . list item 1
                        +
                        * nested list item 1
                        * nested list item 2
                        +
                        paragraph for nested list item 2

                        +
                        paragraph for list item 1

                        . list item 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".olist ol", output, 1);
                assertCss(".olist ol > li", output, 2);
                assertCss(".ulist ul", output, 1);
                assertCss(".ulist ul > li", output, 2);
                assertCss(".olist .ulist", output, 1);
                assertXpath("(//ol/li)[1]/*", output, 3);
                assertXpath("((//ol/li)[1]/*)[1]/self::p", output, 1);
                assertXpath("((//ol/li)[1]/*)[1]/self::p[text()=\"list item 1\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li", output, 2);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[2]/*", output, 2);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[2]/p", output, 1);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[2]/div[@class=\"paragraph\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[3]/self::div[@class=\"paragraph\"]", output, 1);
            }

            @Test
            @DisplayName("trailing list continuations should attach to list items of different types at respective levels")
            void trailingListContinuationsShouldAttachToListItemsOfDifferentTypesAtRespectiveLevels() throws Exception {
                var input = """
                        == Lists

                        * bullet 1
                        . numbered 1.1
                        ** bullet 1.1.1

                        +
                        numbered 1.1 paragraph

                        +
                        bullet 1 paragraph

                        * bullet 2
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("(//ul)[1]/li", output, 2);
                assertXpath("((//ul)[1]/li[1])/*", output, 3);
                assertXpath("(((//ul)[1]/li[1])/*)[1]/self::p[text()=\"bullet 1\"]", output, 1);
                assertXpath("(((//ul)[1]/li[1])/*)[2]/ol", output, 1);
                assertXpath("(((//ul)[1]/li[1])/*)[3]/self::div[@class=\"paragraph\"]/p[text()=\"bullet 1 paragraph\"]", output, 1);
                assertXpath("((//ul)[1]/li)[1]/div/ol/li", output, 1);
                assertXpath("((//ul)[1]/li)[1]/div/ol/li/*", output, 3);
                assertXpath("(((//ul)[1]/li)[1]/div/ol/li/*)[1]/self::p[text()=\"numbered 1.1\"]", output, 1);
                assertXpath("(((//ul)[1]/li)[1]/div/ol/li/*)[2]/self::div[@class=\"ulist\"]", output, 1);
                assertXpath("(((//ul)[1]/li)[1]/div/ol/li/*)[3]/self::div[@class=\"paragraph\"]/p[text()=\"numbered 1.1 paragraph\"]", output, 1);
                assertXpath("((//ul)[1]/li)[1]/div/ol/li/div[@class=\"ulist\"]/ul/li", output, 1);
                assertXpath("((//ul)[1]/li)[1]/div/ol/li/div[@class=\"ulist\"]/ul/li/*", output, 1);
                assertXpath("((//ul)[1]/li)[1]/div/ol/li/div[@class=\"ulist\"]/ul/li/p[text()=\"bullet 1.1.1\"]", output, 1);
            }

            @Test
            @DisplayName("repeated list continuations should attach to list items at respective levels")
            void repeatedListContinuationsShouldAttachToListItemsAtRespectiveLevels() throws Exception {
                var input = """
                        == Lists

                        . list item 1

                        * nested list item 1
                        +
                        --
                        open block for nested list item 1
                        --
                        +
                        * nested list item 2
                        +
                        paragraph for nested list item 2

                        +
                        paragraph for list item 1

                        . list item 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".olist ol", output, 1);
                assertCss(".olist ol > li", output, 2);
                assertCss(".ulist ul", output, 1);
                assertCss(".ulist ul > li", output, 2);
                assertCss(".olist .ulist", output, 1);
                assertXpath("(//ol/li)[1]/*", output, 3);
                assertXpath("((//ol/li)[1]/*)[1]/self::p", output, 1);
                assertXpath("((//ol/li)[1]/*)[1]/self::p[text()=\"list item 1\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li", output, 2);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[1]/*", output, 2);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[1]/p", output, 1);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[1]/div[@class=\"openblock\"]", output, 1);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[2]/*", output, 2);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[2]/p", output, 1);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[2]/div[@class=\"paragraph\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[3]/self::div[@class=\"paragraph\"]", output, 1);
            }

            @Test
            @DisplayName("repeated list continuations attached directly to list item should attach to list items at respective levels")
            void repeatedListContinuationsAttachedDirectlyToListItemShouldAttachToListItemsAtRespectiveLevels() throws Exception {
                var input = """
                        == Lists

                        . list item 1
                        +
                        * nested list item 1
                        +
                        --
                        open block for nested list item 1
                        --
                        +
                        * nested list item 2
                        +
                        paragraph for nested list item 2

                        +
                        paragraph for list item 1

                        . list item 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".olist ol", output, 1);
                assertCss(".olist ol > li", output, 2);
                assertCss(".ulist ul", output, 1);
                assertCss(".ulist ul > li", output, 2);
                assertCss(".olist .ulist", output, 1);
                assertXpath("(//ol/li)[1]/*", output, 3);
                assertXpath("((//ol/li)[1]/*)[1]/self::p", output, 1);
                assertXpath("((//ol/li)[1]/*)[1]/self::p[text()=\"list item 1\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li", output, 2);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[1]/*", output, 2);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[1]/p", output, 1);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[1]/div[@class=\"openblock\"]", output, 1);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[2]/*", output, 2);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[2]/p", output, 1);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[2]/div[@class=\"paragraph\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[3]/self::div[@class=\"paragraph\"]", output, 1);
            }

            @Test
            @DisplayName("repeated list continuations should attach to list items at respective levels ignoring blank lines")
            void repeatedListContinuationsShouldAttachToListItemsAtRespectiveLevelsIgnoringBlankLines() throws Exception {
                var input = """
                        == Lists

                        . list item 1
                        +
                        * nested list item 1
                        +
                        --
                        open block for nested list item 1
                        --
                        +
                        * nested list item 2
                        +
                        paragraph for nested list item 2


                        +
                        paragraph for list item 1

                        . list item 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".olist ol", output, 1);
                assertCss(".olist ol > li", output, 2);
                assertCss(".ulist ul", output, 1);
                assertCss(".ulist ul > li", output, 2);
                assertCss(".olist .ulist", output, 1);
                assertXpath("(//ol/li)[1]/*", output, 3);
                assertXpath("((//ol/li)[1]/*)[1]/self::p", output, 1);
                assertXpath("((//ol/li)[1]/*)[1]/self::p[text()=\"list item 1\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li", output, 2);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[1]/*", output, 2);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[1]/p", output, 1);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[1]/div[@class=\"openblock\"]", output, 1);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[2]/*", output, 2);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[2]/p", output, 1);
                assertXpath("(((//ol/li)[1]/*)[2]/self::div[@class=\"ulist\"]/ul/li)[2]/div[@class=\"paragraph\"]", output, 1);
                assertXpath("((//ol/li)[1]/*)[3]/self::div[@class=\"paragraph\"]", output, 1);
            }

            @Test
            @DisplayName("trailing list continuations should ignore preceding blank lines")
            void trailingListContinuationsShouldIgnorePrecedingBlankLines() throws Exception {
                var input = """
                        == Lists

                        * bullet 1
                        ** bullet 1.1
                        *** bullet 1.1.1
                        +
                        --
                        open block
                        --


                        +
                        bullet 1.1 paragraph


                        +
                        bullet 1 paragraph

                        * bullet 2
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("((//ul)[1]/li[1])/*", output, 3);
                assertXpath("(((//ul)[1]/li[1])/*)[1]/self::p[text()=\"bullet 1\"]", output, 1);
                assertXpath("(((//ul)[1]/li[1])/*)[2]/self::div[@class=\"ulist\"]", output, 1);
                assertXpath("(((//ul)[1]/li[1])/*)[3]/self::div[@class=\"paragraph\"]/p[text()=\"bullet 1 paragraph\"]", output, 1);
                assertXpath("((//ul)[1]/li)[1]/div[@class=\"ulist\"]/ul/li", output, 1);
                assertXpath("((//ul)[1]/li)[1]/div[@class=\"ulist\"]/ul/li/*", output, 3);
                assertXpath("(((//ul)[1]/li)[1]/div[@class=\"ulist\"]/ul/li/*)[1]/self::p[text()=\"bullet 1.1\"]", output, 1);
                assertXpath("(((//ul)[1]/li)[1]/div[@class=\"ulist\"]/ul/li/*)[2]/self::div[@class=\"ulist\"]", output, 1);
                assertXpath("(((//ul)[1]/li)[1]/div[@class=\"ulist\"]/ul/li/*)[3]/self::div[@class=\"paragraph\"]/p[text()=\"bullet 1.1 paragraph\"]", output, 1);
                assertXpath("((//ul)[1]/li)[1]/div[@class=\"ulist\"]/ul/li/div[@class=\"ulist\"]/ul/li", output, 1);
                assertXpath("((//ul)[1]/li)[1]/div[@class=\"ulist\"]/ul/li/div[@class=\"ulist\"]/ul/li/*", output, 2);
                assertXpath("(((//ul)[1]/li)[1]/div[@class=\"ulist\"]/ul/li/div[@class=\"ulist\"]/ul/li/*)[1]/self::p", output, 1);
                assertXpath("(((//ul)[1]/li)[1]/div[@class=\"ulist\"]/ul/li/div[@class=\"ulist\"]/ul/li/*)[2]/self::div[@class=\"openblock\"]", output, 1);
            }

            @Test
            @DisplayName("indented outline list item with different marker offset by a blank line should be recognized as a nested list")
            void indentedOutlineListItemWithDifferentMarkerOffsetByABlankLineShouldBeRecognizedAsANestedList() throws Exception {
                var input = """
                        * item 1

                          . item 1.1
                        +
                        attached paragraph

                          . item 1.2
                        +
                        attached paragraph

                        * item 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul", output, 1);
                assertCss("ol", output, 1);
                assertCss("ul ol", output, 1);
                assertCss("ul > li", output, 2);
                assertXpath("((//ul/li)[1]/*)", output, 2);
                assertXpath("((//ul/li)[1]/*)[1]/self::p", output, 1);
                assertXpath("((//ul/li)[1]/*)[2]/self::div/ol", output, 1);
                assertXpath("((//ul/li)[1]/*)[2]/self::div/ol/li", output, 2);
                for (var idx : range(1, 2)) {
                    assertXpath(("(((//ul/li)[1]/*)[2]/self::div/ol/li)[" + str(idx) + "]/*"), output, 2);
                    assertXpath(("((((//ul/li)[1]/*)[2]/self::div/ol/li)[" + str(idx) + "]/*)[1]/self::p"), output, 1);
                    assertXpath(("((((//ul/li)[1]/*)[2]/self::div/ol/li)[" + str(idx) + "]/*)[2]/self::div[@class=\"paragraph\"]"), output, 1);
                }
            }

            @Test
            @DisplayName("indented description list item inside outline list item offset by a blank line should be recognized as a nested list")
            void indentedDescriptionListItemInsideOutlineListItemOffsetByABlankLineShouldBeRecognizedAsANestedList() throws Exception {
                var input = """
                        * item 1

                          term a:: description a
                        +
                        attached paragraph

                          term b:: description b
                        +
                        attached paragraph

                        * item 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul", output, 1);
                assertCss("dl", output, 1);
                assertCss("ul dl", output, 1);
                assertCss("ul > li", output, 2);
                assertXpath("((//ul/li)[1]/*)", output, 2);
                assertXpath("((//ul/li)[1]/*)[1]/self::p", output, 1);
                assertXpath("((//ul/li)[1]/*)[2]/self::div/dl", output, 1);
                assertXpath("((//ul/li)[1]/*)[2]/self::div/dl/dt", output, 2);
                assertXpath("((//ul/li)[1]/*)[2]/self::div/dl/dd", output, 2);
                for (var idx : range(1, 2)) {
                    assertXpath(("(((//ul/li)[1]/*)[2]/self::div/dl/dd)[" + str(idx) + "]/*"), output, 2);
                    assertXpath(("((((//ul/li)[1]/*)[2]/self::div/dl/dd)[" + str(idx) + "]/*)[1]/self::p"), output, 1);
                    assertXpath(("((((//ul/li)[1]/*)[2]/self::div/dl/dd)[" + str(idx) + "]/*)[2]/self::div[@class=\"paragraph\"]"), output, 1);
                }
            }

            @Test
            @DisplayName("consecutive list continuation lines are folded")
            void consecutiveListContinuationLinesAreFolded() throws Exception {
                var input = """
                        Lists
                        =====

                        * Item one, paragraph one
                        +
                        +
                        Item one, paragraph two
                        +
                        +
                        * Item two
                        +
                        +
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//ul", output, 1);
                assertXpath("//ul/li", output, 2);
                assertXpath("//ul/li[1]/p", output, 1);
                assertXpath("//ul/li[1]/div/p", output, 1);
                assertXpath("//ul/li[1]//p[text() = \"Item one, paragraph one\"]", output, 1);
                assertXpath("//ul/li[1]//p[text() = \"+\nItem one, paragraph two\"]", output, 1);
            }

            @Test
            @DisplayName("should warn if unterminated block is detected in list item")
            void shouldWarnIfUnterminatedBlockIsDetectedInListItem() throws Exception {
                var input = """
                        * item
                        +
                        ====
                        example
                        * swallowed item
                        """;
                try (var logger = usingMemoryLogger()) {
                    var output = convertStringToEmbedded(input);
                    assertXpath("//ul/li", output, 1);
                    assertXpath("//ul/li/*[@class=\"exampleblock\"]", output, 1);
                    assertXpath("//p[text()=\"example\n* swallowed item\"]", output, 1);
                    assertMessage(logger, "WARN", "<stdin>: line 3: unterminated example block", LogMessage.class);
                }
            }
        }
    }

    @Nested
    @DisplayName("Ordered lists (:olist)")
    class OrderedListsOlist {

        @Nested
        @DisplayName("Simple lists")
        class SimpleLists2 {

            @Test
            @DisplayName("dot elements with no blank lines")
            void dotElementsWithNoBlankLines() throws Exception {
                var input = """
                        List
                        ====

                        . Foo
                        . Boo
                        . Blech
                        """;
                var output = convertString(input);
                assertXpath("//ol", output, 1);
                assertCss("ol[start]", output, 0);
                assertXpath("//ol/li", output, 3);
            }

            @Test
            @DisplayName("indented dot elements using spaces")
            void indentedDotElementsUsingSpaces() throws Exception {
                var input = """
                         . Foo
                         . Boo
                         . Blech
                        """;
                var output = convertString(input);
                assertXpath("//ol", output, 1);
                assertXpath("//ol/li", output, 3);
            }

            @Test
            @DisplayName("indented dot elements using tabs")
            void indentedDotElementsUsingTabs() throws Exception {
                var input = """
                        	.	Foo
                        	.	Boo
                        	.	Blech
                        """;
                var output = convertString(input);
                assertXpath("//ol", output, 1);
                assertXpath("//ol/li", output, 3);
            }

            @Test
            @DisplayName("should represent explicit role attribute as style class")
            void shouldRepresentExplicitRoleAttributeAsStyleClass() throws Exception {
                var input = """
                        [role="dry"]
                        . Once
                        . Again
                        . Refactor!
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".olist.arabic.dry", output, 1);
                assertCss(".olist ol.arabic", output, 1);
            }

            @Test
            @DisplayName("should base list style on marker length rather than list depth")
            void shouldBaseListStyleOnMarkerLengthRatherThanListDepth() throws Exception {
                var input = """
                        ... parent
                        .. child
                        . grandchild
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".olist.lowerroman", output, 1);
                assertCss(".olist.lowerroman .olist.loweralpha", output, 1);
                assertCss(".olist.lowerroman .olist.loweralpha .olist.arabic", output, 1);
            }

            @Test
            @DisplayName("should allow list style to be specified explicitly when using markers with implicit style")
            void shouldAllowListStyleToBeSpecifiedExplicitlyWhenUsingMarkersWithImplicitStyle() throws Exception {
                var input = """
                        [loweralpha]
                        i) 1
                        ii) 2
                        iii) 3
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".olist.loweralpha", output, 1);
                assertCss(".olist.lowerroman", output, 0);
            }

            @Test
            @DisplayName("should represent custom numbering and explicit role attribute as style classes")
            void shouldRepresentCustomNumberingAndExplicitRoleAttributeAsStyleClasses() throws Exception {
                var input = """
                        [loweralpha, role="dry"]
                        . Once
                        . Again
                        . Refactor!
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".olist.loweralpha.dry", output, 1);
                assertCss(".olist ol.loweralpha", output, 1);
            }

            @Test
            @DisplayName("should set reversed attribute on list if reversed option is set")
            void shouldSetReversedAttributeOnListIfReversedOptionIsSet() throws Exception {
                var input = """
                        [%reversed, start=3]
                        . three
                        . two
                        . one
                        . blast off!
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ol[reversed][start=\"3\"]", output, 1);
            }

            @Test
            @DisplayName("should represent implicit role attribute as style class")
            void shouldRepresentImplicitRoleAttributeAsStyleClass() throws Exception {
                var input = """
                        [.dry]
                        . Once
                        . Again
                        . Refactor!
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".olist.arabic.dry", output, 1);
                assertCss(".olist ol.arabic", output, 1);
            }

            @Test
            @DisplayName("should represent custom numbering and implicit role attribute as style classes")
            void shouldRepresentCustomNumberingAndImplicitRoleAttributeAsStyleClasses() throws Exception {
                var input = """
                        [loweralpha.dry]
                        . Once
                        . Again
                        . Refactor!
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".olist.loweralpha.dry", output, 1);
                assertCss(".olist ol.loweralpha", output, 1);
            }

            @Test
            @DisplayName("dot elements separated by blank lines should merge lists")
            void dotElementsSeparatedByBlankLinesShouldMergeLists() throws Exception {
                var input = """
                        List
                        ====

                        . Foo

                        . Boo


                        . Blech
                        """;
                var output = convertString(input);
                assertXpath("//ol", output, 1);
                assertXpath("//ol/li", output, 3);
            }

            @Test
            @DisplayName("should escape special characters in all literal paragraphs attached to list item")
            void shouldEscapeSpecialCharactersInAllLiteralParagraphsAttachedToListItem2() throws Exception {
                var input = """
                        . first item

                          <code>text</code>

                          more <code>text</code>

                        . second item
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("li", output, 2);
                assertCss("code", output, 0);
                assertCss("li:first-of-type > *", output, 3);
                assertCss("li:first-of-type pre", output, 2);
                assertXpath("((//li)[1]//pre)[1][text()=\"<code>text</code>\"]", output, 1);
                assertXpath("((//li)[1]//pre)[2][text()=\"more <code>text</code>\"]", output, 1);
            }

            @Test
            @DisplayName("dot elements with interspersed line comments should be skipped and not break list")
            void dotElementsWithInterspersedLineCommentsShouldBeSkippedAndNotBreakList() throws Exception {
                var input = """
                        == List

                        . Foo
                        // line comment
                        // another line comment
                        . Boo
                        // line comment
                        more text
                        // another line comment
                        . Blech
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//ol", output, 1);
                assertXpath("//ol/li", output, 3);
                assertXpath("(//ol/li)[2]/p[text()=\"Boo\nmore text\"]", output, 1);
            }

            @Test
            @DisplayName("dot elements separated by line comment offset by blank lines should not merge lists")
            void dotElementsSeparatedByLineCommentOffsetByBlankLinesShouldNotMergeLists() throws Exception {
                var input = """
                        List
                        ====

                        . Foo
                        . Boo

                        //

                        . Blech
                        """;
                var output = convertString(input);
                assertXpath("//ol", output, 2);
                assertXpath("(//ol)[1]/li", output, 2);
                assertXpath("(//ol)[2]/li", output, 1);
            }

            @Test
            @DisplayName("dot elements separated by a block title offset by a blank line should not merge lists")
            void dotElementsSeparatedByABlockTitleOffsetByABlankLineShouldNotMergeLists() throws Exception {
                var input = """
                        List
                        ====

                        . Foo
                        . Boo

                        .Also
                        . Blech
                        """;
                var output = convertString(input);
                assertXpath("//ol", output, 2);
                assertXpath("(//ol)[1]/li", output, 2);
                assertXpath("(//ol)[2]/li", output, 1);
                assertXpath("(//ol)[2]/preceding-sibling::*[@class = \"title\"][text() = \"Also\"]", output, 1);
            }

            @Test
            @DisplayName("dot elements separated by an attribute entry offset by a blank line should not merge lists")
            void dotElementsSeparatedByAnAttributeEntryOffsetByABlankLineShouldNotMergeLists() throws Exception {
                var input = """
                        == List

                        . Foo
                        . Boo

                        :foo: bar
                        . Blech
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//ol", output, 2);
                assertXpath("(//ol)[1]/li", output, 2);
                assertXpath("(//ol)[2]/li", output, 1);
            }

            @Test
            @DisplayName("should honor start attribute on ordered list")
            void shouldHonorStartAttributeOnOrderedList() throws Exception {
                var input = """
                        == List

                        [start=7]
                        . item 7
                        . item 8
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ol.arabic", output, 1);
                assertCss("ol[start=7]", output, 1);
            }

            @Test
            @DisplayName("should allow value of start attribute to be 0")
            void shouldAllowValueOfStartAttributeToBe0() throws Exception {
                var input = """
                        == List

                        [start=0]
                        . item 0
                        . item 1
                        . item 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ol.arabic", output, 1);
                assertCss("ol[start=0]", output, 1);
            }

            @Test
            @DisplayName("should allow value of start attribute to be negative")
            void shouldAllowValueOfStartAttributeToBeNegative() throws Exception {
                var input = """
                        == List

                        [start=-10]
                        . -10
                        . -9
                        . -8
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ol.arabic", output, 1);
                assertCss("ol[start=-10]", output, 1);
            }

            @Test
            @DisplayName("should use start number in docbook5 backend")
            void shouldUseStartNumberInDocbook5Backend() throws Exception {
                var input = """
                        == List

                        [start=7]
                        . item 7
                        . item 8
                        """;
                var output = convertStringToEmbedded(input, Options.of("backend", "docbook5"));
                assertXpath("//orderedlist", output, 1);
                assertXpath("(//orderedlist)/listitem", output, 2);
                assertXpath("(//orderedlist)[@startingnumber = \"7\"]", output, 1);
            }

            @Test
            @DisplayName("should not warn if explicit numbering starts at value of start attribute")
            void shouldNotWarnIfExplicitNumberingStartsAtValueOfStartAttribute() throws Exception {
                var input = """
                        == List

                        [start=7]
                        7. item 7
                        8. item 8
                        """;
                try (var logger = usingMemoryLogger()) {
                    var output = convertStringToEmbedded(input);
                    assertCss("ol[start=7]", output, 1);
                    assertCss("ol.arabic", output, 1);
                    assertCss("ol li", output, 2);
                    assertEmpty(logger.getMessages());
                }
            }

            @Test
            @DisplayName("should implicitly set start on ordered list if explicit arabic numbering does not start at 1")
            void shouldImplicitlySetStartOnOrderedListIfExplicitArabicNumberingDoesNotStartAt1() throws Exception {
                var input = """
                        == List

                        7. item 7
                        8. item 8
                        """;
                try (var logger = usingMemoryLogger()) {
                    var output = convertStringToEmbedded(input);
                    assertCss("ol[start=7]", output, 1);
                    assertCss("ol.arabic", output, 1);
                    assertCss("ol li", output, 2);
                    assertEmpty(logger.getMessages());
                }
            }

            @Test
            @DisplayName("should implicitly set start on ordered list if explicit roman numbering does not start at 1")
            void shouldImplicitlySetStartOnOrderedListIfExplicitRomanNumberingDoesNotStartAt1() throws Exception {
                var input = """
                        == List

                        IV) item 4
                        V) item 5
                        """;
                try (var logger = usingMemoryLogger()) {
                    var output = convertStringToEmbedded(input);
                    assertCss("ol[start=4]", output, 1);
                    assertCss("ol.upperroman", output, 1);
                    assertCss("ol li", output, 2);
                    assertEmpty(logger.getMessages());
                }
            }

            @Test
            @DisplayName("should warn if item with explicit numbering in ordered list is out of sequence")
            void shouldWarnIfItemWithExplicitNumberingInOrderedListIsOutOfSequence() throws Exception {
                var input = """
                        == List

                        x. x
                        z. z
                        """;
                try (var logger = usingMemoryLogger()) {
                    var output = convertStringToEmbedded(input);
                    assertCss("ol[start=24]", output, 1);
                    assertCss("ol.loweralpha", output, 1);
                    assertCss("ol li", output, 2);
                    assertMessage(logger, "WARN", "<stdin>: line 4: list item index: expected y, got z", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should match trailing line separator in text of list item")
            void shouldMatchTrailingLineSeparatorInTextOfListItem2() throws Exception {
                var input = Strings.chop((". a\n. b" + str(decodeChar(8232)) + "\n. c\n"));
                var output = convertString(input);
                assertCss("li", output, 3);
                assertXpath(("(//li)[2]/p[text()=\"b" + str(decodeChar(8232)) + "\"]"), output, 1);
            }

            @Test
            @DisplayName("should match line separator in text of list item")
            void shouldMatchLineSeparatorInTextOfListItem2() throws Exception {
                var input = Strings.chop((". a\n. b" + str(decodeChar(8232)) + "b\n. c\n"));
                var output = convertString(input);
                assertCss("li", output, 3);
                assertXpath(("(//li)[2]/p[text()=\"b" + str(decodeChar(8232)) + "b\"]"), output, 1);
            }
        }

        @Test
        @DisplayName("should warn if explicit uppercase roman numerals in list are out of sequence")
        void shouldWarnIfExplicitUppercaseRomanNumeralsInListAreOutOfSequence() throws Exception {
            var input = """
                    I) one
                    III) three
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertXpath("//ol/li", output, 2);
                assertMessage(logger, "WARN", "<stdin>: line 2: list item index: expected II, got III", LogMessage.class);
            }
        }

        @Test
        @DisplayName("should warn if explicit lowercase roman numerals in list are out of sequence")
        void shouldWarnIfExplicitLowercaseRomanNumeralsInListAreOutOfSequence() throws Exception {
            var input = """
                    i) one
                    iii) three
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertXpath("//ol/li", output, 2);
                assertMessage(logger, "WARN", "<stdin>: line 2: list item index: expected ii, got iii", LogMessage.class);
            }
        }
    }

    @Nested
    @DisplayName("Description lists (:dlist)")
    class DescriptionListsDlist {

        @Nested
        @DisplayName("Simple lists")
        class SimpleLists3 {

            @Test
            @DisplayName("should not parse a bare dlist delimiter as a dlist")
            void shouldNotParseABareDlistDelimiterAsADlist() throws Exception {
                var input = "::";
                var output = convertStringToEmbedded(input);
                assertCss("dl", output, 0);
                assertXpath("//p[text()=\"::\"]", output, 1);
            }

            @Test
            @DisplayName("should not parse an indented bare dlist delimiter as a dlist")
            void shouldNotParseAnIndentedBareDlistDelimiterAsADlist() throws Exception {
                var input = " ::";
                var output = convertStringToEmbedded(input);
                assertCss("dl", output, 0);
                assertXpath("//pre[text()=\"::\"]", output, 1);
            }

            @Test
            @DisplayName("should parse a dlist delimiter preceded by a blank attribute as a dlist")
            void shouldParseADlistDelimiterPrecededByABlankAttributeAsADlist() throws Exception {
                var input = "{blank}::";
                var output = convertStringToEmbedded(input);
                assertCss("dl", output, 1);
                assertCss("dl > dt", output, 1);
                assertCss("dl > dt:empty", output, 1);
            }

            @Test
            @DisplayName("should parse a dlist if term is include and principal text is []")
            void shouldParseADlistIfTermIsIncludeAndPrincipalTextIs() throws Exception {
                var input = "include:: []";
                var output = convertStringToEmbedded(input);
                assertCss("dl", output, 1);
                assertCss("dl > dt", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \"[]\"]", output, 1);
            }

            @Test
            @DisplayName("should parse a dlist if term is include and principal text matches macro form")
            void shouldParseADlistIfTermIsIncludeAndPrincipalTextMatchesMacroForm() throws Exception {
                var input = "include:: pass:[${placeholder}]";
                var output = convertStringToEmbedded(input);
                assertCss("dl", output, 1);
                assertCss("dl > dt", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \"${placeholder}\"]", output, 1);
            }

            @Test
            @DisplayName("single-line adjacent elements")
            void singleLineAdjacentElements() throws Exception {
                var input = """
                        term1:: def1
                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dt/following-sibling::dd", output, 2);
                assertXpath("(//dl/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("should parse sibling items using same rules")
            void shouldParseSiblingItemsUsingSameRules() throws Exception {
                var input = """
                        term1;; ;; def1
                        term2;; ;; def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dt/following-sibling::dd", output, 2);
                assertXpath("(//dl/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \";; def1\"]", output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd/p[text() = \";; def2\"]", output, 1);
            }

            @Test
            @DisplayName("should allow term to end with a semicolon when using double semicolon delimiter")
            void shouldAllowTermToEndWithASemicolonWhenUsingDoubleSemicolonDelimiter() throws Exception {
                var input = "term;;; def\n";
                var output = convertStringToEmbedded(input);
                assertCss("dl", output, 1);
                assertCss("dl > dt", output, 1);
                assertXpath("(//dl/dt)[1][text() = \"term;\"]", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \"def\"]", output, 1);
            }

            @Test
            @DisplayName("single-line indented adjacent elements")
            void singleLineIndentedAdjacentElements() throws Exception {
                var input = """
                        term1:: def1
                         term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dt/following-sibling::dd", output, 2);
                assertXpath("(//dl/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("single-line indented adjacent elements with tabs")
            void singleLineIndentedAdjacentElementsWithTabs() throws Exception {
                var input = """
                        term1::	def1
                        	term2::	def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dt/following-sibling::dd", output, 2);
                assertXpath("(//dl/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("single-line elements separated by blank line should create a single list")
            void singleLineElementsSeparatedByBlankLineShouldCreateASingleList() throws Exception {
                var input = """
                        term1:: def1

                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dt/following-sibling::dd", output, 2);
            }

            @Test
            @DisplayName("a line comment between elements should divide them into separate lists")
            void aLineCommentBetweenElementsShouldDivideThemIntoSeparateLists() throws Exception {
                var input = """
                        term1:: def1

                        //

                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl/dt", output, 2);
                assertXpath("(//dl)[1]/dt", output, 1);
                assertXpath("(//dl)[2]/dt", output, 1);
            }

            @Test
            @DisplayName("a ruler between elements should divide them into separate lists")
            void aRulerBetweenElementsShouldDivideThemIntoSeparateLists() throws Exception {
                var input = """
                        term1:: def1

                        '''

                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl//hr", output, 0);
                assertXpath("(//dl)[1]/dt", output, 1);
                assertXpath("(//dl)[2]/dt", output, 1);
            }

            @Test
            @DisplayName("a block title between elements should divide them into separate lists")
            void aBlockTitleBetweenElementsShouldDivideThemIntoSeparateLists() throws Exception {
                var input = """
                        term1:: def1

                        .Some more
                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl/dt", output, 2);
                assertXpath("(//dl)[1]/dt", output, 1);
                assertXpath("(//dl)[2]/dt", output, 1);
                assertXpath("(//dl)[2]/preceding-sibling::*[@class=\"title\"][text() = \"Some more\"]", output, 1);
            }

            @Test
            @DisplayName("multi-line elements with paragraph content")
            void multiLineElementsWithParagraphContent() throws Exception {
                var input = """
                        term1::
                        def1
                        term2::
                        def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dt/following-sibling::dd", output, 2);
                assertXpath("(//dl/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("multi-line elements with indented paragraph content")
            void multiLineElementsWithIndentedParagraphContent() throws Exception {
                var input = """
                        term1::
                         def1
                        term2::
                          def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dt/following-sibling::dd", output, 2);
                assertXpath("(//dl/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("multi-line elements with indented paragraph content that includes comment lines")
            void multiLineElementsWithIndentedParagraphContentThatIncludesCommentLines() throws Exception {
                var input = """
                        term1::
                         def1
                        // comment
                        term2::
                          def2
                        // comment
                          def2 continued
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dt/following-sibling::dd", output, 2);
                assertXpath("(//dl/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd/p[text() = \"def2\ndef2 continued\"]", output, 1);
            }

            @Test
            @DisplayName("should not strip comment line in literal paragraph block attached to list item")
            void shouldNotStripCommentLineInLiteralParagraphBlockAttachedToListItem() throws Exception {
                var input = """
                        term1::
                        +
                         line 1
                        // not a comment
                         line 3
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"literalblock\"]", output, 1);
                assertXpath("//*[@class=\"literalblock\"]//pre[text()=\" line 1\n// not a comment\n line 3\"]", output, 1);
            }

            @Test
            @DisplayName("should escape special characters in all literal paragraphs attached to list item")
            void shouldEscapeSpecialCharactersInAllLiteralParagraphsAttachedToListItem3() throws Exception {
                var input = """
                        term:: desc

                          <code>text</code>

                          more <code>text</code>

                        another term::

                          <code>text</code> in a paragraph
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("dt", output, 2);
                assertCss("code", output, 0);
                assertCss("dd:first-of-type > *", output, 3);
                assertCss("dd:first-of-type pre", output, 2);
                assertXpath("((//dd)[1]//pre)[1][text()=\"<code>text</code>\"]", output, 1);
                assertXpath("((//dd)[1]//pre)[2][text()=\"more <code>text</code>\"]", output, 1);
                assertXpath("((//dd)[2]//p)[1][text()=\"<code>text</code> in a paragraph\"]", output, 1);
            }

            @Test
            @DisplayName("multi-line element with paragraph starting with multiple dashes should not be seen as list")
            void multiLineElementWithParagraphStartingWithMultipleDashesShouldNotBeSeenAsList() throws Exception {
                var input = """
                        term1::
                          def1
                          -- and a note

                        term2::
                          def2
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dt/following-sibling::dd", output, 2);
                assertXpath("(//dl/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath(("(//dl/dt)[1]/following-sibling::dd/p[text() = \"def1" + str(decodeChar(8201)) + str(decodeChar(8212)) + str(decodeChar(8201)) + "and a note\"]"), output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("multi-line element with multiple terms")
            void multiLineElementWithMultipleTerms() throws Exception {
                var input = """
                        term1::
                        term2::
                        def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dd", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dt", output, 1);
                assertXpath("(//dl/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("consecutive terms share same varlistentry in docbook")
            void consecutiveTermsShareSameVarlistentryInDocbook() throws Exception {
                var input = """
                        term::
                        alt term::
                        description

                        last::
                        """;
                var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
                assertXpath("//varlistentry", output, 2);
                assertXpath("(//varlistentry)[1]/term", output, 2);
                assertXpath("(//varlistentry)[2]/term", output, 1);
                assertXpath("(//varlistentry)[2]/listitem", output, 1);
                assertXpath("(//varlistentry)[2]/listitem[normalize-space(text())=\"\"]", output, 1);
            }

            @Test
            @DisplayName("multi-line elements with blank line before paragraph content")
            void multiLineElementsWithBlankLineBeforeParagraphContent() throws Exception {
                var input = """
                        term1::

                        def1
                        term2::

                        def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dt/following-sibling::dd", output, 2);
                assertXpath("(//dl/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("multi-line elements with paragraph and literal content")
            void multiLineElementsWithParagraphAndLiteralContent() throws Exception {
                var input = """
                        term1::
                        def1

                          literal

                        term2::
                          def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dt/following-sibling::dd", output, 2);
                assertXpath("//dl/dt/following-sibling::dd//pre", output, 1);
                assertXpath("(//dl/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("mixed single and multi-line adjacent elements")
            void mixedSingleAndMultiLineAdjacentElements() throws Exception {
                var input = """
                        term1:: def1
                        term2::
                        def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dt/following-sibling::dd", output, 2);
                assertXpath("(//dl/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl/dt)[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl/dt)[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("should discover anchor at start of description term text and register it as a reference")
            void shouldDiscoverAnchorAtStartOfDescriptionTermTextAndRegisterItAsAReference() throws Exception {
                var input = """
                        The highest peak in the Front Range is <<grays-peak>>, which tops <<mount-evans>> by just a few feet.

                        [[mount-evans,Mount Evans]]Mount Evans:: 14,271 feet
                        [[grays-peak]]Grays Peak:: 14,278 feet
                        """;
                var doc = documentFromString(input);
                var refs = doc.getCatalog().getRefs();
                assertTruthy(refs.containsKey("mount-evans"));
                assertTruthy(refs.containsKey("grays-peak"));
                var output = doc.convert(Options.of("standalone", false));
                assertXpath("(//p)[1]/a[@href=\"#grays-peak\"][text()=\"Grays Peak\"]", output, 1);
                assertXpath("(//p)[1]/a[@href=\"#mount-evans\"][text()=\"Mount Evans\"]", output, 1);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dt", output, 2);
                assertXpath("(//dl/dt)[1]/a[@id=\"mount-evans\"]", output, 1);
                assertXpath("(//dl/dt)[2]/a[@id=\"grays-peak\"]", output, 1);
            }

            @Test
            @DisplayName("missing space before term does not produce description list")
            void missingSpaceBeforeTermDoesNotProduceDescriptionList() throws Exception {
                var input = """
                        term1::def1
                        term2::def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 0);
            }

            @Test
            @DisplayName("literal block inside description list")
            void literalBlockInsideDescriptionList() throws Exception {
                var input = """
                        term::
                        +
                        ....
                        literal, line 1

                        literal, line 2
                        ....
                        anotherterm:: def
                        """;
                var output = convertString(input);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dd", output, 2);
                assertXpath("//dl/dd//pre", output, 1);
                assertXpath("(//dl/dd)[1]/*[@class=\"literalblock\"]//pre", output, 1);
                assertXpath("(//dl/dd)[2]/p[text() = \"def\"]", output, 1);
            }

            @Test
            @DisplayName("literal block inside description list with trailing line continuation")
            void literalBlockInsideDescriptionListWithTrailingLineContinuation() throws Exception {
                var input = """
                        term::
                        +
                        ....
                        literal, line 1

                        literal, line 2
                        ....
                        +
                        anotherterm:: def
                        """;
                var output = convertString(input);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dd", output, 2);
                assertXpath("//dl/dd//pre", output, 1);
                assertXpath("(//dl/dd)[1]/*[@class=\"literalblock\"]//pre", output, 1);
                assertXpath("(//dl/dd)[2]/p[text() = \"def\"]", output, 1);
            }

            @Test
            @DisplayName("multiple listing blocks inside description list")
            void multipleListingBlocksInsideDescriptionList() throws Exception {
                var input = """
                        term::
                        +
                        ----
                        listing, line 1

                        listing, line 2
                        ----
                        +
                        ----
                        listing, line 1

                        listing, line 2
                        ----
                        anotherterm:: def
                        """;
                var output = convertString(input);
                assertXpath("//dl/dt", output, 2);
                assertXpath("//dl/dd", output, 2);
                assertXpath("//dl/dd//pre", output, 2);
                assertXpath("(//dl/dd)[1]/*[@class=\"listingblock\"]//pre", output, 2);
                assertXpath("(//dl/dd)[2]/p[text() = \"def\"]", output, 1);
            }

            @Test
            @DisplayName("open block inside description list")
            void openBlockInsideDescriptionList() throws Exception {
                var input = """
                        term::
                        +
                        --
                        Open block as description of term.

                        And some more detail...
                        --
                        anotherterm:: def
                        """;
                var output = convertString(input);
                assertXpath("//dl/dd//p", output, 3);
                assertXpath("(//dl/dd)[1]//*[@class=\"openblock\"]//p", output, 2);
            }

            @Test
            @DisplayName("paragraph attached by a list continuation on either side in a description list")
            void paragraphAttachedByAListContinuationOnEitherSideInADescriptionList() throws Exception {
                var input = """
                        term1:: def1
                        +
                        more detail
                        +
                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("(//dl/dt)[1][normalize-space(text())=\"term1\"]", output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text())=\"term2\"]", output, 1);
                assertXpath("(//dl/dd)[1]//p", output, 2);
                assertXpath("((//dl/dd)[1]//p)[1][text()=\"def1\"]", output, 1);
                assertXpath("(//dl/dd)[1]/p/following-sibling::*[@class=\"paragraph\"]/p[text() = \"more detail\"]", output, 1);
            }

            @Test
            @DisplayName("paragraph attached by a list continuation on either side to a multi-line element in a description list")
            void paragraphAttachedByAListContinuationOnEitherSideToAMultiLineElementInADescriptionList() throws Exception {
                var input = """
                        term1::
                        def1
                        +
                        more detail
                        +
                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("(//dl/dt)[1][normalize-space(text())=\"term1\"]", output, 1);
                assertXpath("(//dl/dt)[2][normalize-space(text())=\"term2\"]", output, 1);
                assertXpath("(//dl/dd)[1]//p", output, 2);
                assertXpath("((//dl/dd)[1]//p)[1][text()=\"def1\"]", output, 1);
                assertXpath("(//dl/dd)[1]/p/following-sibling::*[@class=\"paragraph\"]/p[text() = \"more detail\"]", output, 1);
            }

            @Test
            @DisplayName("should continue to parse subsequent blocks attached to list item after first block is dropped")
            void shouldContinueToParseSubsequentBlocksAttachedToListItemAfterFirstBlockIsDropped() throws Exception {
                var input = """
                        :attribute-missing: drop-line

                        term::
                        +
                        image::{unresolved}[]
                        +
                        paragraph
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("dl", output, 1);
                assertCss("dl > dt", output, 1);
                assertCss("dl > dt + dd", output, 1);
                assertCss("dl > dt + dd > .imageblock", output, 0);
                assertCss("dl > dt + dd > .paragraph", output, 1);
            }

            @Test
            @DisplayName("verse paragraph inside a description list")
            void verseParagraphInsideADescriptionList() throws Exception {
                var input = """
                        term1:: def
                        +
                        [verse]
                        la la la

                        term2:: def
                        """;
                var output = convertString(input);
                assertXpath("//dl/dd//p", output, 2);
                assertXpath("(//dl/dd)[1]/*[@class=\"verseblock\"]/pre[text() = \"la la la\"]", output, 1);
            }

            @Test
            @DisplayName("list inside a description list")
            void listInsideADescriptionList() throws Exception {
                var input = """
                        term1::
                        * level 1
                        ** level 2
                        * level 1
                        term2:: def
                        """;
                var output = convertString(input);
                assertXpath("//dl/dd", output, 2);
                assertXpath("//dl/dd/p", output, 1);
                assertXpath("(//dl/dd)[1]//ul", output, 2);
                assertXpath("((//dl/dd)[1]//ul)[1]//ul", output, 1);
            }

            @Test
            @DisplayName("list inside a description list offset by blank lines")
            void listInsideADescriptionListOffsetByBlankLines() throws Exception {
                var input = """
                        term1::

                        * level 1
                        ** level 2
                        * level 1

                        term2:: def
                        """;
                var output = convertString(input);
                assertXpath("//dl/dd", output, 2);
                assertXpath("//dl/dd/p", output, 1);
                assertXpath("(//dl/dd)[1]//ul", output, 2);
                assertXpath("((//dl/dd)[1]//ul)[1]//ul", output, 1);
            }

            @Test
            @DisplayName("should only grab one line following last item if item has no inline description")
            void shouldOnlyGrabOneLineFollowingLastItemIfItemHasNoInlineDescription() throws Exception {
                var input = """
                        term1::

                        def1

                        term2::

                        def2

                        A new paragraph

                        Another new paragraph
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dd", output, 2);
                assertXpath("(//dl/dd)[1]/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl/dd)[2]/p[text() = \"def2\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"]", output, 2);
                assertXpath("(//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"])[1]/p[text() = \"A new paragraph\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"])[2]/p[text() = \"Another new paragraph\"]", output, 1);
            }

            @Test
            @DisplayName("should only grab one literal line following last item if item has no inline description")
            void shouldOnlyGrabOneLiteralLineFollowingLastItemIfItemHasNoInlineDescription() throws Exception {
                var input = """
                        term1::

                        def1

                        term2::

                          def2

                        A new paragraph

                        Another new paragraph
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dd", output, 2);
                assertXpath("(//dl/dd)[1]/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl/dd)[2]/p[text() = \"def2\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"]", output, 2);
                assertXpath("(//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"])[1]/p[text() = \"A new paragraph\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"])[2]/p[text() = \"Another new paragraph\"]", output, 1);
            }

            @Test
            @DisplayName("should append subsequent paragraph literals to list item as block content")
            void shouldAppendSubsequentParagraphLiteralsToListItemAsBlockContent() throws Exception {
                var input = """
                        term1::

                        def1

                        term2::

                          def2

                          literal

                        A new paragraph.
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dd", output, 2);
                assertXpath("(//dl/dd)[1]/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl/dd)[2]/p[text() = \"def2\"]", output, 1);
                assertXpath("(//dl/dd)[2]/p/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("(//dl/dd)[2]/p/following-sibling::*[@class=\"literalblock\"]//pre[text() = \"literal\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"])[1]/p[text() = \"A new paragraph.\"]", output, 1);
            }

            @Test
            @DisplayName("should not match comment line that looks like description list term")
            void shouldNotMatchCommentLineThatLooksLikeDescriptionListTerm() throws Exception {
                var input = """
                        before

                        //key:: val

                        after
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("dl", output, 0);
            }

            @Test
            @DisplayName("should not match comment line following list that looks like description list term")
            void shouldNotMatchCommentLineFollowingListThatLooksLikeDescriptionListTerm() throws Exception {
                var input = """
                        * item

                        //term:: desc
                        == Section

                        section text
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("/*[@class=\"ulist\"]", output, 1);
                assertXpath("/*[@class=\"sect1\"]", output, 1);
                assertXpath("/*[@class=\"sect1\"]/h2[text()=\"Section\"]", output, 1);
                assertXpath("/*[@class=\"ulist\"]/following-sibling::*[@class=\"sect1\"]", output, 1);
            }

            @Test
            @DisplayName("should not match comment line that looks like sibling description list term")
            void shouldNotMatchCommentLineThatLooksLikeSiblingDescriptionListTerm() throws Exception {
                var input = """
                        before

                        foo:: bar
                        //yin:: yang

                        after
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".dlist", output, 1);
                assertCss(".dlist dt", output, 1);
                refuteIncludes(output, "yin");
            }

            @Test
            @DisplayName("should not hang on description list item in list that begins with ///")
            void shouldNotHangOnDescriptionListItemInListThatBeginsWith() throws Exception {
                var input = """
                        * a
                        ///b::
                        c
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("ul", output, 1);
                assertCss("ul li dl", output, 1);
                assertXpath("//ul/li/p[text()=\"a\"]", output, 1);
                assertXpath("//dt[text()=\"///b\"]", output, 1);
                assertXpath("//dd/p[text()=\"c\"]", output, 1);
            }

            @Test
            @DisplayName("should not hang on sibling description list item that begins with ///")
            void shouldNotHangOnSiblingDescriptionListItemThatBeginsWith() throws Exception {
                var input = """
                        a::
                        ///b::
                        c
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("dl", output, 1);
                assertXpath("(//dl/dt)[1][text()=\"a\"]", output, 1);
                assertXpath("(//dl/dt)[2][text()=\"///b\"]", output, 1);
                assertXpath("//dl/dd/p[text()=\"c\"]", output, 1);
            }

            @Test
            @DisplayName("should skip dlist term that begins with // unless it begins with ///")
            void shouldSkipDlistTermThatBeginsWithUnlessItBeginsWith() throws Exception {
                var input = """
                        category a::
                        //ignored term:: def

                        category b::
                        ///term:: def
                        """;
                var output = convertStringToEmbedded(input);
                refuteIncludes(output, "ignored term");
                assertXpath("//dt[text()=\"///term\"]", output, 1);
            }

            @Test
            @DisplayName("more than 4 consecutive colons should become part of description list term")
            void moreThan4ConsecutiveColonsShouldBecomePartOfDescriptionListTerm() throws Exception {
                var input = "A term::::: a description\n";
                var output = convertStringToEmbedded(input);
                assertCss("dl", output, 1);
                assertCss("dl > dt", output, 1);
                assertXpath("//dl/dt[text()=\"A term:\"]", output, 1);
                assertXpath("//dl/dd/p[text()=\"a description\"]", output, 1);
            }

            @Test
            @DisplayName("text method of dd node should return nil if dd node only contains blocks")
            void textMethodOfDdNodeShouldReturnNilIfDdNodeOnlyContainsBlocks() throws Exception {
                var input = """
                        term::
                        +
                        paragraph
                        """;
                var doc = documentFromString(input);
                var dd = ((DescriptionListEntry) ((List) doc.getBlocks().get(0)).getItems().get(0)).getDescription();
                assertNull(dd.getText());
            }

            @Test
            @DisplayName("should match trailing line separator in text of list item")
            void shouldMatchTrailingLineSeparatorInTextOfListItem3() throws Exception {
                var input = Strings.chop(("A:: a\nB:: b" + str(decodeChar(8232)) + "\nC:: c\n"));
                var output = convertString(input);
                assertCss("dd", output, 3);
                assertXpath(("(//dd)[2]/p[text()=\"b" + str(decodeChar(8232)) + "\"]"), output, 1);
            }

            @Test
            @DisplayName("should match line separator in text of list item")
            void shouldMatchLineSeparatorInTextOfListItem3() throws Exception {
                var input = Strings.chop(("A:: a\nB:: b" + str(decodeChar(8232)) + "b\nC:: c\n"));
                var output = convertString(input);
                assertCss("dd", output, 3);
                assertXpath(("(//dd)[2]/p[text()=\"b" + str(decodeChar(8232)) + "b\"]"), output, 1);
            }
        }

        @Nested
        @DisplayName("Nested lists")
        class NestedLists2 {

            @Test
            @DisplayName("should not parse a nested dlist delimiter without a term as a dlist")
            void shouldNotParseANestedDlistDelimiterWithoutATermAsADlist() throws Exception {
                var input = """
                        t::
                        ;;
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dd/p[text()=\";;\"]", output, 1);
            }

            @Test
            @DisplayName("should not parse a nested indented dlist delimiter without a term as a dlist")
            void shouldNotParseANestedIndentedDlistDelimiterWithoutATermAsADlist() throws Exception {
                var input = """
                        t::
                        desc
                          ;;
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//dl", output, 1);
                assertXpath("//dl/dd/p[text()=\"desc\n  ;;\"]", output, 1);
            }

            @Test
            @DisplayName("single-line adjacent nested elements")
            void singleLineAdjacentNestedElements() throws Exception {
                var input = """
                        term1:: def1
                        label1::: detail1
                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl//dl", output, 1);
                assertXpath("(//dl)[1]/dt[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("//dl//dl/dt[normalize-space(text()) = \"label1\"]", output, 1);
                assertXpath("//dl//dl/dt/following-sibling::dd/p[text() = \"detail1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl)[1]/dt[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("single-line adjacent maximum nested elements")
            void singleLineAdjacentMaximumNestedElements() throws Exception {
                var input = """
                        term1:: def1
                        label1::: detail1
                        name1:::: value1
                        item1;; price1
                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 4);
                assertXpath("//dl//dl//dl//dl", output, 1);
            }

            @Test
            @DisplayName("single-line nested elements separated by blank line at top level")
            void singleLineNestedElementsSeparatedByBlankLineAtTopLevel() throws Exception {
                var input = """
                        term1:: def1

                        label1::: detail1

                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl//dl", output, 1);
                assertXpath("(//dl)[1]/dt[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("//dl//dl/dt[normalize-space(text()) = \"label1\"]", output, 1);
                assertXpath("//dl//dl/dt/following-sibling::dd/p[text() = \"detail1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl)[1]/dt[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("single-line nested elements separated by blank line at nested level")
            void singleLineNestedElementsSeparatedByBlankLineAtNestedLevel() throws Exception {
                var input = """
                        term1:: def1
                        label1::: detail1

                        label2::: detail2
                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl//dl", output, 1);
                assertXpath("(//dl)[1]/dt[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("//dl//dl/dt[normalize-space(text()) = \"label1\"]", output, 1);
                assertXpath("//dl//dl/dt/following-sibling::dd/p[text() = \"detail1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl)[1]/dt[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("single-line adjacent nested elements with alternate delimiters")
            void singleLineAdjacentNestedElementsWithAlternateDelimiters() throws Exception {
                var input = """
                        term1:: def1
                        label1;; detail1
                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl//dl", output, 1);
                assertXpath("(//dl)[1]/dt[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("//dl//dl/dt[normalize-space(text()) = \"label1\"]", output, 1);
                assertXpath("//dl//dl/dt/following-sibling::dd/p[text() = \"detail1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl)[1]/dt[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("multi-line adjacent nested elements")
            void multiLineAdjacentNestedElements() throws Exception {
                var input = """
                        term1::
                        def1
                        label1:::
                        detail1
                        term2::
                        def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl//dl", output, 1);
                assertXpath("(//dl)[1]/dt[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("//dl//dl/dt[normalize-space(text()) = \"label1\"]", output, 1);
                assertXpath("//dl//dl/dt/following-sibling::dd/p[text() = \"detail1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl)[1]/dt[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("multi-line nested elements separated by blank line at nested level repeated")
            void multiLineNestedElementsSeparatedByBlankLineAtNestedLevelRepeated() throws Exception {
                var input = """
                        term1::
                        def1
                        label1:::

                        detail1
                        label2:::
                        detail2

                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl//dl", output, 1);
                assertXpath("(//dl)[1]/dt[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("(//dl//dl/dt)[1][normalize-space(text()) = \"label1\"]", output, 1);
                assertXpath("(//dl//dl/dt)[1]/following-sibling::dd/p[text() = \"detail1\"]", output, 1);
                assertXpath("(//dl//dl/dt)[2][normalize-space(text()) = \"label2\"]", output, 1);
                assertXpath("(//dl//dl/dt)[2]/following-sibling::dd/p[text() = \"detail2\"]", output, 1);
            }

            @Test
            @DisplayName("multi-line element with indented nested element")
            void multiLineElementWithIndentedNestedElement() throws Exception {
                var input = """
                        term1::
                          def1
                          label1;;
                           detail1
                        term2::
                          def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl//dl", output, 1);
                assertXpath("(//dl)[1]/dt", output, 2);
                assertXpath("(//dl)[1]/dd", output, 2);
                assertXpath("((//dl)[1]/dt)[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("((//dl)[1]/dt)[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("//dl//dl/dt", output, 1);
                assertXpath("//dl//dl/dt[normalize-space(text()) = \"label1\"]", output, 1);
                assertXpath("//dl//dl/dt/following-sibling::dd/p[text() = \"detail1\"]", output, 1);
                assertXpath("((//dl)[1]/dt)[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("((//dl)[1]/dt)[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("mixed single and multi-line elements with indented nested elements")
            void mixedSingleAndMultiLineElementsWithIndentedNestedElements() throws Exception {
                var input = """
                        term1:: def1
                          label1:::
                           detail1
                        term2:: def2
                        """;
                var output = convertString(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl//dl", output, 1);
                assertXpath("(//dl)[1]/dt[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[1]/following-sibling::dd/p[text() = \"def1\"]", output, 1);
                assertXpath("//dl//dl/dt[normalize-space(text()) = \"label1\"]", output, 1);
                assertXpath("//dl//dl/dt/following-sibling::dd/p[text() = \"detail1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[2][normalize-space(text()) = \"term2\"]", output, 1);
                assertXpath("(//dl)[1]/dt[2]/following-sibling::dd/p[text() = \"def2\"]", output, 1);
            }

            @Test
            @DisplayName("multi-line elements with first paragraph folded to text with adjacent nested element")
            void multiLineElementsWithFirstParagraphFoldedToTextWithAdjacentNestedElement() throws Exception {
                var input = """
                        term1:: def1
                        continued
                        label1:::
                        detail1
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl//dl", output, 1);
                assertXpath("(//dl)[1]/dt[1][normalize-space(text()) = \"term1\"]", output, 1);
                assertXpath("(//dl)[1]/dt[1]/following-sibling::dd/p[starts-with(text(), \"def1\")]", output, 1);
                assertXpath("(//dl)[1]/dt[1]/following-sibling::dd/p[contains(text(), \"continued\")]", output, 1);
                assertXpath("//dl//dl/dt[normalize-space(text()) = \"label1\"]", output, 1);
                assertXpath("//dl//dl/dt/following-sibling::dd/p[text() = \"detail1\"]", output, 1);
            }

            @Test
            @DisplayName("nested dlist attached by list continuation should not consume detached paragraph")
            void nestedDlistAttachedByListContinuationShouldNotConsumeDetachedParagraph() throws Exception {
                var input = """
                        term:: text
                        +
                        nested term::: text

                        paragraph
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl//dl", output, 1);
                assertCss(".dlist .paragraph", output, 0);
                assertCss(".dlist + .paragraph", output, 1);
            }

            @Test
            @DisplayName("nested dlist with attached block offset by empty line")
            void nestedDlistWithAttachedBlockOffsetByEmptyLine() throws Exception {
                var input = """
                        category::

                        term 1:::
                        +
                        --
                        def 1
                        --
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//dl", output, 2);
                assertXpath("//dl//dl", output, 1);
                assertXpath("(//dl)[1]/dt[1][normalize-space(text()) = \"category\"]", output, 1);
                assertXpath("(//dl)[1]//dl/dt[1][normalize-space(text()) = \"term 1\"]", output, 1);
                assertXpath("(//dl)[1]//dl/dt[1]/following-sibling::dd//p[starts-with(text(), \"def 1\")]", output, 1);
            }
        }

        @Nested
        @DisplayName("Special lists")
        class SpecialLists {

            @Test
            @DisplayName("should convert glossary list with proper semantics")
            void shouldConvertGlossaryListWithProperSemantics() throws Exception {
                var input = """
                        [glossary]
                        term 1:: def 1
                        term 2:: def 2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".dlist.glossary", output, 1);
                assertCss(".dlist dt:not([class])", output, 2);
            }

            @Test
            @DisplayName("consecutive glossary terms should share same glossentry element in docbook")
            void consecutiveGlossaryTermsShouldShareSameGlossentryElementInDocbook() throws Exception {
                var input = """
                        [glossary]
                        term::
                        alt term::
                        description

                        last::
                        """;
                var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
                assertXpath("/glossentry", output, 2);
                assertXpath("(/glossentry)[1]/glossterm", output, 2);
                assertXpath("(/glossentry)[2]/glossterm", output, 1);
                assertXpath("(/glossentry)[2]/glossdef", output, 1);
                assertXpath("(/glossentry)[2]/glossdef[normalize-space(text())=\"\"]", output, 1);
            }

            @Test
            @DisplayName("should convert horizontal list with proper markup")
            void shouldConvertHorizontalListWithProperMarkup() throws Exception {
                var input = """
                        [horizontal]
                        first term:: description
                        +
                        more detail

                        second term:: description
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".hdlist", output, 1);
                assertCss(".hdlist table", output, 1);
                assertCss(".hdlist table colgroup", output, 0);
                assertCss(".hdlist table tr", output, 2);
                var tbodyPath = (isJruby() ? "tbody/" : "");
                refuteIncludes(output, "<tbody>");
                assertXpath(("/*[@class=\"hdlist\"]/table/" + str(tbodyPath) + "tr[1]/td"), output, 2);
                assertXpath(("/*[@class=\"hdlist\"]/table/" + str(tbodyPath) + "tr[1]/td[@class=\"hdlist1\"]"), output, 1);
                assertXpath(("/*[@class=\"hdlist\"]/table/" + str(tbodyPath) + "tr[1]/td[@class=\"hdlist2\"]"), output, 1);
                assertXpath(("/*[@class=\"hdlist\"]/table/" + str(tbodyPath) + "tr[1]/td[@class=\"hdlist2\"]/p"), output, 1);
                assertXpath(("/*[@class=\"hdlist\"]/table/" + str(tbodyPath) + "tr[1]/td[@class=\"hdlist2\"]/p/following-sibling::*[@class=\"paragraph\"]"), output, 1);
                assertXpath("((//tr)[1]/td)[1][normalize-space(text())=\"first term\"]", output, 1);
                assertXpath("((//tr)[1]/td)[2]/p[normalize-space(text())=\"description\"]", output, 1);
                assertXpath(("/*[@class=\"hdlist\"]/table/" + str(tbodyPath) + "tr[2]/td"), output, 2);
                assertXpath("((//tr)[2]/td)[1][normalize-space(text())=\"second term\"]", output, 1);
                assertXpath("((//tr)[2]/td)[2]/p[normalize-space(text())=\"description\"]", output, 1);
            }

            @Test
            @DisplayName("should set col widths of item and label if specified")
            void shouldSetColWidthsOfItemAndLabelIfSpecified() throws Exception {
                var input = """
                        [horizontal]
                        [labelwidth="25", itemwidth="75"]
                        term:: def
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("table", output, 1);
                assertCss("table > colgroup", output, 1);
                assertCss("table > colgroup > col", output, 2);
                assertXpath("(//table/colgroup/col)[1][@width=\"25%\"]", output, 1);
                assertXpath("(//table/colgroup/col)[2][@width=\"75%\"]", output, 1);
            }

            @Test
            @DisplayName("should set col widths of item and label in docbook if specified")
            void shouldSetColWidthsOfItemAndLabelInDocbookIfSpecified() throws Exception {
                var input = """
                        [horizontal]
                        [labelwidth="25", itemwidth="75"]
                        term:: def
                        """;
                var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
                assertCss("informaltable", output, 1);
                assertCss("informaltable > tgroup", output, 1);
                assertCss("informaltable > tgroup > colspec", output, 2);
                assertXpath("(/informaltable/tgroup/colspec)[1][@colwidth=\"25*\"]", output, 1);
                assertXpath("(/informaltable/tgroup/colspec)[2][@colwidth=\"75*\"]", output, 1);
            }

            @Test
            @DisplayName("should add strong class to label if strong option is set")
            void shouldAddStrongClassToLabelIfStrongOptionIsSet() throws Exception {
                var input = """
                        [horizontal, options="strong"]
                        term:: def
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".hdlist", output, 1);
                assertCss(".hdlist td.hdlist1.strong", output, 1);
            }

            @Test
            @DisplayName("consecutive terms in horizontal list should share same cell")
            void consecutiveTermsInHorizontalListShouldShareSameCell() throws Exception {
                var input = """
                        [horizontal]
                        term::
                        alt term::
                        description

                        last::
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//tr", output, 2);
                assertXpath("(//tr)[1]/td[@class=\"hdlist1\"]", output, 1);
                assertXpath("(//tr)[1]/td[@class=\"hdlist1\"]/br", output, 1);
                assertXpath("(//tr)[2]/td[@class=\"hdlist2\"]", output, 1);
            }

            @Test
            @DisplayName("consecutive terms in horizontal list should share same entry in docbook")
            void consecutiveTermsInHorizontalListShouldShareSameEntryInDocbook() throws Exception {
                var input = """
                        [horizontal]
                        term::
                        alt term::
                        description

                        last::
                        """;
                var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
                assertXpath("//row", output, 2);
                assertXpath("(//row)[1]/entry", output, 2);
                assertXpath("((//row)[1]/entry)[1]/simpara", output, 2);
                assertXpath("(//row)[2]/entry", output, 2);
                assertXpath("((//row)[2]/entry)[2][normalize-space(text())=\"\"]", output, 1);
            }

            @Test
            @DisplayName("should convert horizontal list in docbook with proper markup")
            void shouldConvertHorizontalListInDocbookWithProperMarkup() throws Exception {
                var input = """
                        .Terms
                        [horizontal]
                        first term:: description
                        +
                        more detail

                        second term:: description
                        """;
                var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
                assertXpath("/table", output, 1);
                assertXpath("/table[@tabstyle=\"horizontal\"]", output, 1);
                assertXpath("/table[@tabstyle=\"horizontal\"]/title[text()=\"Terms\"]", output, 1);
                assertXpath("/table//row", output, 2);
                assertXpath("(/table//row)[1]/entry", output, 2);
                assertXpath("(/table//row)[2]/entry", output, 2);
                assertXpath("((/table//row)[1]/entry)[2]/simpara", output, 2);
            }

            @Test
            @DisplayName("should convert qanda list in HTML with proper semantics")
            void shouldConvertQandaListInHTMLWithProperSemantics() throws Exception {
                var input = """
                        [qanda]
                        Question 1::
                                Answer 1.
                        Question 2::
                                Answer 2.
                        +
                        NOTE: A note about Answer 2.
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".qlist.qanda", output, 1);
                assertCss(".qanda > ol", output, 1);
                assertCss(".qanda > ol > li", output, 2);
                for (var idx : range(1, 2)) {
                    assertCss((".qanda > ol > li:nth-child(" + str(idx) + ") > p"), output, 2);
                    assertCss((".qanda > ol > li:nth-child(" + str(idx) + ") > p:first-child > em"), output, 1);
                    assertXpath(("/*[@class = 'qlist qanda']/ol/li[" + str(idx) + "]/p[1]/em[normalize-space(text()) = 'Question " + str(idx) + "']"), output, 1);
                    assertCss((".qanda > ol > li:nth-child(" + str(idx) + ") > p:last-child > *"), output, 0);
                    assertXpath(("/*[@class = 'qlist qanda']/ol/li[" + str(idx) + "]/p[2][normalize-space(text()) = 'Answer " + str(idx) + ".']"), output, 1);
                }
                assertXpath("/*[@class = \"qlist qanda\"]/ol/li[2]/p[2]/following-sibling::div[@class=\"admonitionblock note\"]", output, 1);
            }

            @Test
            @DisplayName("should convert qanda list in DocBook with proper semantics")
            void shouldConvertQandaListInDocBookWithProperSemantics() throws Exception {
                var input = """
                        [qanda]
                        Question 1::
                                Answer 1.
                        Question 2::
                                Answer 2.
                        +
                        NOTE: A note about Answer 2.
                        """;
                var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
                assertCss("qandaset", output, 1);
                assertCss("qandaset > qandaentry", output, 2);
                for (var idx : range(1, 2)) {
                    assertCss(("qandaset > qandaentry:nth-child(" + str(idx) + ") > question"), output, 1);
                    assertCss(("qandaset > qandaentry:nth-child(" + str(idx) + ") > question > simpara"), output, 1);
                    assertXpath(("/qandaset/qandaentry[" + str(idx) + "]/question/simpara[normalize-space(text()) = 'Question " + str(idx) + "']"), output, 1);
                    assertCss(("qandaset > qandaentry:nth-child(" + str(idx) + ") > answer"), output, 1);
                    assertCss(("qandaset > qandaentry:nth-child(" + str(idx) + ") > answer > simpara"), output, 1);
                    assertXpath(("/qandaset/qandaentry[" + str(idx) + "]/answer/simpara[normalize-space(text()) = 'Answer " + str(idx) + ".']"), output, 1);
                }
                assertXpath("/qandaset/qandaentry[2]/answer/simpara/following-sibling::note", output, 1);
            }

            @Test
            @DisplayName("consecutive questions should share same question element in docbook")
            void consecutiveQuestionsShouldShareSameQuestionElementInDocbook() throws Exception {
                var input = """
                        [qanda]
                        question::
                        follow-up question::
                        response

                        last question::
                        """;
                var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
                assertXpath("//qandaentry", output, 2);
                assertXpath("(//qandaentry)[1]/question", output, 1);
                assertXpath("(//qandaentry)[1]/question/simpara", output, 2);
                assertXpath("(//qandaentry)[2]/question", output, 1);
                assertXpath("(//qandaentry)[2]/answer", output, 1);
                assertXpath("(//qandaentry)[2]/answer[normalize-space(text())=\"\"]", output, 1);
            }

            @Test
            @DisplayName("should convert bibliography list with proper semantics")
            void shouldConvertBibliographyListWithProperSemantics() throws Exception {
                var input = """
                        [bibliography]
                        - [[[taoup]]] Eric Steven Raymond. _The Art of Unix
                          Programming_. Addison-Wesley. ISBN 0-13-142901-9.
                        - [[[walsh-muellner]]] Norman Walsh & Leonard Muellner.
                          _DocBook - The Definitive Guide_. O'Reilly & Associates. 1999.
                          ISBN 1-56592-580-7.
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".ulist.bibliography", output, 1);
                assertCss(".ulist.bibliography ul", output, 1);
                assertCss(".ulist.bibliography ul li", output, 2);
                assertCss(".ulist.bibliography ul li p", output, 2);
                assertCss(".ulist.bibliography ul li:nth-child(1) p a#taoup", output, 1);
                assertXpath("//a/*", output, 0);
                assertXpath("(//a)[1][starts-with(following-sibling::text(), \"[taoup] \")]", output, 1);
            }

            @Test
            @DisplayName("should convert bibliography list with proper semantics to DocBook")
            void shouldConvertBibliographyListWithProperSemanticsToDocBook() throws Exception {
                var input = """
                        [bibliography]
                        - [[[taoup]]] Eric Steven Raymond. _The Art of Unix
                          Programming_. Addison-Wesley. ISBN 0-13-142901-9.
                        - [[[walsh-muellner]]] Norman Walsh & Leonard Muellner.
                          _DocBook - The Definitive Guide_. O'Reilly & Associates. 1999.
                          ISBN 1-56592-580-7.
                        """;
                var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
                assertCss("bibliodiv", output, 1);
                assertCss("bibliodiv > bibliomixed", output, 2);
                assertCss("bibliodiv > bibliomixed > bibliomisc", output, 2);
                assertCss("bibliodiv > bibliomixed:nth-child(1) > bibliomisc > anchor", output, 1);
                assertCss("bibliodiv > bibliomixed:nth-child(1) > bibliomisc > anchor[xreflabel=\"[taoup]\"]", output, 1);
                assertXpath("(//bibliomixed)[1]/bibliomisc/anchor[starts-with(following-sibling::text(), \"[taoup] Eric\")]", output, 1);
                assertCss("bibliodiv > bibliomixed:nth-child(2) > bibliomisc > anchor", output, 1);
                assertCss("bibliodiv > bibliomixed:nth-child(2) > bibliomisc > anchor[xreflabel=\"[walsh-muellner]\"]", output, 1);
                assertXpath("(//bibliomixed)[2]/bibliomisc/anchor[starts-with(following-sibling::text(), \"[walsh-muellner] Norman\")]", output, 1);
            }

            @Test
            @DisplayName("should warn if a bibliography ID is already in use")
            void shouldWarnIfABibliographyIDIsAlreadyInUse() throws Exception {
                var input = """
                        [bibliography]
                        * [[[Fowler]]] Fowler M. _Analysis Patterns: Reusable Object Models_.
                        Addison-Wesley. 1997.
                        * [[[Fowler]]] Fowler M. _Analysis Patterns: Reusable Object Models_.
                        Addison-Wesley. 1997.
                        """;
                try (var logger = usingMemoryLogger()) {
                    var output = convertStringToEmbedded(input);
                    assertCss(".ulist.bibliography", output, 1);
                    assertCss(".ulist.bibliography ul li:nth-child(1) p a#Fowler", output, 1);
                    assertCss(".ulist.bibliography ul li:nth-child(2) p a#Fowler", output, 1);
                    assertMessage(logger, "WARN", "<stdin>: line 4: id assigned to bibliography anchor already in use: Fowler", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should automatically add bibliography style to top-level lists in bibliography section")
            void shouldAutomaticallyAddBibliographyStyleToTopLevelListsInBibliographySection() throws Exception {
                var input = """
                        [bibliography]
                        == Bibliography

                        .Books
                        * [[[taoup]]] Eric Steven Raymond. _The Art of Unix
                          Programming_. Addison-Wesley. ISBN 0-13-142901-9.
                        * [[[walsh-muellner]]] Norman Walsh & Leonard Muellner.
                          _DocBook - The Definitive Guide_. O'Reilly & Associates. 1999.
                          ISBN 1-56592-580-7.

                        .Periodicals
                        * [[[doc-writer]]] Doc Writer. _Documentation As Code_. Static Times, 54. August 2016.
                        """;
                var doc = documentFromString(input);
                var ulists = doc.findBy(AbstractBlock.Selector.any().withContext("ulist"));
                assertEquals(2, size(ulists));
                assertEquals("bibliography", ulists.get(0).getStyle());
                assertEquals("bibliography", ulists.get(1).getStyle());
            }

            @Test
            @DisplayName("should not recognize bibliography anchor that begins with a digit")
            void shouldNotRecognizeBibliographyAnchorThatBeginsWithADigit() throws Exception {
                var input = """
                        [bibliography]
                        - [[[1984]]] George Orwell. _1984_. New American Library. 1950.
                        """;
                var output = convertStringToEmbedded(input);
                assertIncludes(output, "[[[1984]]]");
                assertXpath("//a[@id=\"1984\"]", output, 0);
            }

            @Test
            @DisplayName("should recognize bibliography anchor that contains a digit but does not start with one")
            void shouldRecognizeBibliographyAnchorThatContainsADigitButDoesNotStartWithOne() throws Exception {
                var input = """
                        [bibliography]
                        - [[[_1984]]] George Orwell. __1984__. New American Library. 1950.
                        """;
                var output = convertStringToEmbedded(input);
                refuteIncludes(output, "[[[_1984]]]");
                assertIncludes(output, "[_1984]");
                assertXpath("//a[@id=\"_1984\"]", output, 1);
            }

            @Test
            @DisplayName("should catalog bibliography anchors in bibliography list")
            void shouldCatalogBibliographyAnchorsInBibliographyList() throws Exception {
                var input = """
                        = Article Title

                        Please read <<Fowler_1997>>.

                        [bibliography]
                        == References

                        * [[[Fowler_1997]]] Fowler M. _Analysis Patterns: Reusable Object Models_. Addison-Wesley. 1997.
                        """;
                var doc = documentFromString(input);
                assertTruthy(doc.getCatalog().getRefs().containsKey("Fowler_1997"));
            }

            @Test
            @DisplayName("should use reftext from bibliography anchor at xref and entry")
            void shouldUseReftextFromBibliographyAnchorAtXrefAndEntry() throws Exception {
                var input = """
                        = Article Title

                        Begin with <<TMMM>>.
                        Then move on to <<Fowler_1997>>.

                        [bibliography]
                        == References

                        * [[[TMMM]]] Brooks F. _The Mythical Man-Month_. Addison-Wesley. 1975.
                        * [[[Fowler_1997,1]]] Fowler M. _Analysis Patterns: Reusable Object Models_. Addison-Wesley. 1997.
                        """;
                var doc = documentFromString(input, Options.of("standalone", false));
                var tmmmRef = get(doc.getCatalog().getRefs(), "TMMM");
                assertNotNull(tmmmRef);
                assertNull(tmmmRef.getReftext());
                var fowler1997Ref = get(doc.getCatalog().getRefs(), "Fowler_1997");
                assertNotNull(fowler1997Ref);
                assertEquals("[1]", fowler1997Ref.getReftext());
                var result = doc.convert(Options.of("standalone", false));
                assertXpath("//a[@href=\"#Fowler_1997\"]", result, 1);
                assertXpath("//a[@href=\"#Fowler_1997\"][text()=\"[1]\"]", result, 1);
                assertXpath("//a[@id=\"Fowler_1997\"]", result, 1);
                assertXpath("(//a[@id=\"Fowler_1997\"])[1][starts-with(following-sibling::text(), \"[1] \")]", result, 1);
                assertXpath("//a[@href=\"#TMMM\"]", result, 1);
                assertXpath("//a[@href=\"#TMMM\"][text()=\"[TMMM]\"]", result, 1);
                assertXpath("//a[@id=\"TMMM\"]", result, 1);
                assertXpath("(//a[@id=\"TMMM\"])[1][starts-with(following-sibling::text(), \"[TMMM] \")]", result, 1);
            }

            @Test
            @DisplayName("should assign reftext of bibliography anchor to xreflabel in DocBook backend")
            void shouldAssignReftextOfBibliographyAnchorToXreflabelInDocBookBackend() throws Exception {
                var input = """
                        [bibliography]
                        * [[[Fowler_1997,1]]] Fowler M. _Analysis Patterns: Reusable Object Models_. Addison-Wesley. 1997.
                        """;
                var result = convertStringToEmbedded(input, Options.of("backend", "docbook"));
                assertIncludes(result, "<anchor xml:id=\"Fowler_1997\" xreflabel=\"[1]\"/>[1] Fowler");
            }
        }
    }

    @Nested
    @DisplayName("Description lists redux")
    class DescriptionListsRedux {

        @Nested
        @DisplayName("Label without text on same line")
        class LabelWithoutTextOnSameLine {

            @Test
            @DisplayName("folds text from subsequent line")
            void foldsTextFromSubsequentLine() throws Exception {
                var input = """
                        == Lists

                        term1::
                        def1
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
            }

            @Test
            @DisplayName("folds text from first line after blank lines")
            void foldsTextFromFirstLineAfterBlankLines() throws Exception {
                var input = """
                        == Lists

                        term1::


                        def1
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
            }

            @Test
            @DisplayName("folds text from first line after blank line and immediately preceding next item")
            void foldsTextFromFirstLineAfterBlankLineAndImmediatelyPrecedingNextItem() throws Exception {
                var input = """
                        == Lists

                        term1::

                        def1
                        term2:: def2
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 2);
                assertXpath("(//*[@class=\"dlist\"]//dd)[1]/p[text()=\"def1\"]", output, 1);
            }

            @Test
            @DisplayName("paragraph offset by blank lines does not break list if label does not have inline text")
            void paragraphOffsetByBlankLinesDoesNotBreakListIfLabelDoesNotHaveInlineText() throws Exception {
                var input = """
                        == Lists

                        term1::

                        def1

                        term2:: def2
                        """;
                var output = convertStringToEmbedded(input);
                assertCss("dl", output, 1);
                assertCss("dl > dt", output, 2);
                assertCss("dl > dd", output, 2);
                assertXpath("(//dl/dd)[1]/p[text()=\"def1\"]", output, 1);
            }

            @Test
            @DisplayName("folds text from first line after comment line")
            void foldsTextFromFirstLineAfterCommentLine() throws Exception {
                var input = """
                        == Lists

                        term1::
                        // comment
                        def1
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
            }

            @Test
            @DisplayName("folds text from line following comment line offset by blank line")
            void foldsTextFromLineFollowingCommentLineOffsetByBlankLine() throws Exception {
                var input = """
                        == Lists

                        term1::

                        // comment
                        def1
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
            }

            @Test
            @DisplayName("folds text from subsequent indented line")
            void foldsTextFromSubsequentIndentedLine() throws Exception {
                var input = """
                        == Lists

                        term1::
                          def1
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
            }

            @Test
            @DisplayName("folds text from indented line after blank line")
            void foldsTextFromIndentedLineAfterBlankLine() throws Exception {
                var input = """
                        == Lists

                        term1::

                          def1
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
            }

            @Test
            @DisplayName("folds text that looks like ruler offset by blank line")
            void foldsTextThatLooksLikeRulerOffsetByBlankLine() throws Exception {
                var input = """
                        == Lists

                        term1::

                        '''
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"'''\"]", output, 1);
            }

            @Test
            @DisplayName("folds text that looks like ruler offset by blank line and line comment")
            void foldsTextThatLooksLikeRulerOffsetByBlankLineAndLineComment() throws Exception {
                var input = """
                        == Lists

                        term1::

                        // comment
                        '''
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"'''\"]", output, 1);
            }

            @Test
            @DisplayName("folds text that looks like ruler and the line following it offset by blank line")
            void foldsTextThatLooksLikeRulerAndTheLineFollowingItOffsetByBlankLine() throws Exception {
                var input = """
                        == Lists

                        term1::

                        '''
                        continued
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[normalize-space(text())=\"''' continued\"]", output, 1);
            }

            @Test
            @DisplayName("folds text that looks like title offset by blank line")
            void foldsTextThatLooksLikeTitleOffsetByBlankLine() throws Exception {
                var input = """
                        == Lists

                        term1::

                        .def1
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\".def1\"]", output, 1);
            }

            @Test
            @DisplayName("folds text that looks like title offset by blank line and line comment")
            void foldsTextThatLooksLikeTitleOffsetByBlankLineAndLineComment() throws Exception {
                var input = """
                        == Lists

                        term1::

                        // comment
                        .def1
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\".def1\"]", output, 1);
            }

            @Test
            @DisplayName("folds text that looks like admonition offset by blank line")
            void foldsTextThatLooksLikeAdmonitionOffsetByBlankLine() throws Exception {
                var input = """
                        == Lists

                        term1::

                        NOTE: def1
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"NOTE: def1\"]", output, 1);
            }

            @Test
            @DisplayName("folds text that looks like section title offset by blank line")
            void foldsTextThatLooksLikeSectionTitleOffsetByBlankLine() throws Exception {
                var input = """
                        == Lists

                        term1::

                        == Another Section
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"== Another Section\"]", output, 1);
                assertXpath("//h2", output, 1);
            }

            @Test
            @DisplayName("folds text of first literal line offset by blank line appends subsequent literals offset by blank line as blocks")
            void foldsTextOfFirstLiteralLineOffsetByBlankLineAppendsSubsequentLiteralsOffsetByBlankLineAsBlocks() throws Exception {
                var input = """
                        == Lists

                        term1::

                          def1

                          literal


                          literal
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"literalblock\"]", output, 2);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"literalblock\"]//pre[text()=\"literal\"]", output, 2);
            }

            @Test
            @DisplayName("folds text of subsequent line and appends following literal line offset by blank line as block if term has no inline description")
            void foldsTextOfSubsequentLineAndAppendsFollowingLiteralLineOffsetByBlankLineAsBlockIfTermHasNoInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1::
                        def1

                          literal

                        term2:: def2
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 2);
                assertXpath("(//*[@class=\"dlist\"]//dd)[1]/p[text()=\"def1\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]//dd)[1]/p/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]//dd)[1]/p/following-sibling::*[@class=\"literalblock\"]//pre[text()=\"literal\"]", output, 1);
            }

            @Test
            @DisplayName("appends literal line attached by continuation as block if item has no inline description")
            void appendsLiteralLineAttachedByContinuationAsBlockIfItemHasNoInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1::
                        +
                          literal
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p", output, 0);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"literalblock\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"literalblock\"]//pre[text()=\"literal\"]", output, 1);
            }

            @Test
            @DisplayName("appends literal line attached by continuation as block if item has no inline description followed by ruler")
            void appendsLiteralLineAttachedByContinuationAsBlockIfItemHasNoInlineDescriptionFollowedByRuler() throws Exception {
                var input = """
                        == Lists

                        term1::
                        +
                          literal

                        '''
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p", output, 0);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"literalblock\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"literalblock\"]//pre[text()=\"literal\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::hr", output, 1);
            }

            @Test
            @DisplayName("appends line attached by continuation as block if item has no inline description followed by ruler")
            void appendsLineAttachedByContinuationAsBlockIfItemHasNoInlineDescriptionFollowedByRuler() throws Exception {
                var input = """
                        == Lists

                        term1::
                        +
                        para

                        '''
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p", output, 0);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::hr", output, 1);
            }

            @Test
            @DisplayName("appends line attached by continuation as block if item has no inline description followed by block")
            void appendsLineAttachedByContinuationAsBlockIfItemHasNoInlineDescriptionFollowedByBlock() throws Exception {
                var input = """
                        == Lists

                        term1::
                        +
                        para

                        ....
                        literal
                        ....
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p", output, 0);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"literalblock\"]//pre[text()=\"literal\"]", output, 1);
            }

            @Test
            @DisplayName("appends block attached by continuation but not subsequent block not attached by continuation")
            void appendsBlockAttachedByContinuationButNotSubsequentBlockNotAttachedByContinuation() throws Exception {
                var input = """
                        == Lists

                        term1::
                        +
                        ....
                        literal
                        ....
                        ....
                        detached
                        ....
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p", output, 0);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"literalblock\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"literalblock\"]//pre[text()=\"literal\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"literalblock\"]//pre[text()=\"detached\"]", output, 1);
            }

            @Test
            @DisplayName("appends list if item has no inline description")
            void appendsListIfItemHasNoInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1::

                        * one
                        * two
                        * three
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p", output, 0);
                assertXpath("//*[@class=\"dlist\"]//dd//ul/li", output, 3);
            }

            @Test
            @DisplayName("appends list to first term when followed immediately by second term")
            void appendsListToFirstTermWhenFollowedImmediatelyBySecondTerm() throws Exception {
                var input = """
                        == Lists

                        term1::

                        * one
                        * two
                        * three
                        term2:: def2
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 2);
                assertXpath("(//*[@class=\"dlist\"]//dd)[1]/p", output, 0);
                assertXpath("(//*[@class=\"dlist\"]//dd)[1]//ul/li", output, 3);
                assertXpath("(//*[@class=\"dlist\"]//dd)[2]/p[text()=\"def2\"]", output, 1);
            }

            @Test
            @DisplayName("appends indented list to first term that is adjacent to second term")
            void appendsIndentedListToFirstTermThatIsAdjacentToSecondTerm() throws Exception {
                var input = """
                        == Lists

                        label 1::
                          description 1

                          * one
                          * two
                          * three
                        label 2::
                          description 2

                        paragraph
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".dlist > dl", output, 1);
                assertCss(".dlist dt", output, 2);
                assertXpath("(//*[@class=\"dlist\"]//dt)[1][normalize-space(text())=\"label 1\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]//dt)[2][normalize-space(text())=\"label 2\"]", output, 1);
                assertCss(".dlist dd", output, 2);
                assertXpath("(//*[@class=\"dlist\"]//dd)[1]/p[text()=\"description 1\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]//dd)[2]/p[text()=\"description 2\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]//dd)[1]/p/following-sibling::*[@class=\"ulist\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]//dd)[1]/p/following-sibling::*[@class=\"ulist\"]//li", output, 3);
                assertCss(".dlist + .paragraph", output, 1);
            }

            @Test
            @DisplayName("appends indented list to first term that is attached by a continuation and adjacent to second term")
            void appendsIndentedListToFirstTermThatIsAttachedByAContinuationAndAdjacentToSecondTerm() throws Exception {
                var input = """
                        == Lists

                        label 1::
                          description 1
                        +
                          * one
                          * two
                          * three
                        label 2::
                          description 2

                        paragraph
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".dlist > dl", output, 1);
                assertCss(".dlist dt", output, 2);
                assertXpath("(//*[@class=\"dlist\"]//dt)[1][normalize-space(text())=\"label 1\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]//dt)[2][normalize-space(text())=\"label 2\"]", output, 1);
                assertCss(".dlist dd", output, 2);
                assertXpath("(//*[@class=\"dlist\"]//dd)[1]/p[text()=\"description 1\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]//dd)[2]/p[text()=\"description 2\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]//dd)[1]/p/following-sibling::*[@class=\"ulist\"]", output, 1);
                assertXpath("(//*[@class=\"dlist\"]//dd)[1]/p/following-sibling::*[@class=\"ulist\"]//li", output, 3);
                assertCss(".dlist + .paragraph", output, 1);
            }

            @Test
            @DisplayName("appends list and paragraph block when line following list attached by continuation")
            void appendsListAndParagraphBlockWhenLineFollowingListAttachedByContinuation() throws Exception {
                var input = """
                        == Lists

                        term1::

                        * one
                        * two
                        * three

                        +
                        para
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p", output, 0);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"ulist\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"ulist\"]/ul/li", output, 3);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"ulist\"]/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"ulist\"]/following-sibling::*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
            }

            @Test
            @DisplayName("first continued line associated with nested list item and second continued line associated with term")
            void firstContinuedLineAssociatedWithNestedListItemAndSecondContinuedLineAssociatedWithTerm() throws Exception {
                var input = """
                        == Lists

                        term1::
                        * one
                        +
                        nested list para

                        +
                        term1 para
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p", output, 0);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"ulist\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"ulist\"]/ul/li", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"ulist\"]/ul/li/*[@class=\"paragraph\"]/p[text()=\"nested list para\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"ulist\"]/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"ulist\"]/following-sibling::*[@class=\"paragraph\"]/p[text()=\"term1 para\"]", output, 1);
            }

            @Test
            @DisplayName("literal line attached by continuation swallows adjacent line that looks like term")
            void literalLineAttachedByContinuationSwallowsAdjacentLineThatLooksLikeTerm() throws Exception {
                var input = """
                        == Lists

                        term1::
                        +
                          literal
                        notnestedterm:::
                        +
                          literal
                        notnestedterm:::
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p", output, 0);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"literalblock\"]", output, 2);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"literalblock\"]//pre[text()=\"  literal\nnotnestedterm:::\"]", output, 2);
            }

            @Test
            @DisplayName("line attached by continuation is appended as paragraph if term has no inline description")
            void lineAttachedByContinuationIsAppendedAsParagraphIfTermHasNoInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1::
                        +
                        para
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p", output, 0);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
            }

            @Test
            @DisplayName("attached paragraph does not break on adjacent nested description list term")
            void attachedParagraphDoesNotBreakOnAdjacentNestedDescriptionListTerm() throws Exception {
                var input = """
                        term1:: def
                        +
                        more description
                        not a term::: def
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".dlist > dl > dt", output, 1);
                assertCss(".dlist > dl > dd", output, 1);
                assertCss(".dlist > dl > dd > .paragraph", output, 1);
                assertIncludes(output, "not a term::: def");
            }

            @Test
            @DisplayName("attached paragraph is terminated by adjacent sibling description list term")
            void attachedParagraphIsTerminatedByAdjacentSiblingDescriptionListTerm() throws Exception {
                var input = """
                        term1:: def
                        +
                        more description
                        not a term:: def
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".dlist > dl > dt", output, 2);
                assertCss(".dlist > dl > dd", output, 2);
                assertCss(".dlist > dl > dd > .paragraph", output, 1);
                refuteIncludes(output, "not a term:: def");
            }

            @Test
            @DisplayName("attached styled paragraph does not break on adjacent nested description list term")
            void attachedStyledParagraphDoesNotBreakOnAdjacentNestedDescriptionListTerm() throws Exception {
                var input = """
                        term1:: def
                        +
                        [quote]
                        more description
                        not a term::: def
                        """;
                var output = convertStringToEmbedded(input);
                assertCss(".dlist > dl > dt", output, 1);
                assertCss(".dlist > dl > dd", output, 1);
                assertCss(".dlist > dl > dd > .quoteblock", output, 1);
                assertIncludes(output, "not a term::: def");
            }

            @Test
            @DisplayName("appends line as paragraph if attached by continuation following blank line and line comment when term has no inline description")
            void appendsLineAsParagraphIfAttachedByContinuationFollowingBlankLineAndLineCommentWhenTermHasNoInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1::

                        // comment
                        +
                        para
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p", output, 0);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
            }

            @Test
            @DisplayName("line attached by continuation offset by blank line is appended as paragraph if term has no inline description")
            void lineAttachedByContinuationOffsetByBlankLineIsAppendedAsParagraphIfTermHasNoInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1::

                        +
                        para
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p", output, 0);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
            }

            @Test
            @DisplayName("delimited block breaks list even when term has no inline description")
            void delimitedBlockBreaksListEvenWhenTermHasNoInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1::
                        ====
                        detached
                        ====
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 0);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"exampleblock\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"exampleblock\"]//p[text()=\"detached\"]", output, 1);
            }

            @Test
            @DisplayName("block attribute line above delimited block that breaks a dlist is not duplicated")
            void blockAttributeLineAboveDelimitedBlockThatBreaksADlistIsNotDuplicated() throws Exception {
                var input = """
                        == Lists

                        term:: desc
                        [.rolename]
                        ----
                        detached
                        ----
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"listingblock rolename\"]", output, 1);
            }

            @Test
            @DisplayName("block attribute line above paragraph breaks list even when term has no inline description")
            void blockAttributeLineAboveParagraphBreaksListEvenWhenTermHasNoInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1::
                        [verse]
                        detached
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 0);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"verseblock\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"verseblock\"]/pre[text()=\"detached\"]", output, 1);
            }

            @Test
            @DisplayName("block attribute line above paragraph that breaks a dlist is not duplicated")
            void blockAttributeLineAboveParagraphThatBreaksADlistIsNotDuplicated() throws Exception {
                var input = """
                        == Lists

                        term:: desc
                        [.rolename]
                        detached
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph rolename\"]", output, 1);
            }

            @Test
            @DisplayName("block anchor line breaks list even when term has no inline description")
            void blockAnchorLineBreaksListEvenWhenTermHasNoInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1::
                        [[id]]
                        detached
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 0);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"]/p[text()=\"detached\"]", output, 1);
            }

            @Test
            @DisplayName("block attribute lines above nested horizontal list does not break list")
            void blockAttributeLinesAboveNestedHorizontalListDoesNotBreakList() throws Exception {
                var input = """
                        Operating Systems::
                        [horizontal]
                          Linux::: Fedora
                          BSD::: OpenBSD

                        Cloud Providers::
                          PaaS::: OpenShift
                          IaaS::: AWS
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//dl", output, 2);
                assertXpath("/*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("(//dl)[1]/dd", output, 2);
                assertXpath("((//dl)[1]/dd)[1]//table", output, 1);
                assertXpath("((//dl)[1]/dd)[2]//table", output, 0);
            }

            @Test
            @DisplayName("block attribute lines above nested list with style does not break list")
            void blockAttributeLinesAboveNestedListWithStyleDoesNotBreakList() throws Exception {
                var input = """
                        TODO List::
                        * get groceries
                        Grocery List::
                        [square]
                        * bread
                        * milk
                        * lettuce
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//dl", output, 1);
                assertXpath("(//dl)[1]/dd", output, 2);
                assertXpath("((//dl)[1]/dd)[2]//ul[@class=\"square\"]", output, 1);
            }

            @Test
            @DisplayName("multiple block attribute lines above nested list does not break list")
            void multipleBlockAttributeLinesAboveNestedListDoesNotBreakList() throws Exception {
                var input = """
                        Operating Systems::
                        [[variants]]
                        [horizontal]
                          Linux::: Fedora
                          BSD::: OpenBSD

                        Cloud Providers::
                          PaaS::: OpenShift
                          IaaS::: AWS
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//dl", output, 2);
                assertXpath("/*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("(//dl)[1]/dd", output, 2);
                assertXpath("(//dl)[1]/dd/*[@id=\"variants\"]", output, 1);
                assertXpath("((//dl)[1]/dd)[1]//table", output, 1);
                assertXpath("((//dl)[1]/dd)[2]//table", output, 0);
            }

            @Test
            @DisplayName("multiple block attribute lines separated by empty line above nested list does not break list")
            void multipleBlockAttributeLinesSeparatedByEmptyLineAboveNestedListDoesNotBreakList() throws Exception {
                var input = """
                        Operating Systems::
                        [[variants]]

                        [horizontal]

                          Linux::: Fedora
                          BSD::: OpenBSD

                        Cloud Providers::
                          PaaS::: OpenShift
                          IaaS::: AWS
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//dl", output, 2);
                assertXpath("/*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("(//dl)[1]/dd", output, 2);
                assertXpath("(//dl)[1]/dd/*[@id=\"variants\"]", output, 1);
                assertXpath("((//dl)[1]/dd)[1]//table", output, 1);
                assertXpath("((//dl)[1]/dd)[2]//table", output, 0);
            }
        }

        @Nested
        @DisplayName("Item with text inline")
        class ItemWithTextInline {

            @Test
            @DisplayName("folds text from inline description and subsequent line")
            void foldsTextFromInlineDescriptionAndSubsequentLine() throws Exception {
                var input = """
                        == Lists

                        term1:: def1
                        continued
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\ncontinued\"]", output, 1);
            }

            @Test
            @DisplayName("folds text from inline description and subsequent lines")
            void foldsTextFromInlineDescriptionAndSubsequentLines() throws Exception {
                var input = """
                        == Lists

                        term1:: def1
                        continued
                        continued
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\ncontinued\ncontinued\"]", output, 1);
            }

            @Test
            @DisplayName("folds text from inline description and line following comment line")
            void foldsTextFromInlineDescriptionAndLineFollowingCommentLine() throws Exception {
                var input = """
                        == Lists

                        term1:: def1
                        // comment
                        continued
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\ncontinued\"]", output, 1);
            }

            @Test
            @DisplayName("folds text from inline description and subsequent indented line")
            void foldsTextFromInlineDescriptionAndSubsequentIndentedLine() throws Exception {
                var input = """
                        == List

                        term1:: def1
                          continued
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\ncontinued\"]", output, 1);
            }

            @Test
            @DisplayName("appends literal line offset by blank line as block if item has inline description")
            void appendsLiteralLineOffsetByBlankLineAsBlockIfItemHasInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1:: def1

                          literal
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"literalblock\"]//pre[text()=\"literal\"]", output, 1);
            }

            @Test
            @DisplayName("appends literal line offset by blank line as block and appends line after continuation as block if item has inline description")
            void appendsLiteralLineOffsetByBlankLineAsBlockAndAppendsLineAfterContinuationAsBlockIfItemHasInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1:: def1

                          literal
                        +
                        para
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"literalblock\"]//pre[text()=\"literal\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"literalblock\"]/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"literalblock\"]/following-sibling::*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
            }

            @Test
            @DisplayName("appends line after continuation as block and literal line offset by blank line as block if item has inline description")
            void appendsLineAfterContinuationAsBlockAndLiteralLineOffsetByBlankLineAsBlockIfItemHasInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1:: def1
                        +
                        para

                          literal
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"paragraph\"]/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/*[@class=\"paragraph\"]/following-sibling::*[@class=\"literalblock\"]//pre[text()=\"literal\"]", output, 1);
            }

            @Test
            @DisplayName("appends list if item has inline description")
            void appendsListIfItemHasInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1:: def1

                        * one
                        * two
                        * three
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"ulist\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"ulist\"]/ul/li", output, 3);
            }

            @Test
            @DisplayName("appends literal line attached by continuation as block if item has inline description followed by ruler")
            void appendsLiteralLineAttachedByContinuationAsBlockIfItemHasInlineDescriptionFollowedByRuler() throws Exception {
                var input = """
                        == Lists

                        term1:: def1
                        +
                          literal

                        '''
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"literalblock\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"literalblock\"]//pre[text()=\"literal\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::hr", output, 1);
            }

            @Test
            @DisplayName("line offset by blank line breaks list if term has inline description")
            void lineOffsetByBlankLineBreaksListIfTermHasInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1:: def1

                        detached
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"]/p[text()=\"detached\"]", output, 1);
            }

            @Test
            @DisplayName("nested term with description does not consume following heading")
            void nestedTermWithDescriptionDoesNotConsumeFollowingHeading() throws Exception {
                var input = """
                        == Lists

                        term::
                          def
                          nestedterm;;
                            nesteddef

                        Detached
                        ~~~~~~~~
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 2);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 2);
                assertXpath("//*[@class=\"dlist\"]/dl//dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]/dl//dl/dt", output, 1);
                assertXpath("((//*[@class=\"dlist\"])[1]//dd)[1]/p[text()=\"def\"]", output, 1);
                assertXpath("((//*[@class=\"dlist\"])[1]//dd)[1]/p/following-sibling::*[@class=\"dlist\"]", output, 1);
                assertXpath("((//*[@class=\"dlist\"])[1]//dd)[1]/p/following-sibling::*[@class=\"dlist\"]//dd/p[text()=\"nesteddef\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"sect2\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"sect2\"]/h3[text()=\"Detached\"]", output, 1);
            }

            @Test
            @DisplayName("line attached by continuation is appended as paragraph if term has inline description followed by detached paragraph")
            void lineAttachedByContinuationIsAppendedAsParagraphIfTermHasInlineDescriptionFollowedByDetachedParagraph() throws Exception {
                var input = """
                        == Lists

                        term1:: def1
                        +
                        para

                        detached
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"paragraph\"]/p[text()=\"detached\"]", output, 1);
            }

            @Test
            @DisplayName("line attached by continuation is appended as paragraph if term has inline description followed by detached block")
            void lineAttachedByContinuationIsAppendedAsParagraphIfTermHasInlineDescriptionFollowedByDetachedBlock() throws Exception {
                var input = """
                        == Lists

                        term1:: def1
                        +
                        para

                        ****
                        detached
                        ****
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"sidebarblock\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]/following-sibling::*[@class=\"sidebarblock\"]//p[text()=\"detached\"]", output, 1);
            }

            @Test
            @DisplayName("line attached by continuation offset by line comment is appended as paragraph if term has inline description")
            void lineAttachedByContinuationOffsetByLineCommentIsAppendedAsParagraphIfTermHasInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1:: def1
                        // comment
                        +
                        para
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
            }

            @Test
            @DisplayName("line attached by continuation offset by blank line is appended as paragraph if term has inline description")
            void lineAttachedByContinuationOffsetByBlankLineIsAppendedAsParagraphIfTermHasInlineDescription() throws Exception {
                var input = """
                        == Lists

                        term1:: def1

                        +
                        para
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p[text()=\"def1\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"paragraph\"]", output, 1);
                assertXpath("//*[@class=\"dlist\"]//dd/p/following-sibling::*[@class=\"paragraph\"]/p[text()=\"para\"]", output, 1);
            }

            @Test
            @DisplayName("line comment offset by blank line divides lists because item has text")
            void lineCommentOffsetByBlankLineDividesListsBecauseItemHasText() throws Exception {
                var input = """
                        == Lists

                        term1:: def1

                        //

                        term2:: def2
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 2);
            }

            @Test
            @DisplayName("ruler offset by blank line divides lists because item has text")
            void rulerOffsetByBlankLineDividesListsBecauseItemHasText() throws Exception {
                var input = """
                        == Lists

                        term1:: def1

                        '''

                        term2:: def2
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 2);
            }

            @Test
            @DisplayName("block title offset by blank line divides lists and becomes title of second list because item has text")
            void blockTitleOffsetByBlankLineDividesListsAndBecomesTitleOfSecondListBecauseItemHasText() throws Exception {
                var input = """
                        == Lists

                        term1:: def1

                        .title

                        term2:: def2
                        """;
                var output = convertStringToEmbedded(input);
                assertXpath("//*[@class=\"dlist\"]/dl", output, 2);
                assertXpath("(//*[@class=\"dlist\"])[2]/*[@class=\"title\"][text()=\"title\"]", output, 1);
            }
        }
    }

    @Nested
    @DisplayName("Callout lists")
    class CalloutLists {

        @Test
        @DisplayName("does not recognize callout list denoted by markers that only have a trailing bracket")
        void doesNotRecognizeCalloutListDenotedByMarkersThatOnlyHaveATrailingBracket() throws Exception {
            var input = """
                    ----
                    require 'asciidoctor' # <1>
                    ----
                    1> Not a callout list item
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".colist", output, 0);
        }

        @Test
        @DisplayName("should not hang if obsolete callout list is found inside list item")
        void shouldNotHangIfObsoleteCalloutListIsFoundInsideListItem() throws Exception {
            var input = """
                    * foo
                    1> bar
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".colist", output, 0);
        }

        @Test
        @DisplayName("should not hang if obsolete callout list is found inside dlist item")
        void shouldNotHangIfObsoleteCalloutListIsFoundInsideDlistItem() throws Exception {
            var input = """
                    foo::
                    1> bar
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".colist", output, 0);
        }

        @Test
        @DisplayName("should recognize auto-numberd callout list inside list")
        void shouldRecognizeAutoNumberdCalloutListInsideList() throws Exception {
            var input = """
                    ----
                    require 'asciidoctor' # <1>
                    ----
                    * foo
                    <.> bar
                    """;
            var output = convertStringToEmbedded(input);
            assertCss(".colist", output, 1);
        }

        @Test
        @DisplayName("listing block with sequential callouts followed by adjacent callout list")
        void listingBlockWithSequentialCalloutsFollowedByAdjacentCalloutList() throws Exception {
            var input = """
                    [source, ruby]
                    ----
                    require 'asciidoctor' # <1>
                    doc = Asciidoctor::Document.new('Hello, World!') # <2>
                    puts doc.convert # <3>
                    ----
                    <1> Describe the first line
                    <2> Describe the second line
                    <3> Describe the third line
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("backend", "docbook")));
            assertXpath("//programlisting", output, 1);
            assertXpath("//programlisting//co", output, 3);
            assertXpath("(//programlisting//co)[1][@xml:id=\"CO1-1\"]", output, 1);
            assertXpath("(//programlisting//co)[2][@xml:id=\"CO1-2\"]", output, 1);
            assertXpath("(//programlisting//co)[3][@xml:id=\"CO1-3\"]", output, 1);
            assertXpath("//programlisting/following-sibling::calloutlist/callout", output, 3);
            assertXpath("(//programlisting/following-sibling::calloutlist/callout)[1][@arearefs = \"CO1-1\"]", output, 1);
            assertXpath("(//programlisting/following-sibling::calloutlist/callout)[2][@arearefs = \"CO1-2\"]", output, 1);
            assertXpath("(//programlisting/following-sibling::calloutlist/callout)[3][@arearefs = \"CO1-3\"]", output, 1);
        }

        @Test
        @DisplayName("listing block with sequential callouts followed by non-adjacent callout list")
        void listingBlockWithSequentialCalloutsFollowedByNonAdjacentCalloutList() throws Exception {
            var input = """
                    [source, ruby]
                    ----
                    require 'asciidoctor' # <1>
                    doc = Asciidoctor::Document.new('Hello, World!') # <2>
                    puts doc.convert # <3>
                    ----

                    Paragraph.

                    <1> Describe the first line
                    <2> Describe the second line
                    <3> Describe the third line
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("backend", "docbook")));
            assertXpath("//programlisting", output, 1);
            assertXpath("//programlisting//co", output, 3);
            assertXpath("(//programlisting//co)[1][@xml:id=\"CO1-1\"]", output, 1);
            assertXpath("(//programlisting//co)[2][@xml:id=\"CO1-2\"]", output, 1);
            assertXpath("(//programlisting//co)[3][@xml:id=\"CO1-3\"]", output, 1);
            assertXpath("//programlisting/following-sibling::*[1][self::simpara]", output, 1);
            assertXpath("//programlisting/following-sibling::calloutlist/callout", output, 3);
            assertXpath("(//programlisting/following-sibling::calloutlist/callout)[1][@arearefs = \"CO1-1\"]", output, 1);
            assertXpath("(//programlisting/following-sibling::calloutlist/callout)[2][@arearefs = \"CO1-2\"]", output, 1);
            assertXpath("(//programlisting/following-sibling::calloutlist/callout)[3][@arearefs = \"CO1-3\"]", output, 1);
        }

        @Test
        @DisplayName("listing block with a callout that refers to two different lines")
        void listingBlockWithACalloutThatRefersToTwoDifferentLines() throws Exception {
            var input = """
                    [source, ruby]
                    ----
                    require 'asciidoctor' # <1>
                    doc = Asciidoctor::Document.new('Hello, World!') # <2>
                    puts doc.convert # <2>
                    ----
                    <1> Import the library
                    <2> Where the magic happens
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("backend", "docbook")));
            assertXpath("//programlisting", output, 1);
            assertXpath("//programlisting//co", output, 3);
            assertXpath("(//programlisting//co)[1][@xml:id=\"CO1-1\"]", output, 1);
            assertXpath("(//programlisting//co)[2][@xml:id=\"CO1-2\"]", output, 1);
            assertXpath("(//programlisting//co)[3][@xml:id=\"CO1-3\"]", output, 1);
            assertXpath("//programlisting/following-sibling::calloutlist/callout", output, 2);
            assertXpath("(//programlisting/following-sibling::calloutlist/callout)[1][@arearefs = \"CO1-1\"]", output, 1);
            assertXpath("(//programlisting/following-sibling::calloutlist/callout)[2][@arearefs = \"CO1-2 CO1-3\"]", output, 1);
        }

        @Test
        @DisplayName("source block with non-sequential callouts followed by adjacent callout list")
        void sourceBlockWithNonSequentialCalloutsFollowedByAdjacentCalloutList() throws Exception {
            var input = """
                    [source,ruby]
                    ----
                    require 'asciidoctor' # <2>
                    doc = Asciidoctor::Document.new('Hello, World!') # <3>
                    puts doc.convert # <1>
                    ----
                    <1> Describe the first line
                    <2> Describe the second line
                    <3> Describe the third line
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("backend", "docbook")));
            assertXpath("//programlisting", output, 1);
            assertXpath("//programlisting//co", output, 3);
            assertXpath("(//programlisting//co)[1][@xml:id=\"CO1-1\"]", output, 1);
            assertXpath("(//programlisting//co)[2][@xml:id=\"CO1-2\"]", output, 1);
            assertXpath("(//programlisting//co)[3][@xml:id=\"CO1-3\"]", output, 1);
            assertXpath("//programlisting/following-sibling::calloutlist/callout", output, 3);
            assertXpath("(//programlisting/following-sibling::calloutlist/callout)[1][@arearefs = \"CO1-3\"]", output, 1);
            assertXpath("(//programlisting/following-sibling::calloutlist/callout)[2][@arearefs = \"CO1-1\"]", output, 1);
            assertXpath("(//programlisting/following-sibling::calloutlist/callout)[3][@arearefs = \"CO1-2\"]", output, 1);
        }

        @Test
        @DisplayName("two listing blocks can share the same callout list")
        void twoListingBlocksCanShareTheSameCalloutList() throws Exception {
            var input = """
                    .Import library
                    [source, ruby]
                    ----
                    require 'asciidoctor' # <1>
                    ----

                    .Use library
                    [source, ruby]
                    ----
                    doc = Asciidoctor::Document.new('Hello, World!') # <2>
                    puts doc.convert # <3>
                    ----

                    <1> Describe the first line
                    <2> Describe the second line
                    <3> Describe the third line
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("backend", "docbook")));
            assertXpath("//programlisting", output, 2);
            assertXpath("(//programlisting)[1]//co", output, 1);
            assertXpath("(//programlisting)[1]//co[@xml:id=\"CO1-1\"]", output, 1);
            assertXpath("(//programlisting)[2]//co", output, 2);
            assertXpath("((//programlisting)[2]//co)[1][@xml:id=\"CO1-2\"]", output, 1);
            assertXpath("((//programlisting)[2]//co)[2][@xml:id=\"CO1-3\"]", output, 1);
            assertXpath("(//calloutlist/callout)[1][@arearefs = \"CO1-1\"]", output, 1);
            assertXpath("(//calloutlist/callout)[2][@arearefs = \"CO1-2\"]", output, 1);
            assertXpath("(//calloutlist/callout)[3][@arearefs = \"CO1-3\"]", output, 1);
        }

        @Test
        @DisplayName("two listing blocks each followed by an adjacent callout list")
        void twoListingBlocksEachFollowedByAnAdjacentCalloutList() throws Exception {
            var input = """
                    .Import library
                    [source, ruby]
                    ----
                    require 'asciidoctor' # <1>
                    ----
                    <1> Describe the first line

                    .Use library
                    [source, ruby]
                    ----
                    doc = Asciidoctor::Document.new('Hello, World!') # <1>
                    puts doc.convert # <2>
                    ----
                    <1> Describe the second line
                    <2> Describe the third line
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("backend", "docbook")));
            assertXpath("//programlisting", output, 2);
            assertXpath("(//programlisting)[1]//co", output, 1);
            assertXpath("(//programlisting)[1]//co[@xml:id=\"CO1-1\"]", output, 1);
            assertXpath("(//programlisting)[2]//co", output, 2);
            assertXpath("((//programlisting)[2]//co)[1][@xml:id=\"CO2-1\"]", output, 1);
            assertXpath("((//programlisting)[2]//co)[2][@xml:id=\"CO2-2\"]", output, 1);
            assertXpath("//calloutlist", output, 2);
            assertXpath("(//calloutlist)[1]/callout", output, 1);
            assertXpath("((//calloutlist)[1]/callout)[1][@arearefs = \"CO1-1\"]", output, 1);
            assertXpath("(//calloutlist)[2]/callout", output, 2);
            assertXpath("((//calloutlist)[2]/callout)[1][@arearefs = \"CO2-1\"]", output, 1);
            assertXpath("((//calloutlist)[2]/callout)[2][@arearefs = \"CO2-2\"]", output, 1);
        }

        @Test
        @DisplayName("callout list retains block content")
        void calloutListRetainsBlockContent() throws Exception {
            var input = """
                    [source, ruby]
                    ----
                    require 'asciidoctor' # <1>
                    doc = Asciidoctor::Document.new('Hello, World!') # <2>
                    puts doc.convert # <3>
                    ----
                    <1> Imports the library
                    as a RubyGem
                    <2> Creates a new document
                    * Scans the lines for known blocks
                    * Converts the lines into blocks
                    <3> Renders the document
                    +
                    You can write this to file rather than printing to stdout.
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//ol/li", output, 3);
            assertXpath("(//ol/li)[1]/p[text()=\"Imports the library\nas a RubyGem\"]", output, 1);
            assertXpath("(//ol/li)[2]//ul", output, 1);
            assertXpath("(//ol/li)[2]//ul/li", output, 2);
            assertXpath("(//ol/li)[3]//p", output, 2);
        }

        @Test
        @DisplayName("callout list retains block content when converted to DocBook")
        void calloutListRetainsBlockContentWhenConvertedToDocBook() throws Exception {
            var input = """
                    [source, ruby]
                    ----
                    require 'asciidoctor' # <1>
                    doc = Asciidoctor::Document.new('Hello, World!') # <2>
                    puts doc.convert # <3>
                    ----
                    <1> Imports the library
                    as a RubyGem
                    <2> Creates a new document
                    * Scans the lines for known blocks
                    * Converts the lines into blocks
                    <3> Renders the document
                    +
                    You can write this to file rather than printing to stdout.
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("backend", "docbook")));
            assertXpath("//calloutlist", output, 1);
            assertXpath("//calloutlist/callout", output, 3);
            assertXpath("(//calloutlist/callout)[1]/*", output, 1);
            assertXpath("(//calloutlist/callout)[2]/para", output, 1);
            assertXpath("(//calloutlist/callout)[2]/itemizedlist", output, 1);
            assertXpath("(//calloutlist/callout)[3]/para", output, 1);
            assertXpath("(//calloutlist/callout)[3]/simpara", output, 1);
        }

        @Test
        @DisplayName("escaped callout should not be interpreted as a callout")
        void escapedCalloutShouldNotBeInterpretedAsACallout() throws Exception {
            var input = """
                    [source,text]
                    ----
                    require 'asciidoctor' # \\<1>
                    Asciidoctor.convert 'convert me!' \\<2>
                    ----
                    """;
            for (var attributes : list(Options.of(), Options.of("source-highlighter", "coderay"))) {
                var output = convertStringToEmbedded(input, Options.of("attributes", attributes));
                assertCss("pre b", output, 0);
                assertIncludes(output, " # &lt;1&gt;");
                assertIncludes(output, " &lt;2&gt;");
            }
        }

        @Test
        @DisplayName("should autonumber <.> callouts")
        void shouldAutonumberCallouts() throws Exception {
            var input = """
                    [source, ruby]
                    ----
                    require 'asciidoctor' # <.>
                    doc = Asciidoctor::Document.new('Hello, World!') # <.>
                    puts doc.convert # <.>
                    ----
                    <.> Describe the first line
                    <.> Describe the second line
                    <.> Describe the third line
                    """;
            var output = convertStringToEmbedded(input);
            var preHtml = xmlnodesAtCss("pre", output).get(0).getInnerHtml();
            assertIncludes(preHtml, "(1)");
            assertIncludes(preHtml, "(2)");
            assertIncludes(preHtml, "(3)");
            assertCss(".colist ol", output, 1);
            assertCss(".colist ol li", output, 3);
        }

        @Test
        @DisplayName("should not recognize callouts in middle of line")
        void shouldNotRecognizeCalloutsInMiddleOfLine() throws Exception {
            var input = """
                    [source, ruby]
                    ----
                    puts "The syntax <1> at the end of the line makes a code callout"
                    ----
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//b", output, 0);
        }

        @Test
        @DisplayName("should allow multiple callouts on the same line")
        void shouldAllowMultipleCalloutsOnTheSameLine() throws Exception {
            var input = """
                    [source, ruby]
                    ----
                    require 'asciidoctor' <1>
                    doc = Asciidoctor.load('Hello, World!') # <2> <3> <4>
                    puts doc.convert <5><6>
                    exit 0
                    ----
                    <1> Require library
                    <2> Load document from String
                    <3> Uses default backend and doctype
                    <4> One more for good luck
                    <5> Renders document to String
                    <6> Prints output to stdout
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//code/b", output, 6);
            assertMatch(rx(" <b class=\"conum\">\\(1\\)</b>$"), output);
            assertMatch(rx(" <b class=\"conum\">\\(2\\)</b> <b class=\"conum\">\\(3\\)</b> <b class=\"conum\">\\(4\\)</b>$"), output);
            assertMatch(rx(" <b class=\"conum\">\\(5\\)</b><b class=\"conum\">\\(6\\)</b>$"), output);
        }

        @Test
        @DisplayName("should allow XML comment-style callouts")
        void shouldAllowXMLCommentStyleCallouts() throws Exception {
            var input = """
                    [source, xml]
                    ----
                    <section>
                      <title>Section Title</title> <!--1-->
                      <simpara>Just a paragraph</simpara> <!--2-->
                    </section>
                    ----
                    <1> The title is required
                    <2> The content isn't
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//b", output, 2);
            assertXpath("//b[text()=\"(1)\"]", output, 1);
            assertXpath("//b[text()=\"(2)\"]", output, 1);
        }

        @Test
        @DisplayName("should not allow callouts with half an XML comment")
        void shouldNotAllowCalloutsWithHalfAnXMLComment() throws Exception {
            var input = """
                    ----
                    First line <1-->
                    Second line <2-->
                    ----
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//b", output, 0);
        }

        @Test
        @DisplayName("should not recognize callouts in an indented description list paragraph")
        void shouldNotRecognizeCalloutsInAnIndentedDescriptionListParagraph() throws Exception {
            var input = """
                    foo::
                      bar <1>

                    <1> Not pointing to a callout
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertXpath("//dl//b", output, 0);
                assertXpath("//dl/dd/p[text()=\"bar <1>\"]", output, 1);
                assertXpath("//ol/li/p[text()=\"Not pointing to a callout\"]", output, 1);
                assertMessage(logger, "WARN", "<stdin>: line 4: no callout found for <1>", LogMessage.class);
            }
        }

        @Test
        @DisplayName("should not recognize callouts in an indented outline list paragraph")
        void shouldNotRecognizeCalloutsInAnIndentedOutlineListParagraph() throws Exception {
            var input = """
                    * foo
                      bar <1>

                    <1> Not pointing to a callout
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertXpath("//ul//b", output, 0);
                assertXpath("//ul/li/p[text()=\"foo\nbar <1>\"]", output, 1);
                assertXpath("//ol/li/p[text()=\"Not pointing to a callout\"]", output, 1);
                assertMessage(logger, "WARN", "<stdin>: line 4: no callout found for <1>", LogMessage.class);
            }
        }

        @Test
        @DisplayName("should warn if numbers in callout list are out of sequence")
        void shouldWarnIfNumbersInCalloutListAreOutOfSequence() throws Exception {
            var input = """
                    ----
                    <beans> <1>
                      <bean class="com.example.HelloWorld"/>
                    </beans>
                    ----
                    <1> Container of beans.
                    Beans are fun.
                    <3> An actual bean.
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertXpath("//ol/li", output, 2);
                assertMessages(logger, list(list("WARN", "<stdin>: line 8: callout list item index: expected 2, got 3", LogMessage.class), list("WARN", "<stdin>: line 8: no callout found for <2>", LogMessage.class)));
            }
        }

        @Test
        @DisplayName("should preserve line comment chars that precede callout number if icons is not set")
        void shouldPreserveLineCommentCharsThatPrecedeCalloutNumberIfIconsIsNotSet() throws Exception {
            var input = """
                    [source,ruby]
                    ----
                    puts 'Hello, world!' # <1>
                    ----
                    <1> Ruby

                    [source,groovy]
                    ----
                    println 'Hello, world!' // <1>
                    ----
                    <1> Groovy

                    [source,clojure]
                    ----
                    (def hello (fn [] "Hello, world!")) ;; <1>
                    (hello)
                    ----
                    <1> Clojure

                    [source,haskell]
                    ----
                    main = putStrLn "Hello, World!" -- <1>
                    ----
                    <1> Haskell
                    """;
            for (var attributes : list(Options.of(), Options.of("source-highlighter", "coderay"))) {
                var output = convertStringToEmbedded(input, Options.of("attributes", attributes));
                assertXpath("//b", output, 4);
                var nodes = xmlnodesAtCss("pre", output);
                assertEquals("puts 'Hello, world!' # (1)", nodes.get(0).getText());
                assertEquals("println 'Hello, world!' // (1)", nodes.get(1).getText());
                assertEquals("(def hello (fn [] \"Hello, world!\")) ;; (1)\n(hello)", nodes.get(2).getText());
                assertEquals("main = putStrLn \"Hello, World!\" -- (1)", nodes.get(3).getText());
            }
        }

        @Test
        @DisplayName("should remove line comment chars that precede callout number if icons is font")
        void shouldRemoveLineCommentCharsThatPrecedeCalloutNumberIfIconsIsFont() throws Exception {
            var input = """
                    [source,ruby]
                    ----
                    puts 'Hello, world!' # <1>
                    ----
                    <1> Ruby

                    [source,groovy]
                    ----
                    println 'Hello, world!' // <1>
                    ----
                    <1> Groovy

                    [source,clojure]
                    ----
                    (def hello (fn [] "Hello, world!")) ;; <1>
                    (hello)
                    ----
                    <1> Clojure

                    [source,haskell]
                    ----
                    main = putStrLn "Hello, World!" -- <1>
                    ----
                    <1> Haskell
                    """;
            for (var attributes : list(Options.of(), Options.of("source-highlighter", "coderay"))) {
                var output = convertStringToEmbedded(input, Options.of("attributes", merge(attributes, Options.of("icons", "font"))));
                assertCss("pre b", output, 4);
                assertCss("pre i.conum", output, 4);
                var nodes = xmlnodesAtCss("pre", output);
                assertEquals("puts 'Hello, world!' (1)", nodes.get(0).getText());
                assertEquals("println 'Hello, world!' (1)", nodes.get(1).getText());
                assertEquals("(def hello (fn [] \"Hello, world!\")) (1)\n(hello)", nodes.get(2).getText());
                assertEquals("main = putStrLn \"Hello, World!\" (1)", nodes.get(3).getText());
            }
        }

        @Test
        @DisplayName("should allow line comment chars that precede callout number to be specified")
        void shouldAllowLineCommentCharsThatPrecedeCalloutNumberToBeSpecified() throws Exception {
            var input = """
                    [source,erlang,line-comment=%]
                    ----
                    hello_world() -> % <1>
                      io:fwrite("hello, world~n"). %<2>
                    ----
                    <1> Erlang function clause head.
                    <2> ~n adds a new line to the output.
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//b", output, 2);
            var nodes = xmlnodesAtCss("pre", output);
            assertEquals("hello_world() -> % (1)\n  io:fwrite(\"hello, world~n\"). %(2)", nodes.get(0).getText());
        }

        @Test
        @DisplayName("should allow line comment chars preceding callout number to be configurable when source-highlighter is coderay")
        void shouldAllowLineCommentCharsPrecedingCalloutNumberToBeConfigurableWhenSourceHighlighterIsCoderay() throws Exception {
            var input = """
                    [source,html,line-comment=-#]
                    ----
                    -# <1>
                    %p Hello
                    ----
                    <1> Prints a paragraph with the text "Hello"
                    """;
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("source-highlighter", "coderay")));
            assertXpath("//b", output, 1);
            var nodes = xmlnodesAtCss("pre", output);
            assertEquals("-# (1)\n%p Hello", nodes.get(0).getText());
        }

        @Test
        @DisplayName("should not eat whitespace before callout number if line-comment attribute is empty")
        void shouldNotEatWhitespaceBeforeCalloutNumberIfLineCommentAttributeIsEmpty() throws Exception {
            var input = """
                    [source,asciidoc,line-comment=]
                    ----
                    -- <1>
                    ----
                    <1> The start of an open block.
                    """;
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("icons", "font")));
            assertIncludes(output, "-- <i class=\"conum\"");
        }

        @Test
        @DisplayName("literal block with callouts")
        void literalBlockWithCallouts() throws Exception {
            var input = """
                    ....
                    Roses are red <1>
                    Violets are blue <2>
                    ....


                    <1> And so is Ruby
                    <2> But violet is more like purple
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("backend", "docbook")));
            assertXpath("//literallayout", output, 1);
            assertXpath("//literallayout//co", output, 2);
            assertXpath("(//literallayout//co)[1][@xml:id=\"CO1-1\"]", output, 1);
            assertXpath("(//literallayout//co)[2][@xml:id=\"CO1-2\"]", output, 1);
            assertXpath("//literallayout/following-sibling::*[1][self::calloutlist]/callout", output, 2);
            assertXpath("(//literallayout/following-sibling::*[1][self::calloutlist]/callout)[1][@arearefs = \"CO1-1\"]", output, 1);
            assertXpath("(//literallayout/following-sibling::*[1][self::calloutlist]/callout)[2][@arearefs = \"CO1-2\"]", output, 1);
        }

        @Test
        @DisplayName("callout list with icons enabled")
        void calloutListWithIconsEnabled() throws Exception {
            var input = """
                    [source, ruby]
                    ----
                    require 'asciidoctor' # <1>
                    doc = Asciidoctor::Document.new('Hello, World!') # <2>
                    puts doc.convert # <3>
                    ----
                    <1> Describe the first line
                    <2> Describe the second line
                    <3> Describe the third line
                    """;
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("icons", "")));
            assertCss(".listingblock code > img", output, 3);
            for (var i : range(1, 3)) {
                assertXpath(("(/div[@class=\"listingblock\"]//code/img)[" + str(i) + "][@src=\"./images/icons/callouts/" + str(i) + ".png\"][@alt=\"" + str(i) + "\"]"), output, 1);
            }
            assertCss(".colist table td img", output, 3);
            for (var i : range(1, 3)) {
                assertXpath(("(/div[@class=\"colist arabic\"]//td/img)[" + str(i) + "][@src=\"./images/icons/callouts/" + str(i) + ".png\"][@alt=\"" + str(i) + "\"]"), output, 1);
            }
        }

        @Test
        @DisplayName("callout list with font-based icons enabled")
        void calloutListWithFontBasedIconsEnabled() throws Exception {
            var input = """
                    [source]
                    ----
                    require 'asciidoctor' # <1>
                    doc = Asciidoctor::Document.new('Hello, World!') #<2>
                    puts doc.convert #<3>
                    ----
                    <1> Describe the first line
                    <2> Describe the second line
                    <3> Describe the third line
                    """;
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("icons", "font")));
            assertCss(".listingblock code > i", output, 3);
            for (var i : range(1, 3)) {
                assertXpath(("(/div[@class=\"listingblock\"]//code/i)[" + str(i) + "]"), output, 1);
                assertXpath(("(/div[@class=\"listingblock\"]//code/i)[" + str(i) + "][@class=\"conum\"][@data-value=\"" + str(i) + "\"]"), output, 1);
                assertXpath(("(/div[@class=\"listingblock\"]//code/i)[" + str(i) + "]/following-sibling::b[text()=\"(" + str(i) + ")\"]"), output, 1);
            }
            assertCss(".colist table td i", output, 3);
            for (var i : range(1, 3)) {
                assertXpath(("(/div[@class=\"colist arabic\"]//td/i)[" + str(i) + "]"), output, 1);
                assertXpath(("(/div[@class=\"colist arabic\"]//td/i)[" + str(i) + "][@class=\"conum\"][@data-value = \"" + str(i) + "\"]"), output, 1);
                assertXpath(("(/div[@class=\"colist arabic\"]//td/i)[" + str(i) + "]/following-sibling::b[text() = \"" + str(i) + "\"]"), output, 1);
            }
        }

        @Test
        @DisplayName("should match trailing line separator in text of list item")
        void shouldMatchTrailingLineSeparatorInTextOfListItem4() throws Exception {
            var input = Strings.chop(("----\nA <1>\nB <2>\nC <3>\n----\n<1> a\n<2> b" + str(decodeChar(8232)) + "\n<3> c\n"));
            var output = convertString(input);
            assertCss("li", output, 3);
            assertXpath(("(//li)[2]/p[text()=\"b" + str(decodeChar(8232)) + "\"]"), output, 1);
        }

        @Test
        @DisplayName("should match line separator in text of list item")
        void shouldMatchLineSeparatorInTextOfListItem4() throws Exception {
            var input = Strings.chop(("----\nA <1>\nB <2>\nC <3>\n----\n<1> a\n<2> b" + str(decodeChar(8232)) + "b\n<3> c\n"));
            var output = convertString(input);
            assertCss("li", output, 3);
            assertXpath(("(//li)[2]/p[text()=\"b" + str(decodeChar(8232)) + "b\"]"), output, 1);
        }
    }

    @Nested
    @DisplayName("Checklists")
    class Checklists {

        @Test
        @DisplayName("should create checklist if at least one item has checkbox syntax")
        void shouldCreateChecklistIfAtLeastOneItemHasCheckboxSyntax() throws Exception {
            var input = """
                    - [ ] todo
                    - [x] done
                    - [ ] another todo
                    - [*] another done
                    - plain
                    """;
            var doc = documentFromString(input);
            var checklist = doc.getBlocks().get(0);
            assertTruthy(checklist.isOption("checklist"));
            assertTruthy(((List) checklist).getItems().get(0).hasAttribute("checkbox"));
            assertFalsy(((List) checklist).getItems().get(0).hasAttribute("checked"));
            assertTruthy(((List) checklist).getItems().get(1).hasAttribute("checkbox"));
            assertTruthy(((List) checklist).getItems().get(1).hasAttribute("checked"));
            assertFalsy(((List) checklist).getItems().get(4).hasAttribute("checkbox"));
            var output = doc.convert(Options.of("standalone", false));
            assertCss(".ulist.checklist", output, 1);
            assertXpath(("(/*[@class=\"ulist checklist\"]/ul/li)[1]/p[text()=\"" + str(decodeChar(10063)) + " todo\"]"), output, 1);
            assertXpath(("(/*[@class=\"ulist checklist\"]/ul/li)[2]/p[text()=\"" + str(decodeChar(10003)) + " done\"]"), output, 1);
            assertXpath(("(/*[@class=\"ulist checklist\"]/ul/li)[3]/p[text()=\"" + str(decodeChar(10063)) + " another todo\"]"), output, 1);
            assertXpath(("(/*[@class=\"ulist checklist\"]/ul/li)[4]/p[text()=\"" + str(decodeChar(10003)) + " another done\"]"), output, 1);
            assertXpath("(/*[@class=\"ulist checklist\"]/ul/li)[5]/p[text()=\"plain\"]", output, 1);
        }

        @Test
        @DisplayName("entry is not a checklist item if the closing bracket is not immediately followed by the space character")
        void entryIsNotAChecklistItemIfTheClosingBracketIsNotImmediatelyFollowedByTheSpaceCharacter() throws Exception {
            var input = """
                    - [ ]    todo
                    - [x] 	 done
                    - [ ]	  another todo
                    - [x]	  another done
                    """;
            var doc = documentFromString(input);
            var checklist = doc.getBlocks().get(0);
            assertTruthy(checklist.isOption("checklist"));
            assertTruthy(((List) checklist).getItems().get(0).hasAttribute("checkbox"));
            assertFalsy(((List) checklist).getItems().get(0).hasAttribute("checked"));
            assertTruthy(((List) checklist).getItems().get(1).hasAttribute("checkbox"));
            assertTruthy(((List) checklist).getItems().get(1).hasAttribute("checked"));
            assertFalsy(((List) checklist).getItems().get(2).hasAttribute("checkbox"));
            assertFalsy(((List) checklist).getItems().get(3).hasAttribute("checkbox"));
        }

        @Test
        @DisplayName("should create checklist with font icons if at least one item has checkbox syntax and icons attribute is font")
        void shouldCreateChecklistWithFontIconsIfAtLeastOneItemHasCheckboxSyntaxAndIconsAttributeIsFont() throws Exception {
            var input = """
                    - [ ] todo
                    - [x] done
                    - plain
                    """;
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("icons", "font")));
            assertCss(".ulist.checklist", output, 1);
            assertCss(".ulist.checklist li i.fa-check-square-o", output, 1);
            assertCss(".ulist.checklist li i.fa-square-o", output, 1);
            assertXpath("(/*[@class=\"ulist checklist\"]/ul/li)[3]/p[text()=\"plain\"]", output, 1);
        }

        @Test
        @DisplayName("should create interactive checklist if interactive option is set even with icons attribute is font")
        void shouldCreateInteractiveChecklistIfInteractiveOptionIsSetEvenWithIconsAttributeIsFont() throws Exception {
            var input = """
                    :icons: font

                    [%interactive]
                    - [ ] todo
                    - [x] done
                    """;
            var doc = documentFromString(input);
            var checklist = doc.getBlocks().get(0);
            assertTruthy(checklist.isOption("checklist"));
            assertTruthy(checklist.isOption("interactive"));
            var output = doc.convert(Options.of("standalone", false));
            assertCss(".ulist.checklist", output, 1);
            assertCss(".ulist.checklist li input[type=\"checkbox\"]", output, 2);
            assertCss(".ulist.checklist li input[type=\"checkbox\"][disabled]", output, 0);
            assertCss(".ulist.checklist li input[type=\"checkbox\"][checked]", output, 1);
        }

        @Test
        @DisplayName("should not create checklist if checkbox on item is followed by a tab")
        void shouldNotCreateChecklistIfCheckboxOnItemIsFollowedByATab() throws Exception {
            for (var checkbox : list("[ ]", "[x]", "[*]")) {
                var input = ("- " + str(checkbox) + "\ttodo\n");
                var doc = documentFromString(input);
                var list = doc.getBlocks().get(0);
                assertEquals("ulist", list.getContext());
                assertFalsy(list.isOption("checklist"));
            }
        }
    }

    @Nested
    @DisplayName("Lists model")
    class ListsModel {

        @Test
        @DisplayName("content should return items in list")
        void contentShouldReturnItemsInList() throws Exception {
            var input = """
                    * one
                    * two
                    * three
                    """;
            var doc = documentFromString(input);
            var list = (List) first(doc.getBlocks());
            assertInstanceOf(List.class, list);
            var items = ((List) list).getItems();
            assertEquals(3, size(items));
            assertEquals(((List) list).getItems(), list.getItems());
        }

        @Test
        @DisplayName("list item should be the parent of block attached to a list item")
        void listItemShouldBeTheParentOfBlockAttachedToAListItem() throws Exception {
            var input = """
                    * list item 1
                    +
                    ----
                    listing block in list item 1
                    ----
                    """;
            var doc = documentFromString(input);
            var list = (List) first(doc.getBlocks());
            var listItem1 = first(((List) list).getItems());
            var listingBlock = first(listItem1.getBlocks());
            assertEquals("listing", listingBlock.getContext());
            assertEquals(listItem1, listingBlock.getParent());
        }

        @Test
        @DisplayName("outline? should return true for unordered list")
        void outlineShouldReturnTrueForUnorderedList() throws Exception {
            var input = """
                    * one
                    * two
                    * three
                    """;
            var doc = documentFromString(input);
            var list = (List) first(doc.getBlocks());
            assertTruthy(list.isOutline());
        }

        @Test
        @DisplayName("outline? should return true for ordered list")
        void outlineShouldReturnTrueForOrderedList() throws Exception {
            var input = """
                    . one
                    . two
                    . three
                    """;
            var doc = documentFromString(input);
            var list = (List) first(doc.getBlocks());
            assertTruthy(list.isOutline());
        }

        @Test
        @DisplayName("outline? should return false for description list")
        void outlineShouldReturnFalseForDescriptionList() throws Exception {
            var input = "label:: desc";
            var doc = documentFromString(input);
            var list = (List) first(doc.getBlocks());
            assertFalsy(list.isOutline());
        }

        @Test
        @DisplayName("simple? should return true for list item with no nested blocks")
        void simpleShouldReturnTrueForListItemWithNoNestedBlocks() throws Exception {
            var input = """
                    * one
                    * two
                    * three
                    """;
            var doc = documentFromString(input);
            var list = (List) first(doc.getBlocks());
            assertTruthy(((ListItem) first(((List) list).getItems())).isSimple());
            assertFalsy(((ListItem) first(((List) list).getItems())).isCompound());
        }

        @Test
        @DisplayName("simple? should return true for list item with nested outline list")
        void simpleShouldReturnTrueForListItemWithNestedOutlineList() throws Exception {
            var input = """
                    * one
                      ** more about one
                      ** and more
                    * two
                    * three
                    """;
            var doc = documentFromString(input);
            var list = (List) first(doc.getBlocks());
            assertTruthy(((ListItem) first(((List) list).getItems())).isSimple());
            assertFalsy(((ListItem) first(((List) list).getItems())).isCompound());
        }

        @Test
        @DisplayName("simple? should return false for list item with block content")
        void simpleShouldReturnFalseForListItemWithBlockContent() throws Exception {
            var input = """
                    * one
                    +
                    ----
                    listing block in list item 1
                    ----
                    * two
                    * three
                    """;
            var doc = documentFromString(input);
            var list = (List) first(doc.getBlocks());
            assertFalsy(((ListItem) first(((List) list).getItems())).isSimple());
            assertTruthy(((ListItem) first(((List) list).getItems())).isCompound());
        }

        @Test
        @DisplayName("should allow text of ListItem to be assigned")
        void shouldAllowTextOfListItemToBeAssigned() throws Exception {
            var input = """
                    * one
                    * two
                    * three
                    """;
            var doc = documentFromString(input);
            var list = first(doc.findBy(AbstractBlock.Selector.any().withContext("ulist")));
            assertEquals(3, size(((List) list).getItems()));
            assertEquals("one", ((ListItem) ((List) list).getItems().get(0)).getText());
            ((ListItem) ((List) list).getItems().get(0)).setText("un");
            assertEquals("un", ((ListItem) ((List) list).getItems().get(0)).getText());
        }

        @Test
        @DisplayName("id and role assigned to ulist item in model are transmitted to output")
        void idAndRoleAssignedToUlistItemInModelAreTransmittedToOutput() throws Exception {
            var input = """
                    * one
                    * two
                    * three
                    """;
            var doc = documentFromString(input);
            var item0 = ((List) doc.getBlocks().get(0)).getItems().get(0);
            item0.setId("one");
            item0.addRole("item");
            var output = doc.convert();
            assertCss("li#one.item", output, 1);
        }

        @Test
        @DisplayName("id and role assigned to olist item in model are transmitted to output")
        void idAndRoleAssignedToOlistItemInModelAreTransmittedToOutput() throws Exception {
            var input = """
                    . one
                    . two
                    . three
                    """;
            var doc = documentFromString(input);
            var item0 = ((List) doc.getBlocks().get(0)).getItems().get(0);
            item0.setId("one");
            item0.addRole("item");
            var output = doc.convert();
            assertCss("li#one.item", output, 1);
        }

        @Test
        @DisplayName("should allow API control over substitutions applied to ListItem text")
        void shouldAllowAPIControlOverSubstitutionsAppliedToListItemText() throws Exception {
            var input = """
                    * *one*
                    * _two_
                    * `three`
                    * #four#
                    """;
            var doc = documentFromString(input);
            var list = first(doc.findBy(AbstractBlock.Selector.any().withContext("ulist")));
            assertEquals(4, size(((List) list).getItems()));
            ((List) list).getItems().get(0).removeSub(Substitution.QUOTES);
            assertEquals("*one*", ((ListItem) ((List) list).getItems().get(0)).getText());
            refuteIncludes(((List) list).getItems().get(0).getSubs(), Substitution.QUOTES);
            ((List) list).getItems().get(1).getSubs().clear();
            assertEmpty(((List) list).getItems().get(1).getSubs());
            assertEquals("_two_", ((ListItem) ((List) list).getItems().get(1)).getText());
            ((List) list).getItems().get(2).getSubs().clear();
            ((List) list).getItems().get(2).getSubs().add(Substitution.SPECIAL_CHARACTERS);
            assertEquals(list(Substitution.SPECIAL_CHARACTERS), ((List) list).getItems().get(2).getSubs());
            assertEquals("`three`", ((ListItem) ((List) list).getItems().get(2)).getText());
            assertEquals("<mark>four</mark>", ((ListItem) ((List) list).getItems().get(3)).getText());
        }

        @Test
        @DisplayName("should set lineno to line number in source where list starts")
        void shouldSetLinenoToLineNumberInSourceWhereListStarts() throws Exception {
            var input = """
                    * bullet 1
                    ** bullet 1.1
                    *** bullet 1.1.1
                    * bullet 2
                    """;
            var doc = documentFromString(input, Options.of("sourcemap", true));
            var lists = doc.findBy(AbstractBlock.Selector.any().withContext("ulist"));
            assertEquals(1, lists.get(0).getLineNumber());
            assertEquals(2, lists.get(1).getLineNumber());
            assertEquals(3, lists.get(2).getLineNumber());
            var listItems = doc.findBy(AbstractBlock.Selector.any().withContext("list_item"));
            assertEquals(1, listItems.get(0).getLineNumber());
            assertEquals(2, listItems.get(1).getLineNumber());
            assertEquals(3, listItems.get(2).getLineNumber());
            assertEquals(4, listItems.get(3).getLineNumber());
        }
    }
}
