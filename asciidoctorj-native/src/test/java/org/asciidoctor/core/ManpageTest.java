package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

@DisplayName("Manpage")
public class ManpageTest extends TestHelper {
    static final String SAMPLE_MANPAGE_HEADER = Strings.chop("""
            = command (1)
            Author Name
            :doctype: manpage
            :man manual: Command Manual
            :man source: Command 1.2.3

            == NAME

            command - does stuff

            == SYNOPSIS

            *command* [_OPTION_]... _FILE_...

            == DESCRIPTION
            """);

    @Nested
    @DisplayName("Configuration")
    class Configuration {

        @Test
        @DisplayName("should set proper manpage-related attributes")
        void shouldSetProperManpageRelatedAttributes() throws Exception {
            var input = SAMPLE_MANPAGE_HEADER;
            var doc = Asciidoctor.load(input, Options.of("backend", "manpage"));
            assertEquals("man", get(doc.getAttributes(), "filetype"));
            assertEquals("", get(doc.getAttributes(), "filetype-man"));
            assertEquals("1", get(doc.getAttributes(), "manvolnum"));
            assertEquals(".1", get(doc.getAttributes(), "outfilesuffix"));
            assertEquals("command", get(doc.getAttributes(), "manname"));
            assertEquals("command", get(doc.getAttributes(), "mantitle"));
            assertEquals("does stuff", get(doc.getAttributes(), "manpurpose"));
            assertEquals("command", get(doc.getAttributes(), "docname"));
        }

