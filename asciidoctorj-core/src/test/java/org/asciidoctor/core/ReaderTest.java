package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.*;
import org.asciidoctor.core.extension.Group;
import java.nio.file.Path;
import java.nio.file.Files;
import org.asciidoctor.core.extension.Extensions;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.*;

public class ReaderTest extends TestHelper {
    static final String DIRNAME = testdir();
    static final java.util.List<String> SAMPLE_DATA = list("first line", "second line", "third line");

    @Nested
    @DisplayName("Reader")
    class ReaderTests {

        @Nested
        @DisplayName("Prepare lines")
        class PrepareLines {

            @Test
            @DisplayName("should prepare lines from Array data")
            void shouldPrepareLinesFromArrayData() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertEquals(SAMPLE_DATA, lines(reader));
            }

            @Test
            @DisplayName("should prepare lines from String data")
            void shouldPrepareLinesFromStringData() throws Exception {
                var reader = new Reader(join(SAMPLE_DATA, Asciidoctor.LF));
                assertEquals(SAMPLE_DATA, lines(reader));
            }

            @Test
            @DisplayName("should prepare lines from String data with trailing newline")
            void shouldPrepareLinesFromStringDataWithTrailingNewline() throws Exception {
                var reader = new Reader((join(SAMPLE_DATA, Asciidoctor.LF) + Asciidoctor.LF));
                assertEquals(SAMPLE_DATA, lines(reader));
            }

            @Test
            @DisplayName("should remove UTF-8 BOM from first line of String data")
            void shouldRemoveUTF8BOMFromFirstLineOfStringData() throws Exception {
                var data = "\uFEFF" + String.join(Asciidoctor.LF, SAMPLE_DATA);
                for (var input : list(data, data.getBytes(StandardCharsets.UTF_8))) {
                    var reader = new Reader(input, null, true);
                    assertEquals("f", Strings.chr(lines(reader).get(0)));
                    assertEquals(SAMPLE_DATA, lines(reader));
                }
            }

            @Test
            @DisplayName("should remove UTF-8 BOM from first line of Array data")
            void shouldRemoveUTF8BOMFromFirstLineOfArrayData() throws Exception {
                var data = new ArrayList<>(SAMPLE_DATA);
                data.set(0, "\uFEFF" + data.get(0));
                var reader = new Reader(data, null, true);
                assertEquals("f", Strings.chr(lines(reader).get(0)));
                assertEquals(SAMPLE_DATA, lines(reader));
            }

            @Test
            @DisplayName("should encode UTF-16LE string to UTF-8 when BOM is found")
            void shouldEncodeUTF16LEStringToUTF8WhenBOMIsFound() throws Exception {
                var data = ("\uFEFF" + String.join(Asciidoctor.LF, SAMPLE_DATA)).getBytes(StandardCharsets.UTF_16LE);
                var reader = new Reader(data, null, true);
                assertEquals("f", Strings.chr(lines(reader).get(0)));
                assertEquals(SAMPLE_DATA, lines(reader));
            }

            @Test
            @Disabled("Java strings are Unicode; only byte array data is decoded")
            @DisplayName("should encode UTF-16LE string array to UTF-8 when BOM is found")
            void shouldEncodeUTF16LEStringArrayToUTF8WhenBOMIsFound() throws Exception {
            }

            @Test
            @DisplayName("should encode UTF-16BE string to UTF-8 when BOM is found")
            void shouldEncodeUTF16BEStringToUTF8WhenBOMIsFound() throws Exception {
                var data = ("\uFEFF" + String.join(Asciidoctor.LF, SAMPLE_DATA)).getBytes(StandardCharsets.UTF_16BE);
                var reader = new Reader(data, null, true);
                assertEquals("f", Strings.chr(lines(reader).get(0)));
                assertEquals(SAMPLE_DATA, lines(reader));
            }

            @Test
            @Disabled("Java strings are Unicode; only byte array data is decoded")
            @DisplayName("should encode UTF-16BE string array to UTF-8 when BOM is found")
            void shouldEncodeUTF16BEStringArrayToUTF8WhenBOMIsFound() throws Exception {
            }
        }

        @Nested
        @DisplayName("With empty data")
        class WithEmptyData {

            @Test
            @DisplayName("has_more_lines? should return false with empty data")
            void has_more_linesShouldReturnFalseWithEmptyData() throws Exception {
                assertFalsy(new Reader().hasMoreLines());
            }

            @Test
            @DisplayName("empty? should return true with empty data")
            void emptyShouldReturnTrueWithEmptyData() throws Exception {
                assertEmpty(new Reader());
                assertTruthy(new Reader().isEof());
            }

            @Test
            @DisplayName("next_line_empty? should return true with empty data")
            void next_line_emptyShouldReturnTrueWithEmptyData() throws Exception {
                assertTruthy(new Reader().isNextLineEmpty());
            }

            @Test
            @DisplayName("peek_line should return nil with empty data")
            void peek_lineShouldReturnNilWithEmptyData() throws Exception {
                assertNull(new Reader().peekLine());
            }

            @Test
            @DisplayName("peek_lines should return empty Array with empty data")
            void peek_linesShouldReturnEmptyArrayWithEmptyData() throws Exception {
                assertEmpty(new Reader().peekLines(1));
            }

            @Test
            @DisplayName("read_line should return nil with empty data")
            void read_lineShouldReturnNilWithEmptyData() throws Exception {
                assertNull(new Reader().readLine());
            }

            @Test
            @DisplayName("read_lines should return empty Array with empty data")
            void read_linesShouldReturnEmptyArrayWithEmptyData() throws Exception {
                assertEmpty(new Reader().readLines());
            }
        }

        @Nested
        @DisplayName("With data")
        class WithData {

            @Test
            @DisplayName("has_more_lines? should return true if there are lines remaining")
            void has_more_linesShouldReturnTrueIfThereAreLinesRemaining() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertTruthy(reader.hasMoreLines());
            }

