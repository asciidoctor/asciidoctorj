package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.nio.file.Files;

import org.junit.jupiter.api.*;

@DisplayName("Tables")
public class TablesTest extends TestHelper {

    @Nested
    @DisplayName("PSV")
    class PSV {

        @Test
        @DisplayName("converts simple psv table")
        void convertsSimplePsvTable() throws Exception {
            var input = """
                    |=======
                    |A |B |C
                    |a |b |c
                    |1 |2 |3
                    |=======
                    """;
            var cells = list(list("A", "B", "C"), list("a", "b", "c"), list("1", "2", "3"));
            var doc = documentFromString(input, Options.of("standalone", false));
            var table = doc.getBlocks().get(0);
            assertEquals(100, sum(map(((Table) table).getColumns(), col -> get(col.getAttributes(), "colpcwidth"))));
            var output = doc.convert();
            assertCss("table", output, 1);
            assertCss("table.tableblock.frame-all.grid-all.stretch", output, 1);
            assertCss("table > colgroup > col[width=\"33.3333%\"]", output, 2);
            assertCss("table > colgroup > col:last-of-type[width=\"33.3334%\"]", output, 1);
            assertCss("table tr", output, 3);
            assertCss("table > tbody > tr", output, 3);
            assertCss("table td", output, 9);
            assertCss("table > tbody > tr > td.tableblock.halign-left.valign-top > p.tableblock", output, 9);
            var rowList = cells;
            for (int rowi = 0; rowi < rowList.size(); rowi++) {
                var row = rowList.get(rowi);
                assertCss(("table > tbody > tr:nth-child(" + str((rowi + 1)) + ") > td"), output, size(row));
                assertCss(("table > tbody > tr:nth-child(" + str((rowi + 1)) + ") > td > p"), output, size(row));
                var cellList = row;
                for (int celli = 0; celli < cellList.size(); celli++) {
                    var cell = cellList.get(celli);
                    assertXpath(("(//tr)[" + str((rowi + 1)) + "]/td[" + str((celli + 1)) + "]/p[text()='" + str(cell) + "']"), output, 1);
                }
            }
        }

        @Test
        @DisplayName("should add direction CSS class if float attribute is set on table")
        void shouldAddDirectionCSSClassIfFloatAttributeIsSetOnTable() throws Exception {
            var input = """
                    [float=left]
                    |=======
                    |A |B |C
                    |a |b |c
                    |1 |2 |3
                    |=======
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table.left", output, 1);
        }

        @Test
        @DisplayName("should set stripes class if stripes option is set")
        void shouldSetStripesClassIfStripesOptionIsSet() throws Exception {
            var input = """
                    [stripes=odd]
                    |=======
                    |A |B |C
                    |a |b |c
                    |1 |2 |3
                    |=======
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table.stripes-odd", output, 1);
        }

        @Test
        @DisplayName("outputs a caption on simple psv table")
        void outputsACaptionOnSimplePsvTable() throws Exception {
            var input = """
                    .Simple psv table
                    |=======
                    |A |B |C
                    |a |b |c
                    |1 |2 |3
                    |=======
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/table/caption[@class=\"title\"][text()=\"Table 1. Simple psv table\"]", output, 1);
            assertXpath("/table/caption/following-sibling::colgroup", output, 1);
        }

        @Test
        @DisplayName("only increments table counter for tables that have a title")
        void onlyIncrementsTableCounterForTablesThatHaveATitle() throws Exception {
            var input = """
                    .First numbered table
                    |=======
                    |1 |2 |3
                    |=======

                    |=======
                    |4 |5 |6
                    |=======

                    .Second numbered table
                    |=======
                    |7 |8 |9
                    |=======
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table:root", output, 3);
            assertXpath("(/table)[1]/caption", output, 1);
            assertXpath("(/table)[1]/caption[text()=\"Table 1. First numbered table\"]", output, 1);
            assertXpath("(/table)[2]/caption", output, 0);
            assertXpath("(/table)[3]/caption", output, 1);
            assertXpath("(/table)[3]/caption[text()=\"Table 2. Second numbered table\"]", output, 1);
        }

        @Test
        @DisplayName("uses explicit caption in front of title in place of default caption and number")
        void usesExplicitCaptionInFrontOfTitleInPlaceOfDefaultCaptionAndNumber() throws Exception {
            var input = """
                    [caption="All the Data. "]
                    .Simple psv table
                    |=======
                    |A |B |C
                    |a |b |c
                    |1 |2 |3
                    |=======
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/table/caption[@class=\"title\"][text()=\"All the Data. Simple psv table\"]", output, 1);
            assertXpath("/table/caption/following-sibling::colgroup", output, 1);
        }

        @Test
        @DisplayName("disables caption when caption attribute on table is empty")
        void disablesCaptionWhenCaptionAttributeOnTableIsEmpty() throws Exception {
            var input = """
                    [caption=]
                    .Simple psv table
                    |=======
                    |A |B |C
                    |a |b |c
                    |1 |2 |3
                    |=======
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/table/caption[@class=\"title\"][text()=\"Simple psv table\"]", output, 1);
            assertXpath("/table/caption/following-sibling::colgroup", output, 1);
        }

        @Test
        @DisplayName("disables caption when caption attribute on table is empty string")
        void disablesCaptionWhenCaptionAttributeOnTableIsEmptyString() throws Exception {
            var input = """
                    [caption=""]
                    .Simple psv table
                    |=======
                    |A |B |C
                    |a |b |c
                    |1 |2 |3
                    |=======
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/table/caption[@class=\"title\"][text()=\"Simple psv table\"]", output, 1);
            assertXpath("/table/caption/following-sibling::colgroup", output, 1);
        }

        @Test
        @DisplayName("disables caption on table when table-caption document attribute is unset")
        void disablesCaptionOnTableWhenTableCaptionDocumentAttributeIsUnset() throws Exception {
            var input = """
                    :!table-caption:

                    .Simple psv table
                    |=======
                    |A |B |C
                    |a |b |c
                    |1 |2 |3
                    |=======
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/table/caption[@class=\"title\"][text()=\"Simple psv table\"]", output, 1);
            assertXpath("/table/caption/following-sibling::colgroup", output, 1);
        }

        @Test
        @DisplayName("ignores escaped separators")
        void ignoresEscapedSeparators() throws Exception {
            var input = """
                    |===
                    |A \\| here| a \\| there
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > tbody > tr", output, 1);
            assertCss("table > tbody > tr > td", output, 2);
            assertXpath("/table/tbody/tr/td[1]/p[text()=\"A | here\"]", output, 1);
            assertXpath("/table/tbody/tr/td[2]/p[text()=\"a | there\"]", output, 1);
        }

        @Test
        @DisplayName("preserves escaped delimiters at the end of the line")
        void preservesEscapedDelimitersAtTheEndOfTheLine() throws Exception {
            var input = """
                    [%header,cols="1,1"]
                    |===
                    |A |B\\|
                    |A1 |B1\\|
                    |A2 |B2\\|
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > thead > tr", output, 1);
            assertCss("table > thead > tr:nth-child(1) > th", output, 2);
            assertXpath("/table/thead/tr[1]/th[2][text()=\"B|\"]", output, 1);
            assertCss("table > tbody > tr", output, 2);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 2);
            assertXpath("/table/tbody/tr[1]/td[2]/p[text()=\"B1|\"]", output, 1);
            assertCss("table > tbody > tr:nth-child(2) > td", output, 2);
            assertXpath("/table/tbody/tr[2]/td[2]/p[text()=\"B2|\"]", output, 1);
        }