        @Test
        @DisplayName("should not escape hyphen when printing manname in NAME section")
        void shouldNotEscapeHyphenWhenPrintingMannameInNAMESection() throws Exception {
            var input = sub(SAMPLE_MANPAGE_HEADER, rx("^command - "), "git-describe - ");
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "standalone", true));
            assertIncludes(output, "\n.SH \"NAME\"\ngit-describe \\- does stuff\n");
        }

        @Test
        @DisplayName("should output multiple mannames in NAME section")
        void shouldOutputMultipleMannamesInNAMESection() throws Exception {
            var input = sub(SAMPLE_MANPAGE_HEADER, rx("^command - "), "command, alt_command - ");
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "standalone", true));
            assertIncludes(lines(output), "command, alt_command \\- does stuff\n");
        }

        @Test
        @DisplayName("should replace invalid characters in mantitle in info comment")
        void shouldReplaceInvalidCharactersInMantitleInInfoComment() throws Exception {
            var input = """
                    = foo\\--<bar> (1)
                    Author Name
                    :doctype: manpage
                    :man manual: Foo Bar Manual
                    :man source: Foo Bar 1.0

                    == NAME

                    foo-bar - puts the foo in your bar
                    """;
            var doc = Asciidoctor.load(input, Options.of("backend", "manpage", "standalone", true));
            var output = doc.convert();
            assertIncludes(output, "Title: foo--bar\n");
        }

        @Test
        @DisplayName("should substitute attributes in manname and manpurpose in NAME section")
        void shouldSubstituteAttributesInMannameAndManpurposeInNAMESection() throws Exception {
            var input = """
                    = {cmdname} (1)
                    Author Name
                    :doctype: manpage
                    :man manual: Foo Bar Manual
                    :man source: Foo Bar 1.0

                    == NAME

                    {cmdname} - {cmdname} puts the foo in your bar
                    """;
            var doc = Asciidoctor.load(input, Options.of("backend", "manpage", "standalone", true, "attributes", Options.of("cmdname", "foobar")));
            assertEquals("foobar", doc.getAttribute("manname"));
            assertEquals(list("foobar"), doc.getAttribute("mannames"));
            assertEquals("foobar puts the foo in your bar", doc.getAttribute("manpurpose"));
            assertEquals("foobar", doc.getAttribute("docname"));
        }

        @Test
        @DisplayName("should not parse NAME section if manname and manpurpose attributes are set")
        void shouldNotParseNAMESectionIfMannameAndManpurposeAttributesAreSet() throws Exception {
            var input = """
                    = foobar (1)
                    Author Name
                    :doctype: manpage
                    :man manual: Foo Bar Manual
                    :man source: Foo Bar 1.0

                    == SYNOPSIS

                    *foobar* [_OPTIONS_]...

                    == DESCRIPTION

                    When you need to put some foo on the bar.
                    """;
            var attrs = Options.of("manname", "foobar", "manpurpose", "puts some foo on the bar");
            var doc = Asciidoctor.load(input, Options.of("backend", "manpage", "standalone", true, "attributes", attrs));
            assertEquals("foobar", doc.getAttribute("manname"));
            assertEquals(list("foobar"), doc.getAttribute("mannames"));
            assertEquals("puts some foo on the bar", doc.getAttribute("manpurpose"));
            assertEquals("SYNOPSIS", doc.getSections().get(0).getTitle());
        }

        @Test
        @DisplayName("should normalize whitespace and skip line comments before and inside NAME section")
        void shouldNormalizeWhitespaceAndSkipLineCommentsBeforeAndInsideNAMESection() throws Exception {
            var input = """
                    = foobar (1)
                    Author Name
                    :doctype: manpage
                    :man manual: Foo Bar Manual
                    :man source: Foo Bar 1.0

                    // this is the name section
                    == NAME

                    // it follows the form `name - description`
                    foobar - puts some foo
                     on the bar
                    // a little bit of this, a little bit of that

                    == SYNOPSIS

                    *foobar* [_OPTIONS_]...

                    == DESCRIPTION

                    When you need to put some foo on the bar.
                    """;
            var doc = Asciidoctor.load(input, Options.of("backend", "manpage", "standalone", true));
            assertEquals("puts some foo on the bar", doc.getAttribute("manpurpose"));
        }

        @Test
        @DisplayName("should parse malformed document with warnings")
        void shouldParseMalformedDocumentWithWarnings() throws Exception {
            var input = "garbage in";
            try (var logger = usingMemoryLogger()) {
                var doc = Asciidoctor.load(input, Options.of("backend", "manpage", "standalone", true, "attributes", Options.of("docname", "cmd")));
                assertEquals("cmd", doc.getAttribute("manname"));
                assertEquals(list("cmd"), doc.getAttribute("mannames"));
                assertEquals(".1", doc.getAttribute("outfilesuffix"));
                var output = doc.convert();
                refuteEmpty(logger.getMessages());
                assertIncludes(output, "Title: cmd\n");
                assertTruthy(endsWith(output, "garbage in"));
            }
        }

        @Test
        @DisplayName("should warn if document title is non-conforming")
        void shouldWarnIfDocumentTitleIsNonConforming() throws Exception {
            var input = """
                    = command

                    == Name

                    command - does stuff
                    """;
            try (var logger = usingMemoryLogger()) {
                documentFromString(input, Options.of("backend", "manpage"));
                assertMessage(logger, "ERROR", "<stdin>: line 1: non-conforming manpage title", LogMessage.class);
            }
        }

        @Test
        @DisplayName("should warn if first section is not name section")
        void shouldWarnIfFirstSectionIsNotNameSection() throws Exception {
            var input = """
                    = command(1)

                    == Synopsis

                    Does stuff.
                    """;
            try (var logger = usingMemoryLogger()) {
                var doc = documentFromString(input, Options.of("backend", "manpage"));
                assertMessage(logger, "ERROR", "<stdin>: line 3: non-conforming name section body", LogMessage.class);
                assertNotNull(doc.getSections().get(0));
                assertEquals("Synopsis", doc.getSections().get(0).getTitle());
            }
        }

        @Test
        @DisplayName("should break circular reference in section title")
        void shouldBreakCircularReferenceInSectionTitle() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n[#a]\n== A <<b>>\n\n[#b]\n== B <<a>>\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertMatch(rx("^\\.SH \"A B \\[A\\]\"$"), output);
            assertMatch(rx("^\\.SH \"B \\[A\\]\"$"), output);
        }

        @Test
        @DisplayName("should define default linkstyle")
        void shouldDefineDefaultLinkstyle() throws Exception {
            var input = SAMPLE_MANPAGE_HEADER;
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "standalone", true));
            assertIncludes(lines(output), ".  LINKSTYLE blue R < >\n");
        }

        @Test
        @DisplayName("should use linkstyle defined by man-linkstyle attribute")
        void shouldUseLinkstyleDefinedByManLinkstyleAttribute() throws Exception {
            var input = SAMPLE_MANPAGE_HEADER;
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "standalone", true, "attributes", Options.of("man-linkstyle", "cyan B \\[fo] \\[fc]")));
            assertIncludes(lines(output), ".  LINKSTYLE cyan B \\[fo] \\[fc]\n");
        }

        @Test
        @DisplayName("should require specialchars in value of man-linkstyle attribute defined in document to be escaped")
        void shouldRequireSpecialcharsInValueOfManLinkstyleAttributeDefinedInDocumentToBeEscaped() throws Exception {
            var input = Strings.chop((":man-linkstyle: cyan R < >\n" + str(SAMPLE_MANPAGE_HEADER) + "\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "standalone", true));
            assertIncludes(lines(output), ".  LINKSTYLE cyan R &lt; &gt;\n");
            input = Strings.chop((":man-linkstyle: pass:[cyan R < >]\n" + str(SAMPLE_MANPAGE_HEADER) + "\n"));
            output = Asciidoctor.convert(input, Options.of("backend", "manpage", "standalone", true));
            assertIncludes(lines(output), ".  LINKSTYLE cyan R < >\n");
        }
    }

    @Nested
    @DisplayName("Manify")
    class Manify {

        @Test
        @DisplayName("should unescape literal ampersand")
        void shouldUnescapeLiteralAmpersand() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n(C) & (R) are translated to character references, but not the &.\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals("\\(co & \\(rg are translated to character references, but not the &.", Strings.chomp(last(lines(output))));
        }

        @Test
        @DisplayName("should replace numeric character reference for plus")
        void shouldReplaceNumericCharacterReferenceForPlus() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nA {plus} B\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals("A + B", Strings.chomp(last(lines(output))));
        }

        @Test
        @DisplayName("should replace numeric character reference for degree sign")
        void shouldReplaceNumericCharacterReferenceForDegreeSign() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n0{deg} is freezing\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals("0\\(de is freezing", Strings.chomp(last(lines(output))));
        }

        @Test
        @DisplayName("should replace em dashes")
        void shouldReplaceEmDashes() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\ngo -- to\n\ngo--to\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertIncludes(output, "go \\(em to");
            assertIncludes(output, "go\\(emto");
        }

        @Test
        @DisplayName("should replace quotes")
        void shouldReplaceQuotes() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n'command'\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertIncludes(output, "\\*(Aqcommand\\*(Aq");
        }

        @Test
        @DisplayName("should escape lone period")
        void shouldEscapeLonePeriod() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n.\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals("\\&.", Strings.chomp(last(lines(output))));
        }

        @Test
        @DisplayName("should escape raw macro")
        void shouldEscapeRawMacro() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nAAA this line of text should be show\n.if 1 .nx\nBBB this line and the one above it should be visible\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals("\\&.if 1 .nx", Strings.chomp(lines(output).get(lines(output).size() - 2)));
        }

        @Test
        @DisplayName("should escape ellipsis at start of line")
        void shouldEscapeEllipsisAtStartOfLine() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n-x::\nAo gravar o commit, acrescente uma linha que diz \"(cherry picked from commit\n...)\" à mensagem de commit original para indicar qual commit esta mudança\nfoi escolhida. Isso é feito apenas para picaretas de cereja sem conflitos.\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals("\\&.\\|.\\|.", Strings.partition(lines(output).get(lines(output).size() - 3), ")")[0]);
        }

        @Test
        @DisplayName("should not escape ellipsis in the middle of a line")
        void shouldNotEscapeEllipsisInTheMiddleOfALine() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n-x::\nAo gravar o commit, acrescente uma linha que diz\n\"(cherry picked from commit...)\" à mensagem de commit\noriginal para indicar qual commit esta mudança\nfoi escolhida. Isso é feito apenas para picaretas\nde cereja sem conflitos.\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertInclude("commit.\\|.\\|.", lines(output).get(lines(output).size() - 5));
        }

        @Test
        @DisplayName("should not space dots that are not meant to be an ellipsis")
        void shouldNotSpaceDotsThatAreNotMeantToBeAnEllipsis() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n-x::\nAo gravar o commit, acrescente uma linha que diz\n\"(cherry picked from commit\\...)\" à mensagem de commit\noriginal para indicar qual commit esta mudança\nfoi escolhida. Isso é feito apenas para picaretas\nde cereja sem conflitos.\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertInclude("commit...", lines(output).get(lines(output).size() - 5));
        }

        @Test
        @DisplayName("should normalize whitespace in a paragraph")
        void shouldNormalizeWhitespaceInAParagraph() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nOh, here it goes again\n  I should have known,\n    should have known,\nshould have known again\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertIncludes(output, "Oh, here it goes again\nI should have known,\nshould have known,\nshould have known again");
        }

        @Test
        @DisplayName("should escape repeated spaces in literal content")
        void shouldEscapeRepeatedSpacesInLiteralContent() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n....\n  ,---.          ,-----.\n  |Bob|          |Alice|\n  `-+-'          `--+--'\n    |    hello      |\n    |-------------->|\n  ,-+-.          ,--+--.\n  |Bob|          |Alice|\n  `---'          `-----'\n....\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertIncludes(output, """
                    .fam C
                      ,\\-\\-\\-.\\&          ,\\-\\-\\-\\-\\-.
                      |Bob|\\&          |Alice|
                      `\\-+\\-\\*(Aq\\&          `\\-\\-+\\-\\-\\*(Aq
                        |\\&    hello\\&      |
                        |\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\-\\->|
                      ,\\-+\\-.\\&          ,\\-\\-+\\-\\-.
                      |Bob|\\&          |Alice|
                      `\\-\\-\\-\\*(Aq\\&          `\\-\\-\\-\\-\\-\\*(Aq
                    .fam
                    """);
        }

        @Test
        @DisplayName("should preserve break between paragraphs in normal table cell")
        void shouldPreserveBreakBetweenParagraphsInNormalTableCell() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n[cols=3*]\n|===\n|single paragraph\n|first paragraph\n\nsecond paragraph\n|foo\n\nmore foo\n\neven more foo\n|===\n"));
            var expectedCoda = Strings.chop("""
                    .TS
                    allbox tab(:);
                    lt lt lt.
                    T{
                    single paragraph
                    T}:T{
                    first paragraph
                    .sp
                    second paragraph
                    T}:T{
                    foo
                    .sp
                    more foo
                    .sp
                    even more foo
                    T}
                    .TE
                    .sp
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should normalize whitespace in a list item")
        void shouldNormalizeWhitespaceInAListItem() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n* Oh, here it goes again\n    I should have known,\n  should have known,\nshould have known again\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertIncludes(output, "Oh, here it goes again\nI should have known,\nshould have known,\nshould have known again");
        }

        @Test
        @DisplayName("should drop principal text of list item in ulist if empty")
        void shouldDropPrincipalTextOfListItemInUlistIfEmpty() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n* {empty}\n+\nthe main text\n"));
            var expectedCoda = Strings.chop("""
                    .\\}
                    the main text
                    .RE
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should drop principal text of list item in olist if empty")
        void shouldDropPrincipalTextOfListItemInOlistIfEmpty() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n. {empty}\n+\nthe main text\n"));
            var expectedCoda = Strings.chop("""
                    .\\}
                    the main text
                    .RE
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should not add extra space before block content if dlist item has no text")
        void shouldNotAddExtraSpaceBeforeBlockContentIfDlistItemHasNoText() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nterm::\n+\ndescription\n"));
            var expectedCoda = Strings.chop("""
                    term
                    .RS 4
                    description
                    .RE
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should honor start attribute on ordered list")
        void shouldHonorStartAttributeOnOrderedList() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n[start=5]\n. five\n. six\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertMatch(rx("IP \" 5\\.\".*five", "m"), output);
            assertMatch(rx("IP \" 6\\.\".*six", "m"), output);
        }

        @Test
        @DisplayName("should collapse whitespace in the man manual and man source")
        void shouldCollapseWhitespaceInTheManManualAndManSource() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nDescribe this thing.\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "standalone", true, "attributes", Options.of("manmanual", "General\nCommands\nManual", "mansource", "Control\nAll\nThe\nThings\n5.0")));
            assertIncludes(output, "Manual: General Commands Manual");
            assertIncludes(output, "Source: Control All The Things 5.0");
            assertIncludes(output, "\"Control All The Things 5.0\" \"General Commands Manual\"");
        }

        @Test
        @DisplayName("should uppercase section titles without mangling formatting macros")
        void shouldUppercaseSectionTitlesWithoutManglingFormattingMacros() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\ndoes stuff\n\n== \"`Main`\" _<Options>_\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertIncludes(output, ".SH \"\\(lqMAIN\\(rq \\fI<OPTIONS>\\fP\"");
        }

        @Test
        @DisplayName("should not uppercase monospace span in section titles")
        void shouldNotUppercaseMonospaceSpanInSectionTitles() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\ndoes stuff\n\n== `show` option\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertIncludes(output, ".SH \"\\f(CRshow\\fP OPTION\"");
        }
    }

    @Nested
    @DisplayName("Backslash")
    class Backslash {

        @Test
        @DisplayName("should not escape spaces for empty manual or source fields")
        void shouldNotEscapeSpacesForEmptyManualOrSourceFields() throws Exception {
            var input = reject(lines(SAMPLE_MANPAGE_HEADER), l -> startsWith(l, ":man "));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "standalone", true));
            assertMatch(" Manual: \\ \\&", output);
            assertMatch(" Source: \\ \\&", output);
            assertMatch(rx("^\\.TH \"COMMAND\" .* \"\\\\ \\\\&\" \"\\\\ \\\\&\"$"), output);
        }

        @Test
        @DisplayName("should preserve backslashes in escape sequences")
        void shouldPreserveBackslashesInEscapeSequences() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n\"`hello`\" '`goodbye`' *strong* _weak_ `even`\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals("\\(lqhello\\(rq \\(oqgoodbye\\(cq \\fBstrong\\fP \\fIweak\\fP \\f(CReven\\fP", Strings.chomp(last(lines(output))));
        }

        @Test
        @DisplayName("should preserve literal backslashes in content")
        void shouldPreserveLiteralBackslashesInContent() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n\\.foo \\ bar \\\\ baz\\\nmore\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals("\\(rs.foo \\(rs bar \\(rs\\(rs baz\\(rs", Strings.chomp(lines(output).get(lines(output).size() - 2)));
        }

        @Test
        @DisplayName("should escape literal escape sequence")
        void shouldEscapeLiteralEscapeSequence() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n \\fB makes text bold\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertMatch("\\(rsfB makes text bold", output);
        }

        @Test
        @DisplayName("should preserve inline breaks")
        void shouldPreserveInlineBreaks() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nBefore break. +\nAfter break.\n"));
            var expected = Strings.chop("""
                    Before break.
                    .br
                    After break.
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals(expected, join(slice(lines(output), range(-3, -1))));
        }
    }

    @Nested
    @DisplayName("URL macro")
    class URLMacro {

        @Test
        @DisplayName("should not leave blank line before URL macro")
        void shouldNotLeaveBlankLineBeforeURLMacro() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\nFirst paragraph.\n\nhttp://asciidoc.org[AsciiDoc]\n"));
            var expected = Strings.chop("""
                    .sp
                    First paragraph.
                    .sp
                    .URL "http://asciidoc.org" "AsciiDoc" ""
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals(expected, join(slice(lines(output), range(-4, -1))));
        }

        @Test
        @DisplayName("should not swallow content following URL")
        void shouldNotSwallowContentFollowingURL() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nhttp://asciidoc.org[AsciiDoc] can be used to create man pages.\n"));
            var expected = Strings.chop("""
                    .URL "http://asciidoc.org" "AsciiDoc" ""
                    can be used to create man pages.
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals(expected, join(slice(lines(output), range(-2, -1))));
        }

        @Test
        @DisplayName("should pass adjacent character as final argument of URL macro")
        void shouldPassAdjacentCharacterAsFinalArgumentOfURLMacro() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nThis is http://asciidoc.org[AsciiDoc].\n"));
            var expected = Strings.chop("""
                    This is \\c
                    .URL "http://asciidoc.org" "AsciiDoc" "."
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals(expected, join(slice(lines(output), range(-2, -1))));
        }

        @Test
        @DisplayName("should pass adjacent character as final argument of URL macro and move trailing content to next line")
        void shouldPassAdjacentCharacterAsFinalArgumentOfURLMacroAndMoveTrailingContentToNextLine() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nThis is http://asciidoc.org[AsciiDoc], which can be used to write content.\n"));
            var expected = Strings.chop("""
                    This is \\c
                    .URL "http://asciidoc.org" "AsciiDoc" ","
                    which can be used to write content.
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals(expected, join(slice(lines(output), range(-3, -1))));
        }

        @Test
        @DisplayName("should not leave blank lines between URLs on contiguous lines of input")
        void shouldNotLeaveBlankLinesBetweenURLsOnContiguousLinesOfInput() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nThe corresponding implementations are\nhttp://clisp.sf.net[CLISP],\nhttp://ccl.clozure.com[Clozure CL],\nhttp://cmucl.org[CMUCL],\nhttp://ecls.sf.net[ECL],\nand http://sbcl.sf.net[SBCL].\n"));
            var expected = Strings.chop("""
                    .sp
                    The corresponding implementations are
                    .URL "http://clisp.sf.net" "CLISP" ","
                    .URL "http://ccl.clozure.com" "Clozure CL" ","
                    .URL "http://cmucl.org" "CMUCL" ","
                    .URL "http://ecls.sf.net" "ECL" ","
                    and \\c
                    .URL "http://sbcl.sf.net" "SBCL" "."
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals(expected, join(slice(lines(output), range(-8, -1))));
        }

        @Test
        @DisplayName("should not leave blank lines between URLs on same line of input")
        void shouldNotLeaveBlankLinesBetweenURLsOnSameLineOfInput() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nThe corresponding implementations are http://clisp.sf.net[CLISP], http://ccl.clozure.com[Clozure CL], http://cmucl.org[CMUCL], http://ecls.sf.net[ECL], and http://sbcl.sf.net[SBCL].\n"));
            var expected = Strings.chop("""
                    .sp
                    The corresponding implementations are \\c
                    .URL "http://clisp.sf.net" "CLISP" ","
                    .URL "http://ccl.clozure.com" "Clozure CL" ","
                    .URL "http://cmucl.org" "CMUCL" ","
                    .URL "http://ecls.sf.net" "ECL" ","
                    and
                    .URL "http://sbcl.sf.net" "SBCL" "."
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals(expected, join(slice(lines(output), range(-8, -1))));
        }

        @Test
        @DisplayName("should not insert space between link and non-whitespace characters surrounding it")
        void shouldNotInsertSpaceBetweenLinkAndNonWhitespaceCharactersSurroundingIt() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nPlease search |link:http://discuss.asciidoctor.org[the forums]| before asking.\n"));
            var expected = Strings.chop("""
                    .sp
                    Please search |\\c
                    .URL "http://discuss.asciidoctor.org" "the forums" "|"
                    before asking.
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals(expected, join(slice(lines(output), range(-4, -1))));
        }

        @Test
        @DisplayName("should be able to use monospaced text inside a link")
        void shouldBeAbleToUseMonospacedTextInsideALink() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nEnter the link:cat[`cat`] command.\n"));
            var expected = Strings.chop("""
                    .sp
                    Enter the \\c
                    .URL "cat" "\\f(CRcat\\fP" ""
                    command.
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals(expected, join(slice(lines(output), range(-4, -1))));
        }
    }

    @Nested
    @DisplayName("MTO macro")
    class MTOMacro {

        @Test
        @DisplayName("should convert inline email macro into MTO macro")
        void shouldConvertInlineEmailMacroIntoMTOMacro() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\nFirst paragraph.\n\nmailto:doc@example.org[Contact the doc]\n"));
            var expected = Strings.chop("""
                    .sp
                    First paragraph.
                    .sp
                    .MTO "doc\\(atexample.org" "Contact the doc" ""
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertEquals(expected, join(slice(lines(output), range(-4, -1))));
        }

        @Test
        @DisplayName("should set text of MTO macro to blank for implicit email")
        void shouldSetTextOfMTOMacroToBlankForImplicitEmail() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\nBugs fixed daily by doc@example.org.\n"));
            var expectedCoda = Strings.chop("""
                    Bugs fixed daily by \\c
                    .MTO "doc\\(atexample.org" "" "."
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }
    }

    @Nested
    @DisplayName("Table")
    class TableTests {

        @Test
        @DisplayName("should create header, body, and footer rows in correct order")
        void shouldCreateHeaderBodyAndFooterRowsInCorrectOrder() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n[%header%footer]\n|===\n|Header\n|Body 1\n|Body 2\n|Footer\n|===\n"));
            var expectedCoda = Strings.chop("""
                    allbox tab(:);
                    ltB.
                    T{
                    Header
                    T}
                    .T&
                    lt.
                    T{
                    Body 1
                    T}
                    T{
                    Body 2
                    T}
                    T{
                    Footer
                    T}
                    .TE
                    .sp
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should manify normal table cell content")
        void shouldManifyNormalTableCellContent() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n|===\n|*Col A* |_Col B_\n\n|*bold* |`mono`\n|_italic_ | #mark#\n|===\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            refuteMatch(rx("</?BOUNDARY>"), output);
        }

        @Test
        @DisplayName("should manify table title")
        void shouldManifyTableTitle() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n.Table of options\n|===\n| Name | Description | Default\n\n| dim\n| dimension of the object\n| 3\n|===\n"));
            var expectedCoda = Strings.chop("""
                    .it 1 an-trap
                    .nr an-no-space-flag 1
                    .nr an-break-flag 1
                    .br
                    .B Table 1. Table of options
                    .TS
                    allbox tab(:);
                    ltB ltB ltB.
                    T{
                    Name
                    T}:T{
                    Description
                    T}:T{
                    Default
                    T}
                    .T&
                    lt lt lt.
                    T{
                    dim
                    T}:T{
                    dimension of the object
                    T}:T{
                    3
                    T}
                    .TE
                    .sp
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should manify and preserve whitespace in literal table cell")
        void shouldManifyAndPreserveWhitespaceInLiteralTableCell() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n|===\n|a l|b\nc    _d_\n.\n|===\n"));
            var expectedCoda = Strings.chop("""
                    .TS
                    allbox tab(:);
                    lt lt.
                    T{
                    a
                    T}:T{
                    .nf
                    b
                    c\\&    _d_
                    \\&.
                    .fi
                    T}
                    .TE
                    .sp
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }
    }

    @Nested
    @DisplayName("Images")
    class Images {

        @Test
        @DisplayName("should replace block image with alt text enclosed in square brackets")
        void shouldReplaceBlockImageWithAltTextEnclosedInSquareBrackets() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nBehold the wisdom of the Magic 8 Ball!\n\nimage::signs-point-to-yes.jpg[]\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, "\n.sp\n[signs point to yes]"));
        }

        @Test
        @DisplayName("should manify alt text of block image")
        void shouldManifyAltTextOfBlockImage() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nimage::rainbow.jpg[\"That's a double rainbow, otherwise known as rainbow{pp}!\"]\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, "\n.sp\n[That\\(cqs a double rainbow, otherwise known as rainbow++!]"));
        }

        @Test
        @DisplayName("should replace inline image with alt text enclosed in square brackets")
        void shouldReplaceInlineImageWithAltTextEnclosedInSquareBrackets() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nThe Magic 8 Ball says image:signs-point-to-yes.jpg[].\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertIncludes(output, "The Magic 8 Ball says [signs point to yes].");
        }

        @Test
        @DisplayName("should place link after alt text for inline image if link is defined")
        void shouldPlaceLinkAfterAltTextForInlineImageIfLinkIsDefined() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nThe Magic 8 Ball says image:signs-point-to-yes.jpg[link=https://en.wikipedia.org/wiki/Magic_8-Ball].\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertIncludes(output, "The Magic 8 Ball says [signs point to yes] <https://en.wikipedia.org/wiki/Magic_8\\-Ball>.");
        }

        @Test
        @DisplayName("should reference image with title usign styled xref")
        void shouldReferenceImageWithTitleUsignStyledXref() throws Exception {
            var input = Strings.chomp((str(SAMPLE_MANPAGE_HEADER) + "\n\nTo get your fortune, see <<magic-8-ball>>.\n\n.Magic 8-Ball\n[#magic-8-ball]\nimage::signs-point-to-yes.jpg[]\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "attributes", Options.of("xrefstyle", "full")));
            var lines = map(lines(output), (String it) -> Strings.chomp(it));
            assertIncludes(lines, "To get your fortune, see Figure 1, \\(lqMagic 8\\-Ball\\(rq.");
            assertIncludes(lines, ".B Figure 1. Magic 8\\-Ball");
        }
    }

    @Nested
    @DisplayName("Quote Block")
    class QuoteBlock {

        @Test
        @DisplayName("should indent quote block")
        void shouldIndentQuoteBlock() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n[,James Baldwin]\n____\nNot everything that is faced can be changed.\nBut nothing can be changed until it is faced.\n____\n"));
            var expectedCoda = Strings.chop("""
                    .RS 3
                    .ll -.6i
                    .sp
                    Not everything that is faced can be changed.
                    But nothing can be changed until it is faced.
                    .br
                    .RE
                    .ll
                    .RS 5
                    .ll -.10i
                    \\(em James Baldwin
                    .RE
                    .ll
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }
    }

    @Nested
    @DisplayName("Verse Block")
    class VerseBlock {

        @Test
        @DisplayName("should preserve hard line breaks in verse block")
        void shouldPreserveHardLineBreaksInVerseBlock() throws Exception {
            var lines = lines(SAMPLE_MANPAGE_HEADER);
            var synopsisIdx = lines.indexOf("== SYNOPSIS\n") + 2;
            lines.remove(synopsisIdx);
            lines.addAll(synopsisIdx, lines("""
                    [verse]
                    _command_ [_OPTION_]... _FILE_...
                    """));
            var input = Strings.chop(join(lines) + "\n\ndescription\n");
            var expectedCoda = Strings.chop("""
                    .SH "SYNOPSIS"
                    .sp
                    .nf
                    \\fIcommand\\fP [\\fIOPTION\\fP].\\|.\\|. \\fIFILE\\fP.\\|.\\|.
                    .fi
                    .br
                    .SH "DESCRIPTION"
                    .sp
                    description
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }
    }

    @Nested
    @DisplayName("Callout List")
    class CalloutList {

        @Test
        @DisplayName("should generate callout list using proper formatting commands")
        void shouldGenerateCalloutListUsingProperFormattingCommands() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n----\n$ gem install asciidoctor # <1>\n----\n<1> Installs the asciidoctor gem from RubyGems.org\n"));
            var expectedCoda = Strings.chop("""
                    .TS
                    tab(:);
                    r lw(\\n(.lu*75u/100u).
                    \\fB(1)\\fP\\h'-2n':T{
                    Installs the asciidoctor gem from RubyGems.org
                    T}
                    .TE
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }
    }

    @Nested
    @DisplayName("Page breaks")
    class PageBreaks {

        @Test
        @DisplayName("should insert page break at location of page break macro")
        void shouldInsertPageBreakAtLocationOfPageBreakMacro() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n== Section With Break\n\nbefore break\n\n<<<\n\nafter break\n"));
            var expectedCoda = Strings.chop("""
                    .SH "SECTION WITH BREAK"
                    .sp
                    before break
                    .bp
                    .sp
                    after break
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }
    }

    @Nested
    @DisplayName("UI macros")
    class UIMacros {

        @Test
        @DisplayName("should enclose button in square brackets and format as bold")
        void shouldEncloseButtonInSquareBracketsAndFormatAsBold() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n== UI Macros\n\nbtn:[Save]\n"));
            var expectedCoda = Strings.chop("""
                    .SH "UI MACROS"
                    .sp
                    \\fB[\\0Save\\0]\\fP
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "attributes", Options.of("experimental", "")));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should format single key in monospaced text")
        void shouldFormatSingleKeyInMonospacedText() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n== UI Macros\n\nkbd:[Enter]\n"));
            var expectedCoda = Strings.chop("""
                    .SH "UI MACROS"
                    .sp
                    \\f(CREnter\\fP
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "attributes", Options.of("experimental", "")));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should format each key in sequence as monospaced text separated by +")
        void shouldFormatEachKeyInSequenceAsMonospacedTextSeparatedBy() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n== UI Macros\n\nkbd:[Ctrl,s]\n"));
            var expectedCoda = Strings.chop("""
                    .SH "UI MACROS"
                    .sp
                    \\f(CRCtrl\\0+\\0s\\fP
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "attributes", Options.of("experimental", "")));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should format single menu reference in italic")
        void shouldFormatSingleMenuReferenceInItalic() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n== UI Macros\n\nmenu:File[]\n"));
            var expectedCoda = Strings.chop("""
                    .SH "UI MACROS"
                    .sp
                    \\fIFile\\fP
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "attributes", Options.of("experimental", "")));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should format menu sequence in italic separated by carets")
        void shouldFormatMenuSequenceInItalicSeparatedByCarets() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n== UI Macros\n\nmenu:File[New Tab]\n"));
            var expectedCoda = Strings.chop("""
                    .SH "UI MACROS"
                    .sp
                    \\fIFile\\0\\(fc\\0New Tab\\fP
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "attributes", Options.of("experimental", "")));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should format menu sequence with submenu in italic separated by carets")
        void shouldFormatMenuSequenceWithSubmenuInItalicSeparatedByCarets() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\n== UI Macros\n\nmenu:View[Zoom > Zoom In]\n"));
            var expectedCoda = Strings.chop("""
                    .SH "UI MACROS"
                    .sp
                    \\fIView\\fP\\0\\(fc\\0\\fIZoom\\fP\\0\\(fc\\0\\fIZoom In\\fP
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "attributes", Options.of("experimental", "")));
            assertTruthy(endsWith(output, expectedCoda));
        }
    }

    @Nested
    @DisplayName("xrefs")
    class Xrefs {

        @Test
        @DisplayName("should populate automatic link text for internal xref")
        void shouldPopulateAutomaticLinkTextForInternalXref() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nYou can access this information using the options listed under <<_generic_program_information>>.\n\n== Options\n\n=== Generic Program Information\n\n--help:: Output a usage message and exit.\n\n-V, --version:: Output the version number of grep and exit.\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "attributes", Options.of("experimental", "")));
            assertIncludes(output, "You can access this information using the options listed under Generic Program Information.");
        }

        @Test
        @DisplayName("should populate automatic link text for each occurrence of internal xref")
        void shouldPopulateAutomaticLinkTextForEachOccurrenceOfInternalXref() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nYou can access this information using the options listed under <<_generic_program_information>>.\n\nThe options listed in <<_generic_program_information>> should always be used by themselves.\n\n== Options\n\n=== Generic Program Information\n\n--help:: Output a usage message and exit.\n\n-V, --version:: Output the version number of grep and exit.\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "attributes", Options.of("experimental", "")));
            assertIncludes(output, "You can access this information using the options listed under Generic Program Information.");
            assertIncludes(output, "The options listed in Generic Program Information should always be used by themselves.");
        }

        @Test
        @DisplayName("should uppercase the reftext for level-2 section titles if the reftext matches the section title")
        void shouldUppercaseTheReftextForLevel2SectionTitlesIfTheReftextMatchesTheSectionTitle() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\nIf you read nothing else, read the <<_foo_bar>> section.\n\n=== Options\n\n--foo-bar _foobar_::\nPuts the foo in your bar.\nSee <<_foo_bar>> section for details.\n\n== Foo Bar\n\nFoo goes with bar, not baz.\n"));
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "attributes", Options.of("experimental", "")));
            assertIncludes(output, "If you read nothing else, read the FOO BAR section.");
            assertIncludes(output, "See FOO BAR section for details.");
        }
    }

    @Nested
    @DisplayName("Footnotes")
    class Footnotes {

        @Test
        @DisplayName("should generate list of footnotes using numbered list with numbers enclosed in brackets")
        void shouldGenerateListOfFootnotesUsingNumberedListWithNumbersEnclosedInBrackets() throws Exception {
            for (var standalone : list(true, false)) {
                var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\ntext.footnote:[first footnote]\n\nmore text.footnote:[second footnote]\n"));
                var expectedCoda = Strings.chop("""
                        .sp
                        text.[1]
                        .sp
                        more text.[2]
                        .SH "NOTES"
                        .IP [1]
                        first footnote
                        .IP [2]
                        second footnote
                        """);
                if (isTruthy(standalone)) {
                    expectedCoda = Strings.chop((str(expectedCoda) + "\n.SH \"AUTHOR\"\n.sp\nAuthor Name\n"));
                }
                var output = Asciidoctor.convert(input, Options.of("backend", "manpage", "standalone", standalone));
                assertTruthy(endsWith(output, expectedCoda));
            }
        }

        @Test
        @DisplayName("should number footnotes according to footnote index")
        void shouldNumberFootnotesAccordingToFootnoteIndex() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\ntext.footnote:fn1[first footnote]footnote:[second footnote]\n\nmore text.footnote:fn1[]\n"));
            var expectedCoda = Strings.chop("""
                    .sp
                    text.[1][2]
                    .sp
                    more text.[1]
                    .SH "NOTES"
                    .IP [1]
                    first footnote
                    .IP [2]
                    second footnote
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should format footnote with bare URL")
        void shouldFormatFootnoteWithBareURL() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\ntext.footnote:[https://example.org]\n"));
            var expectedCoda = Strings.chop("""
                    .SH "NOTES"
                    .IP [1]
                    .URL "https://example.org" "" ""
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should format footnote with text before bare URL")
        void shouldFormatFootnoteWithTextBeforeBareURL() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\ntext.footnote:[see https://example.org]\n"));
            var expectedCoda = Strings.chop("""
                    .SH "NOTES"
                    .IP [1]
                    see \\c
                    .URL "https://example.org" "" ""
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should format footnote with text after bare URL")
        void shouldFormatFootnoteWithTextAfterBareURL() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\ntext.footnote:[https://example.org is the place]\n"));
            var expectedCoda = Strings.chop("""
                    .SH "NOTES"
                    .IP [1]
                    .URL "https://example.org" "" ""
                    is the place
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should format footnote with URL macro")
        void shouldFormatFootnoteWithURLMacro() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\ntext.footnote:[go to https://example.org[example site].]\n"));
            var expectedCoda = Strings.chop("""
                    .SH "NOTES"
                    .IP [1]
                    go to \\c
                    .URL "https://example.org" "example site" "."
                    """);
            var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
            assertTruthy(endsWith(output, expectedCoda));
        }

        @Test
        @DisplayName("should produce a warning message and output fallback text at location of macro of unresolved footnote")
        void shouldProduceAWarningMessageAndOutputFallbackTextAtLocationOfMacroOfUnresolvedFootnote() throws Exception {
            var input = Strings.chop((str(SAMPLE_MANPAGE_HEADER) + "\n\ntext.footnote:does-not-exist[]\n"));
            var expectedCoda = Strings.chop("""
                    .sp
                    text.[does\\-not\\-exist]
                    """);
            try (var logger = usingMemoryLogger()) {
                var output = Asciidoctor.convert(input, Options.of("backend", "manpage"));
                assertTruthy(endsWith(output, expectedCoda));
                assertMessage(logger, "WARN", "invalid footnote reference: does-not-exist");
            }
        }
    }

    @Nested
    @DisplayName("Environment")
    class Environment {

        @Test
        @Disabled("environment variables cannot be modified in Java")
        @DisplayName("should use SOURCE_DATE_EPOCH as modified time of input file and local time")
        void shouldUseSOURCE_DATE_EPOCHAsModifiedTimeOfInputFileAndLocalTime() throws Exception {
        }

        @Test
        @Disabled("environment variables cannot be modified in Java")
        @DisplayName("should fail if SOURCE_DATE_EPOCH is malformed")
        void shouldFailIfSOURCE_DATE_EPOCHIsMalformed() throws Exception {
        }
    }
}