            @Test
            @DisplayName("empty? should return false if there are lines remaining")
            void emptyShouldReturnFalseIfThereAreLinesRemaining() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                refuteEmpty(reader);
                assertFalsy(reader.isEof());
            }

            @Test
            @DisplayName("next_line_empty? should return false if next line is not blank")
            void next_line_emptyShouldReturnFalseIfNextLineIsNotBlank() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertFalsy(reader.isNextLineEmpty());
            }

            @Test
            @DisplayName("next_line_empty? should return true if next line is blank")
            void next_line_emptyShouldReturnTrueIfNextLineIsBlank() throws Exception {
                var reader = new Reader(list("", "second line"));
                assertTruthy(reader.isNextLineEmpty());
            }

            @Test
            @DisplayName("peek_line should return nil if next entry is nil")
            void peek_lineShouldReturnNilIfNextEntryIsNil() throws Exception {
                assertNull(new Reader(list((Object) null)).peekLine());
            }

            @Test
            @DisplayName("peek_line should return next line if there are lines remaining")
            void peek_lineShouldReturnNextLineIfThereAreLinesRemaining() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertEquals(first(SAMPLE_DATA), reader.peekLine());
            }

            @Test
            @DisplayName("peek_line should not consume line or increment line number")
            void peek_lineShouldNotConsumeLineOrIncrementLineNumber() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertEquals(first(SAMPLE_DATA), reader.peekLine());
                assertEquals(first(SAMPLE_DATA), reader.peekLine());
                assertEquals(1, reader.getLineNumber());
            }

            @Test
            @DisplayName("peek_line should return next lines if there are lines remaining")
            void peek_lineShouldReturnNextLinesIfThereAreLinesRemaining() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertEquals(slice(SAMPLE_DATA, range(0, 1)), reader.peekLines(2));
            }

            @Test
            @DisplayName("peek_lines should not consume lines or increment line number")
            void peek_linesShouldNotConsumeLinesOrIncrementLineNumber() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertEquals(slice(SAMPLE_DATA, range(0, 1)), reader.peekLines(2));
                assertEquals(slice(SAMPLE_DATA, range(0, 1)), reader.peekLines(2));
                assertEquals(1, reader.getLineNumber());
            }

            @Test
            @DisplayName("peek_lines should not increment line number if reader overruns buffer")
            void peek_linesShouldNotIncrementLineNumberIfReaderOverrunsBuffer() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertEquals(SAMPLE_DATA, reader.peekLines((size(SAMPLE_DATA) * 2)));
                assertEquals(1, reader.getLineNumber());
            }

            @Test
            @DisplayName("peek_lines should peek all lines if no arguments are given")
            void peek_linesShouldPeekAllLinesIfNoArgumentsAreGiven() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertEquals(SAMPLE_DATA, reader.peekLines());
                assertEquals(1, reader.getLineNumber());
            }

            @Test
            @DisplayName("peek_lines should not invert order of lines")
            void peek_linesShouldNotInvertOrderOfLines() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertEquals(SAMPLE_DATA, lines(reader));
                reader.peekLines(3);
                assertEquals(SAMPLE_DATA, lines(reader));
            }

            @Test
            @DisplayName("read_line should return next line if there are lines remaining")
            void read_lineShouldReturnNextLineIfThereAreLinesRemaining() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertEquals(first(SAMPLE_DATA), reader.readLine());
            }

            @Test
            @DisplayName("read_line should consume next line and increment line number")
            void read_lineShouldConsumeNextLineAndIncrementLineNumber() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertEquals(SAMPLE_DATA.get(0), reader.readLine());
                assertEquals(SAMPLE_DATA.get(1), reader.readLine());
                assertEquals(3, reader.getLineNumber());
            }

            @Test
            @DisplayName("advance should consume next line and return a Boolean indicating if a line was consumed")
            void advanceShouldConsumeNextLineAndReturnABooleanIndicatingIfALineWasConsumed() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertTruthy(reader.advance());
                assertTruthy(reader.advance());
                assertTruthy(reader.advance());
                assertFalsy(reader.advance());
            }

            @Test
            @DisplayName("read_lines should return all lines")
            void read_linesShouldReturnAllLines() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertEquals(SAMPLE_DATA, reader.readLines());
            }

            @Test
            @DisplayName("read should return all lines joined as String")
            void readShouldReturnAllLinesJoinedAsString() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                assertEquals(join(SAMPLE_DATA, Asciidoctor.LF), reader.read());
            }

            @Test
            @DisplayName("has_more_lines? should return false after read_lines is invoked")
            void has_more_linesShouldReturnFalseAfterRead_linesIsInvoked() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                reader.readLines();
                assertFalsy(reader.hasMoreLines());
            }

            @Test
            @DisplayName("unshift puts line onto Reader as next line to read")
            void unshiftPutsLineOntoReaderAsNextLineToRead() throws Exception {
                var reader = new Reader(SAMPLE_DATA, null, Options.of("normalize", true));
                reader.unshift("line zero");
                assertEquals("line zero", reader.peekLine());
                assertEquals("line zero", reader.readLine());
                assertEquals(1, reader.getLineNumber());
            }

            @Test
            @DisplayName("terminate should consume all lines and update line number")
            void terminateShouldConsumeAllLinesAndUpdateLineNumber() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                reader.terminate();
                assertTruthy(reader.isEof());
                assertEquals(4, reader.getLineNumber());
            }

            @Test
            @DisplayName("skip_blank_lines should skip blank lines")
            void skip_blank_linesShouldSkipBlankLines() throws Exception {
                var reader = new Reader(concat(list("", ""), SAMPLE_DATA));
                reader.skipBlankLines();
                assertEquals(first(SAMPLE_DATA), reader.peekLine());
            }

            @Test
            @DisplayName("lines should return remaining lines")
            void linesShouldReturnRemainingLines() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                reader.readLine();
                assertEquals(slice(SAMPLE_DATA, range(1, -1)), lines(reader));
            }

            @Test
            @DisplayName("source_lines should return copy of original data Array")
            void source_linesShouldReturnCopyOfOriginalDataArray() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                reader.readLines();
                assertEquals(SAMPLE_DATA, reader.getSourceLines());
            }

            @Test
            @DisplayName("source should return original data Array joined as String")
            void sourceShouldReturnOriginalDataArrayJoinedAsString() throws Exception {
                var reader = new Reader(SAMPLE_DATA);
                reader.readLines();
                assertEquals(join(SAMPLE_DATA, Asciidoctor.LF), reader.getSource());
            }
        }

        @Nested
        @DisplayName("Line context")
        class LineContext {

            @Test
            @DisplayName("cursor.to_s should return file name and line number of current line")
            void cursorTo_sShouldReturnFileNameAndLineNumberOfCurrentLine() throws Exception {
                var reader = new Reader(SAMPLE_DATA, "sample.adoc");
                reader.readLine();
                assertEquals("sample.adoc: line 2", str(reader.cursor()));
            }

            @Test
            @DisplayName("line_info should return file name and line number of current line")
            void line_infoShouldReturnFileNameAndLineNumberOfCurrentLine() throws Exception {
                var reader = new Reader(SAMPLE_DATA, "sample.adoc");
                reader.readLine();
                assertEquals("sample.adoc: line 2", reader.getLineInfo());
            }

            @Test
            @DisplayName("cursor_at_prev_line should return file name and line number of previous line read")
            void cursor_at_prev_lineShouldReturnFileNameAndLineNumberOfPreviousLineRead() throws Exception {
                var reader = new Reader(SAMPLE_DATA, "sample.adoc");
                reader.readLine();
                assertEquals("sample.adoc: line 1", str(reader.cursorAtPrevLine()));
            }
        }

        @Nested
        @DisplayName("Read lines until")
        class ReadLinesUntil {

            @Test
            @DisplayName("Read lines until until end")
            void readLinesUntilUntilEnd() throws Exception {
                var lines = lines("""
                        This is one paragraph.

                        This is another paragraph.
                        """);
                var reader = new Reader(lines, null, Options.of("normalize", true));
                var result = reader.readLinesUntil(Reader.ReadOptions.create());
                assertEquals(3, size(result));
                assertEquals(map(lines, (String it) -> Strings.chomp(it)), result);
                assertFalsy(reader.hasMoreLines());
                assertTruthy(reader.isEof());
            }

            @Test
            @DisplayName("Read lines until until blank line")
            void readLinesUntilUntilBlankLine() throws Exception {
                var lines = lines("""
                        This is one paragraph.

                        This is another paragraph.
                        """);
                var reader = new Reader(lines, null, Options.of("normalize", true));
                var result = reader.readLinesUntil(Reader.ReadOptions.create().breakOnBlankLines());
                assertEquals(1, size(result));
                assertEquals(Strings.chomp(first(lines)), first(result));
                assertEquals(Strings.chomp(last(lines)), reader.peekLine());
            }

            @Test
            @DisplayName("Read lines until until blank line preserving last line")
            void readLinesUntilUntilBlankLinePreservingLastLine() throws Exception {
                var lines = split("""
                        This is one paragraph.

                        This is another paragraph.
                        """, Asciidoctor.LF);
                var reader = new Reader(lines);
                var result = reader.readLinesUntil(Reader.ReadOptions.create().breakOnBlankLines().preserveLastLine());
                assertEquals(1, size(result));
                assertEquals(Strings.chomp(first(lines)), first(result));
                assertTruthy(reader.isNextLineEmpty());
            }

            @Test
            @DisplayName("Read lines until until condition is true")
            void readLinesUntilUntilConditionIsTrue() throws Exception {
                var lines = split("""
                        --
                        This is one paragraph inside the block.

                        This is another paragraph inside the block.
                        --

                        This is a paragraph outside the block.
                        """, Asciidoctor.LF);
                var reader = new Reader(lines);
                reader.readLine();
                var result = reader.readLinesUntil(Reader.ReadOptions.create(), line -> Objects.equals(line, "--"));
                assertEquals(3, size(result));
                assertEquals(slice(lines, 1, 3), result);
                assertTruthy(reader.isNextLineEmpty());
            }

            @Test
            @DisplayName("Read lines until until condition is true, taking last line")
            void readLinesUntilUntilConditionIsTrueTakingLastLine() throws Exception {
                var lines = split("""
                        --
                        This is one paragraph inside the block.

                        This is another paragraph inside the block.
                        --

                        This is a paragraph outside the block.
                        """, Asciidoctor.LF);
                var reader = new Reader(lines);
                reader.readLine();
                var result = reader.readLinesUntil(Reader.ReadOptions.create().readLastLine(), line -> Objects.equals(line, "--"));
                assertEquals(4, size(result));
                assertEquals(slice(lines, 1, 4), result);
                assertTruthy(reader.isNextLineEmpty());
            }

            @Test
            @DisplayName("Read lines until until condition is true, taking and preserving last line")
            void readLinesUntilUntilConditionIsTrueTakingAndPreservingLastLine() throws Exception {
                var lines = split("""
                        --
                        This is one paragraph inside the block.

                        This is another paragraph inside the block.
                        --

                        This is a paragraph outside the block.
                        """, Asciidoctor.LF);
                var reader = new Reader(lines);
                reader.readLine();
                var result = reader.readLinesUntil(Reader.ReadOptions.create().readLastLine().preserveLastLine(), line -> Objects.equals(line, "--"));
                assertEquals(4, size(result));
                assertEquals(slice(lines, 1, 4), result);
                assertEquals("--", reader.peekLine());
            }

            @Test
            @DisplayName("read lines until terminator")
            void readLinesUntilTerminator() throws Exception {
                var lines = lines("""
                        ****
                        captured

                        also captured
                        ****

                        not captured
                        """);
                var expected = list("captured", "", "also captured");
                var doc = emptySafeDocument(Options.of("base_dir", DIRNAME));
                var reader = new PreprocessorReader(doc, lines, null, Options.of("normalize", true));
                var terminator = reader.readLine();
                var result = reader.readLinesUntil(Reader.ReadOptions.create().terminator(terminator).skipProcessing());
                assertEquals(expected, result);
                assertFalsy(reader.getUnterminated());
            }

            @Test
            @DisplayName("should flag reader as unterminated if reader reaches end of source without finding terminator")
            void shouldFlagReaderAsUnterminatedIfReaderReachesEndOfSourceWithoutFindingTerminator() throws Exception {
                var lines = lines("""
                        ****
                        captured

                        also captured

                        captured yet again
                        """);
                var expected = map(slice(lines, range(1, -1)), (String it) -> Strings.chomp(it));
                try (var logger = usingMemoryLogger()) {
                    var doc = emptySafeDocument(Options.of("base_dir", DIRNAME));
                    var reader = new PreprocessorReader(doc, lines, null, Options.of("normalize", true));
                    var terminator = reader.peekLine();
                    var result = reader.readLinesUntil(Reader.ReadOptions.create().terminator(terminator).skipFirstLine().skipProcessing());
                    assertEquals(expected, result);
                    assertTruthy(reader.getUnterminated());
                    assertMessage(logger, "WARN", "<stdin>: line 1: unterminated **** block", LogMessage.class);
                }
            }
        }
    }

    @Nested
    @DisplayName("PreprocessorReader")
    class PreprocessorReaderTests {

        @Nested
        @DisplayName("Type hierarchy")
        class TypeHierarchy {

            @Test
            @DisplayName("PreprocessorReader should extend from Reader")
            void preprocessorreaderShouldExtendFromReader() throws Exception {
                var reader = (PreprocessorReader) emptyDocument().getReader();
                assertInstanceOf(PreprocessorReader.class, reader);
            }

            @Test
            @DisplayName("PreprocessorReader should invoke or emulate Reader initializer")
            void preprocessorreaderShouldInvokeOrEmulateReaderInitializer() throws Exception {
                var doc = new Document(SAMPLE_DATA);
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals(SAMPLE_DATA, lines(reader));
                assertEquals(1, reader.getLineNumber());
            }
        }

        @Nested
        @DisplayName("Prepare lines")
        class PrepareLines2 {

            @Test
            @DisplayName("should prepare and normalize lines from Array data")
            void shouldPrepareAndNormalizeLinesFromArrayData() throws Exception {
                var data = drop(SAMPLE_DATA, 0);
                data.add(0, "");
                data.add("");
                var doc = new Document(data);
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals(plus(list(""), SAMPLE_DATA), lines(reader));
            }

            @Test
            @DisplayName("should prepare and normalize lines from String data")
            void shouldPrepareAndNormalizeLinesFromStringData() throws Exception {
                var data = drop(SAMPLE_DATA, 0);
                data.add(0, " ");
                data.add(" ");
                var dataAsString = join(data, Asciidoctor.LF);
                var doc = new Document(dataAsString);
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals(plus(list(""), SAMPLE_DATA), lines(reader));
            }

            @Test
            @DisplayName("should drop all lines if all lines are empty")
            void shouldDropAllLinesIfAllLinesAreEmpty() throws Exception {
                var data = list("", " ", "", " ");
                var doc = new Document(data);
                var reader = (PreprocessorReader) doc.getReader();
                assertEmpty(lines(reader));
            }

            @Test
            @DisplayName("should clean CRLF from end of lines")
            void shouldCleanCRLFFromEndOfLines() throws Exception {
                var input = """
                        source\u000d
                        with\u000d
                        CRLF\u000d
                        line endings\u000d
                        """;
                for (var lines : list(input, lines(input), split(input, Asciidoctor.LF), join(split(input, Asciidoctor.LF), Asciidoctor.LF))) {
                    var doc = new Document(lines);
                    var reader = (PreprocessorReader) doc.getReader();
                    for (var line : lines(reader)) {
                        assertFalsy(endsWith(line, "\r"), ("CRLF not properly cleaned for source lines: " + str(Strings.inspect(lines))));
                        assertFalsy(endsWith(line, "\r\n"), ("CRLF not properly cleaned for source lines: " + str(Strings.inspect(lines))));
                        assertFalsy(endsWith(line, "\n"), ("CRLF not properly cleaned for source lines: " + str(Strings.inspect(lines))));
                    }
                }
            }

            @Test
            @DisplayName("should not skip front matter by default")
            void shouldNotSkipFrontMatterByDefault() throws Exception {
                var input = """
                        ---
                        layout: post
                        title: Document Title
                        author: username
                        tags: [ first, second ]
                        ---
                        = Document Title
                        Author Name

                        preamble
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                assertFalsy(doc.getAttributes().containsKey("front-matter"));
                assertEquals("---", reader.peekLine());
                assertEquals(1, reader.getLineNumber());
            }

            @Test
            @DisplayName("should not skip front matter if ending delimiter is not found")
            void shouldNotSkipFrontMatterIfEndingDelimiterIsNotFound() throws Exception {
                var input = """
                        ---
                        title: Document Title
                        tags: [ first, second ]
                        = Document Title
                        Author Name

                        preamble
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("skip-front-matter", "")));
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals("---", reader.peekLine());
                assertFalsy(doc.getAttributes().containsKey("front-matter"));
                assertEquals(1, reader.getLineNumber());
            }

            @Test
            @DisplayName("should skip front matter if specified by skip-front-matter attribute")
            void shouldSkipFrontMatterIfSpecifiedBySkipFrontMatterAttribute() throws Exception {
                var frontMatter = Strings.chop("""
                        layout: post
                        title: Document Title
                        author: username
                        tags: [ first, second ]
                        """);
                var input = ("---\n" + str(frontMatter) + "\n---\n= Document Title\nAuthor Name\n\npreamble\n");
                var doc = new Document(input, Options.of("attributes", Options.of("skip-front-matter", "")));
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals("= Document Title", reader.peekLine());
                assertEquals(frontMatter, get(doc.getAttributes(), "front-matter"));
                assertEquals(7, reader.getLineNumber());
            }

            @Test
            @DisplayName("should skip TOML front matter if specified by skip-front-matter attribute")
            void shouldSkipTOMLFrontMatterIfSpecifiedBySkipFrontMatterAttribute() throws Exception {
                var frontMatter = Strings.chop("""
                        layout = 'post'
                        title = 'Document Title'
                        author = 'username'
                        tags = ['first', 'second']
                        """);
                var input = ("+++\n" + str(frontMatter) + "\n+++\n= Document Title\nAuthor Name\n\npreamble\n");
                var doc = new Document(input, Options.of("attributes", Options.of("skip-front-matter", "")));
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals("= Document Title", reader.peekLine());
                assertEquals(frontMatter, get(doc.getAttributes(), "front-matter"));
                assertEquals(7, reader.getLineNumber());
            }

            @Test
            @DisplayName("should not skip front matter in include file if skip-front-matter attribute is set")
            void shouldNotSkipFrontMatterInIncludeFileIfSkipFrontMatterAttributeIsSet() throws Exception {
                var input = """
                        ....
                        include::fixtures/with-front-matter.adoc[]
                        ....
                        """;
                var doc = documentFromString(input, Options.of("safe", "safe", "parse", false, "base_dir", DIRNAME));
                var reader = (PreprocessorReader) doc.getReader();
                var expected = list("....", "---", "name: value", "---", "content", "....");
                assertEquals(expected, reader.readLines());
                assertFalsy(doc.hasAttribute("front-matter"));
            }

            @Test
            @DisplayName("should skip front matter in include file if skip-front-matter option is set on include directiv")
            void shouldSkipFrontMatterInIncludeFileIfSkipFrontMatterOptionIsSetOnIncludeDirectiv() throws Exception {
                var input = """
                        ....
                        include::fixtures/with-front-matter.adoc[opts=skip-front-matter]
                        ....
                        """;
                var doc = documentFromString(input, Options.of("safe", "safe", "parse", false, "base_dir", DIRNAME));
                var reader = (PreprocessorReader) doc.getReader();
                var expected = list("....", "content", "....");
                assertEquals(expected, reader.readLines());
                assertFalsy(doc.hasAttribute("front-matter"));
            }
        }

        @Nested
        @DisplayName("Include Stack")
        class IncludeStack {

            @Test
            @DisplayName("PreprocessorReader#push_include method should return reader")
            void preprocessorreaderPush_includeMethodShouldReturnReader() throws Exception {
                var reader = (PreprocessorReader) emptyDocument().getReader();
                var appendLines = list("one", "two", "three");
                var result = reader.pushInclude(appendLines, "<stdin>", "<stdin>");
                assertEquals(reader, result);
            }

            @Test
            @DisplayName("PreprocessorReader#push_include method should put lines on top of stack")
            void preprocessorreaderPush_includeMethodShouldPutLinesOnTopOfStack() throws Exception {
                var lines = list("a", "b", "c");
                var doc = new Document(lines);
                var reader = (PreprocessorReader) doc.getReader();
                var appendLines = list("one", "two", "three");
                reader.pushInclude(appendLines, "", "<stdin>");
                assertEquals(1, size(reader.getIncludeStack()));
                assertEquals("one", Strings.rstrip(reader.readLine()));
            }

            @Test
            @DisplayName("PreprocessorReader#push_include method should gracefully handle file and path")
            void preprocessorreaderPush_includeMethodShouldGracefullyHandleFileAndPath() throws Exception {
                var lines = list("a", "b", "c");
                var doc = new Document(lines);
                var reader = (PreprocessorReader) doc.getReader();
                var appendLines = list("one", "two", "three");
                reader.pushInclude(appendLines);
                assertEquals(1, size(reader.getIncludeStack()));
                assertEquals("one", Strings.rstrip(reader.readLine()));
                assertNull(reader.getFile());
                assertEquals("<stdin>", reader.getPath());
            }

            @Test
            @DisplayName("PreprocessorReader#push_include method should set path from file automatically if not specified")
            void preprocessorreaderPush_includeMethodShouldSetPathFromFileAutomaticallyIfNotSpecified() throws Exception {
                var lines = list("a", "b", "c");
                var doc = new Document(lines);
                var reader = (PreprocessorReader) doc.getReader();
                var appendLines = list("one", "two", "three");
                reader.pushInclude(appendLines, "/tmp/lines.adoc");
                assertEquals("/tmp/lines.adoc", reader.getFile());
                assertEquals("lines.adoc", reader.getPath());
                assertTruthy(get(doc.getCatalog().getIncludes(), "lines"));
            }

            @Test
            @DisplayName("PreprocessorReader#push_include method should accept file as a URI and compute dir and path")
            void preprocessorreaderPush_includeMethodShouldAcceptFileAsAURIAndComputeDirAndPath() throws Exception {
                var fileUri = "http://example.com/docs/file.adoc";
                var dirUri = "http://example.com/docs";
                var reader = (PreprocessorReader) emptyDocument().getReader();
                reader.pushInclude(list("one", "two", "three"), fileUri);
                assertSame(fileUri, reader.getFile());
                assertEquals(dirUri, reader.getDir());
                assertEquals("file.adoc", reader.getPath());
            }

            @Test
            @DisplayName("PreprocessorReader#push_include method should accept file as a top-level URI and compute dir and path")
            void preprocessorreaderPush_includeMethodShouldAcceptFileAsATopLevelURIAndComputeDirAndPath() throws Exception {
                var fileUri = "http://example.com/index.adoc";
                var dirUri = "http://example.com";
                var reader = (PreprocessorReader) emptyDocument().getReader();
                reader.pushInclude(list("one", "two", "three"), fileUri);
                assertSame(fileUri, reader.getFile());
                assertEquals(dirUri, reader.getDir());
                assertEquals("index.adoc", reader.getPath());
            }

            @Test
            @DisplayName("PreprocessorReader#push_include method should not fail if data is nil")
            void preprocessorreaderPush_includeMethodShouldNotFailIfDataIsNil() throws Exception {
                var lines = list("a", "b", "c");
                var doc = new Document(lines);
                var reader = (PreprocessorReader) doc.getReader();
                reader.pushInclude(null, "", "<stdin>");
                assertEquals(0, size(reader.getIncludeStack()));
                assertEquals("a", Strings.rstrip(reader.readLine()));
            }

            @Test
            @DisplayName("PreprocessorReader#push_include method should ignore dot in directory name when computing include path")
            void preprocessorreaderPush_includeMethodShouldIgnoreDotInDirectoryNameWhenComputingIncludePath() throws Exception {
                var lines = list("a", "b", "c");
                var doc = new Document(lines);
                var reader = (PreprocessorReader) doc.getReader();
                var appendLines = list("one", "two", "three");
                reader.pushInclude(appendLines, null, "include.d/data");
                assertNull(reader.getFile());
                assertEquals("include.d/data", reader.getPath());
                assertTruthy(get(doc.getCatalog().getIncludes(), "include.d/data"));
            }
        }

        @Nested
        @DisplayName("Include Directive")
        class IncludeDirective {

            @Test
            @DisplayName("should replace include directive with link macro in default safe mode")
            void shouldReplaceIncludeDirectiveWithLinkMacroInDefaultSafeMode() throws Exception {
                var input = "include::include-file.adoc[]";
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals("link:include-file.adoc[role=include]", reader.readLine());
            }

            @Test
            @DisplayName("should not add role to link macro used to replace include directive in compat mode")
            void shouldNotAddRoleToLinkMacroUsedToReplaceIncludeDirectiveInCompatMode() throws Exception {
                var input = "include::include-file.adoc[]";
                var doc = new Document(input, Options.of("attributes", Options.of("compat-mode", "")));
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals("link:include-file.adoc[]", reader.readLine());
            }

            @Test
            @DisplayName("should escape spaces in target when generating link from include directive")
            void shouldEscapeSpacesInTargetWhenGeneratingLinkFromIncludeDirective() throws Exception {
                var input = "include::foo bar baz.adoc[]";
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals("link:pass:c[foo bar baz.adoc][role=include]", reader.readLine());
            }

            @Test
            @DisplayName("should preserve attrlist when replacing include directive with link macro")
            void shouldPreserveAttrlistWhenReplacingIncludeDirectiveWithLinkMacro() throws Exception {
                var input = "include::include-file.adoc[leveloffset=+1]";
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals("link:include-file.adoc[role=include,leveloffset=+1]", reader.readLine());
            }

            @Test
            @DisplayName("should replace include directive with link macro if safe mode allows it, but allow-uri-read is not set")
            void shouldReplaceIncludeDirectiveWithLinkMacroIfSafeModeAllowsItButAllowUriReadIsNotSet() throws Exception {
                try (var logger = usingMemoryLogger()) {
                    var input = "include::https://example.org/dist/info.adoc[]";
                    var doc = new Document(input, Options.of("safe", "safe"));
                    var reader = (PreprocessorReader) doc.getReader();
                    assertEquals("link:https://example.org/dist/info.adoc[role=include]", reader.readLine());
                    assertMessage(logger, "WARN", "<stdin>: line 1: cannot include contents of URI: https://example.org/dist/info.adoc (allow-uri-read attribute not enabled)", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should not add role to link macro that replaces include directive with remote target in compat mode")
            void shouldNotAddRoleToLinkMacroThatReplacesIncludeDirectiveWithRemoteTargetInCompatMode() throws Exception {
                var input = "include::https://example.org/dist/info.adoc[]";
                try (var logger = usingMemoryLogger()) {
                    var doc = new Document(input, Options.of("safe", "safe", "attributes", Options.of("compat-mode", "")));
                    var reader = (PreprocessorReader) doc.getReader();
                    assertEquals("link:https://example.org/dist/info.adoc[]", reader.readLine());
                    assertMessage(logger, "WARN", "<stdin>: line 1: cannot include contents of URI: https://example.org/dist/info.adoc (allow-uri-read attribute not enabled)", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should escape spaces in target when generating link from remote include directive")
            void shouldEscapeSpacesInTargetWhenGeneratingLinkFromRemoteIncludeDirective() throws Exception {
                try (var logger = usingMemoryLogger()) {
                    var input = "include::https://example.org/no such file.adoc[]";
                    var doc = new Document(input, Options.of("safe", "safe"));
                    var reader = (PreprocessorReader) doc.getReader();
                    assertEquals("link:pass:c[https://example.org/no such file.adoc][role=include]", reader.readLine());
                    assertMessage(logger, "WARN", "<stdin>: line 1: cannot include contents of URI: https://example.org/no such file.adoc (allow-uri-read attribute not enabled)", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should preserve attrlist when replacing remove include directive with link macro")
            void shouldPreserveAttrlistWhenReplacingRemoveIncludeDirectiveWithLinkMacro() throws Exception {
                try (var logger = usingMemoryLogger()) {
                    var input = "include::https://example.org/dist/info.adoc[leveloffset=+1]";
                    var doc = new Document(input, Options.of("safe", "safe"));
                    var reader = (PreprocessorReader) doc.getReader();
                    assertEquals("link:https://example.org/dist/info.adoc[role=include,leveloffset=+1]", reader.readLine());
                    assertMessage(logger, "WARN", "<stdin>: line 1: cannot include contents of URI: https://example.org/dist/info.adoc (allow-uri-read attribute not enabled)", LogMessage.class);
                }
            }

            @Test
            @DisplayName("include directive with remote target is converted to a link when allow-uri-read is not set")
            void includeDirectiveWithRemoteTargetIsConvertedToALinkWhenAllowUriReadIsNotSet() throws Exception {
                try (var logger = usingMemoryLogger()) {
                    var input = "include::http://example.org/team.adoc[]";
                    var doc = new Document(input, Options.of("safe", "safe"));
                    var reader = (PreprocessorReader) doc.getReader();
                    assertEquals("link:http://example.org/team.adoc[role=include]", reader.readLine());
                    assertMessage(logger, "WARN", "<stdin>: line 1: cannot include contents of URI: http://example.org/team.adoc (allow-uri-read attribute not enabled)", LogMessage.class);
                }
            }

            @Test
            @DisplayName("include directive with remote target is converted to a link when safe mode is secure")
            void includeDirectiveWithRemoteTargetIsConvertedToALinkWhenSafeModeIsSecure() throws Exception {
                try (var logger = usingMemoryLogger()) {
                    var input = "include::http://example.org/team.adoc[]";
                    var doc = new Document(input, Options.of("safe", "secure"));
                    var reader = (PreprocessorReader) doc.getReader();
                    assertEquals("link:http://example.org/team.adoc[role=include]", reader.readLine());
                    assertEmpty(logger.getMessages());
                }
            }

            @Test
            @DisplayName("include directive is enabled when safe mode is less than SECURE")
            void includeDirectiveIsEnabledWhenSafeModeIsLessThanSECURE() throws Exception {
                var input = "include::fixtures/include-file.adoc[]";
                var doc = documentFromString(input, Options.of("safe", "safe", "standalone", false, "base_dir", DIRNAME));
                var output = doc.convert();
                assertMatch(rx("included content"), output);
                assertTruthy(get(doc.getCatalog().getIncludes(), "fixtures/include-file"));
            }

            @Test
            @DisplayName("should strip BOM from include file")
            void shouldStripBOMFromIncludeFile() throws Exception {
                var input = ":showtitle:\ninclude::fixtures/file-with-utf8-bom.adoc[]";
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertCss(".paragraph", output, 0);
                assertCss("h1", output, 1);
                assertMatch(rx("<h1>人</h1>"), output);
            }

            @Test
            @Disabled("uri:classloader is specific to JRuby")
            @DisplayName("should include content from a file on the classloader")
            void shouldIncludeContentFromAFileOnTheClassloader() throws Exception {
            }

            @Test
            @DisplayName("should not track include in catalog for non-AsciiDoc include files")
            void shouldNotTrackIncludeInCatalogForNonAsciiDocIncludeFiles() throws Exception {
                var input = """
                        ----
                        include::fixtures/circle.svg[]
                        ----
                        """;
                var doc = documentFromString(input, Options.of("safe", "safe", "standalone", false, "base_dir", DIRNAME));
                assertEmpty(doc.getCatalog().getIncludes());
            }

            @Test
            @DisplayName("include directive should resolve file with spaces in name")
            void includeDirectiveShouldResolveFileWithSpacesInName() throws Exception {
                var input = "include::fixtures/include file.adoc[]";
                var includeFile = joinPath(DIRNAME, "fixtures", "include-file.adoc");
                var includeFileWithSp = joinPath(DIRNAME, "fixtures", "include file.adoc");
                try {
                    copyFile(includeFile, includeFileWithSp);
                    var doc = documentFromString(input, Options.of("safe", "safe", "standalone", false, "base_dir", DIRNAME));
                    var output = doc.convert();
                    assertMatch(rx("included content"), output);
                } finally {
                    removeFile(includeFileWithSp);
                }
            }

            @Test
            @DisplayName("include directive should resolve file with {sp} in name")
            void includeDirectiveShouldResolveFileWithSpInName() throws Exception {
                var input = "include::fixtures/include{sp}file.adoc[]";
                var includeFile = joinPath(DIRNAME, "fixtures", "include-file.adoc");
                var includeFileWithSp = joinPath(DIRNAME, "fixtures", "include file.adoc");
                try {
                    copyFile(includeFile, includeFileWithSp);
                    var doc = documentFromString(input, Options.of("safe", "safe", "standalone", false, "base_dir", DIRNAME));
                    var output = doc.convert();
                    assertMatch(rx("included content"), output);
                } finally {
                    removeFile(includeFileWithSp);
                }
            }

            @Test
            @DisplayName("include directive should not match if target is empty or starts or ends with space")
            void includeDirectiveShouldNotMatchIfTargetIsEmptyOrStartsOrEndsWithSpace() throws Exception {
                for (var input : list("include::[]", "include:: []", "include:: not-include[]", "include::not-include []")) {
                    var doc = new Document(input);
                    var reader = (PreprocessorReader) doc.getReader();
                    assertEquals(input, reader.readLine());
                }
            }

            @Test
            @DisplayName("include directive should not attempt to resolve target as remote if allow-uri-read is set and URL is not on first line")
            void includeDirectiveShouldNotAttemptToResolveTargetAsRemoteIfAllowUriReadIsSetAndURLIsNotOnFirstLine() throws Exception {
                try (var logger = usingMemoryLogger()) {
                    var input = """
                            :target: not-a-file.adoc + \\
                            http://example.org/team.adoc

                            include::{target}[]
                            """;
                    var doc = Asciidoctor.load(input, Options.of("safe", "safe", "base_dir", fixturedir()));
                    var lines = lines(doc.getBlocks().get(0));
                    assertEquals(list("Unresolved directive in <stdin> - include::not-a-file.adoc +\nhttp://example.org/team.adoc[]"), lines);
                    assertMessage(logger, "ERROR", ("<stdin>: line 4: include file not found: " + str(fixturePath("not-a-file.adoc")) + " +\nhttp://example.org/team.adoc"), LogMessage.class);
                }
            }

            @Test
            @DisplayName("include directive should resolve file relative to current include")
            void includeDirectiveShouldResolveFileRelativeToCurrentInclude() throws Exception {
                var input = "include::fixtures/parent-include.adoc[]";
                var pseudoDocfile = joinPath(DIRNAME, "main.adoc");
                var fixturesDir = joinPath(DIRNAME, "fixtures");
                var parentIncludeDocfile = joinPath(fixturesDir, "parent-include.adoc");
                var childIncludeDocfile = joinPath(fixturesDir, "child-include.adoc");
                var grandchildIncludeDocfile = joinPath(fixturesDir, "grandchild-include.adoc");
                var doc = emptySafeDocument(Options.of("base_dir", DIRNAME));
                var reader = new PreprocessorReader(doc, input, pseudoDocfile, Options.of("normalize", true));
                assertEquals(pseudoDocfile, reader.getFile());
                assertEquals(DIRNAME, reader.getDir());
                assertEquals("main.adoc", reader.getPath());
                assertEquals("first line of parent", reader.readLine());
                assertEquals("fixtures/parent-include.adoc: line 1", str(reader.cursorAtPrevLine()));
                assertEquals(parentIncludeDocfile, reader.getFile());
                assertEquals(fixturesDir, reader.getDir());
                assertEquals("fixtures/parent-include.adoc", reader.getPath());
                reader.skipBlankLines();
                assertEquals("first line of child", reader.readLine());
                assertEquals("fixtures/child-include.adoc: line 1", str(reader.cursorAtPrevLine()));
                assertEquals(childIncludeDocfile, reader.getFile());
                assertEquals(fixturesDir, reader.getDir());
                assertEquals("fixtures/child-include.adoc", reader.getPath());
                reader.skipBlankLines();
                assertEquals("first line of grandchild", reader.readLine());
                assertEquals("fixtures/grandchild-include.adoc: line 1", str(reader.cursorAtPrevLine()));
                assertEquals(grandchildIncludeDocfile, reader.getFile());
                assertEquals(fixturesDir, reader.getDir());
                assertEquals("fixtures/grandchild-include.adoc", reader.getPath());
                reader.skipBlankLines();
                assertEquals("last line of grandchild", reader.readLine());
                reader.skipBlankLines();
                assertEquals("last line of child", reader.readLine());
                reader.skipBlankLines();
                assertEquals("last line of parent", reader.readLine());
                assertEquals("fixtures/parent-include.adoc: line 5", str(reader.cursorAtPrevLine()));
                assertEquals(parentIncludeDocfile, reader.getFile());
                assertEquals(fixturesDir, reader.getDir());
                assertEquals("fixtures/parent-include.adoc", reader.getPath());
            }

            @Test
            @DisplayName("include directive should process lines when file extension of target is .asciidoc")
            void includeDirectiveShouldProcessLinesWhenFileExtensionOfTargetIsAsciidoc() throws Exception {
                var input = "include::fixtures/include-alt-extension.asciidoc[]";
                var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertEquals(3, size(doc.getBlocks()));
                assertEquals(list("first line"), lines(doc.getBlocks().get(0)));
                assertEquals(list("Asciidoctor!"), lines(doc.getBlocks().get(1)));
                assertEquals(list("last line"), lines(doc.getBlocks().get(2)));
            }

            @Test
            @DisplayName("should only strip trailing newlines, not trailing whitespace, if include file is not AsciiDoc")
            void shouldOnlyStripTrailingNewlinesNotTrailingWhitespaceIfIncludeFileIsNotAsciiDoc() throws Exception {
                var input = """
                        ....
                        include::fixtures/data.tsv[]
                        ....
                        """;
                var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertEquals(1, size(doc.getBlocks()));
                assertTruthy(endsWith(lines(doc.getBlocks().get(0)).get(2), "\t"));
            }

            @Test
            @DisplayName("should fail to read include file if not UTF-8 encoded and encoding is not specified")
            void shouldFailToReadIncludeFileIfNotUTF8EncodedAndEncodingIsNotSpecified() throws Exception {
                var input = """
                        ....
                        include::fixtures/iso-8859-1.txt[]
                        ....
                        """;
                assertThrows(AsciidoctorException.class, () -> {
                    var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    assertEquals(1, size(doc.getBlocks()));
                    assertNotEquals(list("Où est l'hôpital ?"), lines(doc.getBlocks().get(0)));
                    doc.convert();
                }, "invalid byte sequence in UTF-8");
            }

            @Test
            @DisplayName("should ignore encoding attribute if value is not a valid encoding")
            void shouldIgnoreEncodingAttributeIfValueIsNotAValidEncoding() throws Exception {
                var input = """
                        ....
                        include::fixtures/encoding.adoc[tag=romé,encoding=iso-1000-1]
                        ....
                        """;
                var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertEquals(1, size(doc.getBlocks()));
                assertEquals(list("Gregory Romé has written an AsciiDoc plugin for the Redmine project management application."), lines(doc.getBlocks().get(0)));
            }

            @Test
            @DisplayName("should use encoding specified by encoding attribute when reading include file")
            void shouldUseEncodingSpecifiedByEncodingAttributeWhenReadingIncludeFile() throws Exception {
                var input = """
                        ....
                        include::fixtures/iso-8859-1.txt[encoding=iso-8859-1]
                        ....
                        """;
                var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertEquals(1, size(doc.getBlocks()));
                assertEquals(list("Où est l'hôpital ?"), lines(doc.getBlocks().get(0)));
            }

            @Test
            @DisplayName("unresolved target referenced by include directive is skipped when optional option is set")
            void unresolvedTargetReferencedByIncludeDirectiveIsSkippedWhenOptionalOptionIsSet() throws Exception {
                var input = """
                        include::fixtures/{no-such-file}[opts=optional]

                        trailing content
                        """;
                try (var logger = usingMemoryLogger()) {
                    var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    assertEquals(1, size(doc.getBlocks()));
                    assertEquals(list("trailing content"), lines(doc.getBlocks().get(0)));
                    assertMessage(logger, "INFO", "~<stdin>: line 1: optional include dropped because include file not found", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should skip include directive that references missing file if optional option is set")
            void shouldSkipIncludeDirectiveThatReferencesMissingFileIfOptionalOptionIsSet() throws Exception {
                var input = """
                        include::fixtures/no-such-file.adoc[opts=optional]

                        trailing content
                        """;
                try (var logger = usingMemoryLogger()) {
                    var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    assertEquals(1, size(doc.getBlocks()));
                    assertEquals(list("trailing content"), lines(doc.getBlocks().get(0)));
                    assertMessage(logger, "INFO", "~<stdin>: line 1: optional include dropped because include file not found", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should replace include directive that references missing file with message")
            void shouldReplaceIncludeDirectiveThatReferencesMissingFileWithMessage() throws Exception {
                var input = """
                        include::fixtures/no-such-file.adoc[]

                        trailing content
                        """;
                try (var logger = usingMemoryLogger()) {
                    var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    assertEquals(2, size(doc.getBlocks()));
                    assertEquals(list("Unresolved directive in <stdin> - include::fixtures/no-such-file.adoc[]"), lines(doc.getBlocks().get(0)));
                    assertEquals(list("trailing content"), lines(doc.getBlocks().get(1)));
                    assertMessage(logger, "ERROR", "~<stdin>: line 1: include file not found", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should replace include directive that references unreadable file with message")
            void shouldReplaceIncludeDirectiveThatReferencesUnreadableFileWithMessage() throws Exception {
                assumeTrue(!isWindows());
                var includeFile = Path.of(joinPath(DIRNAME, "fixtures", "chapter-a.adoc"));
                var oldPermissions = Files.getPosixFilePermissions(includeFile);
                Files.setPosixFilePermissions(includeFile, Set.of());
                var input = """
                        include::fixtures/chapter-a.adoc[]

                        trailing content
                        """;
                try {
                    try (var logger = usingMemoryLogger()) {
                        var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                        assertEquals(2, size(doc.getBlocks()));
                        assertEquals(list("Unresolved directive in <stdin> - include::fixtures/chapter-a.adoc[]"), lines(doc.getBlocks().get(0)));
                        assertEquals(list("trailing content"), lines(doc.getBlocks().get(1)));
                        assertMessage(logger, "ERROR", "~<stdin>: line 1: include file not readable", LogMessage.class);
                    }
                } finally {
                    Files.setPosixFilePermissions(includeFile, oldPermissions);
                }
            }

            @Test
            @DisplayName("can resolve include directive with absolute path")
            void canResolveIncludeDirectiveWithAbsolutePath() throws Exception {
                var includePath = joinPath(DIRNAME, "fixtures", "chapter-a.adoc");
                var input = ("include::" + str(includePath) + "[]");
                var result = documentFromString(input, Options.of("safe", "safe"));
                assertEquals("Chapter A", result.getDoctitle());
                result = documentFromString(input, Options.of("safe", "unsafe", "base_dir", System.getProperty("java.io.tmpdir")));
                assertEquals("Chapter A", result.getDoctitle());
            }

            @Test
            @DisplayName("include directive can retrieve data from uri")
            void includeDirectiveCanRetrieveDataFromUri() throws Exception {
                var url = ("http://" + str(resolveLocalhost()) + ":9876/name/asciidoctor");
                var input = ("....\ninclude::" + str(url) + "[]\n....\n");
                var expect = rx("\\{\"name\": \"asciidoctor\"\\}");
                var output = usingTestWebserver(() -> convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("allow-uri-read", ""))));
                assertNotNull(output);
                assertMatch(expect, output);
            }

            @Test
            @DisplayName("nested include directives are resolved relative to current file")
            void nestedIncludeDirectivesAreResolvedRelativeToCurrentFile() throws Exception {
                var input = """
                        ....
                        include::fixtures/outer-include.adoc[]
                        ....
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        first line of outer

                        first line of middle

                        first line of inner

                        last line of inner

                        last line of middle

                        last line of outer
                        """);
                assertIncludes(output, expected);
            }

            @Test
            @DisplayName("nested remote include directive is resolved relative to uri of current file")
            void nestedRemoteIncludeDirectiveIsResolvedRelativeToUriOfCurrentFile() throws Exception {
                var url = ("http://" + str(resolveLocalhost()) + ":9876/fixtures/outer-include.adoc");
                var input = ("....\ninclude::" + str(url) + "[]\n....\n");
                var output = usingTestWebserver(() -> convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("allow-uri-read", ""))));
                var expected = Strings.chop("""
                        first line of outer

                        first line of middle

                        first line of inner

                        last line of inner

                        last line of middle

                        last line of outer
                        """);
                assertIncludes(output, expected);
            }

            @Test
            @DisplayName("nested remote include directive that cannot be resolved does not crash processor")
            void nestedRemoteIncludeDirectiveThatCannotBeResolvedDoesNotCrashProcessor() throws Exception {
                var includeUrl = ("http://" + str(resolveLocalhost()) + ":9876/fixtures/file-with-missing-include.adoc");
                var nestedIncludeUrl = "no-such-file.adoc";
                var input = ("....\ninclude::" + str(includeUrl) + "[]\n....\n");
                try (var logger = usingMemoryLogger()) {
                    var result = usingTestWebserver(() -> convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("allow-uri-read", ""))));
                    assertIncludes(result, ("Unresolved directive in " + str(includeUrl) + " - include::" + str(nestedIncludeUrl) + "[]"));
                    assertMessage(logger, "ERROR", (str(includeUrl) + ": line 1: include uri not readable: http://" + str(resolveLocalhost()) + ":9876/fixtures/" + str(nestedIncludeUrl)), LogMessage.class);
                }
            }

            @Test
            @DisplayName("should support tag filtering for remote includes")
            void shouldSupportTagFilteringForRemoteIncludes() throws Exception {
                var url = ("http://" + str(resolveLocalhost()) + ":9876/fixtures/tagged-class.rb");
                var input = ("[source,ruby]\n----\ninclude::" + str(url) + "[tag=init,indent=0]\n----\n");
                var output = usingTestWebserver(() -> convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("allow-uri-read", ""))));
                var expected = Strings.chop("""
                        <code class="language-ruby" data-lang="ruby">def initialize breed
                          @breed = breed
                        end</code>
                        """);
                assertIncludes(output, expected);
            }

            @Test
            @DisplayName("should not crash if include directive references inaccessible uri")
            void shouldNotCrashIfIncludeDirectiveReferencesInaccessibleUri() throws Exception {
                var url = ("http://" + str(resolveLocalhost()) + ":9876/no_such_file");
                var input = ("....\ninclude::" + str(url) + "[]\n....\n");
                try (var logger = usingMemoryLogger()) {
                    var output = usingTestWebserver(() -> convertStringToEmbedded(input, Options.of("safe", "safe", "attributes", Options.of("allow-uri-read", ""))));
                    assertNotNull(output);
                    assertMatch(rx("Unresolved directive"), output);
                    assertMessage(logger, "ERROR", ("<stdin>: line 2: include uri not readable: " + str(url)), LogMessage.class);
                }
            }

            @Test
            @DisplayName("include directive supports selecting lines by line number")
            void includeDirectiveSupportsSelectingLinesByLineNumber() throws Exception {
                var input = "include::fixtures/include-file.adoc[lines=1;3..4;6..-1]";
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertMatch(rx("first line"), output);
                refuteMatch(rx("second line"), output);
                assertMatch(rx("third line"), output);
                assertMatch(rx("fourth line"), output);
                refuteMatch(rx("fifth line"), output);
                assertMatch(rx("sixth line"), output);
                assertMatch(rx("seventh line"), output);
                assertMatch(rx("eighth line"), output);
                assertMatch(rx("last line of included content"), output);
            }

            @Test
            @DisplayName("include directive supports line ranges separated by commas in quoted attribute value")
            void includeDirectiveSupportsLineRangesSeparatedByCommasInQuotedAttributeValue() throws Exception {
                var input = "include::fixtures/include-file.adoc[lines=\"1,3..4,6..-1\"]";
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertMatch(rx("first line"), output);
                refuteMatch(rx("second line"), output);
                assertMatch(rx("third line"), output);
                assertMatch(rx("fourth line"), output);
                refuteMatch(rx("fifth line"), output);
                assertMatch(rx("sixth line"), output);
                assertMatch(rx("seventh line"), output);
                assertMatch(rx("eighth line"), output);
                assertMatch(rx("last line of included content"), output);
            }

            @Test
            @DisplayName("include directive ignores spaces between line ranges in quoted attribute value")
            void includeDirectiveIgnoresSpacesBetweenLineRangesInQuotedAttributeValue() throws Exception {
                var input = "include::fixtures/include-file.adoc[lines=\"1, 3..4 , 6 .. -1\"]";
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertMatch(rx("first line"), output);
                refuteMatch(rx("second line"), output);
                assertMatch(rx("third line"), output);
                assertMatch(rx("fourth line"), output);
                refuteMatch(rx("fifth line"), output);
                assertMatch(rx("sixth line"), output);
                assertMatch(rx("seventh line"), output);
                assertMatch(rx("eighth line"), output);
                assertMatch(rx("last line of included content"), output);
            }

            @Test
            @DisplayName("include directive supports implicit endless range")
            void includeDirectiveSupportsImplicitEndlessRange() throws Exception {
                var input = "include::fixtures/include-file.adoc[lines=6..]";
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                refuteMatch(rx("first line"), output);
                refuteMatch(rx("second line"), output);
                refuteMatch(rx("third line"), output);
                refuteMatch(rx("fourth line"), output);
                refuteMatch(rx("fifth line"), output);
                assertMatch(rx("sixth line"), output);
                assertMatch(rx("seventh line"), output);
                assertMatch(rx("eighth line"), output);
                assertMatch(rx("last line of included content"), output);
            }

            @Test
            @DisplayName("include directive ignores lines attribute if empty")
            void includeDirectiveIgnoresLinesAttributeIfEmpty() throws Exception {
                var input = """
                        ++++
                        include::fixtures/include-file.adoc[lines=]
                        ++++
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertIncludes(output, "first line of included content");
                assertIncludes(output, "last line of included content");
            }

            @Test
            @DisplayName("include directive ignores lines attribute with invalid range")
            void includeDirectiveIgnoresLinesAttributeWithInvalidRange() throws Exception {
                var input = """
                        ++++
                        include::fixtures/include-file.adoc[lines=10..5]
                        ++++
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertIncludes(output, "first line of included content");
                assertIncludes(output, "last line of included content");
            }

            @Test
            @DisplayName("include directive supports selecting lines by tag")
            void includeDirectiveSupportsSelectingLinesByTag() throws Exception {
                var input = "include::fixtures/include-file.adoc[tag=snippetA]";
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertMatch(rx("snippetA content"), output);
                refuteMatch(rx("snippetB content"), output);
                refuteMatch(rx("non-tagged content"), output);
                refuteMatch(rx("included content"), output);
            }

            @Test
            @DisplayName("include directive supports selecting lines by tags")
            void includeDirectiveSupportsSelectingLinesByTags() throws Exception {
                var input = "include::fixtures/include-file.adoc[tags=snippetA;snippetB]";
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertMatch(rx("snippetA content"), output);
                assertMatch(rx("snippetB content"), output);
                refuteMatch(rx("non-tagged content"), output);
                refuteMatch(rx("included content"), output);
            }

            @Test
            @DisplayName("include directive supports selecting lines by tag in language that uses circumfix comments")
            void includeDirectiveSupportsSelectingLinesByTagInLanguageThatUsesCircumfixComments() throws Exception {
                for (var entry : entries(smap("include-file.xml", "<snippet>content</snippet>", "include-file.ml", "let s = SS.empty;;", "include-file.jsx", "<p>Welcome to the club.</p>"))) {
                    var filename = entry.getKey();
                    var expect = entry.getValue();
                    var input = ("[source,xml]\n----\ninclude::fixtures/" + str(filename) + "[tag=snippet,indent=0]\n----\n");
                    var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    assertEquals(expect, ((Block) doc.getBlocks().get(0)).getSource());
                }
            }

            @Test
            @DisplayName("include directive supports selecting lines by tag in file that has CRLF line endings")
            void includeDirectiveSupportsSelectingLinesByTagInFileThatHasCRLFLineEndings() throws Exception {
                var tmpInclude = Files.createTempFile("include-", ".adoc");
                try {
                    Files.writeString(tmpInclude, "do not include\r\ntag::include-me[]\r\nincluded line\r\nend::include-me[]\r\ndo not include\r\n");
                    var input = "include::" + tmpInclude.getFileName() + "[tag=include-me]";
                    var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", tmpInclude.getParent().toString()));
                    assertIncludes(output, "included line");
                    refuteIncludes(output, "do not include");
                } finally {
                    Files.deleteIfExists(tmpInclude);
                }
            }

            @Test
            @DisplayName("include directive finds closing tag on last line of file without a trailing newline")
            void includeDirectiveFindsClosingTagOnLastLineOfFileWithoutATrailingNewline() throws Exception {
                var tmpInclude = Files.createTempFile("include-", ".adoc");
                try {
                    Files.writeString(tmpInclude, "line not included\ntag::include-me[]\nline included\nend::include-me[]");
                    var input = "include::" + tmpInclude.getFileName() + "[tag=include-me]";
                    try (var logger = usingMemoryLogger()) {
                        var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", tmpInclude.getParent().toString()));
                        assertEmpty(logger);
                        assertIncludes(output, "line included");
                        refuteIncludes(output, "line not included");
                    }
                } finally {
                    Files.deleteIfExists(tmpInclude);
                }
            }

            @Test
            @DisplayName("include directive does not select lines containing tag directives within selected tag region")
            void includeDirectiveDoesNotSelectLinesContainingTagDirectivesWithinSelectedTagRegion() throws Exception {
                var input = """
                        ++++
                        include::fixtures/include-file.adoc[tags=snippet]
                        ++++
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        snippetA content

                        non-tagged content

                        snippetB content
                        """);
                assertEquals(expected, output);
            }

            @Test
            @DisplayName("include directive skips lines inside tag which is negated")
            void includeDirectiveSkipsLinesInsideTagWhichIsNegated() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class-enclosed.rb[tags=all;!bark]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog
                          def initialize breed
                            @breed = breed
                          end
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive selects all lines without a tag directive when value is double asterisk")
            void includeDirectiveSelectsAllLinesWithoutATagDirectiveWhenValueIsDoubleAsterisk() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=**]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog
                          def initialize breed
                            @breed = breed
                          end

                          def bark
                            if @breed == 'beagle'
                              'woof woof woof woof woof'
                            else
                              'woof woof'
                            end
                          end
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive selects all lines except lines inside tag which is negated when value starts with double asterisk")
            void includeDirectiveSelectsAllLinesExceptLinesInsideTagWhichIsNegatedWhenValueStartsWithDoubleAsterisk() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=**;!bark]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog
                          def initialize breed
                            @breed = breed
                          end
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive selects all lines, including lines inside nested tags, except lines inside tag which is negated when value starts with double asterisk")
            void includeDirectiveSelectsAllLinesIncludingLinesInsideNestedTagsExceptLinesInsideTagWhichIsNegatedWhenValueStartsWithDoubleAsterisk() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=**;!init]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog

                          def bark
                            if @breed == 'beagle'
                              'woof woof woof woof woof'
                            else
                              'woof woof'
                            end
                          end
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive selects all lines outside of tags when value is double asterisk followed by negated wildcard")
            void includeDirectiveSelectsAllLinesOutsideOfTagsWhenValueIsDoubleAsteriskFollowedByNegatedWildcard() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=**;!*]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive skips all tagged regions when value of tags attribute is negated wildcard")
            void includeDirectiveSkipsAllTaggedRegionsWhenValueOfTagsAttributeIsNegatedWildcard() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=!*]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = "class Dog\nend";
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive selects all lines except for lines containing tag directive if value is double asterisk followed by nested tag names")
            void includeDirectiveSelectsAllLinesExceptForLinesContainingTagDirectiveIfValueIsDoubleAsteriskFollowedByNestedTagNames() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=**;bark-beagle;bark-all]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog
                          def initialize breed
                            @breed = breed
                          end

                          def bark
                            if @breed == 'beagle'
                              'woof woof woof woof woof'
                            else
                              'woof woof'
                            end
                          end
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive selects all lines except for lines containing tag directive when value is double asterisk followed by outer tag name")
            void includeDirectiveSelectsAllLinesExceptForLinesContainingTagDirectiveWhenValueIsDoubleAsteriskFollowedByOuterTagName() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=**;bark]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog
                          def initialize breed
                            @breed = breed
                          end

                          def bark
                            if @breed == 'beagle'
                              'woof woof woof woof woof'
                            else
                              'woof woof'
                            end
                          end
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive selects all lines inside unspecified tags when value is negated double asterisk followed by negated tags")
            void includeDirectiveSelectsAllLinesInsideUnspecifiedTagsWhenValueIsNegatedDoubleAsteriskFollowedByNegatedTags() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=!**;!init]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                          def bark
                            if @breed == 'beagle'
                              'woof woof woof woof woof'
                            else
                              'woof woof'
                            end
                          end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive selects all lines except tag which is negated when value only contains negated tag")
            void includeDirectiveSelectsAllLinesExceptTagWhichIsNegatedWhenValueOnlyContainsNegatedTag() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tag=!bark]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog
                          def initialize breed
                            @breed = breed
                          end
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive selects all lines except tags which are negated when value only contains negated tags")
            void includeDirectiveSelectsAllLinesExceptTagsWhichAreNegatedWhenValueOnlyContainsNegatedTags() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=!bark;!init]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("should recognize tag wildcard if not at start of tags list")
            void shouldRecognizeTagWildcardIfNotAtStartOfTagsList() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=init;**;*;!bark-other]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog
                          def initialize breed
                            @breed = breed
                          end

                          def bark
                            if @breed == 'beagle'
                              'woof woof woof woof woof'
                            end
                          end
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive selects lines between tags when value of tags attribute is wildcard")
            void includeDirectiveSelectsLinesBetweenTagsWhenValueOfTagsAttributeIsWildcard() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=*]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                          def initialize breed
                            @breed = breed
                          end

                          def bark
                            if @breed == 'beagle'
                              'woof woof woof woof woof'
                            else
                              'woof woof'
                            end
                          end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive selects lines inside tags when value of tags attribute is wildcard and tag surrounds content")
            void includeDirectiveSelectsLinesInsideTagsWhenValueOfTagsAttributeIsWildcardAndTagSurroundsContent() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class-enclosed.rb[tags=*]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog
                          def initialize breed
                            @breed = breed
                          end

                          def bark
                            if @breed == 'beagle'
                              'woof woof woof woof woof'
                            else
                              'woof woof'
                            end
                          end
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive selects lines inside all tags except tag which is negated when value of tags attribute is wildcard followed by negated tag")
            void includeDirectiveSelectsLinesInsideAllTagsExceptTagWhichIsNegatedWhenValueOfTagsAttributeIsWildcardFollowedByNegatedTag() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class-enclosed.rb[tags=*;!init]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog

                          def bark
                            if @breed == 'beagle'
                              'woof woof woof woof woof'
                            else
                              'woof woof'
                            end
                          end
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive skips all tagged regions except ones re-enabled when value of tags attribute is negated wildcard followed by tag name")
            void includeDirectiveSkipsAllTaggedRegionsExceptOnesReEnabledWhenValueOfTagsAttributeIsNegatedWildcardFollowedByTagName() throws Exception {
                for (var pattern : list("!*;init", "**;!*;init")) {
                    var input = ("----\ninclude::fixtures/tagged-class.rb[tags=" + str(pattern) + "]\n----\n");
                    var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    var expected = Strings.chop("""
                            class Dog
                              def initialize breed
                                @breed = breed
                              end
                            end
                            """);
                    assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
                }
            }

            @Test
            @DisplayName("include directive includes regions outside tags and inside specified tags when value begins with negated wildcard")
            void includeDirectiveIncludesRegionsOutsideTagsAndInsideSpecifiedTagsWhenValueBeginsWithNegatedWildcard() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=!*;bark]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog

                          def bark
                          end
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive includes lines inside tag except for lines inside nested tags when tag is followed by negated wildcard")
            void includeDirectiveIncludesLinesInsideTagExceptForLinesInsideNestedTagsWhenTagIsFollowedByNegatedWildcard() throws Exception {
                for (var pattern : list("bark;!*", "!**;bark;!*", "!**;!*;bark")) {
                    var input = ("----\ninclude::fixtures/tagged-class.rb[tags=" + str(pattern) + "]\n----\n");
                    var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    var expected = Strings.chop("""
                              def bark
                              end
                            """);
                    assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
                }
            }

            @Test
            @DisplayName("include directive selects lines inside tag except for lines inside nested tags when tag is preceded by negated double asterisk and negated wildcard")
            void includeDirectiveSelectsLinesInsideTagExceptForLinesInsideNestedTagsWhenTagIsPrecededByNegatedDoubleAsteriskAndNegatedWildcard() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=!**;!*;bark]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                          def bark
                          end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive does not select lines inside tag that has been included then excluded")
            void includeDirectiveDoesNotSelectLinesInsideTagThatHasBeenIncludedThenExcluded() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class.rb[tags=!*;init;!init]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        class Dog
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("include directive only selects lines inside specified tag, even if proceeded by negated double asterisk")
            void includeDirectiveOnlySelectsLinesInsideSpecifiedTagEvenIfProceededByNegatedDoubleAsterisk() throws Exception {
                for (var pattern : list("bark", "!**;bark")) {
                    var input = ("----\ninclude::fixtures/tagged-class.rb[tags=" + str(pattern) + "]\n----\n");
                    var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    var expected = Strings.chop("""
                              def bark
                                if @breed == 'beagle'
                                  'woof woof woof woof woof'
                                else
                                  'woof woof'
                                end
                              end
                            """);
                    assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
                }
            }

            @Test
            @DisplayName("include directive selects lines inside specified tag and ignores lines inside a negated tag")
            void includeDirectiveSelectsLinesInsideSpecifiedTagAndIgnoresLinesInsideANegatedTag() throws Exception {
                var input = """
                        [indent=0]
                        ----
                        include::fixtures/tagged-class.rb[tags=bark;!bark-other]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var expected = Strings.chop("""
                        def bark
                          if @breed == 'beagle'
                            'woof woof woof woof woof'
                          end
                        end
                        """);
                assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
            }

            @Test
            @DisplayName("should warn if specified tag is not found in include file")
            void shouldWarnIfSpecifiedTagIsNotFoundInIncludeFile() throws Exception {
                var input = "include::fixtures/include-file.adoc[tag=no-such-tag]";
                try (var logger = usingMemoryLogger()) {
                    convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    assertMessage(logger, "WARN", "~<stdin>: line 1: tag 'no-such-tag' not found in include file", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should not warn if specified negated tag is not found in include file")
            void shouldNotWarnIfSpecifiedNegatedTagIsNotFoundInIncludeFile() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class-enclosed.rb[tag=!no-such-tag]
                        ----
                        """;
                var expected = Strings.chop("""
                        class Dog
                          def initialize breed
                            @breed = breed
                          end

                          def bark
                            if @breed == 'beagle'
                              'woof woof woof woof woof'
                            else
                              'woof woof'
                            end
                          end
                        end
                        """);
                try (var logger = usingMemoryLogger()) {
                    var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
                    assertEmpty(logger);
                }
            }

            @Test
            @DisplayName("should warn if specified tags are not found in include file")
            void shouldWarnIfSpecifiedTagsAreNotFoundInIncludeFile() throws Exception {
                var input = """
                        ++++
                        include::fixtures/include-file.adoc[tags=no-such-tag-b;no-such-tag-a]
                        ++++
                        """;
                try (var logger = usingMemoryLogger()) {
                    convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    var expectedTags = "no-such-tag-b, no-such-tag-a";
                    assertMessage(logger, "WARN", ("~<stdin>: line 2: tags '" + str(expectedTags) + "' not found in include file"), LogMessage.class);
                }
            }

            @Test
            @DisplayName("should not warn if specified negated tags are not found in include file")
            void shouldNotWarnIfSpecifiedNegatedTagsAreNotFoundInIncludeFile() throws Exception {
                var input = """
                        ----
                        include::fixtures/tagged-class-enclosed.rb[tags=all;!no-such-tag;!unknown-tag]
                        ----
                        """;
                var expected = Strings.chop("""
                        class Dog
                          def initialize breed
                            @breed = breed
                          end

                          def bark
                            if @breed == 'beagle'
                              'woof woof woof woof woof'
                            else
                              'woof woof'
                            end
                          end
                        end
                        """);
                try (var logger = usingMemoryLogger()) {
                    var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    assertIncludes(output, ("<pre>" + str(expected) + "</pre>"));
                    assertEmpty(logger);
                }
            }

            @Test
            @DisplayName("should warn if specified tag in include file is not closed")
            void shouldWarnIfSpecifiedTagInIncludeFileIsNotClosed() throws Exception {
                var input = """
                        ++++
                        include::fixtures/unclosed-tag.adoc[tag=a]
                        ++++
                        """;
                try (var logger = usingMemoryLogger()) {
                    var result = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    assertEquals("a", result);
                    assertMessage(logger, "WARN", "~<stdin>: line 2: detected unclosed tag 'a' starting at line 2 of include file", LogMessage.class);
                    assertNotNull(((LogMessage) logger.getMessages().get(0).message()).includeLocation());
                }
            }

            @Test
            @DisplayName("should warn if end tag in included file is mismatched")
            void shouldWarnIfEndTagInIncludedFileIsMismatched() throws Exception {
                var input = """
                        ++++
                        include::fixtures/mismatched-end-tag.adoc[tags=a;b]
                        ++++
                        """;
                var incPath = joinPath(DIRNAME, "fixtures/mismatched-end-tag.adoc");
                try (var logger = usingMemoryLogger()) {
                    var result = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    assertEquals("a\nb", result);
                    assertMessage(logger, "WARN", ("<stdin>: line 2: mismatched end tag (expected 'b' but found 'a') at line 5 of include file: " + str(incPath)), LogMessage.class);
                    assertNotNull(((LogMessage) logger.getMessages().get(0).message()).includeLocation());
                }
            }

            @Test
            @DisplayName("should warn if unexpected end tag is found in included file")
            void shouldWarnIfUnexpectedEndTagIsFoundInIncludedFile() throws Exception {
                var input = """
                        ++++
                        include::fixtures/unexpected-end-tag.adoc[tags=a]
                        ++++
                        """;
                var incPath = joinPath(DIRNAME, "fixtures/unexpected-end-tag.adoc");
                try (var logger = usingMemoryLogger()) {
                    var result = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    assertEquals("a", result);
                    assertMessage(logger, "WARN", ("<stdin>: line 2: unexpected end tag 'a' at line 4 of include file: " + str(incPath)), LogMessage.class);
                    assertNotNull(((LogMessage) logger.getMessages().get(0).message()).includeLocation());
                }
            }

            @Test
            @DisplayName("include directive ignores tags attribute when empty")
            void includeDirectiveIgnoresTagsAttributeWhenEmpty() throws Exception {
                for (var attrName : list("tag", "tags")) {
                    var input = ("++++\ninclude::fixtures/include-file.xml[" + str(attrName) + "=]\n++++\n");
                    var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                    assertMatch(rx("(?:tag|end)::"), output, 2);
                }
            }

            @Test
            @DisplayName("lines attribute takes precedence over tags attribute in include directive")
            void linesAttributeTakesPrecedenceOverTagsAttributeInIncludeDirective() throws Exception {
                var input = "include::fixtures/include-file.adoc[lines=1, tags=snippetA;snippetB]";
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertMatch(rx("first line of included content"), output);
                refuteMatch(rx("snippetA content"), output);
                refuteMatch(rx("snippetB content"), output);
            }

            @Test
            @DisplayName("indent of included file can be reset to size of indent attribute")
            void indentOfIncludedFileCanBeResetToSizeOfIndentAttribute() throws Exception {
                var input = """
                        [source, xml]
                        ----
                        include::fixtures/basic-docinfo.xml[lines=2..3, indent=0]
                        ----
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var result = xmlnodesAtXpath("//pre", output, 1).getText();
                assertEquals("<year>2013</year>\n<holder>Acme™, Inc.</holder>", result);
            }

            @Test
            @DisplayName("should substitute attribute references in attrlist")
            void shouldSubstituteAttributeReferencesInAttrlist() throws Exception {
                var input = """
                        :name-of-tag: snippetA
                        include::fixtures/include-file.adoc[tag={name-of-tag}]
                        """;
                var output = convertStringToEmbedded(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                assertMatch(rx("snippetA content"), output);
                refuteMatch(rx("snippetB content"), output);
                refuteMatch(rx("non-tagged content"), output);
                refuteMatch(rx("included content"), output);
            }

            @Test
            @DisplayName("should fall back to built-in include directive behavior when not handled by include processor")
            void shouldFallBackToBuiltInIncludeDirectiveBehaviorWhenNotHandledByIncludeProcessor() throws Exception {
                var input = "include::fixtures/include-file.adoc[]";
                var registry = Extensions.create();
                registry.includeProcessor(processor -> {
                    processor.handles(target -> false);
                    processor.process((document, reader, target, attributes) -> {
                        throw new AssertionError("TestIncludeHandler should not have been invoked");
                    });
                });
                var document = emptySafeDocument(Options.of("base_dir", DIRNAME, "extension_registry", registry));
                var reader = new PreprocessorReader(document, input, null, true);
                var lines = reader.readLines();
                var source = String.join(Asciidoctor.LF, lines);
                assertMatch(rx("included content"), source);
            }

            @Test
            @DisplayName("leveloffset attribute entries should be added to content if leveloffset attribute is specified")
            void leveloffsetAttributeEntriesShouldBeAddedToContentIfLeveloffsetAttributeIsSpecified() throws Exception {
                var input = "include::fixtures/main.adoc[]";
                var expected = split("""
                        = Main Document

                        preamble

                        :leveloffset: +1

                        = Chapter A

                        content

                        :leveloffset!:
                        """, Asciidoctor.LF);
                var document = Asciidoctor.load(input, Options.of("safe", "safe", "base_dir", DIRNAME, "parse", false));
                assertEquals(expected, document.getReader().readLines());
            }

            @Test
            @DisplayName("attributes are substituted in target of include directive")
            void attributesAreSubstitutedInTargetOfIncludeDirective() throws Exception {
                var input = """
                        :fixturesdir: fixtures
                        :ext: adoc

                        include::{fixturesdir}/include-file.{ext}[]
                        """;
                var doc = documentFromString(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var output = doc.convert();
                assertMatch(rx("included content"), output);
            }

            @Test
            @DisplayName("line is skipped by default if target of include directive resolves to empty")
            void lineIsSkippedByDefaultIfTargetOfIncludeDirectiveResolvesToEmpty() throws Exception {
                var input = "include::{blank}[]";
                try (var logger = usingMemoryLogger()) {
                    var doc = emptySafeDocument(Options.of("base_dir", DIRNAME));
                    var reader = new PreprocessorReader(doc, input, null, Options.of("normalize", true));
                    var line = reader.readLine();
                    assertEquals("Unresolved directive in <stdin> - include::{blank}[]", line);
                    assertMessage(logger, "WARN", "<stdin>: line 1: include dropped because resolved target is blank: include::{blank}[]", LogMessage.class);
                }
            }

            @Test
            @DisplayName("include is dropped if target contains missing attribute and attribute-missing is drop-line")
            void includeIsDroppedIfTargetContainsMissingAttributeAndAttributeMissingIsDropLine() throws Exception {
                var input = "include::{foodir}/include-file.adoc[]";
                try (var logger = usingMemoryLogger(Logger.Severity.INFO)) {
                    var doc = emptySafeDocument(Options.of("base_dir", DIRNAME, "attributes", Options.of("attribute-missing", "drop-line")));
                    var reader = new PreprocessorReader(doc, input, null, Options.of("normalize", true));
                    var line = reader.readLine();
                    assertNull(line);
                    assertMessages(logger, list(list("INFO", "dropping line containing reference to missing attribute: foodir"), list("INFO", "<stdin>: line 1: include dropped due to missing attribute: include::{foodir}/include-file.adoc[]", LogMessage.class)));
                }
            }

            @Test
            @DisplayName("line following dropped include is not dropped")
            void lineFollowingDroppedIncludeIsNotDropped() throws Exception {
                var input = """
                        include::{foodir}/include-file.adoc[]
                        yo
                        """;
                try (var logger = usingMemoryLogger()) {
                    var doc = emptySafeDocument(Options.of("base_dir", DIRNAME, "attributes", Options.of("attribute-missing", "warn")));
                    var reader = new PreprocessorReader(doc, input, null, Options.of("normalize", true));
                    var line = reader.readLine();
                    assertEquals("Unresolved directive in <stdin> - include::{foodir}/include-file.adoc[]", line);
                    line = reader.readLine();
                    assertEquals("yo", line);
                    assertMessages(logger, list(list("INFO", "dropping line containing reference to missing attribute: foodir"), list("WARN", "<stdin>: line 1: include dropped due to missing attribute: include::{foodir}/include-file.adoc[]", LogMessage.class)));
                }
            }

            @Test
            @DisplayName("escaped include directive is left unprocessed")
            void escapedIncludeDirectiveIsLeftUnprocessed() throws Exception {
                var input = """
                        \\include::fixtures/include-file.adoc[]
                        \\escape preserved here
                        """;
                var doc = emptySafeDocument(Options.of("base_dir", DIRNAME));
                var reader = new PreprocessorReader(doc, input, null, Options.of("normalize", true));
                assertEquals("include::fixtures/include-file.adoc[]", reader.peekLine());
                assertEquals("include::fixtures/include-file.adoc[]", reader.peekLine());
                assertEquals("include::fixtures/include-file.adoc[]", reader.readLine());
                assertEquals("\\escape preserved here", reader.readLine());
            }

            @Test
            @DisplayName("include directive not at start of line is ignored")
            void includeDirectiveNotAtStartOfLineIsIgnored() throws Exception {
                var input = " include::include-file.adoc[]";
                var para = (Block) blockFromString(input);
                assertEquals(1, size(lines(para)));
                assertEquals("literal", para.getContext());
                assertEquals("include::include-file.adoc[]", para.getSource());
            }

            @Test
            @DisplayName("include directive is disabled when max-include-depth attribute is 0")
            void includeDirectiveIsDisabledWhenMaxIncludeDepthAttributeIs0() throws Exception {
                var input = "include::include-file.adoc[]";
                var para = (Block) blockFromString(input, Options.of("safe", "safe", "attributes", Options.of("max-include-depth", 0)));
                assertEquals(1, size(lines(para)));
                assertEquals("include::include-file.adoc[]", para.getSource());
            }

            @Test
            @DisplayName("max-include-depth cannot be set by document")
            void maxIncludeDepthCannotBeSetByDocument() throws Exception {
                var input = """
                        :max-include-depth: 1

                        include::include-file.adoc[]
                        """;
                var para = (Block) blockFromString(input, Options.of("safe", "safe", "attributes", Options.of("max-include-depth", 0)));
                assertEquals(1, size(lines(para)));
                assertEquals("include::include-file.adoc[]", para.getSource());
            }

            @Test
            @DisplayName("include directive should be disabled if max include depth has been exceeded")
            void includeDirectiveShouldBeDisabledIfMaxIncludeDepthHasBeenExceeded() throws Exception {
                var input = "include::fixtures/parent-include.adoc[depth=1]";
                try (var logger = usingMemoryLogger()) {
                    var pseudoDocfile = joinPath(DIRNAME, "main.adoc");
                    var doc = emptySafeDocument(Options.of("base_dir", DIRNAME));
                    var reader = new PreprocessorReader(doc, input, new Reader.Cursor(pseudoDocfile), Options.of("normalize", true));
                    var lines = reader.readLines();
                    assertIncludes(lines, "include::grandchild-include.adoc[]");
                    assertMessage(logger, "ERROR", "fixtures/child-include.adoc: line 3: maximum include depth of 1 exceeded", LogMessage.class);
                }
            }

            @Test
            @DisplayName("include directive should be disabled if max include depth set in nested context has been exceeded")
            void includeDirectiveShouldBeDisabledIfMaxIncludeDepthSetInNestedContextHasBeenExceeded() throws Exception {
                var input = "include::fixtures/parent-include-restricted.adoc[depth=3]";
                try (var logger = usingMemoryLogger()) {
                    var pseudoDocfile = joinPath(DIRNAME, "main.adoc");
                    var doc = emptySafeDocument(Options.of("base_dir", DIRNAME));
                    var reader = new PreprocessorReader(doc, input, new Reader.Cursor(pseudoDocfile), Options.of("normalize", true));
                    var lines = reader.readLines();
                    assertIncludes(lines, "first line of child");
                    assertIncludes(lines, "include::grandchild-include.adoc[]");
                    assertMessage(logger, "ERROR", "fixtures/child-include.adoc: line 3: maximum include depth of 0 exceeded", LogMessage.class);
                }
            }

            @Test
            @DisplayName("read_lines_until should not process lines if process option is false")
            void read_lines_untilShouldNotProcessLinesIfProcessOptionIsFalse() throws Exception {
                var lines = lines("""
                        ////
                        include::fixtures/no-such-file.adoc[]
                        ////
                        """);
                var doc = emptySafeDocument(Options.of("base_dir", DIRNAME));
                var reader = new PreprocessorReader(doc, lines, null, Options.of("normalize", true));
                reader.readLine();
                var result = reader.readLinesUntil(Reader.ReadOptions.create().terminator("////").skipProcessing());
                assertEquals(slice(map(lines, (String it) -> Strings.chomp(it)), range(1, 1)), result);
            }

            @Test
            @DisplayName("skip_comment_lines should not process lines read")
            void skip_comment_linesShouldNotProcessLinesRead() throws Exception {
                var lines = lines("""
                        ////
                        include::fixtures/no-such-file.adoc[]
                        ////
                        """);
                try (var logger = usingMemoryLogger()) {
                    var doc = emptySafeDocument(Options.of("base_dir", DIRNAME));
                    var reader = new PreprocessorReader(doc, lines, null, Options.of("normalize", true));
                    reader.skipCommentLines();
                    assertEmpty(reader);
                    assertEmpty(logger);
                }
            }
        }

        @Nested
        @DisplayName("Conditional Inclusions")
        class ConditionalInclusions {

            @Test
            @DisplayName("process_line returns nil if cursor advanced")
            void process_lineReturnsNilIfCursorAdvanced() throws Exception {
                var input = """
                        ifdef::asciidoctor[]
                        Asciidoctor!
                        endif::asciidoctor[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                assertNull(reader.processLine(first(lines(reader))));
            }

            @Test
            @DisplayName("peek_line advances cursor to next conditional line of content")
            void peek_lineAdvancesCursorToNextConditionalLineOfContent() throws Exception {
                var input = """
                        ifdef::asciidoctor[]
                        Asciidoctor!
                        endif::asciidoctor[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals(1, reader.getLineNumber());
                assertEquals("Asciidoctor!", reader.peekLine());
                assertEquals(2, reader.getLineNumber());
            }

            @Test
            @DisplayName("peek_lines should preprocess lines if direct is false")
            void peek_linesShouldPreprocessLinesIfDirectIsFalse() throws Exception {
                var input = """
                        The Asciidoctor
                        ifdef::asciidoctor[is in.]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                var result = reader.peekLines(2, false);
                assertEquals(list("The Asciidoctor", "is in."), result);
            }

            @Test
            @DisplayName("peek_lines should not preprocess lines if direct is true")
            void peek_linesShouldNotPreprocessLinesIfDirectIsTrue() throws Exception {
                var input = """
                        The Asciidoctor
                        ifdef::asciidoctor[is in.]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                var result = reader.peekLines(2, true);
                assertEquals(list("The Asciidoctor", "ifdef::asciidoctor[is in.]"), result);
            }

            @Test
            @DisplayName("peek_lines should not prevent subsequent preprocessing of peeked lines")
            void peek_linesShouldNotPreventSubsequentPreprocessingOfPeekedLines() throws Exception {
                var input = """
                        The Asciidoctor
                        ifdef::asciidoctor[is in.]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                reader.peekLines(2, true);
                var result = reader.peekLines(2, false);
                assertEquals(list("The Asciidoctor", "is in."), result);
            }

            @Test
            @DisplayName("process_line returns line if cursor not advanced")
            void process_lineReturnsLineIfCursorNotAdvanced() throws Exception {
                var input = """
                        content
                        ifdef::asciidoctor[]
                        Asciidoctor!
                        endif::asciidoctor[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                assertNotNull(reader.processLine(first(lines(reader))));
            }

            @Test
            @DisplayName("peek_line does not advance cursor when on a regular content line")
            void peek_lineDoesNotAdvanceCursorWhenOnARegularContentLine() throws Exception {
                var input = """
                        content
                        ifdef::asciidoctor[]
                        Asciidoctor!
                        endif::asciidoctor[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals(1, reader.getLineNumber());
                assertEquals("content", reader.peekLine());
                assertEquals(1, reader.getLineNumber());
            }

            @Test
            @DisplayName("peek_line returns nil if cursor advances past end of source")
            void peek_lineReturnsNilIfCursorAdvancesPastEndOfSource() throws Exception {
                var input = """
                        ifdef::foobar[]
                        swallowed content
                        endif::foobar[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals(1, reader.getLineNumber());
                assertNull(reader.peekLine());
                assertEquals(4, reader.getLineNumber());
            }

            @Test
            @DisplayName("peek_line returns nil if contents of skipped conditional is empty line")
            void peek_lineReturnsNilIfContentsOfSkippedConditionalIsEmptyLine() throws Exception {
                var input = """
                        ifdef::foobar[]

                        endif::foobar[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                assertEquals(1, reader.getLineNumber());
                assertNull(reader.peekLine());
            }

            @Test
            @DisplayName("ifdef with defined attribute includes content")
            void ifdefWithDefinedAttributeIncludesContent() throws Exception {
                var input = """
                        ifdef::holygrail[]
                        There is a holy grail!
                        endif::holygrail[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("holygrail", "")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("There is a holy grail!", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifdef with defined attribute includes text in brackets")
            void ifdefWithDefinedAttributeIncludesTextInBrackets() throws Exception {
                var input = """
                        On our quest we go...
                        ifdef::holygrail[There is a holy grail!]
                        There was much rejoicing.
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("holygrail", "")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("On our quest we go...\nThere is a holy grail!\nThere was much rejoicing.", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifdef with defined attribute processes include directive in brackets")
            void ifdefWithDefinedAttributeProcessesIncludeDirectiveInBrackets() throws Exception {
                var input = "ifdef::asciidoctor-version[include::fixtures/include-file.adoc[tag=snippetA]]";
                var doc = new Document(input, Options.of("safe", "safe", "base_dir", DIRNAME));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("snippetA content", lines.get(0));
            }

            @Test
            @DisplayName("ifdef attribute name is not case sensitive")
            void ifdefAttributeNameIsNotCaseSensitive() throws Exception {
                var input = """
                        ifdef::showScript[]
                        The script is shown!
                        endif::showScript[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("showscript", "")));
                var result = doc.getReader().read();
                assertEquals("The script is shown!", result);
            }

            @Test
            @DisplayName("ifndef with defined attribute does not include text in brackets")
            void ifndefWithDefinedAttributeDoesNotIncludeTextInBrackets() throws Exception {
                var input = """
                        On our quest we go...
                        ifndef::hardships[There is a holy grail!]
                        There was no rejoicing.
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("hardships", "")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("On our quest we go...\nThere was no rejoicing.", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("include with non-matching nested exclude")
            void includeWithNonMatchingNestedExclude() throws Exception {
                var input = """
                        ifdef::grail[]
                        holy
                        ifdef::swallow[]
                        swallow
                        endif::swallow[]
                        grail
                        endif::grail[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("grail", "")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("holy\ngrail", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("nested excludes with same condition")
            void nestedExcludesWithSameCondition() throws Exception {
                var input = """
                        ifndef::grail[]
                        ifndef::grail[]
                        not here
                        endif::grail[]
                        endif::grail[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("grail", "")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("include with nested exclude of inverted condition")
            void includeWithNestedExcludeOfInvertedCondition() throws Exception {
                var input = """
                        ifdef::grail[]
                        holy
                        ifndef::grail[]
                        not here
                        endif::grail[]
                        grail
                        endif::grail[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("grail", "")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("holy\ngrail", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("exclude with matching nested exclude")
            void excludeWithMatchingNestedExclude() throws Exception {
                var input = """
                        poof
                        ifdef::swallow[]
                        no
                        ifdef::swallow[]
                        swallow
                        endif::swallow[]
                        here
                        endif::swallow[]
                        gone
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("grail", "")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("poof\ngone", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("exclude with nested include using shorthand end")
            void excludeWithNestedIncludeUsingShorthandEnd() throws Exception {
                var input = """
                        poof
                        ifndef::grail[]
                        no grail
                        ifndef::swallow[]
                        or swallow
                        endif::[]
                        in here
                        endif::[]
                        gone
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("grail", "")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("poof\ngone", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifdef with one alternative attribute set includes content")
            void ifdefWithOneAlternativeAttributeSetIncludesContent() throws Exception {
                var input = """
                        ifdef::holygrail,swallow[]
                        Our quest is complete!
                        endif::holygrail,swallow[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("swallow", "")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("Our quest is complete!", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifdef with no alternative attributes set does not include content")
            void ifdefWithNoAlternativeAttributesSetDoesNotIncludeContent() throws Exception {
                var input = """
                        ifdef::holygrail,swallow[]
                        Our quest is complete!
                        endif::holygrail,swallow[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifdef with all required attributes set includes content")
            void ifdefWithAllRequiredAttributesSetIncludesContent() throws Exception {
                var input = """
                        ifdef::holygrail+swallow[]
                        Our quest is complete!
                        endif::holygrail+swallow[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("holygrail", "", "swallow", "")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("Our quest is complete!", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifdef with missing required attributes does not include content")
            void ifdefWithMissingRequiredAttributesDoesNotIncludeContent() throws Exception {
                var input = """
                        ifdef::holygrail+swallow[]
                        Our quest is complete!
                        endif::holygrail+swallow[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("holygrail", "")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifdef should permit leading, trailing, and repeat operators")
            void ifdefShouldPermitLeadingTrailingAndRepeatOperators() throws Exception {
                for (var entry : entries(smap("asciidoctor,", "content", ",asciidoctor", "content", "asciidoctor+", "", "+asciidoctor", "", "asciidoctor,,asciidoctor-version", "content", "asciidoctor++asciidoctor-version", ""))) {
                    var condition = entry.getKey();
                    var expected = entry.getValue();
                    var input = ("ifdef::" + str(condition) + "[]\ncontent\nendif::[]\n");
                    assertEquals(expected, documentFromString(input, Options.of("parse", false)).getReader().read());
                }
            }

            @Test
            @DisplayName("ifndef with undefined attribute includes block")
            void ifndefWithUndefinedAttributeIncludesBlock() throws Exception {
                var input = """
                        ifndef::holygrail[]
                        Our quest continues to find the holy grail!
                        endif::holygrail[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("Our quest continues to find the holy grail!", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifndef with one alternative attribute set does not include content")
            void ifndefWithOneAlternativeAttributeSetDoesNotIncludeContent() throws Exception {
                var input = """
                        ifndef::holygrail,swallow[]
                        Our quest is complete!
                        endif::holygrail,swallow[]
                        """;
                var result = new Document(input, Options.of("attributes", Options.of("swallow", ""))).getReader().read();
                assertEmpty(result);
            }

            @Test
            @DisplayName("ifndef with both alternative attributes set does not include content")
            void ifndefWithBothAlternativeAttributesSetDoesNotIncludeContent() throws Exception {
                var input = """
                        ifndef::holygrail,swallow[]
                        Our quest is complete!
                        endif::holygrail,swallow[]
                        """;
                var result = new Document(input, Options.of("attributes", Options.of("swallow", "", "holygrail", ""))).getReader().read();
                assertEmpty(result);
            }

            @Test
            @DisplayName("ifndef with no alternative attributes set includes content")
            void ifndefWithNoAlternativeAttributesSetIncludesContent() throws Exception {
                var input = """
                        ifndef::holygrail,swallow[]
                        Our quest is complete!
                        endif::holygrail,swallow[]
                        """;
                var result = new Document(input).getReader().read();
                assertEquals("Our quest is complete!", result);
            }

            @Test
            @DisplayName("ifndef with no required attributes set includes content")
            void ifndefWithNoRequiredAttributesSetIncludesContent() throws Exception {
                var input = """
                        ifndef::holygrail+swallow[]
                        Our quest is complete!
                        endif::holygrail+swallow[]
                        """;
                var result = new Document(input).getReader().read();
                assertEquals("Our quest is complete!", result);
            }

            @Test
            @DisplayName("ifndef with all required attributes set does not include content")
            void ifndefWithAllRequiredAttributesSetDoesNotIncludeContent() throws Exception {
                var input = """
                        ifndef::holygrail+swallow[]
                        Our quest is complete!
                        endif::holygrail+swallow[]
                        """;
                var result = new Document(input, Options.of("attributes", Options.of("swallow", "", "holygrail", ""))).getReader().read();
                assertEmpty(result);
            }

            @Test
            @DisplayName("ifndef with at least one required attributes set does not include content")
            void ifndefWithAtLeastOneRequiredAttributesSetDoesNotIncludeContent() throws Exception {
                var input = """
                        ifndef::holygrail+swallow[]
                        Our quest is complete!
                        endif::holygrail+swallow[]
                        """;
                var result = new Document(input, Options.of("attributes", Options.of("swallow", ""))).getReader().read();
                assertEquals("Our quest is complete!", result);
            }

            @Test
            @DisplayName("ifdef around empty line does not introduce extra line")
            void ifdefAroundEmptyLineDoesNotIntroduceExtraLine() throws Exception {
                var input = """
                        before
                        ifdef::no-such-attribute[]

                        endif::[]
                        after
                        """;
                var result = new Document(input).getReader().read();
                assertEquals("before\nafter", result);
            }

            @Test
            @DisplayName("should log warning if endif is unmatched")
            void shouldLogWarningIfEndifIsUnmatched() throws Exception {
                var input = """
                        Our quest is complete!
                        endif::on-quest[]
                        """;
                try (var logger = usingMemoryLogger()) {
                    var result = new Document(input, Options.of("attributes", Options.of("on-quest", ""))).getReader().read();
                    assertEquals("Our quest is complete!", result);
                    assertMessage(logger, "ERROR", "~<stdin>: line 2: unmatched preprocessor directive: endif::on-quest[]", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should log warning if endif is mismatched")
            void shouldLogWarningIfEndifIsMismatched() throws Exception {
                var input = """
                        ifdef::on-quest[]
                        Our quest is complete!
                        endif::on-journey[]
                        """;
                try (var logger = usingMemoryLogger()) {
                    var result = new Document(input, Options.of("attributes", Options.of("on-quest", ""), "sourcemap", true)).getReader().read();
                    assertEquals("Our quest is complete!", result);
                    assertMessages(logger, list(list("ERROR", "~<stdin>: line 3: mismatched preprocessor directive: endif::on-journey[]", LogMessage.class), list("ERROR", "~<stdin>: line 1: detected unterminated preprocessor conditional directive: ifdef::on-quest[]", LogMessage.class)));
                }
            }

            @Test
            @DisplayName("should log warning if endif contains text")
            void shouldLogWarningIfEndifContainsText() throws Exception {
                var input = """
                        ifdef::on-quest[]
                        Our quest is complete!
                        endif::on-quest[complete!]
                        fin
                        """;
                try (var logger = usingMemoryLogger()) {
                    var result = new Document(input, Options.of("attributes", Options.of("on-quest", ""), "sourcemap", true)).getReader().read();
                    assertEquals("Our quest is complete!\nfin", result);
                    assertMessages(logger, list(list("ERROR", "~<stdin>: line 3: malformed preprocessor directive - text not permitted: endif::on-quest[complete!]", LogMessage.class), list("ERROR", "~<stdin>: line 1: detected unterminated preprocessor conditional directive: ifdef::on-quest[]", LogMessage.class)));
                }
            }

            @Test
            @DisplayName("escaped ifdef is unescaped and ignored")
            void escapedIfdefIsUnescapedAndIgnored() throws Exception {
                var input = """
                        \\ifdef::holygrail[]
                        content
                        \\endif::holygrail[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("ifdef::holygrail[]\ncontent\nendif::holygrail[]", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifeval comparing missing attribute to nil includes content")
            void ifevalComparingMissingAttributeToNilIncludesContent() throws Exception {
                var input = """
                        ifeval::['{foo}' == '']
                        No foo for you!
                        endif::[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("No foo for you!", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifeval comparing missing attribute to 0 drops content")
            void ifevalComparingMissingAttributeTo0DropsContent() throws Exception {
                var input = """
                        ifeval::[{leveloffset} == 0]
                        I didn't make the cut!
                        endif::[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifeval running unsupported operation on missing attribute drops content")
            void ifevalRunningUnsupportedOperationOnMissingAttributeDropsContent() throws Exception {
                var input = """
                        ifeval::[{leveloffset} >= 3]
                        I didn't make the cut!
                        endif::[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifeval running invalid operation drops content")
            void ifevalRunningInvalidOperationDropsContent() throws Exception {
                var input = """
                        ifeval::[{asciidoctor-version} > true]
                        I didn't make the cut!
                        endif::[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifeval comparing double-quoted attribute to matching string includes content")
            void ifevalComparingDoubleQuotedAttributeToMatchingStringIncludesContent() throws Exception {
                var input = """
                        ifeval::["{gem}" == "asciidoctor"]
                        Asciidoctor it is!
                        endif::[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("gem", "asciidoctor")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("Asciidoctor it is!", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifeval comparing single-quoted attribute to matching string includes content")
            void ifevalComparingSingleQuotedAttributeToMatchingStringIncludesContent() throws Exception {
                var input = """
                        ifeval::['{gem}' == 'asciidoctor']
                        Asciidoctor it is!
                        endif::[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("gem", "asciidoctor")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("Asciidoctor it is!", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifeval comparing quoted attribute to non-matching string drops content")
            void ifevalComparingQuotedAttributeToNonMatchingStringDropsContent() throws Exception {
                var input = """
                        ifeval::['{gem}' == 'asciidoctor']
                        Asciidoctor it is!
                        endif::[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("gem", "tilt")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifeval comparing attribute to lower version number includes content")
            void ifevalComparingAttributeToLowerVersionNumberIncludesContent() throws Exception {
                var input = """
                        ifeval::['{asciidoctor-version}' >= '0.1.0']
                        That version will do!
                        endif::[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("That version will do!", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifeval comparing attribute to self includes content")
            void ifevalComparingAttributeToSelfIncludesContent() throws Exception {
                var input = """
                        ifeval::['{asciidoctor-version}' == '{asciidoctor-version}']
                        Of course it's the same!
                        endif::[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("Of course it's the same!", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifeval arguments can be transposed")
            void ifevalArgumentsCanBeTransposed() throws Exception {
                var input = """
                        ifeval::['0.1.0' <= '{asciidoctor-version}']
                        That version will do!
                        endif::[]
                        """;
                var doc = new Document(input);
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("That version will do!", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifeval matching numeric equality includes content")
            void ifevalMatchingNumericEqualityIncludesContent() throws Exception {
                var input = """
                        ifeval::[{rings} == 1]
                        One ring to rule them all!
                        endif::[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("rings", "1")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("One ring to rule them all!", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("ifeval matching numeric inequality includes content")
            void ifevalMatchingNumericInequalityIncludesContent() throws Exception {
                var input = """
                        ifeval::[{rings} != 0]
                        One ring to rule them all!
                        endif::[]
                        """;
                var doc = new Document(input, Options.of("attributes", Options.of("rings", "1")));
                var reader = (PreprocessorReader) doc.getReader();
                var lines = new ArrayList<String>();
                while (reader.hasMoreLines()) {
                    lines.add(reader.readLine());
                }
                assertEquals("One ring to rule them all!", join(lines, Asciidoctor.LF));
            }

            @Test
            @DisplayName("should log error if ifeval has target")
            void shouldLogErrorIfIfevalHasTarget() throws Exception {
                var input = """
                        ifeval::target[1 == 1]
                        content
                        """;
                try (var logger = usingMemoryLogger()) {
                    var doc = new Document(input);
                    var reader = (PreprocessorReader) doc.getReader();
                    var lines = new ArrayList<String>();
                    while (reader.hasMoreLines()) {
                        lines.add(reader.readLine());
                    }
                    assertEquals("content", join(lines, Asciidoctor.LF));
                    assertMessage(logger, "ERROR", "~<stdin>: line 1: malformed preprocessor directive - target not permitted: ifeval::target[1 == 1]", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should log error if ifeval has invalid expression")
            void shouldLogErrorIfIfevalHasInvalidExpression() throws Exception {
                var input = """
                        ifeval::[1 | 2]
                        content
                        """;
                try (var logger = usingMemoryLogger()) {
                    var doc = new Document(input);
                    var reader = (PreprocessorReader) doc.getReader();
                    var lines = new ArrayList<String>();
                    while (reader.hasMoreLines()) {
                        lines.add(reader.readLine());
                    }
                    assertEquals("content", join(lines, Asciidoctor.LF));
                    assertMessage(logger, "ERROR", "~<stdin>: line 1: malformed preprocessor directive - invalid expression: ifeval::[1 | 2]", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should log error if ifeval is missing expression")
            void shouldLogErrorIfIfevalIsMissingExpression() throws Exception {
                var input = """
                        ifeval::[]
                        content
                        """;
                try (var logger = usingMemoryLogger()) {
                    var doc = new Document(input);
                    var reader = (PreprocessorReader) doc.getReader();
                    var lines = new ArrayList<String>();
                    while (reader.hasMoreLines()) {
                        lines.add(reader.readLine());
                    }
                    assertEquals("content", join(lines, Asciidoctor.LF));
                    assertMessage(logger, "ERROR", "~<stdin>: line 1: malformed preprocessor directive - missing expression: ifeval::[]", LogMessage.class);
                }
            }

            @Test
            @DisplayName("ifdef with no target is ignored")
            void ifdefWithNoTargetIsIgnored() throws Exception {
                var input = """
                        ifdef::[]
                        content
                        """;
                try (var logger = usingMemoryLogger()) {
                    var doc = new Document(input);
                    var reader = (PreprocessorReader) doc.getReader();
                    var lines = new ArrayList<String>();
                    while (reader.hasMoreLines()) {
                        lines.add(reader.readLine());
                    }
                    assertEquals("content", join(lines, Asciidoctor.LF));
                    assertMessage(logger, "ERROR", "~<stdin>: line 1: malformed preprocessor directive - missing target: ifdef::[]", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should not warn about invalid ifdef preprocessor directive if already skipping")
            void shouldNotWarnAboutInvalidIfdefPreprocessorDirectiveIfAlreadySkipping() throws Exception {
                var input = """
                        ifdef::attribute-not-set[]
                        foo
                        ifdef::[]
                        bar
                        endif::[]
                        baz
                        """;
                try (var logger = usingMemoryLogger()) {
                    var result = new Document(input).getReader().read();
                    assertEquals("baz", result);
                    assertEmpty(logger);
                }
            }

            @Test
            @DisplayName("should not warn about invalid ifeval preprocessor directive if already skipping")
            void shouldNotWarnAboutInvalidIfevalPreprocessorDirectiveIfAlreadySkipping() throws Exception {
                var input = """
                        ifdef::attribute-not-set[]
                        foo
                        ifeval::[]
                        bar
                        endif::[]
                        baz
                        """;
                try (var logger = usingMemoryLogger()) {
                    var result = new Document(input).getReader().read();
                    assertEquals("baz", result);
                    assertEmpty(logger);
                }
            }

            @Test
            @DisplayName("should log error with end position if preprocessor conditional directive is unterminated")
            void shouldLogErrorWithEndPositionIfPreprocessorConditionalDirectiveIsUnterminated() throws Exception {
                var input = """
                        before
                        ifdef::not-set[]
                        skip
                        these
                        lines
                        fin
                        """;
                try (var logger = usingMemoryLogger()) {
                    var doc = new Document(input);
                    var reader = (PreprocessorReader) doc.getReader();
                    var lines = new ArrayList<String>();
                    while (reader.hasMoreLines()) {
                        lines.add(reader.readLine());
                    }
                    assertEquals("before", join(lines, Asciidoctor.LF));
                    assertMessage(logger, "ERROR", "~<stdin>: line 6: detected unterminated preprocessor conditional directive: ifdef::not-set[]", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should log error with start location if preprocessor conditional directive is unterminated and sourcemap is set")
            void shouldLogErrorWithStartLocationIfPreprocessorConditionalDirectiveIsUnterminatedAndSourcemapIsSet() throws Exception {
                var input = """
                        before
                        ifdef::not-set[]
                        skip
                        these
                        lines
                        fin
                        """;
                try (var logger = usingMemoryLogger()) {
                    var doc = new Document(input, Options.of("sourcemap", true));
                    var reader = (PreprocessorReader) doc.getReader();
                    var lines = new ArrayList<String>();
                    while (reader.hasMoreLines()) {
                        lines.add(reader.readLine());
                    }
                    assertEquals("before", join(lines, Asciidoctor.LF));
                    assertMessage(logger, "ERROR", "~<stdin>: line 2: detected unterminated preprocessor conditional directive: ifdef::not-set[]", LogMessage.class);
                }
            }

            @Test
            @DisplayName("should log error if multiple preprocessor conditional directives are unterminated")
            void shouldLogErrorIfMultiplePreprocessorConditionalDirectivesAreUnterminated() throws Exception {
                var input = """
                        before
                        ifdef::not-set[]
                        skip
                        these
                        lines
                        ifeval::[1 == 2]
                        {asciidoctor-version}
                        fin
                        """;
                try (var logger = usingMemoryLogger()) {
                    var doc = new Document(input, Options.of("sourcemap", true));
                    var reader = (PreprocessorReader) doc.getReader();
                    var lines = new ArrayList<String>();
                    while (reader.hasMoreLines()) {
                        lines.add(reader.readLine());
                    }
                    assertEquals("before", join(lines, Asciidoctor.LF));
                    assertMessages(logger, list(list("ERROR", "~<stdin>: line 2: detected unterminated preprocessor conditional directive: ifdef::not-set[]", LogMessage.class), list("ERROR", "~<stdin>: line 6: detected unterminated preprocessor conditional directive: ifeval::[1 == 2]", LogMessage.class)));
                }
            }

            @Test
            @DisplayName("should not fail to process preprocessor directive that evaluates to false and has a large number of lines")
            void shouldNotFailToProcessPreprocessorDirectiveThatEvaluatesToFalseAndHasALargeNumberOfLines() throws Exception {
                var lines = join(multiply(list("data"), 5000), "\n");
                var input = ("before\n\nifdef::attribute-not-set[]\n" + str(lines) + "\nendif::attribute-not-set[]\n\nafter\n");
                var doc = Asciidoctor.load(input);
                assertEquals(2, size(doc.getBlocks()));
                assertEquals("before", ((Block) doc.getBlocks().get(0)).getSource());
                assertEquals("after", ((Block) doc.getBlocks().get(1)).getSource());
            }

            @Test
            @DisplayName("should not fail to process lines if reader contains a nil entry")
            void shouldNotFailToProcessLinesIfReaderContainsANilEntry() throws Exception {
                var input = list("before", "", "", "", "after");
                var doc = Asciidoctor.load(input, Options.of("extensions", (Group) registry -> {
                    registry.preprocessor(preprocessor -> preprocessor.process((document, reader) -> {
                        reader.getSourceLines().set(2, null);
                        return null;
                    }));
                }));
                assertEquals(2, size(doc.getBlocks()));
                assertEquals("before", ((Block) doc.getBlocks().get(0)).getSource());
                assertEquals("after", ((Block) doc.getBlocks().get(1)).getSource());
            }
        }
    }
}