        @Test
        @DisplayName("should treat trailing pipe as an empty cell")
        void shouldTreatTrailingPipeAsAnEmptyCell() throws Exception {
            var input = """
                    |===
                    |A1 |
                    |B1 |B2
                    |C1 |C2
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > tbody > tr", output, 3);
            assertXpath("/table/tbody/tr[1]/td", output, 2);
            assertXpath("/table/tbody/tr[1]/td[1]/p[text()=\"A1\"]", output, 1);
            assertXpath("/table/tbody/tr[1]/td[2]/p", output, 0);
            assertXpath("/table/tbody/tr[2]/td[1]/p[text()=\"B1\"]", output, 1);
        }

        @Test
        @DisplayName("should auto recover with warning if missing leading separator on first cell")
        void shouldAutoRecoverWithWarningIfMissingLeadingSeparatorOnFirstCell() throws Exception {
            var input = """
                    |===
                    A | here| a | there
                    | x
                    | y
                    | z
                    | end
                    |===
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertCss("table", output, 1);
                assertCss("table > tbody > tr", output, 2);
                assertCss("table > tbody > tr > td", output, 8);
                assertXpath("/table/tbody/tr[1]/td[1]/p[text()=\"A\"]", output, 1);
                assertXpath("/table/tbody/tr[1]/td[2]/p[text()=\"here\"]", output, 1);
                assertXpath("/table/tbody/tr[1]/td[3]/p[text()=\"a\"]", output, 1);
                assertXpath("/table/tbody/tr[1]/td[4]/p[text()=\"there\"]", output, 1);
                assertMessage(logger, "ERROR", "<stdin>: line 2: table missing leading separator; recovering automatically", LogMessage.class);
            }
        }

        @Test
        @DisplayName("performs normal substitutions on cell content")
        void performsNormalSubstitutionsOnCellContent() throws Exception {
            var input = """
                    :show_title: Cool new show
                    |===
                    |{show_title} |Coming soon...
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//tbody/tr/td[1]/p[text()=\"Cool new show\"]", output, 1);
            assertXpath(("//tbody/tr/td[2]/p[text()='Coming soon" + str(decodeChar(8230)) + str(decodeChar(8203)) + "']"), output, 1);
        }

        @Test
        @DisplayName("should only substitute specialchars for literal table cells")
        void shouldOnlySubstituteSpecialcharsForLiteralTableCells() throws Exception {
            var input = """
                    |===
                    l|one
                    *two*
                    three
                    <four>
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            var result = xmlnodesAtXpath("/table//pre", output, 1);
            assertEquals("<pre>one\n*two*\nthree\n&lt;four&gt;</pre>", str(result));
        }

        @Test
        @DisplayName("should preserving leading spaces but not leading newlines or trailing spaces in literal table cells")
        void shouldPreservingLeadingSpacesButNotLeadingNewlinesOrTrailingSpacesInLiteralTableCells() throws Exception {
            var input = """
                    [cols=2*]
                    |===
                    l|
                      one
                      two
                    three

                      | normal
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            var result = xmlnodesAtXpath("/table//pre", output, 1);
            assertEquals("<pre>  one\n  two\nthree</pre>", str(result));
        }

        @Test
        @DisplayName("should ignore v table cell style")
        void shouldIgnoreVTableCellStyle() throws Exception {
            var input = """
                    [cols=2*]
                    |===
                    v|
                      one
                      two
                    three

                      | normal
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            var result = xmlnodesAtXpath("/table//p[@class=\"tableblock\"]", output, 1);
            assertEquals("<p class=\"tableblock\">one\n  two\nthree</p>", str(result));
        }

        @Test
        @DisplayName("table and column width not assigned when autowidth option is specified")
        void tableAndColumnWidthNotAssignedWhenAutowidthOptionIsSpecified() throws Exception {
            var input = """
                    [options="autowidth"]
                    |=======
                    |A |B |C
                    |a |b |c
                    |1 |2 |3
                    |=======
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table.fit-content", output, 1);
            assertCss("table[style*=\"width\"]", output, 0);
            assertCss("table colgroup col", output, 3);
            assertCss("table colgroup col[style*=\"width\"]", output, 0);
        }

        @Test
        @DisplayName("does not assign column width for autowidth columns in HTML output")
        void doesNotAssignColumnWidthForAutowidthColumnsInHTMLOutput() throws Exception {
            var input = """
                    [cols="15%,3*~"]
                    |=======
                    |A |B |C |D
                    |a |b |c |d
                    |1 |2 |3 |4
                    |=======
                    """;
            var doc = documentFromString(input);
            var tableRow0 = ((Table) doc.getBlocks().get(0)).getRows().getBody().get(0);
            assertEquals(15, get(tableRow0.get(0).getAttributes(), "width"));
            assertEquals(15, get(tableRow0.get(0).getAttributes(), "colpcwidth"));
            assertNotEquals("", get(tableRow0.get(0).getAttributes(), "autowidth-option"));
            var expectedPcwidths = Options.of("1", 28.3333, "2", 28.3333, "3", 28.3334);
            for (var i : range(1, 3)) {
                assertEquals(28.3333, get(get(tableRow0, i).getAttributes(), "width"));
                assertEquals(get(expectedPcwidths, i), get(get(tableRow0, i).getAttributes(), "colpcwidth"));
                assertEquals("", get(get(tableRow0, i).getAttributes(), "autowidth-option"));
            }
            var output = doc.convert(Options.of("standalone", false));
            assertCss("table", output, 1);
            assertCss("table colgroup col", output, 4);
            assertCss("table colgroup col[width]", output, 1);
            assertCss("table colgroup col[width=\"15%\"]", output, 1);
        }

        @Test
        @DisplayName("can assign autowidth to all columns even when table has a width")
        void canAssignAutowidthToAllColumnsEvenWhenTableHasAWidth() throws Exception {
            var input = """
                    [cols="4*~",width=50%]
                    |=======
                    |A |B |C |D
                    |a |b |c |d
                    |1 |2 |3 |4
                    |=======
                    """;
            var doc = documentFromString(input);
            var tableRow0 = ((Table) doc.getBlocks().get(0)).getRows().getBody().get(0);
            for (var i : range(0, 3)) {
                assertEquals(25, get(get(tableRow0, i).getAttributes(), "width"));
                assertEquals(25, get(get(tableRow0, i).getAttributes(), "colpcwidth"));
                assertEquals("", get(get(tableRow0, i).getAttributes(), "autowidth-option"));
            }
            var output = doc.convert(Options.of("standalone", false));
            assertCss("table", output, 1);
            assertCss("table[width=\"50%\"]", output, 1);
            assertCss("table colgroup col", output, 4);
            assertCss("table colgroup col[style]", output, 0);
        }

        @Test
        @DisplayName("equally distributes remaining column width to autowidth columns in DocBook output")
        void equallyDistributesRemainingColumnWidthToAutowidthColumnsInDocBookOutput() throws Exception {
            var input = """
                    [cols="15%,3*~"]
                    |=======
                    |A |B |C |D
                    |a |b |c |d
                    |1 |2 |3 |4
                    |=======
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook5"));
            assertCss("tgroup[cols=\"4\"]", output, 1);
            assertCss("tgroup colspec", output, 4);
            assertCss("tgroup colspec[colwidth]", output, 4);
            assertCss("tgroup colspec[colwidth=\"15*\"]", output, 1);
            assertCss("tgroup colspec[colwidth=\"28.3333*\"]", output, 2);
            assertCss("tgroup colspec[colwidth=\"28.3334*\"]", output, 1);
        }

        @Test
        @DisplayName("should compute column widths based on pagewidth when width is set on table in DocBook output")
        void shouldComputeColumnWidthsBasedOnPagewidthWhenWidthIsSetOnTableInDocBookOutput() throws Exception {
            var input = """
                    :pagewidth: 500

                    [width=50%]
                    |=======
                    |A |B |C |D

                    |a |b |c |d
                    |1 |2 |3 |4
                    |=======
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook5"));
            assertCss("tgroup[cols=\"4\"]", output, 1);
            assertCss("tgroup colspec", output, 4);
            assertCss("tgroup colspec[colwidth]", output, 4);
            assertCss("tgroup colspec[colwidth=\"62.5*\"]", output, 4);
        }

        @Test
        @DisplayName("explicit table width is used even when autowidth option is specified")
        void explicitTableWidthIsUsedEvenWhenAutowidthOptionIsSpecified() throws Exception {
            var input = """
                    [%autowidth,width=75%]
                    |=======
                    |A |B |C
                    |a |b |c
                    |1 |2 |3
                    |=======
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table[width]", output, 1);
            assertCss("table colgroup col", output, 3);
            assertCss("table colgroup col[style*=\"width\"]", output, 0);
        }

        @Test
        @DisplayName("first row sets number of columns when not specified")
        void firstRowSetsNumberOfColumnsWhenNotSpecified() throws Exception {
            var input = """
                    |===
                    |first |second |third |fourth
                    |1 |2 |3
                    |4
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 4);
            assertCss("table > tbody > tr", output, 2);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 4);
            assertCss("table > tbody > tr:nth-child(2) > td", output, 4);
        }

        @Test
        @DisplayName("colspec attribute using asterisk syntax sets number of columns")
        void colspecAttributeUsingAsteriskSyntaxSetsNumberOfColumns() throws Exception {
            var input = """
                    [cols="3*"]
                    |===
                    |A |B |C |a |b |c |1 |2 |3
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > tbody > tr", output, 3);
        }

        @Test
        @DisplayName("table with explicit column count can have multiple rows on a single line")
        void tableWithExplicitColumnCountCanHaveMultipleRowsOnASingleLine() throws Exception {
            var input = """
                    [cols="3*"]
                    |===
                    |one |two
                    |1 |2 |a |b
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 3);
            assertCss("table > tbody > tr", output, 2);
        }

        @Test
        @DisplayName("table with explicit deprecated colspec syntax can have multiple rows on a single line")
        void tableWithExplicitDeprecatedColspecSyntaxCanHaveMultipleRowsOnASingleLine() throws Exception {
            var input = """
                    [cols="3"]
                    |===
                    |one |two
                    |1 |2 |a |b
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 3);
            assertCss("table > tbody > tr", output, 2);
        }

        @Test
        @DisplayName("columns are added for empty records in colspec attribute")
        void columnsAreAddedForEmptyRecordsInColspecAttribute() throws Exception {
            var input = """
                    [cols="<,"]
                    |===
                    |one |two
                    |1 |2 |a |b
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > tbody > tr", output, 3);
        }

        @Test
        @DisplayName("cols may be separated by semi-colon instead of comma")
        void colsMayBeSeparatedBySemiColonInsteadOfComma() throws Exception {
            var input = """
                    [cols="1s;3m"]
                    |===
                    | strong
                    | mono
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("col[width=\"25%\"]", output, 1);
            assertCss("col[width=\"75%\"]", output, 1);
            assertXpath("(//td)[1]//strong", output, 1);
            assertXpath("(//td)[2]//code", output, 1);
        }

        @Test
        @DisplayName("cols attribute may include spaces")
        void colsAttributeMayIncludeSpaces() throws Exception {
            var input = """
                    [cols=" 1, 1 "]
                    |===
                    |one |two |1 |2 |a |b
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("col[width=\"50%\"]", output, 2);
            assertCss("table > tbody > tr", output, 3);
        }

        @Test
        @DisplayName("blank cols attribute should be ignored")
        void blankColsAttributeShouldBeIgnored() throws Exception {
            var input = """
                    [cols=" "]
                    |===
                    |one |two
                    |1 |2 |a |b
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("col[width=\"50%\"]", output, 2);
            assertCss("table > tbody > tr", output, 3);
        }

        @Test
        @DisplayName("empty cols attribute should be ignored")
        void emptyColsAttributeShouldBeIgnored() throws Exception {
            var input = """
                    [cols=""]
                    |===
                    |one |two
                    |1 |2 |a |b
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("col[width=\"50%\"]", output, 2);
            assertCss("table > tbody > tr", output, 3);
        }

        @Test
        @DisplayName("table with header and footer")
        void tableWithHeaderAndFooter() throws Exception {
            var input = """
                    [options="header,footer"]
                    |===
                    |Item       |Quantity
                    |Item 1     |1
                    |Item 2     |2
                    |Item 3     |3
                    |Total      |6
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > thead", output, 1);
            assertCss("table > thead > tr", output, 1);
            assertCss("table > thead > tr > th", output, 2);
            assertCss("table > tfoot", output, 1);
            assertCss("table > tfoot > tr", output, 1);
            assertCss("table > tfoot > tr > td", output, 2);
            assertCss("table > tbody", output, 1);
            assertCss("table > tbody > tr", output, 3);
            var tableSectionNames = select(map(xmlnodesAtCss("table > *", output), it -> send(it, "node_name")), n -> startsWith(n, "t"));
            assertEquals(list("thead", "tbody", "tfoot"), tableSectionNames);
        }

        @Test
        @DisplayName("table with header and footer docbook")
        void tableWithHeaderAndFooterDocbook() throws Exception {
            var input = """
                    .Table with header, body and footer
                    [options="header,footer"]
                    |===
                    |Item       |Quantity
                    |Item 1     |1
                    |Item 2     |2
                    |Item 3     |3
                    |Total      |6
                    |===
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertCss("table", output, 1);
            assertCss("table > title", output, 1);
            assertCss("table > tgroup", output, 1);
            assertCss("table > tgroup[cols=\"2\"]", output, 1);
            assertCss("table > tgroup[cols=\"2\"] > colspec", output, 2);
            assertCss("table > tgroup[cols=\"2\"] > colspec[colwidth=\"50*\"]", output, 2);
            assertCss("table > tgroup > thead", output, 1);
            assertCss("table > tgroup > thead > row", output, 1);
            assertCss("table > tgroup > thead > row > entry", output, 2);
            assertCss("table > tgroup > thead > row > entry > simpara", output, 0);
            assertCss("table > tgroup > tfoot", output, 1);
            assertCss("table > tgroup > tfoot > row", output, 1);
            assertCss("table > tgroup > tfoot > row > entry", output, 2);
            assertCss("table > tgroup > tfoot > row > entry > simpara", output, 2);
            assertCss("table > tgroup > tbody", output, 1);
            assertCss("table > tgroup > tbody > row", output, 3);
            assertCss("table > tgroup > tbody > row", output, 3);
            var tableSectionNames = select(map(xmlnodesAtCss("table > tgroup > *", output), it -> send(it, "node_name")), n -> startsWith(n, "t"));
            assertEquals(list("thead", "tbody", "tfoot"), tableSectionNames);
        }

        @Test
        @DisplayName("should set horizontal and vertical alignment when converting to DocBook")
        void shouldSetHorizontalAndVerticalAlignmentWhenConvertingToDocBook() throws Exception {
            var input = """
                    |===
                    |A ^.^|B >|C

                    |A1
                    ^.^|B1
                    >|C1
                    |===
                    """;
            var output = convertString(input, Options.of("backend", "docbook"));
            assertCss("informaltable", output, 1);
            assertCss("informaltable thead > row > entry[align=\"left\"][valign=\"top\"]", output, 1);
            assertCss("informaltable thead > row > entry[align=\"center\"][valign=\"middle\"]", output, 1);
            assertCss("informaltable thead > row > entry[align=\"right\"][valign=\"top\"]", output, 1);
            assertCss("informaltable tbody > row > entry[align=\"left\"][valign=\"top\"]", output, 1);
            assertCss("informaltable tbody > row > entry[align=\"center\"][valign=\"middle\"]", output, 1);
            assertCss("informaltable tbody > row > entry[align=\"right\"][valign=\"top\"]", output, 1);
        }

        @Test
        @DisplayName("should preserve frame value ends when converting to HTML")
        void shouldPreserveFrameValueEndsWhenConvertingToHTML() throws Exception {
            var input = """
                    [frame=ends]
                    |===
                    |A |B |C
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table.frame-ends", output, 1);
        }

        @Test
        @DisplayName("should normalize frame value topbot as ends when converting to HTML")
        void shouldNormalizeFrameValueTopbotAsEndsWhenConvertingToHTML() throws Exception {
            var input = """
                    [frame=topbot]
                    |===
                    |A |B |C
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table.frame-ends", output, 1);
        }

        @Test
        @DisplayName("should preserve frame value topbot when converting to DocBook")
        void shouldPreserveFrameValueTopbotWhenConvertingToDocBook() throws Exception {
            var input = """
                    [frame=topbot]
                    |===
                    |A |B |C
                    |===
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertCss("informaltable[frame=\"topbot\"]", output, 1);
        }

        @Test
        @DisplayName("should convert frame value ends to topbot when converting to DocBook")
        void shouldConvertFrameValueEndsToTopbotWhenConvertingToDocBook() throws Exception {
            var input = """
                    [frame=ends]
                    |===
                    |A |B |C
                    |===
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertCss("informaltable[frame=\"topbot\"]", output, 1);
        }

        @Test
        @DisplayName("table with landscape orientation in DocBook")
        void tableWithLandscapeOrientationInDocBook() throws Exception {
            for (var attrs : list("orientation=landscape", "%rotate")) {
                var input = ("[" + str(attrs) + "]\n|===\n|Column A | Column B | Column C\n|===\n");
                var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
                assertCss("informaltable", output, 1);
                assertCss("informaltable[orient=\"land\"]", output, 1);
            }
        }

        @Test
        @DisplayName("table with implicit header row")
        void tableWithImplicitHeaderRow() throws Exception {
            var input = """
                    |===
                    |Column 1 |Column 2

                    |Data A1
                    |Data B1

                    |Data A2
                    |Data B2
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > thead", output, 1);
            assertCss("table > thead > tr", output, 1);
            assertCss("table > thead > tr > th", output, 2);
            assertCss("table > tbody", output, 1);
            assertCss("table > tbody > tr", output, 2);
        }

        @Test
        @DisplayName("table with implicit header row only")
        void tableWithImplicitHeaderRowOnly() throws Exception {
            var input = """
                    |===
                    |Column 1 |Column 2

                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > thead", output, 1);
            assertCss("table > thead > tr", output, 1);
            assertCss("table > thead > tr > th", output, 2);
            assertCss("table > tbody", output, 0);
        }

        @Test
        @DisplayName("table with implicit header row when other options set")
        void tableWithImplicitHeaderRowWhenOtherOptionsSet() throws Exception {
            var input = """
                    [%autowidth]
                    |===
                    |Column 1 |Column 2

                    |Data A1
                    |Data B1
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table[style*=\"width\"]", output, 0);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > thead", output, 1);
            assertCss("table > thead > tr", output, 1);
            assertCss("table > thead > tr > th", output, 2);
            assertCss("table > tbody", output, 1);
            assertCss("table > tbody > tr", output, 1);
        }

        @Test
        @DisplayName("no implicit header row if second line not blank")
        void noImplicitHeaderRowIfSecondLineNotBlank() throws Exception {
            var input = """
                    |===
                    |Column 1 |Column 2
                    |Data A1
                    |Data B1

                    |Data A2
                    |Data B2
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > thead", output, 0);
            assertCss("table > tbody", output, 1);
            assertCss("table > tbody > tr", output, 3);
        }

        @Test
        @DisplayName("no implicit header row if cell in first line spans multiple lines")
        void noImplicitHeaderRowIfCellInFirstLineSpansMultipleLines() throws Exception {
            var input = """
                    [cols=2*]
                    |===
                    |A1


                    A1 continued|B1

                    |A2
                    |B2
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > thead", output, 0);
            assertCss("table > tbody", output, 1);
            assertCss("table > tbody > tr", output, 2);
            assertXpath("(//td)[1]/p", output, 2);
        }

        @Test
        @DisplayName("should format first cell as literal if there is no implicit header row and column has l style")
        void shouldFormatFirstCellAsLiteralIfThereIsNoImplicitHeaderRowAndColumnHasLStyle() throws Exception {
            var input = """
                    [cols="1l,1"]
                    |===
                    |literal
                    |normal
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("tbody pre", output, 1);
            assertCss("tbody p.tableblock", output, 1);
        }

        @Test
        @DisplayName("should format first cell as AsciiDoc if there is no implicit header row and column has a style")
        void shouldFormatFirstCellAsAsciiDocIfThereIsNoImplicitHeaderRowAndColumnHasAStyle() throws Exception {
            var input = """
                    [cols="1a,1"]
                    |===
                    | * list
                    | normal
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("tbody .ulist", output, 1);
            assertCss("tbody p.tableblock", output, 1);
        }

        @Test
        @DisplayName("should interpret leading indent if first cell is AsciiDoc and there is no implicit header row")
        void shouldInterpretLeadingIndentIfFirstCellIsAsciiDocAndThereIsNoImplicitHeaderRow() throws Exception {
            var input = """
                    [cols="1a,1"]
                    |===
                    |
                      literal
                    | normal
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("tbody pre", output, 1);
            assertCss("tbody p.tableblock", output, 1);
        }

        @Test
        @DisplayName("should format first cell as AsciiDoc if there is no implicit header row and cell has a style")
        void shouldFormatFirstCellAsAsciiDocIfThereIsNoImplicitHeaderRowAndCellHasAStyle() throws Exception {
            var input = """
                    |===
                    a| * list
                    | normal
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("tbody .ulist", output, 1);
            assertCss("tbody p.tableblock", output, 1);
        }

        @Test
        @DisplayName("no implicit header row if AsciiDoc cell in first line spans multiple lines")
        void noImplicitHeaderRowIfAsciiDocCellInFirstLineSpansMultipleLines() throws Exception {
            var input = """
                    [cols=2*]
                    |===
                    a|contains AsciiDoc content

                    * a
                    * b
                    * c
                    a|contains no AsciiDoc content

                    just text
                    |A2
                    |B2
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > thead", output, 0);
            assertCss("table > tbody", output, 1);
            assertCss("table > tbody > tr", output, 2);
            assertXpath("(//td)[1]//ul", output, 1);
        }

        @Test
        @DisplayName("no implicit header row if first line blank")
        void noImplicitHeaderRowIfFirstLineBlank() throws Exception {
            var input = """
                    |===

                    |Column 1 |Column 2

                    |Data A1
                    |Data B1

                    |Data A2
                    |Data B2

                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > thead", output, 0);
            assertCss("table > tbody", output, 1);
            assertCss("table > tbody > tr", output, 3);
        }

        @Test
        @DisplayName("no implicit header row if noheader option is specified")
        void noImplicitHeaderRowIfNoheaderOptionIsSpecified() throws Exception {
            var input = """
                    [%noheader]
                    |===
                    |Column 1 |Column 2

                    |Data A1
                    |Data B1

                    |Data A2
                    |Data B2
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > thead", output, 0);
            assertCss("table > tbody", output, 1);
            assertCss("table > tbody > tr", output, 3);
        }

        @Test
        @DisplayName("styles not applied to header cells")
        void stylesNotAppliedToHeaderCells() throws Exception {
            var input = """
                    [cols="1h,1s,1e",options="header,footer"]
                    |===
                    |Name |Occupation| Website
                    |Octocat |Social coding| https://github.com
                    |Name |Occupation| Website
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > thead > tr > th", output, 3);
            assertCss("table > thead > tr > th > *", output, 0);
            assertCss("table > tfoot > tr > th", output, 1);
            assertCss("table > tfoot > tr > td", output, 2);
            assertCss("table > tfoot > tr > td > p > strong", output, 1);
            assertCss("table > tfoot > tr > td > p > em", output, 1);
            assertCss("table > tbody > tr > th", output, 1);
            assertCss("table > tbody > tr > td", output, 2);
            assertCss("table > tbody > tr > td > p.header", output, 0);
            assertCss("table > tbody > tr > td > p > strong", output, 1);
            assertCss("table > tbody > tr > td > p > em > a", output, 1);
        }

        @Test
        @DisplayName("should apply text formatting to cells in implicit header row when column has a style")
        void shouldApplyTextFormattingToCellsInImplicitHeaderRowWhenColumnHasAStyle() throws Exception {
            var input = """
                    [cols="2*a"]
                    |===
                    | _foo_ | *bar*

                    | * list item
                    | paragraph
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("(//thead/tr/th)[1]/em[text()=\"foo\"]", output, 1);
            assertXpath("(//thead/tr/th)[2]/strong[text()=\"bar\"]", output, 1);
            assertCss("tbody .ulist", output, 1);
            assertCss("tbody .paragraph", output, 1);
        }

        @Test
        @DisplayName("should apply style and text formatting to cells in first row if no implicit header")
        void shouldApplyStyleAndTextFormattingToCellsInFirstRowIfNoImplicitHeader() throws Exception {
            var input = """
                    [cols="s,e"]
                    |===
                    | _strong_ | *emphasis*
                    | strong
                    | emphasis
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("((//tbody/tr)[1]/td)[1]//strong/em[text()=\"strong\"]", output, 1);
            assertXpath("((//tbody/tr)[1]/td)[2]//em/strong[text()=\"emphasis\"]", output, 1);
            assertXpath("((//tbody/tr)[2]/td)[1]//strong[text()=\"strong\"]", output, 1);
            assertXpath("((//tbody/tr)[2]/td)[2]//em[text()=\"emphasis\"]", output, 1);
        }

        @Test
        @DisplayName("vertical table headers use th element instead of header class")
        void verticalTableHeadersUseThElementInsteadOfHeaderClass() throws Exception {
            var input = """
                    [cols="1h,1s,1e"]
                    |===

                    |Name |Occupation| Website

                    |Octocat |Social coding| https://github.com

                    |Name |Occupation| Website

                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > tbody > tr > th", output, 3);
            assertCss("table > tbody > tr > td", output, 6);
            assertCss("table > tbody > tr .header", output, 0);
            assertCss("table > tbody > tr > td > p > strong", output, 3);
            assertCss("table > tbody > tr > td > p > em", output, 3);
            assertCss("table > tbody > tr > td > p > em > a", output, 1);
        }

        @Test
        @DisplayName("supports horizontal and vertical source data with blank lines and table header")
        void supportsHorizontalAndVerticalSourceDataWithBlankLinesAndTableHeader() throws Exception {
            var input = """
                    .Horizontal and vertical source data
                    [width="80%",cols="3,^2,^2,10",options="header"]
                    |===
                    |Date |Duration |Avg HR |Notes

                    |22-Aug-08 |10:24 | 157 |
                    Worked out MSHR (max sustainable heart rate) by going hard
                    for this interval.

                    |22-Aug-08 |23:03 | 152 |
                    Back-to-back with previous interval.

                    |24-Aug-08 |40:00 | 145 |
                    Moderately hard interspersed with 3x 3min intervals (2 min
                    hard + 1 min really hard taking the HR up to 160).

                    I am getting in shape!

                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table[width=\"80%\"]", output, 1);
            assertXpath("/table/caption[@class=\"title\"][text()=\"Table 1. Horizontal and vertical source data\"]", output, 1);
            assertCss("table > colgroup > col", output, 4);
            assertCss("table > colgroup > col:nth-child(1)[width=\"17.647%\"]", output, 1);
            assertCss("table > colgroup > col:nth-child(2)[width=\"11.7647%\"]", output, 1);
            assertCss("table > colgroup > col:nth-child(3)[width=\"11.7647%\"]", output, 1);
            assertCss("table > colgroup > col:nth-child(4)[width=\"58.8236%\"]", output, 1);
            assertCss("table > thead", output, 1);
            assertCss("table > thead > tr", output, 1);
            assertCss("table > thead > tr > th", output, 4);
            assertCss("table > tbody > tr", output, 3);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 4);
            assertCss("table > tbody > tr:nth-child(2) > td", output, 4);
            assertCss("table > tbody > tr:nth-child(3) > td", output, 4);
            assertXpath("/table/tbody/tr[1]/td[4]/p[text()='Worked out MSHR (max sustainable heart rate) by going hard\nfor this interval.']", output, 1);
            assertCss("table > tbody > tr:nth-child(3) > td:nth-child(4) > p", output, 2);
            assertXpath("/table/tbody/tr[3]/td[4]/p[2][text()=\"I am getting in shape!\"]", output, 1);
        }

        @Test
        @DisplayName("percentages as column widths")
        void percentagesAsColumnWidths() throws Exception {
            var input = """
                    [cols="<.^10%,<90%"]
                    |===
                    |column A |column B
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/table/colgroup/col", output, 2);
            assertXpath("(/table/colgroup/col)[1][@width=\"10%\"]", output, 1);
            assertXpath("(/table/colgroup/col)[2][@width=\"90%\"]", output, 1);
        }

        @Test
        @DisplayName("spans, alignments and styles")
        void spansAlignmentsAndStyles() throws Exception {
            var input = """
                    [cols="e,m,^,>s",width="25%"]
                    |===
                    |1 >s|2 |3 |4
                    ^|5 2.2+^.^|6 .3+<.>m|7
                    ^|8
                    d|9 2+>|10
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col[width=\"25%\"]", output, 4);
            assertCss("table > tbody > tr", output, 4);
            assertCss("table > tbody > tr > td", output, 10);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 4);
            assertCss("table > tbody > tr:nth-child(2) > td", output, 3);
            assertCss("table > tbody > tr:nth-child(3) > td", output, 1);
            assertCss("table > tbody > tr:nth-child(4) > td", output, 2);
            assertCss("table > tbody > tr:nth-child(1) > td:nth-child(1).halign-left.valign-top p em", output, 1);
            assertCss("table > tbody > tr:nth-child(1) > td:nth-child(2).halign-right.valign-top p strong", output, 1);
            assertCss("table > tbody > tr:nth-child(1) > td:nth-child(3).halign-center.valign-top p", output, 1);
            assertCss("table > tbody > tr:nth-child(1) > td:nth-child(3).halign-center.valign-top p *", output, 0);
            assertCss("table > tbody > tr:nth-child(1) > td:nth-child(4).halign-right.valign-top p strong", output, 1);
            assertCss("table > tbody > tr:nth-child(2) > td:nth-child(1).halign-center.valign-top p em", output, 1);
            assertCss("table > tbody > tr:nth-child(2) > td:nth-child(2).halign-center.valign-middle[colspan=\"2\"][rowspan=\"2\"] p code", output, 1);
            assertCss("table > tbody > tr:nth-child(2) > td:nth-child(3).halign-left.valign-bottom[rowspan=\"3\"] p code", output, 1);
            assertCss("table > tbody > tr:nth-child(3) > td:nth-child(1).halign-center.valign-top p em", output, 1);
            assertCss("table > tbody > tr:nth-child(4) > td:nth-child(1).halign-left.valign-top p", output, 1);
            assertCss("table > tbody > tr:nth-child(4) > td:nth-child(1).halign-left.valign-top p em", output, 0);
            assertCss("table > tbody > tr:nth-child(4) > td:nth-child(2).halign-right.valign-top[colspan=\"2\"] p code", output, 1);
        }

        @Test
        @DisplayName("sets up columns correctly if first row has cell that spans columns")
        void setsUpColumnsCorrectlyIfFirstRowHasCellThatSpansColumns() throws Exception {
            var input = """
                    |===
                    2+^|AAA |CCC
                    |AAA |BBB |CCC
                    |AAA |BBB |CCC
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 2);
            assertCss("table > tbody > tr:nth-child(1) > td:nth-child(1)[colspan=\"2\"]", output, 1);
            assertCss("table > tbody > tr:nth-child(1) > td:nth-child(2):not([colspan])", output, 1);
            assertCss("table > tbody > tr:nth-child(2) > td:not([colspan])", output, 3);
            assertCss("table > tbody > tr:nth-child(3) > td:not([colspan])", output, 3);
        }

        @Test
        @DisplayName("supports repeating cells")
        void supportsRepeatingCells() throws Exception {
            var input = """
                    |===
                    3*|A
                    |1 3*|2
                    |b |c
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 3);
            assertCss("table > tbody > tr", output, 3);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 3);
            assertCss("table > tbody > tr:nth-child(2) > td", output, 3);
            assertCss("table > tbody > tr:nth-child(3) > td", output, 3);
            assertXpath("/table/tbody/tr[1]/td[1]/p[text()=\"A\"]", output, 1);
            assertXpath("/table/tbody/tr[1]/td[2]/p[text()=\"A\"]", output, 1);
            assertXpath("/table/tbody/tr[1]/td[3]/p[text()=\"A\"]", output, 1);
            assertXpath("/table/tbody/tr[2]/td[1]/p[text()=\"1\"]", output, 1);
            assertXpath("/table/tbody/tr[2]/td[2]/p[text()=\"2\"]", output, 1);
            assertXpath("/table/tbody/tr[2]/td[3]/p[text()=\"2\"]", output, 1);
            assertXpath("/table/tbody/tr[3]/td[1]/p[text()=\"2\"]", output, 1);
            assertXpath("/table/tbody/tr[3]/td[2]/p[text()=\"b\"]", output, 1);
            assertXpath("/table/tbody/tr[3]/td[3]/p[text()=\"c\"]", output, 1);
        }

        @Test
        @DisplayName("calculates colnames correctly when using implicit column count and single cell with colspan")
        void calculatesColnamesCorrectlyWhenUsingImplicitColumnCountAndSingleCellWithColspan() throws Exception {
            var input = """
                    |===
                    2+|Two Columns
                    |One Column |One Column
                    |===
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertXpath("//colspec", output, 2);
            assertXpath("(//colspec)[1][@colname=\"col_1\"]", output, 1);
            assertXpath("(//colspec)[2][@colname=\"col_2\"]", output, 1);
            assertXpath("//row", output, 2);
            assertXpath("(//row)[1]/entry", output, 1);
            assertXpath("(//row)[1]/entry[@namest=\"col_1\"][@nameend=\"col_2\"]", output, 1);
        }

        @Test
        @DisplayName("calculates colnames correctly when using implicit column count and cells with mixed colspans")
        void calculatesColnamesCorrectlyWhenUsingImplicitColumnCountAndCellsWithMixedColspans() throws Exception {
            var input = """
                    |===
                    2+|Two Columns | One Column
                    |One Column |One Column |One Column
                    |===
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertXpath("//colspec", output, 3);
            assertXpath("(//colspec)[1][@colname=\"col_1\"]", output, 1);
            assertXpath("(//colspec)[2][@colname=\"col_2\"]", output, 1);
            assertXpath("(//colspec)[3][@colname=\"col_3\"]", output, 1);
            assertXpath("//row", output, 2);
            assertXpath("(//row)[1]/entry", output, 2);
            assertXpath("(//row)[1]/entry[@namest=\"col_1\"][@nameend=\"col_2\"]", output, 1);
            assertXpath("(//row)[2]/entry[@namest]", output, 0);
            assertXpath("(//row)[2]/entry[@nameend]", output, 0);
        }

        @Test
        @DisplayName("assigns unique column names for table with implicit column count and colspans in first row")
        void assignsUniqueColumnNamesForTableWithImplicitColumnCountAndColspansInFirstRow() throws Exception {
            var input = """
                    |===
                    |                 2+| Node 0          2+| Node 1

                    | Host processes    | Core 0 | Core 1   | Core 4 | Core 5
                    | Guest processes   | Core 2 | Core 3   | Core 6 | Core 7
                    |===
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            assertXpath("//colspec", output, 5);
            for (var n : range(1, 5)) {
                assertXpath(("(//colspec)[" + str(n) + "][@colname=\"col_" + str(n) + "\"]"), output, 1);
            }
            assertXpath("(//row)[1]/entry", output, 3);
            assertXpath("((//row)[1]/entry)[1][@namest]", output, 0);
            assertXpath("((//row)[1]/entry)[1][@namend]", output, 0);
            assertXpath("((//row)[1]/entry)[2][@namest=\"col_2\"][@nameend=\"col_3\"]", output, 1);
            assertXpath("((//row)[1]/entry)[3][@namest=\"col_4\"][@nameend=\"col_5\"]", output, 1);
        }

        @Test
        @DisplayName("should drop row but preserve remaining rows after cell with colspan exceeds number of columns")
        void shouldDropRowButPreserveRemainingRowsAfterCellWithColspanExceedsNumberOfColumns() throws Exception {
            var input = """
                    [cols=2*]
                    |===
                    3+|A
                    |B
                    a|C

                    more C
                    |===
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertCss("table", output, 1);
                assertCss("table tr", output, 1);
                assertXpath("/table/tbody/tr/td[1]/p[text()=\"B\"]", output, 1);
                assertMessage(logger, "ERROR", "<stdin>: line 3: dropping cell because it exceeds specified number of columns", LogMessage.class);
            }
        }

        @Test
        @DisplayName("should drop last row if last cell in table has colspan that exceeds specified number of columns")
        void shouldDropLastRowIfLastCellInTableHasColspanThatExceedsSpecifiedNumberOfColumns() throws Exception {
            var input = """
                    [cols=2*]
                    |===
                    |a 2+|b
                    |===
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertCss("table", output, 1);
                assertCss("table *", output, 0);
                assertMessage(logger, "ERROR", "<stdin>: line 3: dropping cell because it exceeds specified number of columns", LogMessage.class);
            }
        }

        @Test
        @DisplayName("should drop last row if last cell in table has colspan that exceeds implicit number of columns")
        void shouldDropLastRowIfLastCellInTableHasColspanThatExceedsImplicitNumberOfColumns() throws Exception {
            var input = """
                    |===
                    |a |b
                    |c 2+|d
                    |===
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertCss("table", output, 1);
                assertCss("table tr", output, 1);
                assertXpath("/table/tbody/tr/td[1]/p[text()=\"a\"]", output, 1);
                assertMessage(logger, "ERROR", "<stdin>: line 3: dropping cell because it exceeds specified number of columns", LogMessage.class);
            }
        }

        @Test
        @DisplayName("should take colspan into account when taking cells for row")
        void shouldTakeColspanIntoAccountWhenTakingCellsForRow() throws Exception {
            var input = """
                    [cols=7]
                    |===
                    2+|a 2+|b 2+|c 2+|d
                    |e |f |g |h |i |j |k
                    |===
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertCss("table", output, 1);
                assertCss("table tr", output, 1);
                assertCss("table tr td", output, 7);
                assertMessage(logger, "ERROR", "<stdin>: line 3: dropping cell because it exceeds specified number of columns", LogMessage.class);
            }
        }

        @Test
        @DisplayName("should drop incomplete row at end of table and log an error")
        void shouldDropIncompleteRowAtEndOfTableAndLogAnError() throws Exception {
            var input = """
                    [cols=2*]
                    |===
                    |a |b
                    |c |d
                    |e
                    |===
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertCss("table", output, 1);
                assertCss("table tr", output, 2);
                assertMessage(logger, "ERROR", "<stdin>: line 5: dropping cells from incomplete row detected end of table", LogMessage.class);
            }
        }

        @Test
        @DisplayName("should apply cell style for column to repeated content")
        void shouldApplyCellStyleForColumnToRepeatedContent() throws Exception {
            var input = """
                    [cols=",^l"]
                    |===
                    |Paragraphs |Literal

                    2*|The discussion about what is good,
                    what is beautiful, what is noble,
                    what is pure, and what is true
                    could always go on.

                    Why is that important?
                    Why would I like to do that?

                    Because that's the only conversation worth having.

                    And whether it goes on or not after I die, I don't know.
                    But, I do know that it is the conversation I want to have while I am still alive.

                    Which means that to me the offer of certainty,
                    the offer of complete security,
                    the offer of an impermeable faith that can't give way
                    is an offer of something not worth having.

                    I want to live my life taking the risk all the time
                    that I don't know anything like enough yet...
                    that I haven't understood enough...
                    that I can't know enough...
                    that I am always hungrily operating on the margins
                    of a potentially great harvest of future knowledge and wisdom.

                    I wouldn't have it any other way.
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > thead", output, 1);
            assertCss("table > thead > tr", output, 1);
            assertCss("table > thead > tr > th", output, 2);
            assertCss("table > tbody", output, 1);
            assertCss("table > tbody > tr", output, 1);
            assertCss("table > tbody > tr > td", output, 2);
            assertCss("table > tbody > tr > td:nth-child(1).halign-left.valign-top > p.tableblock", output, 7);
            assertCss("table > tbody > tr > td:nth-child(2).halign-center.valign-top > div.literal > pre", output, 1);
            var literal = xmlnodesAtCss("table > tbody > tr > td:nth-child(2).halign-center.valign-top > div.literal > pre", output, 1);
            assertEquals(26, size(lines(literal.getText())));
        }

        @Test
        @DisplayName("should not split paragraph at line containing only {blank} that is directly adjacent to non-blank lines")
        void shouldNotSplitParagraphAtLineContainingOnlyBlankThatIsDirectlyAdjacentToNonBlankLines() throws Exception {
            var input = """
                    |===
                    |paragraph
                    {blank}
                    still one paragraph
                    {blank}
                    still one paragraph
                    |===
                    """;
            var result = convertStringToEmbedded(input);
            assertCss("p.tableblock", result, 1);
        }

        @Test
        @DisplayName("should strip trailing newlines when splitting paragraphs")
        void shouldStripTrailingNewlinesWhenSplittingParagraphs() throws Exception {
            var input = """
                    |===
                    |first wrapped
                    paragraph

                    second paragraph

                    third paragraph
                    |===
                    """;
            var result = convertStringToEmbedded(input);
            assertXpath("(//p[@class=\"tableblock\"])[1][text()=\"first wrapped\nparagraph\"]", result, 1);
            assertXpath("(//p[@class=\"tableblock\"])[2][text()=\"second paragraph\"]", result, 1);
            assertXpath("(//p[@class=\"tableblock\"])[3][text()=\"third paragraph\"]", result, 1);
        }

        @Test
        @DisplayName("basic AsciiDoc cell")
        void basicAsciiDocCell() throws Exception {
            var input = """
                    |===
                    a|--
                    NOTE: content

                    content
                    --
                    |===
                    """;
            var result = convertStringToEmbedded(input);
            assertCss("table.tableblock", result, 1);
            assertCss("table.tableblock td.tableblock", result, 1);
            assertCss("table.tableblock td.tableblock .openblock", result, 1);
            assertCss("table.tableblock td.tableblock .openblock .admonitionblock", result, 1);
            assertCss("table.tableblock td.tableblock .openblock .paragraph", result, 1);
        }

        @Test
        @DisplayName("AsciiDoc table cell should be wrapped in div with class \"content\"")
        void asciidocTableCellShouldBeWrappedInDivWithClassContent() throws Exception {
            var input = """
                    |===
                    a|AsciiDoc table cell
                    |===
                    """;
            var result = convertStringToEmbedded(input);
            assertCss("table.tableblock td.tableblock > div.content", result, 1);
            assertCss("table.tableblock td.tableblock > div.content > div.paragraph", result, 1);
        }

        @Test
        @DisplayName("doctype can be set in AsciiDoc table cell")
        void doctypeCanBeSetInAsciiDocTableCell() throws Exception {
            var input = """
                    |===
                    a|
                    :doctype: inline

                    content
                    |===
                    """;
            var result = convertStringToEmbedded(input);
            assertCss("table.tableblock", result, 1);
            assertCss("table.tableblock .paragraph", result, 0);
        }

        @Test
        @DisplayName("should reset doctype to default in AsciiDoc table cell")
        void shouldResetDoctypeToDefaultInAsciiDocTableCell() throws Exception {
            var input = """
                    = Book Title
                    :doctype: book

                    == Chapter 1

                    |===
                    a|
                    = AsciiDoc Table Cell

                    doctype={doctype}
                    {backend-html5-doctype-article}
                    {backend-html5-doctype-book}
                    |===
                    """;
            var result = convertStringToEmbedded(input, Options.of("attributes", Options.of("attribute-missing", "skip")));
            assertIncludes(result, "doctype=article");
            refuteIncludes(result, "{backend-html5-doctype-article}");
            assertIncludes(result, "{backend-html5-doctype-book}");
        }

        @Test
        @DisplayName("should update doctype-related attributes in AsciiDoc table cell when doctype is set")
        void shouldUpdateDoctypeRelatedAttributesInAsciiDocTableCellWhenDoctypeIsSet() throws Exception {
            var input = """
                    = Document Title
                    :doctype: article

                    == Chapter 1

                    |===
                    a|
                    = AsciiDoc Table Cell
                    :doctype: book

                    doctype={doctype}
                    {backend-html5-doctype-book}
                    {backend-html5-doctype-article}
                    |===
                    """;
            var result = convertStringToEmbedded(input, Options.of("attributes", Options.of("attribute-missing", "skip")));
            assertIncludes(result, "doctype=book");
            refuteIncludes(result, "{backend-html5-doctype-book}");
            assertIncludes(result, "{backend-html5-doctype-article}");
        }

        @Test
        @DisplayName("should not allow AsciiDoc table cell to set a document attribute that was hard set by the API")
        void shouldNotAllowAsciiDocTableCellToSetADocumentAttributeThatWasHardSetByTheAPI() throws Exception {
            var input = """
                    |===
                    a|
                    :icons:

                    NOTE: This admonition does not have a font-based icon.
                    |===
                    """;
            var result = convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("icons", "font")));
            assertCss("td.icon .title", result, 0);
            assertCss("td.icon i.icon-note", result, 1);
        }

        @Test
        @DisplayName("should not allow AsciiDoc table cell to set a document attribute that was hard unset by the API")
        void shouldNotAllowAsciiDocTableCellToSetADocumentAttributeThatWasHardUnsetByTheAPI() throws Exception {
            var input = """
                    |===
                    a|
                    :icons: font

                    NOTE: This admonition does not have a font-based icon.
                    |===
                    """;
            var result = convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("icons", null)));
            assertCss("td.icon .title", result, 1);
            assertCss("td.icon i.icon-note", result, 0);
            assertXpath("//td[@class=\"icon\"]/*[@class=\"title\"][text()=\"Note\"]", result, 1);
        }

        @Test
        @DisplayName("should keep attribute unset in AsciiDoc table cell if unset in parent document")
        void shouldKeepAttributeUnsetInAsciiDocTableCellIfUnsetInParentDocument() throws Exception {
            var input = """
                    :!sectids:
                    :!table-caption:

                    == Outer Heading

                    .Outer Table
                    |===
                    a|

                    == Inner Heading

                    .Inner Table
                    !===
                    ! table cell
                    !===
                    |===
                    """;
            var result = convertStringToEmbedded(input);
            assertXpath("h2[id]", result, 0);
            assertXpath("//caption[text()=\"Outer Table\"]", result, 1);
            assertXpath("//caption[text()=\"Inner Table\"]", result, 1);
        }

        @Test
        @DisplayName("should allow attribute unset in parent document to be set in AsciiDoc table cell")
        void shouldAllowAttributeUnsetInParentDocumentToBeSetInAsciiDocTableCell() throws Exception {
            var input = """
                    :!sectids:

                    == No ID

                    |===
                    a|

                    == No ID

                    :sectids:

                    == Has ID
                    |===
                    """;
            var result = convertStringToEmbedded(input);
            var headings = xmlnodesAtCss("h2", result);
            assertEquals(3, size(headings));
            assertNull(headings.get(0).getAttribute("id"));
            assertNull(headings.get(1).getAttribute("id"));
            assertEquals("_has_id", headings.get(2).getAttribute("id"));
        }

        @Test
        @DisplayName("should not allow locked attribute unset in parent document to be set in AsciiDoc table cell")
        void shouldNotAllowLockedAttributeUnsetInParentDocumentToBeSetInAsciiDocTableCell() throws Exception {
            var input = """
                    == No ID

                    |===
                    a|

                    == No ID

                    :sectids:

                    == Has ID
                    |===
                    """;
            var result = convertStringToEmbedded(input, Options.of("attributes", Options.of("sectids", null)));
            var headings = xmlnodesAtCss("h2", result);
            assertEquals(3, size(headings));
            for (var heading : headings) {
                assertNull(heading.getAttribute("id"));
            }
        }

        @Test
        @DisplayName("showtitle can be enabled in AsciiDoc table cell if unset in parent document")
        void showtitleCanBeEnabledInAsciiDocTableCellIfUnsetInParentDocument() throws Exception {
            for (var name : list("showtitle", "notitle")) {
                var input = ("= Document Title\n:" + str((Objects.equals(name, "showtitle") ? "!" : "")) + str(name) + ":\n\n|===\na|\n= Nested Document Title\n:" + str((Objects.equals(name, "showtitle") ? "" : "!")) + str(name) + ":\n\ncontent\n|===\n");
                var result = convertStringToEmbedded(input);
                assertCss("h1", result, 1);
                assertCss(".tableblock h1", result, 1);
            }
        }

        @Test
        @DisplayName("showtitle can be enabled in AsciiDoc table cell if unset by API")
        void showtitleCanBeEnabledInAsciiDocTableCellIfUnsetByAPI() throws Exception {
            for (var name : list("showtitle", "notitle")) {
                var input = ("= Document Title\n\n|===\na|\n= Nested Document Title\n:" + str((Objects.equals(name, "showtitle") ? "" : "!")) + str(name) + ":\n\ncontent\n|===\n");
                var result = convertStringToEmbedded(input, Options.of("attributes", Options.of(name, (Objects.equals(name, "showtitle") ? null : ""))));
                assertCss("h1", result, 1);
                assertCss(".tableblock h1", result, 1);
            }
        }

        @Test
        @DisplayName("showtitle can be disabled in AsciiDoc table cell if set in parent document")
        void showtitleCanBeDisabledInAsciiDocTableCellIfSetInParentDocument() throws Exception {
            for (var name : list("showtitle", "notitle")) {
                var input = ("= Document Title\n:" + str((Objects.equals(name, "showtitle") ? "" : "!")) + str(name) + ":\n\n|===\na|\n= Nested Document Title\n:" + str((Objects.equals(name, "showtitle") ? "!" : "")) + str(name) + ":\n\ncontent\n|===\n");
                var result = convertStringToEmbedded(input);
                assertCss("h1", result, 1);
                assertCss(".tableblock h1", result, 0);
            }
        }

        @Test
        @DisplayName("showtitle can be disabled in AsciiDoc table cell if set by API")
        void showtitleCanBeDisabledInAsciiDocTableCellIfSetByAPI() throws Exception {
            for (var name : list("showtitle", "notitle")) {
                var input = ("= Document Title\n\n|===\na|\n= Nested Document Title\n:" + str((Objects.equals(name, "showtitle") ? "!" : "")) + str(name) + ":\n\ncontent\n|===\n");
                var result = convertStringToEmbedded(input, Options.of("attributes", Options.of(name, (Objects.equals(name, "showtitle") ? "" : null))));
                assertCss("h1", result, 1);
                assertCss(".tableblock h1", result, 0);
            }
        }

        @Test
        @DisplayName("AsciiDoc content")
        void asciidocContent() throws Exception {
            var input = """
                    [cols="1e,1,5a"]
                    |===
                    |Name |Backends |Description

                    |badges |xhtml11, html5 |
                    Link badges ('XHTML 1.1' and 'CSS') in document footers.

                    [NOTE]
                    ====
                    The path names of images, icons and scripts are relative path
                    names to the output document not the source document.
                    ====
                    |[[X97]] docinfo, docinfo1, docinfo2 |All backends |
                    These three attributes control which document information
                    files will be included in the the header of the output file:

                    docinfo:: Include `<filename>-docinfo.<ext>`
                    docinfo1:: Include `docinfo.<ext>`
                    docinfo2:: Include `docinfo.<ext>` and `<filename>-docinfo.<ext>`

                    Where `<filename>` is the file name (sans extension) of the AsciiDoc
                    input file and `<ext>` is `.html` for HTML outputs or `.xml` for
                    DocBook outputs. If the input file is the standard input then the
                    output file name is used.
                    |===
                    """;
            var doc = documentFromString(input, Options.of("sourcemap", true));
            var table = first(doc.getBlocks());
            assertNotNull(table);
            var tbody = ((Table) table).getRows().getBody();
            assertEquals(2, size(tbody));
            var bodyCell12 = tbody.get(0).get(1);
            assertEquals(5, bodyCell12.getLineNumber());
            var bodyCell13 = tbody.get(0).get(2);
            assertNotNull(bodyCell13.getInnerDocument());
            assertTruthy(bodyCell13.getInnerDocument().isNested());
            assertEquals(doc, bodyCell13.getInnerDocument().getParentDocument());
            assertEquals(doc.getConverter(), bodyCell13.getInnerDocument().getConverter());
            assertEquals(5, bodyCell13.getLineNumber());
            assertEquals(6, bodyCell13.getInnerDocument().getLineNumber());
            var note = bodyCell13.getInnerDocument().findBy(AbstractBlock.Selector.any().withContext("admonition")).get(0);
            assertEquals(9, note.getLineNumber());
            var output = doc.convert(Options.of("standalone", false));
            assertCss("table.tableblock > tbody > tr", output, 2);
            assertCss("table.tableblock > tbody > tr:nth-child(1) > td:nth-child(3) div.admonitionblock", output, 1);
            assertCss("table.tableblock > tbody > tr:nth-child(2) > td:nth-child(3) div.dlist", output, 1);
        }

        @Test
        @DisplayName("should preserve leading indentation in contents of AsciiDoc table cell if contents starts with newline")
        void shouldPreserveLeadingIndentationInContentsOfAsciiDocTableCellIfContentsStartsWithNewline() throws Exception {
            var input = """
                    |===
                    a|
                     $ command
                    a| paragraph
                    |===
                    """;
            var doc = documentFromString(input, Options.of("sourcemap", true));
            var table = doc.getBlocks().get(0);
            var tbody = ((Table) table).getRows().getBody();
            assertEquals(1, table.getLineNumber());
            assertEquals(2, tbody.get(0).get(0).getLineNumber());
            assertEquals(3, tbody.get(0).get(0).getInnerDocument().getLineNumber());
            assertEquals(4, tbody.get(1).get(0).getLineNumber());
            var output = doc.convert(Options.of("standalone", false));
            assertCss("td", output, 2);
            assertXpath("(//td)[1]//*[@class=\"literalblock\"]", output, 1);
            assertXpath("(//td)[2]//*[@class=\"paragraph\"]", output, 1);
            assertXpath("(//pre)[1][text()=\"$ command\"]", output, 1);
            assertXpath("(//p)[1][text()=\"paragraph\"]", output, 1);
        }

        @Test
        @DisplayName("preprocessor directive on first line of an AsciiDoc table cell should be processed")
        void preprocessorDirectiveOnFirstLineOfAnAsciiDocTableCellShouldBeProcessed() throws Exception {
            var input = """
                    |===
                    a|include::fixtures/include-file.adoc[]
                    |===
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", testdir()));
            assertMatch(rx("included content"), output);
        }

        @Test
        @DisplayName("error about unresolved preprocessor directive on first line of an AsciiDoc table cell should have correct cursor")
        void errorAboutUnresolvedPreprocessorDirectiveOnFirstLineOfAnAsciiDocTableCellShouldHaveCorrectCursor() throws Exception {
            var tmpInclude = Files.createTempFile("include-", ".adoc");
            try {
                var tmpIncludeDir = tmpInclude.getParent().toString();
                var tmpIncludePath = tmpInclude.getFileName().toString();
                Files.writeString(tmpInclude, """
                        |===
                        |A |B

                        |text
                        a|include::does-not-exist.adoc[]
                        |===
                        """);
                var input = "first\n\ninclude::" + tmpIncludePath + "[]\n\nlast\n";
                try (var logger = usingMemoryLogger()) {
                    var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", tmpIncludeDir));
                    assertIncludes(output, "Unresolved directive in " + tmpIncludePath);
                    assertMessage(logger, "ERROR", tmpIncludePath + ": line 5: include file not found: " + tmpInclude.resolveSibling("does-not-exist.adoc").toString().replace('\\', '/'), LogMessage.class);
                }
            } finally {
                Files.deleteIfExists(tmpInclude);
            }
        }

        @Test
        @DisplayName("cross reference link in an AsciiDoc table cell should resolve to reference in main document")
        void crossReferenceLinkInAnAsciiDocTableCellShouldResolveToReferenceInMainDocument() throws Exception {
            var input = """
                    == Some

                    |===
                    a|See <<_more>>
                    |===

                    == More

                    content
                    """;
            var result = convertString(input);
            assertXpath("//a[@href=\"#_more\"]", result, 1);
            assertXpath("//a[@href=\"#_more\"][text()=\"More\"]", result, 1);
        }

        @Test
        @DisplayName("should discover anchor at start of cell and register it as a reference")
        void shouldDiscoverAnchorAtStartOfCellAndRegisterItAsAReference() throws Exception {
            var input = """
                    The highest peak in the Front Range is <<grays-peak>>, which tops <<mount-evans>> by just a few feet.

                    [cols="1s,1"]
                    |===
                    |[[mount-evans,Mount Evans]]Mount Evans
                    |14,271 feet

                    h|[[grays-peak,Grays Peak]]
                    Grays Peak
                    |14,278 feet
                    |===
                    """;
            var doc = documentFromString(input);
            var refs = doc.getCatalog().getRefs();
            assertTruthy(refs.containsKey("mount-evans"));
            assertTruthy(refs.containsKey("grays-peak"));
            var output = doc.convert(Options.of("standalone", false));
            assertXpath("(//p)[1]/a[@href=\"#grays-peak\"][text()=\"Grays Peak\"]", output, 1);
            assertXpath("(//p)[1]/a[@href=\"#mount-evans\"][text()=\"Mount Evans\"]", output, 1);
            assertXpath("(//table/tbody/tr)[1]//td//a[@id=\"mount-evans\"]", output, 1);
            assertXpath("(//table/tbody/tr)[2]//th//a[@id=\"grays-peak\"]", output, 1);
        }

        @Test
        @DisplayName("should catalog anchor at start of cell in implicit header row when column has a style")
        void shouldCatalogAnchorAtStartOfCellInImplicitHeaderRowWhenColumnHasAStyle() throws Exception {
            var input = """
                    [cols=1a]
                    |===
                    |[[foo,Foo]]* not AsciiDoc

                    | AsciiDoc
                    |===
                    """;
            var doc = documentFromString(input);
            var refs = doc.getCatalog().getRefs();
            assertTruthy(refs.containsKey("foo"));
        }

        @Test
        @DisplayName("should catalog anchor at start of cell in explicit header row when column has a style")
        void shouldCatalogAnchorAtStartOfCellInExplicitHeaderRowWhenColumnHasAStyle() throws Exception {
            var input = """
                    [%header,cols=1a]
                    |===
                    |[[foo,Foo]]* not AsciiDoc
                    | AsciiDoc
                    |===
                    """;
            var doc = documentFromString(input);
            var refs = doc.getCatalog().getRefs();
            assertTruthy(refs.containsKey("foo"));
        }

        @Test
        @DisplayName("should catalog anchor at start of cell in first row")
        void shouldCatalogAnchorAtStartOfCellInFirstRow() throws Exception {
            var input = """
                    |===
                    |[[foo,Foo]]foo
                    | bar
                    |===
                    """;
            var doc = documentFromString(input);
            var refs = doc.getCatalog().getRefs();
            assertTruthy(refs.containsKey("foo"));
        }

        @Test
        @DisplayName("footnotes should not be shared between an AsciiDoc table cell and the main document")
        void footnotesShouldNotBeSharedBetweenAnAsciiDocTableCellAndTheMainDocument() throws Exception {
            var input = """
                    |===
                    a|AsciiDoc footnote:[A lightweight markup language.]
                    |===
                    """;
            var result = convertString(input);
            assertCss("#_footnotedef_1", result, 1);
        }

        @Test
        @DisplayName("callout numbers should be globally unique, including AsciiDoc table cells")
        void calloutNumbersShouldBeGloballyUniqueIncludingAsciiDocTableCells() throws Exception {
            var input = """
                    = Document Title

                    == Section 1

                    |===
                    a|
                    [source, yaml]
                    ----
                    key: value <1>
                    ----
                    <1> First callout
                    |===

                    == Section 2

                    |===
                    a|
                    [source, yaml]
                    ----
                    key: value <1>
                    ----
                    <1> Second callout
                    |===

                    == Section 3

                    [source, yaml]
                    ----
                    key: value <1>
                    ----
                    <1> Third callout
                    """;
            var result = convertStringToEmbedded(input, Options.of("backend", "docbook"));
            var conums = xmlnodesAtXpath("//co", result);
            assertEquals(3, size(conums));
            var conumList = list("CO1-1", "CO2-1", "CO3-1");
            for (int idx = 0; idx < conumList.size(); idx++) {
                var conum = conumList.get(idx);
                assertEquals(conum, get(conums, idx).attribute("xml:id").getValue());
            }
            var callouts = xmlnodesAtXpath("//callout", result);
            assertEquals(3, size(callouts));
            var calloutList = list("CO1-1", "CO2-1", "CO3-1");
            for (int idx = 0; idx < calloutList.size(); idx++) {
                var callout = calloutList.get(idx);
                assertEquals(callout, get(callouts, idx).attribute("arearefs").getValue());
            }
        }

        @Test
        @DisplayName("compat mode can be activated in AsciiDoc table cell")
        void compatModeCanBeActivatedInAsciiDocTableCell() throws Exception {
            var input = """
                    |===
                    a|
                    :compat-mode:

                    The word 'italic' is emphasized.
                    |===
                    """;
            var result = convertStringToEmbedded(input);
            assertXpath("//em[text()=\"italic\"]", result, 1);
        }

        @Test
        @DisplayName("compat mode in AsciiDoc table cell inherits from parent document")
        void compatModeInAsciiDocTableCellInheritsFromParentDocument() throws Exception {
            var input = """
                    :compat-mode:

                    The word 'italic' is emphasized.

                    [cols=1*]
                    |===
                    |The word 'oblique' is emphasized.
                    a|
                    The word 'slanted' is emphasized.
                    |===

                    The word 'askew' is emphasized.
                    """;
            var result = convertStringToEmbedded(input);
            assertXpath("//em[text()=\"italic\"]", result, 1);
            assertXpath("//em[text()=\"oblique\"]", result, 1);
            assertXpath("//em[text()=\"slanted\"]", result, 1);
            assertXpath("//em[text()=\"askew\"]", result, 1);
        }

        @Test
        @DisplayName("compat mode in AsciiDoc table cell can be unset if set in parent document")
        void compatModeInAsciiDocTableCellCanBeUnsetIfSetInParentDocument() throws Exception {
            var input = """
                    :compat-mode:

                    The word 'italic' is emphasized.

                    [cols=1*]
                    |===
                    |The word 'oblique' is emphasized.
                    a|
                    :!compat-mode:

                    The word 'slanted' is not emphasized.
                    |===

                    The word 'askew' is emphasized.
                    """;
            var result = convertStringToEmbedded(input);
            assertXpath("//em[text()=\"italic\"]", result, 1);
            assertXpath("//em[text()=\"oblique\"]", result, 1);
            assertXpath("//em[text()=\"slanted\"]", result, 0);
            assertXpath("//em[text()=\"askew\"]", result, 1);
        }

        @Test
        @DisplayName("nested table")
        void nestedTable() throws Exception {
            var input = """
                    [cols="1,2a"]
                    |===
                    |Normal cell
                    |Cell with nested table
                    [cols="2,1"]
                    !===
                    !Nested table cell 1 !Nested table cell 2
                    !===
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 2);
            assertCss("table table", output, 1);
            assertCss("table > tbody > tr > td:nth-child(2) table", output, 1);
            assertCss("table > tbody > tr > td:nth-child(2) table > tbody > tr > td", output, 2);
        }

        @Test
        @DisplayName("can set format of nested table to psv")
        void canSetFormatOfNestedTableToPsv() throws Exception {
            var input = """
                    [cols="2*"]
                    |===
                    |normal cell
                    a|
                    [format=psv]
                    !===
                    !nested cell
                    !===
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 2);
            assertCss("table table", output, 1);
            assertCss("table > tbody > tr > td:nth-child(2) table", output, 1);
            assertCss("table > tbody > tr > td:nth-child(2) table > tbody > tr > td", output, 1);
        }

        @Test
        @DisplayName("AsciiDoc table cell should inherit to_dir option from parent document")
        void asciidocTableCellShouldInheritTo_dirOptionFromParentDocument() throws Exception {
            var doc = documentFromString("""
                    |===
                    a|
                    AsciiDoc table cell
                    |===
                    """, Options.of("parse", true, "to_dir", testdir()));
            var nestedDoc = (Document) doc.getBlocks().get(0).findBy(AbstractBlock.Selector.any().withContext("document").withTraverseDocuments(true)).get(0);
            assertTruthy(nestedDoc.isNested());
            assertEquals(get(doc.getOptions(), "to_dir"), get(nestedDoc.getOptions(), "to_dir"));
        }

        @Test
        @DisplayName("AsciiDoc table cell should not inherit toc setting from parent document")
        void asciidocTableCellShouldNotInheritTocSettingFromParentDocument() throws Exception {
            var input = """
                    = Document Title
                    :toc:

                    == Section

                    |===
                    a|
                    == Section in Nested Document

                    content
                    |===
                    """;
            var output = convertString(input);
            assertCss(".toc", output, 1);
            assertCss("table .toc", output, 0);
        }

        @Test
        @DisplayName("should be able to enable toc in an AsciiDoc table cell")
        void shouldBeAbleToEnableTocInAnAsciiDocTableCell() throws Exception {
            var input = """
                    = Document Title

                    == Section A

                    |===
                    a|
                    = Subdocument Title
                    :toc:

                    == Subdocument Section A

                    content
                    |===
                    """;
            var output = convertString(input);
            assertCss(".toc", output, 1);
            assertCss("table .toc", output, 1);
        }

        @Test
        @DisplayName("should be able to enable toc in an AsciiDoc table cell even if hard unset by API")
        void shouldBeAbleToEnableTocInAnAsciiDocTableCellEvenIfHardUnsetByAPI() throws Exception {
            var input = """
                    = Document Title

                    == Section A

                    |===
                    a|
                    = Subdocument Title
                    :toc:

                    == Subdocument Section A

                    content
                    |===
                    """;
            var output = convertString(input, Options.of("attributes", Options.of("toc", null)));
            assertCss(".toc", output, 1);
            assertCss("table .toc", output, 1);
        }

        @Test
        @DisplayName("should be able to enable toc in both outer document and in an AsciiDoc table cell")
        void shouldBeAbleToEnableTocInBothOuterDocumentAndInAnAsciiDocTableCell() throws Exception {
            var input = """
                    = Document Title
                    :toc:

                    == Section A

                    |===
                    a|
                    = Subdocument Title
                    :toc: macro

                    [#table-cell-toc]
                    toc::[]

                    == Subdocument Section A

                    content
                    |===
                    """;
            var output = convertString(input);
            assertCss(".toc", output, 2);
            assertCss("#toc", output, 1);
            assertCss("table .toc", output, 1);
            assertCss("table #table-cell-toc", output, 1);
        }

        @Test
        @DisplayName("document in an AsciiDoc table cell should not see doctitle of parent")
        void documentInAnAsciiDocTableCellShouldNotSeeDoctitleOfParent() throws Exception {
            var input = """
                    = Document Title

                    [cols="1a"]
                    |===
                    |AsciiDoc content
                    |===
                    """;
            var output = convertString(input);
            assertCss("table", output, 1);
            assertCss("table > tbody > tr > td", output, 1);
            assertCss("table > tbody > tr > td #preamble", output, 0);
            assertCss("table > tbody > tr > td .paragraph", output, 1);
        }

        @Test
        @DisplayName("cell background color")
        void cellBackgroundColor() throws Exception {
            var input = """
                    [cols="1e,1", options="header"]
                    |===
                    |{set:cellbgcolor:green}green
                    |{set:cellbgcolor!}
                    plain
                    |{set:cellbgcolor:red}red
                    |{set:cellbgcolor!}
                    plain
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("(/table/thead/tr/th)[1][@style=\"background-color: green;\"]", output, 1);
            assertXpath("(/table/thead/tr/th)[2][@style=\"background-color: green;\"]", output, 0);
            assertXpath("(/table/tbody/tr/td)[1][@style=\"background-color: red;\"]", output, 1);
            assertXpath("(/table/tbody/tr/td)[2][@style=\"background-color: green;\"]", output, 0);
        }

        @Test
        @DisplayName("should warn if table block is not terminated")
        void shouldWarnIfTableBlockIsNotTerminated() throws Exception {
            var input = """
                    outside

                    |===
                    |
                    inside

                    still inside

                    eof
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertXpath("/table", output, 1);
                assertMessage(logger, "WARN", "<stdin>: line 3: unterminated table block", LogMessage.class);
            }
        }

        @Test
        @DisplayName("should show correct line number in warning about unterminated block inside AsciiDoc table cell")
        void shouldShowCorrectLineNumberInWarningAboutUnterminatedBlockInsideAsciiDocTableCell() throws Exception {
            var input = """
                    outside

                    * list item
                    +
                    |===
                    |cell
                    a|inside

                    ====
                    unterminated example block
                    |===

                    eof
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertXpath("//ul//table", output, 1);
                assertMessage(logger, "WARN", "<stdin>: line 9: unterminated example block", LogMessage.class);
            }
        }

        @Test
        @DisplayName("custom separator for an AsciiDoc table cell")
        void customSeparatorForAnAsciiDocTableCell() throws Exception {
            var input = """
                    [cols=2,separator=!]
                    |===
                    !Pipe output to vim
                    a!
                    ----
                    asciidoctor -o - -s test.adoc | view -
                    ----
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > tbody > tr", output, 1);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 2);
            assertCss("table > tbody > tr:nth-child(1) > td:nth-child(1) p", output, 1);
            assertCss("table > tbody > tr:nth-child(1) > td:nth-child(2) .listingblock", output, 1);
        }

        @Test
        @DisplayName("table with breakable option docbook 5")
        void tableWithBreakableOptionDocbook5() throws Exception {
            var input = """
                    .Table with breakable
                    [%breakable]
                    |===
                    |Item       |Quantity
                    |Item 1     |1
                    |===
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook5"));
            assertIncludes(output, "<?dbfo keep-together=\"auto\"?>");
        }

        @Test
        @DisplayName("table with unbreakable option docbook 5")
        void tableWithUnbreakableOptionDocbook5() throws Exception {
            var input = """
                    .Table with unbreakable
                    [%unbreakable]
                    |===
                    |Item       |Quantity
                    |Item 1     |1
                    |===
                    """;
            var output = convertStringToEmbedded(input, Options.of("backend", "docbook5"));
            assertIncludes(output, "<?dbfo keep-together=\"always\"?>");
        }

        @Test
        @DisplayName("no implicit header row if cell in first line is quoted and spans multiple lines")
        void noImplicitHeaderRowIfCellInFirstLineIsQuotedAndSpansMultipleLines() throws Exception {
            var input = """
                    [cols=2*l]
                    ,===
                    "A1

                    A1 continued",B1
                    A2,B2
                    ,===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > thead", output, 0);
            assertCss("table > tbody", output, 1);
            assertCss("table > tbody > tr", output, 2);
            assertXpath("(//td)[1]//pre[text()=\"A1\n\nA1 continued\"]", output, 1);
        }
    }

    @Nested
    @DisplayName("DSV")
    class DSV {

        @Test
        @DisplayName("converts simple dsv table")
        void convertsSimpleDsvTable() throws Exception {
            var input = """
                    [width="75%",format="dsv"]
                    |===
                    root:x:0:0:root:/root:/bin/bash
                    bin:x:1:1:bin:/bin:/sbin/nologin
                    mysql:x:27:27:MySQL\\:Server:/var/lib/mysql:/bin/bash
                    gdm:x:42:42::/var/lib/gdm:/sbin/nologin
                    sshd:x:74:74:Privilege-separated SSH:/var/empty/sshd:/sbin/nologin
                    nobody:x:99:99:Nobody:/:/sbin/nologin
                    |===
                    """;
            var doc = documentFromString(input, Options.of("standalone", false));
            var table = doc.getBlocks().get(0);
            assertEquals(100, sum(map(((Table) table).getColumns(), col -> get(col.getAttributes(), "colpcwidth"))));
            var output = doc.convert();
            assertCss("table", output, 1);
            assertCss("table > colgroup > col[width=\"14.2857%\"]", output, 6);
            assertCss("table > colgroup > col:last-of-type[width=\"14.2858%\"]", output, 1);
            assertCss("table > tbody > tr", output, 6);
            assertXpath("//tr[4]/td[5]/p/text()", output, 0);
            assertXpath("//tr[3]/td[5]/p[text()=\"MySQL:Server\"]", output, 1);
        }

        @Test
        @DisplayName("dsv format shorthand")
        void dsvFormatShorthand() throws Exception {
            var input = """
                    :===
                    a:b:c
                    1:2:3
                    :===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 3);
            assertCss("table > tbody > tr", output, 2);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 3);
            assertCss("table > tbody > tr:nth-child(2) > td", output, 3);
        }

        @Test
        @DisplayName("single cell in DSV table should only produce single row")
        void singleCellInDSVTableShouldOnlyProduceSingleRow() throws Exception {
            var input = """
                    :===
                    single cell
                    :===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table td", output, 1);
        }

        @Test
        @DisplayName("should treat trailing colon as an empty cell")
        void shouldTreatTrailingColonAsAnEmptyCell() throws Exception {
            var input = """
                    :===
                    A1:
                    B1:B2
                    C1:C2
                    :===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > tbody > tr", output, 3);
            assertXpath("/table/tbody/tr[1]/td", output, 2);
            assertXpath("/table/tbody/tr[1]/td[1]/p[text()=\"A1\"]", output, 1);
            assertXpath("/table/tbody/tr[1]/td[2]/p", output, 0);
            assertXpath("/table/tbody/tr[2]/td[1]/p[text()=\"B1\"]", output, 1);
        }
    }

    @Nested
    @DisplayName("CSV")
    class CSV {

        @Test
        @DisplayName("should treat trailing comma as an empty cell")
        void shouldTreatTrailingCommaAsAnEmptyCell() throws Exception {
            var input = """
                    ,===
                    A1,
                    B1,B2
                    C1,C2
                    ,===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > tbody > tr", output, 3);
            assertXpath("/table/tbody/tr[1]/td", output, 2);
            assertXpath("/table/tbody/tr[1]/td[1]/p[text()=\"A1\"]", output, 1);
            assertXpath("/table/tbody/tr[1]/td[2]/p", output, 0);
            assertXpath("/table/tbody/tr[2]/td[1]/p[text()=\"B1\"]", output, 1);
        }

        @Test
        @DisplayName("should log error but not crash if cell data has unclosed quote")
        void shouldLogErrorButNotCrashIfCellDataHasUnclosedQuote() throws Exception {
            var input = """
                    ,===
                    a,b
                    c,"
                    ,===
                    """;
            try (var logger = usingMemoryLogger()) {
                var output = convertStringToEmbedded(input);
                assertCss("table", output, 1);
                assertCss("table td", output, 4);
                assertXpath("(/table/td)[4]/p", output, 0);
                assertMessage(logger, "ERROR", "<stdin>: line 3: unclosed quote in CSV data; setting cell to empty", LogMessage.class);
            }
        }

        @Test
        @DisplayName("should preserve newlines in quoted CSV values")
        void shouldPreserveNewlinesInQuotedCSVValues() throws Exception {
            var input = """
                    [cols="1,1,1l"]
                    ,===
                    "A
                    B
                    C","one

                    two

                    three","do

                    re

                    me"
                    ,===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 3);
            assertCss("table > tbody > tr", output, 1);
            assertXpath("/table/tbody/tr[1]/td", output, 3);
            assertXpath("/table/tbody/tr[1]/td[1]/p[text()=\"A\nB\nC\"]", output, 1);
            assertXpath("/table/tbody/tr[1]/td[2]/p", output, 3);
            assertXpath("/table/tbody/tr[1]/td[2]/p[1][text()=\"one\"]", output, 1);
            assertXpath("/table/tbody/tr[1]/td[2]/p[2][text()=\"two\"]", output, 1);
            assertXpath("/table/tbody/tr[1]/td[2]/p[3][text()=\"three\"]", output, 1);
            assertXpath("/table/tbody/tr[1]/td[3]//pre[text()=\"do\n\nre\n\nme\"]", output, 1);
        }

        @Test
        @DisplayName("should not drop trailing empty cell in TSV data when loaded from an include file")
        void shouldNotDropTrailingEmptyCellInTSVDataWhenLoadedFromAnIncludeFile() throws Exception {
            var input = """
                    [%header,format=tsv]
                    |===
                    include::fixtures/data.tsv[]
                    |===
                    """;
            var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", testdir()));
            assertCss("table > tbody > tr", output, 3);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 3);
            assertCss("table > tbody > tr:nth-child(2) > td", output, 3);
            assertCss("table > tbody > tr:nth-child(3) > td", output, 3);
            assertCss("table > tbody > tr:nth-child(2) > td:nth-child(3):empty", output, 1);
        }

        @Test
        @DisplayName("mixed unquoted records and quoted records with escaped quotes, commas, and wrapped lines")
        void mixedUnquotedRecordsAndQuotedRecordsWithEscapedQuotesCommasAndWrappedLines() throws Exception {
            var input = "[format=\"csv\",options=\"header\"]\n|===\nYear,Make,Model,Description,Price\n1997,Ford,E350,\"ac, abs, moon\",3000.00\n1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00\n1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",,5000.00\n1996,Jeep,Grand Cherokee,\"MUST SELL!\nair, moon roof, loaded\",4799.00\n2000,Toyota,Tundra,\"\"\"This one's gonna to blow you're socks off,\"\" per the sticker\",10000.00\n2000,Toyota,Tundra,\"Check it, \"\"this one's gonna to blow you're socks off\"\", per the sticker\",10000.00\n|===\n";
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col[width=\"20%\"]", output, 5);
            assertCss("table > thead > tr", output, 1);
            assertCss("table > tbody > tr", output, 6);
            assertXpath("((//tbody/tr)[1]/td)[4]/p[text()=\"ac, abs, moon\"]", output, 1);
            assertXpath("((//tbody/tr)[2]/td)[3]/p[text()='Venture \"Extended Edition\"']", output, 1);
            assertXpath("((//tbody/tr)[4]/td)[4]/p[text()=\"MUST SELL!\nair, moon roof, loaded\"]", output, 1);
            assertXpath(("((//tbody/tr)[5]/td)[4]/p[text()='\"This one" + str(decodeChar(8217)) + "s gonna to blow you" + str(decodeChar(8217)) + "re socks off,\" per the sticker']"), output, 1);
            assertXpath(("((//tbody/tr)[6]/td)[4]/p[text()='Check it, \"this one" + str(decodeChar(8217)) + "s gonna to blow you" + str(decodeChar(8217)) + "re socks off\", per the sticker']"), output, 1);
        }

        @Test
        @DisplayName("should allow quotes around a CSV value to be on their own lines")
        void shouldAllowQuotesAroundACSVValueToBeOnTheirOwnLines() throws Exception {
            var input = """
                    [cols=2*]
                    ,===
                    "
                    A
                    ","
                    B
                    "
                    ,===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 2);
            assertCss("table > tbody > tr", output, 1);
            assertXpath("/table/tbody/tr[1]/td", output, 2);
            assertXpath("/table/tbody/tr[1]/td[1]/p[text()=\"A\"]", output, 1);
            assertXpath("/table/tbody/tr[1]/td[2]/p[text()=\"B\"]", output, 1);
        }

        @Test
        @DisplayName("csv format shorthand")
        void csvFormatShorthand() throws Exception {
            var input = """
                    ,===
                    a,b,c
                    1,2,3
                    ,===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 3);
            assertCss("table > tbody > tr", output, 2);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 3);
            assertCss("table > tbody > tr:nth-child(2) > td", output, 3);
        }

        @Test
        @DisplayName("tsv as format")
        void tsvAsFormat() throws Exception {
            var input = """
                    [format=tsv]
                    ,===
                    a	b	c
                    1	2	3
                    ,===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 3);
            assertCss("table > tbody > tr", output, 2);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 3);
            assertCss("table > tbody > tr:nth-child(2) > td", output, 3);
        }

        @Test
        @DisplayName("custom csv separator")
        void customCsvSeparator() throws Exception {
            var input = """
                    [format=csv,separator=;]
                    |===
                    a;b;c
                    1;2;3
                    |===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 3);
            assertCss("table > tbody > tr", output, 2);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 3);
            assertCss("table > tbody > tr:nth-child(2) > td", output, 3);
        }

        @Test
        @DisplayName("tab as separator")
        void tabAsSeparator() throws Exception {
            var input = """
                    [separator=\\t]
                    ,===
                    a	b	c
                    1	2	3
                    ,===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table", output, 1);
            assertCss("table > colgroup > col", output, 3);
            assertCss("table > tbody > tr", output, 2);
            assertCss("table > tbody > tr:nth-child(1) > td", output, 3);
            assertCss("table > tbody > tr:nth-child(2) > td", output, 3);
        }

        @Test
        @DisplayName("single cell in CSV table should only produce single row")
        void singleCellInCSVTableShouldOnlyProduceSingleRow() throws Exception {
            var input = """
                    ,===
                    single cell
                    ,===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table td", output, 1);
        }

        @Test
        @DisplayName("cell formatted with AsciiDoc style")
        void cellFormattedWithAsciiDocStyle() throws Exception {
            var input = """
                    [cols="1,1,1a",separator=;]
                    ,===
                    element;description;example

                    thematic break,a visible break; also known as a horizontal rule;---
                    ,===
                    """;
            var output = convertStringToEmbedded(input);
            assertCss("table tbody hr", output, 1);
        }

        @Test
        @DisplayName("should strip whitespace around contents of AsciiDoc cell")
        void shouldStripWhitespaceAroundContentsOfAsciiDocCell() throws Exception {
            var input = """
                    [cols="1,1,1a",separator=;]
                    ,===
                    element;description;example

                    paragraph;contiguous lines of words and phrases;"
                      one sentence, one line
                      "
                    ,===
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/table/tbody//*[@class=\"paragraph\"]/p[text()=\"one sentence, one line\"]", output, 1);
        }
    }
}
