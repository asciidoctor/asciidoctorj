package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.*;
import org.asciidoctor.core.syntaxhighlighter.HtmlPipelineAdapter;
import java.nio.file.Files;
import org.asciidoctor.core.syntaxhighlighter.SyntaxHighlighterFactory;
import org.asciidoctor.core.syntaxhighlighter.SyntaxHighlighter.HighlightResult;
import org.asciidoctor.core.syntaxhighlighter.SyntaxHighlighter;
import org.asciidoctor.core.syntaxhighlighter.AbstractSyntaxHighlighter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;

import org.junit.jupiter.api.*;

@DisplayName("API")
public class ApiTest extends TestHelper {
    Object table;

    @Nested
    @DisplayName("Load")
    class Load {

        @Test
        @DisplayName("should load input file")
        void shouldLoadInputFile() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var doc = Asciidoctor.load(Path.of(sampleInputPath), Options.of("safe", SafeMode.SAFE.getLevel()));
            assertEquals("Document Title", doc.getDoctitle());
            assertEquals(fileExpandPath(sampleInputPath), doc.getAttribute("docfile"));
            assertEquals(fileExpandPath(fileDirname(sampleInputPath)), doc.getAttribute("docdir"));
            assertEquals(".adoc", doc.getAttribute("docfilesuffix"));
        }

        @Test
        @DisplayName("should load input file from filename")
        void shouldLoadInputFileFromFilename() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var doc = Asciidoctor.loadFile(sampleInputPath, Options.of("safe", SafeMode.SAFE.getLevel()));
            assertEquals("Document Title", doc.getDoctitle());
            assertEquals(fileExpandPath(sampleInputPath), doc.getAttribute("docfile"));
            assertEquals(fileExpandPath(fileDirname(sampleInputPath)), doc.getAttribute("docdir"));
            assertEquals(".adoc", doc.getAttribute("docfilesuffix"));
        }

        @Test
        @DisplayName("should load input file from pathname")
        void shouldLoadInputFileFromPathname() throws Exception {
            var sampleInputPath = Path.of(fixturePath("sample.adoc"));
            var doc = Asciidoctor.loadFile(sampleInputPath, Options.of("safe", "safe"));
            assertEquals("Document Title", doc.getDoctitle());
            assertEquals(Helpers.absolutePath(sampleInputPath), doc.getAttribute("docfile"));
            assertEquals(Helpers.dirname(Helpers.absolutePath(sampleInputPath)), doc.getAttribute("docdir"));
            assertEquals(".adoc", doc.getAttribute("docfilesuffix"));
        }

        @Test
        @DisplayName("should load input file with alternate file extension")
        void shouldLoadInputFileWithAlternateFileExtension() throws Exception {
            var sampleInputPath = fixturePath("sample-alt-extension.asciidoc");
            var doc = Asciidoctor.loadFile(sampleInputPath, Options.of("safe", "safe"));
            assertEquals("Document Title", doc.getDoctitle());
            assertEquals(fileExpandPath(sampleInputPath), doc.getAttribute("docfile"));
            assertEquals(fileExpandPath(fileDirname(sampleInputPath)), doc.getAttribute("docdir"));
            assertEquals(".asciidoc", doc.getAttribute("docfilesuffix"));
        }

        @Test
        @Disabled("the default external and internal encodings are specific to Ruby (Java always reads files as UTF-8)")
        @DisplayName("should coerce encoding of file to UTF-8")
        void shouldCoerceEncodingOfFileToUTF8() throws Exception {
        }

        @Test
        @DisplayName("should not load file with unrecognized encoding")
        void shouldNotLoadFileWithUnrecognizedEncoding() throws Exception {
            var tmpInput = Files.createTempFile("test-", ".adoc");
            try {
                // NOTE using a character whose code differs between UTF-8 and IBM437
                Files.write(tmpInput, new byte[] { (byte) 0x9f, '\n' });
                var exception = assertThrows(IllegalArgumentException.class, () -> {
                    Asciidoctor.loadFile(tmpInput.toString(), Options.of("safe", "safe"));
                });
                var expectedMessage = "Failed to load AsciiDoc document - source is either binary or contains invalid Unicode data";
                assertInclude(expectedMessage, exception.getMessage());
            } finally {
                Files.deleteIfExists(tmpInput);
            }
        }

        @Test
        @DisplayName("should not load invalid file")
        void shouldNotLoadInvalidFile() throws Exception {
            var sampleInputPath = fixturePath("hello-asciidoctor.pdf");
            var exception = assertThrows(IllegalArgumentException.class, () -> Asciidoctor.loadFile(sampleInputPath, Options.of("safe", SafeMode.SAFE.getLevel())));
            var expectedMessage = "Failed to load AsciiDoc document - source is either binary or contains invalid Unicode data";
            assertInclude(expectedMessage, exception.getMessage());
        }

        @Test
        @DisplayName("should convert filename that contains non-ASCII characters independent of default encodings")
        void shouldConvertFilenameThatContainsNonASCIICharactersIndependentOfDefaultEncodings() throws Exception {
            var tmpInput = Files.createTempFile("test-ＵＴＦ８-", ".adoc");
            var tmpOutput = tmpInput.toString().replace(".adoc", ".html");
            try {
                Files.writeString(tmpInput, "ＵＴＦ８\n");
                Asciidoctor.convertFile(tmpInput.toString(), Options.of("safe", "safe", "attributes", "linkcss !copycss"));
                assertPathExists(tmpOutput);
                var output = readFile(tmpOutput);
                refuteEmpty(output);
                assertInclude("ＵＴＦ８", output);
            } finally {
                Files.deleteIfExists(tmpInput);
                removeFile(tmpOutput);
            }
        }

        @Test
        @DisplayName("should load input IO")
        void shouldLoadInputIO() throws Exception {
            var input = new StringReader("""
                    Document Title
                    ==============

                    preamble
                    """);
            var doc = Asciidoctor.load(input, Options.of("safe", SafeMode.SAFE.getLevel()));
            assertEquals("Document Title", doc.getDoctitle());
            assertFalsy(doc.hasAttribute("docfile"));
            assertEquals(doc.getBaseDir(), doc.getAttribute("docdir"));
        }

        @Test
        @DisplayName("should load input string")
        void shouldLoadInputString() throws Exception {
            var input = """
                    Document Title
                    ==============

                    preamble
                    """;
            var doc = Asciidoctor.load(input, Options.of("safe", SafeMode.SAFE.getLevel()));
            assertEquals("Document Title", doc.getDoctitle());
            assertFalsy(doc.hasAttribute("docfile"));
            assertEquals(doc.getBaseDir(), doc.getAttribute("docdir"));
        }

        @Test
        @DisplayName("should load input string array")
        void shouldLoadInputStringArray() throws Exception {
            var input = """
                    Document Title
                    ==============

                    preamble
                    """;
            var doc = Asciidoctor.load(lines(input), Options.of("safe", SafeMode.SAFE.getLevel()));
            assertEquals("Document Title", doc.getDoctitle());
            assertFalsy(doc.hasAttribute("docfile"));
            assertEquals(doc.getBaseDir(), doc.getAttribute("docdir"));
        }

        @Test
        @DisplayName("should load nil input")
        void shouldLoadNilInput() throws Exception {
            var doc = Asciidoctor.load(null, Options.of("safe", "safe"));
            assertNotNull(doc);
            assertEmpty(doc.getBlocks());
        }

        @Test
        @DisplayName("should ignore :to_file option if value is truthy but not a string")
        void shouldIgnoreTo_fileOptionIfValueIsTruthyButNotAString() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var doc = Asciidoctor.loadFile(sampleInputPath, Options.of("safe", "safe", "to_file", true));
            assertNotNull(doc);
            assertEquals("Document Title", doc.getDoctitle());
            assertEquals(".html", doc.getAttribute("outfilesuffix"));
            assertEquals(doc.convert(), Asciidoctor.convertFile(sampleInputPath, Options.of("safe", "safe", "to_file", false)));
        }

        @Test
        @DisplayName("should set outfilesuffix attribute to file extension of value of :to_file option if value is a string")
        void shouldSetOutfilesuffixAttributeToFileExtensionOfValueOfTo_fileOptionIfValueIsAString() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var doc = Asciidoctor.loadFile(sampleInputPath, Options.of("safe", "safe", "to_file", "out.htm"));
            assertNotNull(doc);
            assertEquals("Document Title", doc.getDoctitle());
            assertEquals(".htm", doc.getAttribute("outfilesuffix"));
        }

        @Test
        @DisplayName("should accept attributes as array")
        void shouldAcceptAttributesAsArray() throws Exception {
            var doc = Asciidoctor.load("text", Options.of("attributes", list("toc", "sectnums", "source-highlighter=coderay", "idprefix", "idseparator=-")));
            assertInstanceOf(Map.class, doc.getAttributes());
            assertTruthy(doc.hasAttribute("toc"));
            assertEquals("", doc.getAttribute("toc"));
            assertTruthy(doc.hasAttribute("sectnums"));
            assertEquals("", doc.getAttribute("sectnums"));
            assertTruthy(doc.hasAttribute("source-highlighter"));
            assertEquals("coderay", doc.getAttribute("source-highlighter"));
            assertTruthy(doc.hasAttribute("idprefix"));
            assertEquals("", doc.getAttribute("idprefix"));
            assertTruthy(doc.hasAttribute("idseparator"));
            assertEquals("-", doc.getAttribute("idseparator"));
        }

        @Test
        @DisplayName("should accept attributes as empty array")
        void shouldAcceptAttributesAsEmptyArray() throws Exception {
            var doc = Asciidoctor.load("text", Options.of("attributes", new ArrayList<String>()));
            assertInstanceOf(Map.class, doc.getAttributes());
        }

        @Test
        @DisplayName("should accept attributes as string")
        void shouldAcceptAttributesAsString() throws Exception {
            var doc = Asciidoctor.load("text", Options.of("attributes", "toc sectnums\nsource-highlighter=coderay\nidprefix\nidseparator=-"));
            assertInstanceOf(Map.class, doc.getAttributes());
            assertTruthy(doc.hasAttribute("toc"));
            assertEquals("", doc.getAttribute("toc"));
            assertTruthy(doc.hasAttribute("sectnums"));
            assertEquals("", doc.getAttribute("sectnums"));
            assertTruthy(doc.hasAttribute("source-highlighter"));
            assertEquals("coderay", doc.getAttribute("source-highlighter"));
            assertTruthy(doc.hasAttribute("idprefix"));
            assertEquals("", doc.getAttribute("idprefix"));
            assertTruthy(doc.hasAttribute("idseparator"));
            assertEquals("-", doc.getAttribute("idseparator"));
        }

        @Test
        @DisplayName("should accept values containing spaces in attributes string")
        void shouldAcceptValuesContainingSpacesInAttributesString() throws Exception {
            var doc = Asciidoctor.load("text", Options.of("attributes", "idprefix idseparator=-   note-caption=Note\\ to\\\tself toc"));
            assertInstanceOf(Map.class, doc.getAttributes());
            assertTruthy(doc.hasAttribute("idprefix"));
            assertEquals("", doc.getAttribute("idprefix"));
            assertTruthy(doc.hasAttribute("idseparator"));
            assertEquals("-", doc.getAttribute("idseparator"));
            assertTruthy(doc.hasAttribute("note-caption"));
            assertEquals("Note to\tself", doc.getAttribute("note-caption"));
        }

        @Test
        @DisplayName("should accept attributes as empty string")
        void shouldAcceptAttributesAsEmptyString() throws Exception {
            var doc = Asciidoctor.load("text", Options.of("attributes", ""));
            assertInstanceOf(Map.class, doc.getAttributes());
        }

        @Test
        @DisplayName("should accept attributes as nil")
        void shouldAcceptAttributesAsNil() throws Exception {
            var doc = Asciidoctor.load("text", Options.of("attributes", null));
            assertInstanceOf(Map.class, doc.getAttributes());
        }

        @Test
        @DisplayName("should accept attributes if hash like")
        void shouldAcceptAttributesIfHashLike() throws Exception {
            // a Map implementation other than the built-in ones (the equivalent of a Hash-like object in Ruby)
            class Hashlike extends AbstractMap<String, Object> {
                private final Map<String, Object> table = Map.of("toc", "");

                @Override
                public Set<Map.Entry<String, Object>> entrySet() {
                    return table.entrySet();
                }
            }

            var doc = Asciidoctor.load("text", Options.of("attributes", new Hashlike()));
            assertInstanceOf(Map.class, doc.getAttributes());
            assertTruthy(doc.getAttributes().containsKey("toc"));
        }

        @Test
        @DisplayName("should not expand value of docdir attribute if specified via API")
        void shouldNotExpandValueOfDocdirAttributeIfSpecifiedViaAPI() throws Exception {
            var docdir = "virtual/directory";
            var doc = documentFromString("", Options.of("safe", "safe", "attributes", Options.of("docdir", docdir)));
            assertEquals(docdir, doc.getAttribute("docdir"));
            assertEquals(docdir, doc.getBaseDir());
        }

        @Test
        @DisplayName("converts block to output format when convert is called")
        void convertsBlockToOutputFormatWhenConvertIsCalled() throws Exception {
            var doc = Asciidoctor.load("paragraph text");
            var expected = Strings.chop("""
                    <div class="paragraph">
                    <p>paragraph text</p>
                    </div>
                    """);
            assertEquals(1, size(doc.getBlocks()));
            assertEquals("paragraph", doc.getBlocks().get(0).getContext());
            assertEquals(expected, doc.getBlocks().get(0).convert());
        }

        @Test
        @Disabled("method aliases are specific to the Ruby implementation")
        @DisplayName("render method on node is aliased to convert method")
        void renderMethodOnNodeIsAliasedToConvertMethod() throws Exception {
        }

        @Test
        @Disabled("method aliases are specific to the Ruby implementation")
        @DisplayName("content method on Inline node is aliased to text method")
        void contentMethodOnInlineNodeIsAliasedToTextMethod() throws Exception {
        }

        @Test
        @DisplayName("should output timestamps by default")
        void shouldOutputTimestampsByDefault() throws Exception {
            var doc = documentFromString("text", Options.of("backend", "html5", "attributes", null));
            var result = doc.convert();
            assertTruthy(doc.hasAttribute("docdate"));
            assertFalsy(doc.hasAttribute("reproducible"));
            assertXpath("//div[@id=\"footer-text\" and contains(string(.//text()), \"Last updated\")]", result, 1);
        }

        @Test
        @DisplayName("should not output timestamps if reproducible attribute is set in HTML 5")
        void shouldNotOutputTimestampsIfReproducibleAttributeIsSetInHTML5() throws Exception {
            var doc = documentFromString("text", Options.of("backend", "html5", "attributes", Options.of("reproducible", "")));
            var result = doc.convert();
            assertTruthy(doc.hasAttribute("docdate"));
            assertTruthy(doc.hasAttribute("reproducible"));
            assertXpath("//div[@id=\"footer-text\" and contains(string(.//text()), \"Last updated\")]", result, 0);
        }

        @Test
        @DisplayName("should not output meta generator tag in HTML 5 if reproducible attribute is set")
        void shouldNotOutputMetaGeneratorTagInHTML5IfReproducibleAttributeIsSet() throws Exception {
            var doc = documentFromString("text", Options.of("backend", "html5", "attributes", Options.of("reproducible", "")));
            var result = doc.convert();
            assertTruthy(doc.hasAttribute("reproducible"));
            assertXpath("//meta[@name=\"generator\"]", result, 0);
        }

        @Test
        @DisplayName("should not output timestamps if reproducible attribute is set in DocBook")
        void shouldNotOutputTimestampsIfReproducibleAttributeIsSetInDocBook() throws Exception {
            var doc = documentFromString("text", Options.of("backend", "docbook", "attributes", Options.of("reproducible", "")));
            var result = doc.convert();
            assertTruthy(doc.hasAttribute("docdate"));
            assertTruthy(doc.hasAttribute("reproducible"));
            assertXpath("/article/info/date", result, 0);
        }

        @Test
        @DisplayName("should not modify options argument")
        void shouldNotModifyOptionsArgument() throws Exception {
            var options = Options.of("safe", SafeMode.SAFE.getLevel());
            var sampleInputPath = fixturePath("sample.adoc");
            var doc = Asciidoctor.loadFile(sampleInputPath, options);
            assertNotSame(options, doc.getOptions());
        }

        @Test
        @DisplayName("should not modify attributes Hash argument")
        void shouldNotModifyAttributesHashArgument() throws Exception {
            var attributes = Options.of();
            var options = Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", attributes);
            var sampleInputPath = fixturePath("sample.adoc");
            var doc = Asciidoctor.loadFile(sampleInputPath, options);
            assertNotSame(attributes, get(doc.getOptions(), "attributes"));
            assertNotSame(attributes, doc.getAttributes());
        }

        @Test
        @DisplayName("should be able to restore header attributes after call to convert")
        void shouldBeAbleToRestoreHeaderAttributesAfterCallToConvert() throws Exception {
            var input = """
                    = Document Title
                    :foo: bar

                    content

                    :foo: baz

                    content
                    """;
            var doc = Asciidoctor.load(input);
            assertEquals("bar", doc.getAttribute("foo"));
            doc.convert();
            assertEquals("baz", doc.getAttribute("foo"));
            doc.restoreAttributes();
            assertEquals("bar", doc.getAttribute("foo"));
        }

        @Test
        @DisplayName("should track file and line information with blocks if sourcemap option is set")
        void shouldTrackFileAndLineInformationWithBlocksIfSourcemapOptionIsSet() throws Exception {
            var doc = Asciidoctor.loadFile(fixturePath("sample.adoc"), Options.of("sourcemap", true));
            assertNotNull(doc.getSourceLocation());
            assertEquals("sample.adoc", doc.getFile());
            assertEquals(1, doc.getLineNumber());
            var preamble = doc.getBlocks().get(0);
            assertNotNull(preamble.getSourceLocation());
            assertEquals("sample.adoc", preamble.getFile());
            assertEquals(6, preamble.getLineNumber());
            var section1 = doc.getSections().get(0);
            assertEquals("Section A", section1.getTitle());
            assertNotNull(section1.getSourceLocation());
            assertEquals("sample.adoc", section1.getFile());
            assertEquals(10, section1.getLineNumber());
            var section2 = doc.getSections().get(1);
            assertEquals("Section B", section2.getTitle());
            assertNotNull(section2.getSourceLocation());
            assertEquals("sample.adoc", section2.getFile());
            assertEquals(18, section2.getLineNumber());
            var tableBlock = section2.getBlocks().get(1);
            assertEquals("table", tableBlock.getContext());
            assertNotNull(tableBlock.getSourceLocation());
            assertEquals("sample.adoc", tableBlock.getFile());
            assertEquals(22, tableBlock.getLineNumber());
            var firstCell = ((Table) tableBlock).getRows().getBody().get(0).get(0);
            assertNotNull(firstCell.getSourceLocation());
            assertEquals("sample.adoc", firstCell.getFile());
            assertEquals(23, firstCell.getLineNumber());
            var secondCell = ((Table) tableBlock).getRows().getBody().get(0).get(1);
            assertNotNull(secondCell.getSourceLocation());
            assertEquals("sample.adoc", secondCell.getFile());
            assertEquals(23, secondCell.getLineNumber());
            var lastCell = last(last(((Table) tableBlock).getRows().getBody()));
            assertNotNull(lastCell.getSourceLocation());
            assertEquals("sample.adoc", lastCell.getFile());
            assertEquals(24, lastCell.getLineNumber());
            var lastBlock = last(section2.getBlocks());
            assertEquals("ulist", lastBlock.getContext());
            assertNotNull(lastBlock.getSourceLocation());
            assertEquals("sample.adoc", lastBlock.getFile());
            assertEquals(28, lastBlock.getLineNumber());
            var listItems = lastBlock.getBlocks();
            assertNotNull(listItems.get(0).getSourceLocation());
            assertEquals("sample.adoc", listItems.get(0).getFile());
            assertEquals(28, listItems.get(0).getLineNumber());
            assertNotNull(listItems.get(1).getSourceLocation());
            assertEquals("sample.adoc", listItems.get(1).getFile());
            assertEquals(29, listItems.get(1).getLineNumber());
            assertNotNull(listItems.get(2).getSourceLocation());
            assertEquals("sample.adoc", listItems.get(2).getFile());
            assertEquals(30, listItems.get(2).getLineNumber());
            doc = Asciidoctor.loadFile(fixturePath("main.adoc"), Options.of("sourcemap", true, "safe", "safe"));
            section1 = doc.getSections().get(0);
            assertEquals("Chapter A", section1.getTitle());
            assertNotNull(section1.getSourceLocation());
            assertEquals(fixturePath("chapter-a.adoc"), section1.getFile());
            assertEquals(1, section1.getLineNumber());
        }

        @Test
        @DisplayName("should track file and line information on list items if sourcemap option is set")
        void shouldTrackFileAndLineInformationOnListItemsIfSourcemapOptionIsSet() throws Exception {
            var doc = Asciidoctor.loadFile(fixturePath("lists.adoc"), Options.of("sourcemap", true));
            var firstSection = doc.getBlocks().get(1);
            var unorderedBasicList = firstSection.getBlocks().get(0);
            assertEquals(11, unorderedBasicList.getLineNumber());
            var unorderedBasicListItems = unorderedBasicList.findBy(AbstractBlock.Selector.any().withContext("list_item"));
            assertEquals(11, unorderedBasicListItems.get(0).getLineNumber());
            assertEquals(12, unorderedBasicListItems.get(1).getLineNumber());
            assertEquals(13, unorderedBasicListItems.get(2).getLineNumber());
            var unorderedMaxNesting = firstSection.getBlocks().get(1);
            assertEquals(16, unorderedMaxNesting.getLineNumber());
            var unorderedMaxNestingItems = unorderedMaxNesting.findBy(AbstractBlock.Selector.any().withContext("list_item"));
            assertEquals(16, unorderedMaxNestingItems.get(0).getLineNumber());
            assertEquals(17, unorderedMaxNestingItems.get(1).getLineNumber());
            assertEquals(18, unorderedMaxNestingItems.get(2).getLineNumber());
            assertEquals(19, unorderedMaxNestingItems.get(3).getLineNumber());
            assertEquals(20, unorderedMaxNestingItems.get(4).getLineNumber());
            assertEquals(21, unorderedMaxNestingItems.get(5).getLineNumber());
            var checklist = firstSection.getBlocks().get(2);
            assertEquals(24, checklist.getLineNumber());
            var checklistListItems = checklist.findBy(AbstractBlock.Selector.any().withContext("list_item"));
            assertEquals(24, checklistListItems.get(0).getLineNumber());
            assertEquals(25, checklistListItems.get(1).getLineNumber());
            assertEquals(26, checklistListItems.get(2).getLineNumber());
            assertEquals(27, checklistListItems.get(3).getLineNumber());
            var orderedBasic = firstSection.getBlocks().get(3);
            assertEquals(30, orderedBasic.getLineNumber());
            var orderedBasicListItems = orderedBasic.findBy(AbstractBlock.Selector.any().withContext("list_item"));
            assertEquals(30, orderedBasicListItems.get(0).getLineNumber());
            assertEquals(31, orderedBasicListItems.get(1).getLineNumber());
            assertEquals(32, orderedBasicListItems.get(2).getLineNumber());
            var orderedNested = firstSection.getBlocks().get(4);
            assertEquals(35, orderedNested.getLineNumber());
            var orderedNestedListItems = orderedNested.findBy(AbstractBlock.Selector.any().withContext("list_item"));
            assertEquals(35, orderedNestedListItems.get(0).getLineNumber());
            assertEquals(36, orderedNestedListItems.get(1).getLineNumber());
            assertEquals(37, orderedNestedListItems.get(2).getLineNumber());
            assertEquals(38, orderedNestedListItems.get(3).getLineNumber());
            assertEquals(39, orderedNestedListItems.get(4).getLineNumber());
            var orderedMaxNesting = firstSection.getBlocks().get(5);
            assertEquals(42, orderedMaxNesting.getLineNumber());
            var orderedMaxNestingItems = orderedMaxNesting.findBy(AbstractBlock.Selector.any().withContext("list_item"));
            assertEquals(42, orderedMaxNestingItems.get(0).getLineNumber());
            assertEquals(43, orderedMaxNestingItems.get(1).getLineNumber());
            assertEquals(44, orderedMaxNestingItems.get(2).getLineNumber());
            assertEquals(45, orderedMaxNestingItems.get(3).getLineNumber());
            assertEquals(46, orderedMaxNestingItems.get(4).getLineNumber());
            assertEquals(47, orderedMaxNestingItems.get(5).getLineNumber());
            var labeledSingleline = firstSection.getBlocks().get(6);
            assertEquals(50, labeledSingleline.getLineNumber());
            var labeledSinglelineItems = labeledSingleline.findBy(AbstractBlock.Selector.any().withContext("list_item"));
            assertEquals(50, labeledSinglelineItems.get(0).getLineNumber());
            assertEquals(50, labeledSinglelineItems.get(1).getLineNumber());
            assertEquals(51, labeledSinglelineItems.get(2).getLineNumber());
            assertEquals(51, labeledSinglelineItems.get(3).getLineNumber());
            var labeledMultiline = firstSection.getBlocks().get(7);
            assertEquals(54, labeledMultiline.getLineNumber());
            var labeledMultilineItems = labeledMultiline.findBy(AbstractBlock.Selector.any().withContext("list_item"));
            assertEquals(54, labeledMultilineItems.get(0).getLineNumber());
            assertEquals(55, labeledMultilineItems.get(1).getLineNumber());
            assertEquals(56, labeledMultilineItems.get(2).getLineNumber());
            assertEquals(57, labeledMultilineItems.get(3).getLineNumber());
            var qanda = firstSection.getBlocks().get(8);
            assertEquals(61, qanda.getLineNumber());
            var qandaItems = qanda.findBy(AbstractBlock.Selector.any().withContext("list_item"));
            assertEquals(61, qandaItems.get(0).getLineNumber());
            assertEquals(62, qandaItems.get(1).getLineNumber());
            assertEquals(63, qandaItems.get(2).getLineNumber());
            assertEquals(63, qandaItems.get(3).getLineNumber());
            var mixed = firstSection.getBlocks().get(9);
            assertEquals(66, mixed.getLineNumber());
            var mixedItems = mixed.findBy(AbstractBlock.Selector.any().withContext("list_item"), filter(block -> ((ListItem) block).hasText()));
            assertEquals(66, mixedItems.get(0).getLineNumber());
            assertEquals(67, mixedItems.get(1).getLineNumber());
            assertEquals(68, mixedItems.get(2).getLineNumber());
            assertEquals(69, mixedItems.get(3).getLineNumber());
            assertEquals(70, mixedItems.get(4).getLineNumber());
            assertEquals(71, mixedItems.get(5).getLineNumber());
            assertEquals(72, mixedItems.get(6).getLineNumber());
            assertEquals(73, mixedItems.get(7).getLineNumber());
            assertEquals(74, mixedItems.get(8).getLineNumber());
            assertEquals(75, mixedItems.get(9).getLineNumber());
            assertEquals(77, mixedItems.get(10).getLineNumber());
            assertEquals(78, mixedItems.get(11).getLineNumber());
            assertEquals(79, mixedItems.get(12).getLineNumber());
            assertEquals(80, mixedItems.get(13).getLineNumber());
            assertEquals(81, mixedItems.get(14).getLineNumber());
            assertEquals(82, mixedItems.get(15).getLineNumber());
            assertEquals(83, mixedItems.get(16).getLineNumber());
            var unorderedComplexList = firstSection.getBlocks().get(10);
            assertEquals(86, unorderedComplexList.getLineNumber());
            var unorderedComplexItems = unorderedComplexList.findBy(AbstractBlock.Selector.any().withContext("list_item"));
            assertEquals(86, unorderedComplexItems.get(0).getLineNumber());
            assertEquals(87, unorderedComplexItems.get(1).getLineNumber());
            assertEquals(88, unorderedComplexItems.get(2).getLineNumber());
            assertEquals(92, unorderedComplexItems.get(3).getLineNumber());
            assertEquals(96, unorderedComplexItems.get(4).getLineNumber());
        }

        @Test
        @DisplayName("should assign incorrect lineno for paragraph inside a conditional preprocessor directive")
        void shouldAssignIncorrectLinenoForParagraphInsideAConditionalPreprocessorDirective() throws Exception {
            var input = """
                    :conditional-attribute:

                    before

                    ifdef::conditional-attribute[]
                    subject
                    endif::[]

                    after
                    """;
            var doc = documentFromString(input, Options.of("sourcemap", true));
            assertEquals(list(3, 7, 9), map(doc.findBy(AbstractBlock.Selector.any().withContext("paragraph")), it -> send(it, "lineno")));
        }

        @Test
        @DisplayName("should assign correct lineno for paragraph inside a conditional preprocessor directive when underline style off")
        void shouldAssignCorrectLinenoForParagraphInsideAConditionalPreprocessorDirectiveWhenUnderlineStyleOff() throws Exception {
            try {
                var input = """
                        :conditional-attribute:

                        before

                        ifdef::conditional-attribute[]
                        subject
                        endif::[]

                        after
                        """;
                Compliance.setUnderlineStyleSectionTitles(false);
                var doc = documentFromString(input, Options.of("sourcemap", true));
                assertEquals(list(3, 6, 9), map(doc.findBy(AbstractBlock.Selector.any().withContext("paragraph")), it -> send(it, "lineno")));
            } finally {
                Compliance.setUnderlineStyleSectionTitles(true);
            }
        }

        @Test
        @DisplayName("should assign correct lineno for multi-line paragraph inside a conditional preprocessor directive")
        void shouldAssignCorrectLinenoForMultiLineParagraphInsideAConditionalPreprocessorDirective() throws Exception {
            var input = """
                    :conditional-attribute:

                    before

                    ifdef::conditional-attribute[]
                    subject
                    subject
                    endif::[]

                    after
                    """;
            var doc = documentFromString(input, Options.of("sourcemap", true));
            assertEquals(list(3, 6, 10), map(doc.findBy(AbstractBlock.Selector.any().withContext("paragraph")), it -> send(it, "lineno")));
        }

        @Test
        @DisplayName("should assign correct source location to blocks that follow a detached list continuation")
        void shouldAssignCorrectSourceLocationToBlocksThatFollowADetachedListContinuation() throws Exception {
            var input = """
                    * parent
                     ** child

                    +
                    paragraph attached to parent

                    ****
                    sidebar outside list
                    ****
                    """;
            var doc = documentFromString(input, Options.of("sourcemap", true));
            assertEquals(list(5, 8), map(doc.findBy(AbstractBlock.Selector.any().withContext("paragraph")), it -> send(it, "lineno")));
        }

        @Test
        @DisplayName("should assign correct source location if section occurs on last line of input")
        void shouldAssignCorrectSourceLocationIfSectionOccursOnLastLineOfInput() throws Exception {
            var input = """
                    = Document Title

                    == Section A

                    content

                    == Section B
                    """;
            var doc = documentFromString(input, Options.of("sourcemap", true));
            assertEquals(list(1, 3, 7), map(doc.findBy(AbstractBlock.Selector.any().withContext("section")), it -> send(it, "lineno")));
        }

        @Test
        @DisplayName("should allow sourcemap option on document to be modified before document is parsed")
        void shouldAllowSourcemapOptionOnDocumentToBeModifiedBeforeDocumentIsParsed() throws Exception {
            var doc = Asciidoctor.loadFile(fixturePath("sample.adoc"), Options.of("parse", false));
            doc.setSourcemap(true);
            assertFalsy(doc.isParsed());
            doc = doc.parse();
            assertTruthy(doc.isParsed());
            var section1 = doc.getSections().get(0);
            assertEquals("Section A", section1.getTitle());
            assertNotNull(section1.getSourceLocation());
            assertEquals("sample.adoc", section1.getFile());
            assertEquals(10, section1.getLineNumber());
        }

        @Test
        @DisplayName("find_by should return Array of blocks anywhere in document tree that match criteria")
        void find_byShouldReturnArrayOfBlocksAnywhereInDocumentTreeThatMatchCriteria() throws Exception {
            var input = """
                    = Document Title

                    preamble

                    == Section A

                    paragraph

                    --
                    Exhibit A::
                    +
                    [#tiger.animal]
                    image::tiger.png[Tiger]
                    --

                    image::shoe.png[Shoe]

                    == Section B

                    paragraph
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any().withContext("image"));
            assertEquals(2, size(result));
            assertEquals("image", result.get(0).getContext());
            assertEquals("tiger.png", result.get(0).getAttribute("target"));
            assertEquals("image", result.get(1).getContext());
            assertEquals("shoe.png", result.get(1).getAttribute("target"));
        }

        @Test
        @DisplayName("find_by should return an empty Array if no matches are found")
        void find_byShouldReturnAnEmptyArrayIfNoMatchesAreFound() throws Exception {
            var input = "paragraph";
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any().withContext("section"));
            assertNotNull(result);
            assertEquals(0, size(result));
        }

        @Test
        @DisplayName("should only return matched node when return value of block argument is :prune")
        void shouldOnlyReturnMatchedNodeWhenReturnValueOfBlockArgumentIsPrune() throws Exception {
            var input = """
                    * foo
                     ** yin
                      *** zen
                     ** yang
                    * bar
                    * baz
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any().withContext("list_item"), filter(li -> {
                if (Objects.equals(((ListItem) li).getText(), "yin")) {
                    return "prune";
                } else {
                    return false;
                }
            }));
            assertEquals(1, size(result));
            assertEquals("yin", ((ListItem) result.get(0)).getText());
        }

        @Test
        @DisplayName("find_by should discover blocks inside AsciiDoc table cells if traverse_documents selector option is true")
        void find_byShouldDiscoverBlocksInsideAsciiDocTableCellsIfTraverse_documentsSelectorOptionIsTrue() throws Exception {
            var input = """
                    paragraph in parent document (before)

                    [%footer,cols=2*]
                    |===
                    a|
                    paragraph in nested document (body)
                    |normal table cell

                    a|
                    paragraph in nested document (foot)
                    |normal table cell
                    |===

                    paragraph in parent document (after)
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any().withContext("paragraph"));
            assertEquals(2, size(result));
            result = doc.findBy(AbstractBlock.Selector.any().withContext("paragraph").withTraverseDocuments(true));
            assertEquals(4, size(result));
        }

        @Test
        @DisplayName("find_by should return inner document of AsciiDoc table cell if traverse_documents selector option is true")
        void find_byShouldReturnInnerDocumentOfAsciiDocTableCellIfTraverse_documentsSelectorOptionIsTrue() throws Exception {
            var input = """
                    |===
                    a|paragraph in nested document
                    |===
                    """;
            var doc = Asciidoctor.load(input);
            var innerDoc = ((Table) doc.getBlocks().get(0)).getRows().getBody().get(0).get(0).getInnerDocument();
            var result = doc.findBy(AbstractBlock.Selector.any().withTraverseDocuments(true));
            assertInclude(innerDoc, result);
            result = doc.findBy(AbstractBlock.Selector.any().withContext("inner_document").withTraverseDocuments(true));
            assertEquals(1, size(result));
            assertEquals(innerDoc, result.get(0));
        }

        @Test
        @DisplayName("find_by should match table cells")
        void find_byShouldMatchTableCells() throws Exception {
            var input = """
                    |===
                    |a |b |c

                    |1
                    one
                    a|NOTE: 2, as it goes.
                    l|
                    3
                     you
                      me
                    |===
                    """;
            var doc = documentFromString(input);
            var table = doc.getBlocks().get(0);
            var firstHeadCell = ((Table) table).getRows().getHead().get(0).get(0);
            var firstBodyCell = ((Table) table).getRows().getBody().get(0).get(0);
            var result = doc.findBy(AbstractBlock.Selector.any());
            assertInclude(firstHeadCell, result);
            assertInclude(firstBodyCell, result);
            assertEquals("a", firstHeadCell.getSource());
            assertEquals(list("a"), lines(firstHeadCell));
            assertEquals("1\none", firstBodyCell.getSource());
            assertEquals(list("1", "one"), lines(firstBodyCell));
            result = doc.findBy(AbstractBlock.Selector.any().withContext("table_cell").withStyle("asciidoc"));
            assertEquals(1, size(result));
            assertInstanceOf(Table.Cell.class, result.get(0));
            assertEquals("asciidoc", result.get(0).getStyle());
            assertEquals("NOTE: 2, as it goes.", ((Table.Cell) result.get(0)).getSource());
        }

        @Test
        @DisplayName("find_by should return Array of blocks that match style criteria")
        void find_byShouldReturnArrayOfBlocksThatMatchStyleCriteria() throws Exception {
            var input = """
                    [square]
                    * one
                    * two
                    * three

                    ---

                    * apples
                    * bananas
                    * pears
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any().withContext("ulist").withStyle("square"));
            assertEquals(1, size(result));
            assertEquals("ulist", result.get(0).getContext());
        }

        @Test
        @DisplayName("find_by should return Array of blocks that match role criteria")
        void find_byShouldReturnArrayOfBlocksThatMatchRoleCriteria() throws Exception {
            var input = """
                    [#tiger.animal]
                    image::tiger.png[Tiger]

                    image::shoe.png[Shoe]
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any().withContext("image").withRole("animal"));
            assertEquals(1, size(result));
            assertEquals("image", result.get(0).getContext());
            assertEquals("tiger.png", result.get(0).getAttribute("target"));
        }

        @Test
        @DisplayName("find_by should return the document title section if context selector is :section")
        void find_byShouldReturnTheDocumentTitleSectionIfContextSelectorIsSection() throws Exception {
            var input = """
                    = Document Title

                    preamble

                    == Section One

                    content
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any().withContext("section"));
            assertNotNull(result);
            assertEquals(2, size(result));
            assertEquals("section", result.get(0).getContext());
            assertEquals("Document Title", result.get(0).getTitle());
        }

        @Test
        @DisplayName("find_by should only return results for which the block argument yields true")
        void find_byShouldOnlyReturnResultsForWhichTheBlockArgumentYieldsTrue() throws Exception {
            var input = """
                    == Section

                    content

                    === Subsection

                    content
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any().withContext("section"), filter(sect -> Objects.equals(sect.getLevel(), 1)));
            assertNotNull(result);
            assertEquals(1, size(result));
            assertEquals("section", result.get(0).getContext());
            assertEquals("Section", result.get(0).getTitle());
        }

        @Test
        @DisplayName("find_by should reject node and its children if block returns :reject")
        void find_byShouldRejectNodeAndItsChildrenIfBlockReturnsReject() throws Exception {
            var input = """
                    paragraph 1

                    ====
                    paragraph 2

                    term::
                    +
                    paragraph 3
                    ====

                    paragraph 4
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any(), filter(candidate -> {
                if (Objects.equals(candidate.getContext(), "example")) {
                    return "reject";
                } else if (Objects.equals(candidate.getContext(), "paragraph")) {
                    return true;
                }
                return null;
            }));
            assertNotNull(result);
            assertEquals(2, size(result));
            assertEquals("paragraph", result.get(0).getContext());
            assertEquals("paragraph", result.get(1).getContext());
        }

        @Test
        @DisplayName("find_by should reject node matched by ID selector if block returns :reject")
        void find_byShouldRejectNodeMatchedByIDSelectorIfBlockReturnsReject() throws Exception {
            var input = """
                    [.rolename]
                    paragraph 1

                    [.rolename#idname]
                    paragraph 2
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any().withId("idname").withRole("rolename"));
            assertNotNull(result);
            assertEquals(1, size(result));
            assertEquals(doc.getBlocks().get(1), result.get(0));
            result = doc.findBy(AbstractBlock.Selector.any().withId("idname").withRole("rolename"), filter(it -> "reject"));
            assertNotNull(result);
            assertEquals(0, size(result));
        }

        @Test
        @DisplayName("find_by should accept node matched by ID selector if block returns :prune")
        void find_byShouldAcceptNodeMatchedByIDSelectorIfBlockReturnsPrune() throws Exception {
            var input = """
                    [.rolename]
                    paragraph 1

                    [.rolename#idname]
                    ====
                    paragraph 2
                    ====
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any().withId("idname").withRole("rolename"));
            assertNotNull(result);
            assertEquals(1, size(result));
            assertEquals(doc.getBlocks().get(1), result.get(0));
            result = doc.findBy(AbstractBlock.Selector.any().withId("idname").withRole("rolename"), filter(it -> "prune"));
            assertNotNull(result);
            assertEquals(1, size(result));
            assertEquals(doc.getBlocks().get(1), result.get(0));
        }

        @Test
        @DisplayName("find_by should accept node but reject its children if block returns :prune")
        void find_byShouldAcceptNodeButRejectItsChildrenIfBlockReturnsPrune() throws Exception {
            var input = """
                    ====
                    paragraph 2

                    term::
                    +
                    paragraph 3
                    ====
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any(), filter(candidate -> {
                if (Objects.equals(candidate.getContext(), "example")) {
                    return "prune";
                }
                return null;
            }));
            assertNotNull(result);
            assertEquals(1, size(result));
            assertEquals("example", result.get(0).getContext());
        }

        @Test
        @DisplayName("find_by should stop looking for blocks when StopIteration is raised")
        void find_byShouldStopLookingForBlocksWhenStopIterationIsRaised() throws Exception {
            var input = """
                    paragraph 1

                    ====
                    paragraph 2

                    ****
                    paragraph 3
                    ****
                    ====

                    paragraph 4

                    * item
                    +
                    paragraph 5
                    """;
            var doc = Asciidoctor.load(input);
            var stopAtNext = new Ref<>(false);
            var result = doc.findBy(AbstractBlock.Selector.any(), filter(candidate -> {
                if (isTruthy(stopAtNext.get())) {
                    return "stop";
                }
                if (Objects.equals(candidate.getContext(), "paragraph")) {
                    if (Objects.equals(candidate.getParent().getContext(), "sidebar")) {
                        stopAtNext.set(true);
                        return stopAtNext.get();
                    } else {
                        return true;
                    }
                }
                return null;
            }));
            assertNotNull(result);
            assertEquals(3, size(result));
            assertEquals("paragraph 1", result.get(0).getContent());
            assertEquals("paragraph 2", result.get(1).getContent());
            assertEquals("paragraph 3", result.get(2).getContent());
        }

        @Test
        @DisplayName("find_by should stop looking for blocks when filter block returns :stop directive")
        void find_byShouldStopLookingForBlocksWhenFilterBlockReturnsStopDirective() throws Exception {
            var input = """
                    paragraph 1

                    ====
                    paragraph 2

                    ****
                    paragraph 3
                    ****
                    ====

                    paragraph 4

                    * item
                    +
                    paragraph 5
                    """;
            var doc = Asciidoctor.load(input);
            var stopAtNext = new Ref<>(false);
            var result = doc.findBy(AbstractBlock.Selector.any(), filter(candidate -> {
                if (isTruthy(stopAtNext.get())) {
                    return "stop";
                }
                if (Objects.equals(candidate.getContext(), "paragraph")) {
                    if (Objects.equals(candidate.getParent().getContext(), "sidebar")) {
                        stopAtNext.set(true);
                        return stopAtNext.get();
                    } else {
                        return true;
                    }
                }
                return null;
            }));
            assertNotNull(result);
            assertEquals(3, size(result));
            assertEquals("paragraph 1", result.get(0).getContent());
            assertEquals("paragraph 2", result.get(1).getContent());
            assertEquals("paragraph 3", result.get(2).getContent());
        }

        @Test
        @DisplayName("find_by should only return one result when matching by id")
        void find_byShouldOnlyReturnOneResultWhenMatchingById() throws Exception {
            var input = """
                    == Section

                    content

                    [#subsection]
                    === Subsection

                    content
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any().withContext("section").withId("subsection"));
            assertNotNull(result);
            assertEquals(1, size(result));
            assertEquals("section", result.get(0).getContext());
            assertEquals("Subsection", result.get(0).getTitle());
        }

        @Test
        @DisplayName("find_by should stop seeking once match is found")
        void find_byShouldStopSeekingOnceMatchIsFound() throws Exception {
            var input = """
                    == Section

                    content

                    [#subsection]
                    === Subsection

                    [#last]
                    content
                    """;
            var doc = Asciidoctor.load(input);
            var visitedLast = new Ref<>(false);
            var result = doc.findBy(AbstractBlock.Selector.any().withId("subsection"), filter(candidate -> {
                if (Objects.equals(candidate.getId(), "last")) {
                    visitedLast.set(true);
                }
                return true;
            }));
            assertNotNull(result);
            assertEquals(1, size(result));
            assertFalsy(visitedLast.get());
        }

        @Test
        @DisplayName("find_by should return an empty Array if the id criteria matches but the block argument yields false")
        void find_byShouldReturnAnEmptyArrayIfTheIdCriteriaMatchesButTheBlockArgumentYieldsFalse() throws Exception {
            var input = """
                    == Section

                    content

                    [#subsection]
                    === Subsection

                    content
                    """;
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any().withContext("section").withId("subsection"), filter(it -> false));
            assertNotNull(result);
            assertEquals(0, size(result));
        }

        @Test
        @DisplayName("find_by should not crash if dlist entry does not have description")
        void find_byShouldNotCrashIfDlistEntryDoesNotHaveDescription() throws Exception {
            var input = "term without description::";
            var doc = Asciidoctor.load(input);
            var result = doc.findBy(AbstractBlock.Selector.any());
            assertNotNull(result);
            assertEquals(3, size(result));
            assertInstanceOf(Document.class, result.get(0));
            assertInstanceOf(List.class, result.get(1));
            assertInstanceOf(ListItem.class, result.get(2));
        }

        @Test
        @DisplayName("dlist item should always have two entries for terms and desc")
        void dlistItemShouldAlwaysHaveTwoEntriesForTermsAndDesc() throws Exception {
            for (var input : list("term w/o desc::", "term::\nalias::", "primary:: 1\nsecondary:: 2")) {
                var dlist = (List) Asciidoctor.load(input).getBlocks().get(0);
                for (var item : dlist.getItems()) {
                    var entry = (DescriptionListEntry) item;
                    assertInstanceOf(java.util.List.class, entry.getTerms());
                    if (entry.getDescription() != null) {
                        assertInstanceOf(ListItem.class, entry.getDescription());
                    }
                }
            }
        }

        @Test
        @DisplayName("timings are recorded for each step when load and convert are called separately")
        void timingsAreRecordedForEachStepWhenLoadAndConvertAreCalledSeparately() throws Exception {
            var sampleInputPath = fixturePath("asciidoc_index.txt");
            var timings = new Timings();
            Asciidoctor.loadFile(sampleInputPath, Options.of("timings", timings)).convert();
            assertNotEquals("0.00000", sprintf("%05.5f", timings.getReadParse()));
            assertNotEquals("0.00000", sprintf("%05.5f", timings.getConvert()));
            assertNotEquals(timings.getReadParse(), timings.getTotal());
        }

        @Test
        @DisplayName("can disable syntax highlighter by setting value to nil in :syntax_highlighters option")
        void canDisableSyntaxHighlighterBySettingValueToNilInSyntax_highlightersOption() throws Exception {
            var doc = Asciidoctor.load("", Options.of("safe", "safe", "syntax_highlighters", Options.of("coderay", null), "attributes", Options.of("source-highlighter", "coderay")));
            assertNull(doc.getSyntaxHighlighter());
        }

        @Test
        @DisplayName("can substitute a custom syntax highlighter factory instance using the :syntax_highlighter_factory option")
        void canSubstituteACustomSyntaxHighlighterFactoryInstanceUsingTheSyntax_highlighter_factoryOption() throws Exception {
            var input = """
                    [source,ruby]
                    ----
                    puts 'Hello, World!'
                    ----
                    """;
            var syntaxHlFactory = SyntaxHighlighterFactory.custom(Options.of("github", SyntaxHighlighter.forName("html-pipeline")));
            var doc = Asciidoctor.load(input, Options.of("safe", "safe", "syntax_highlighter_factory", syntaxHlFactory, "attributes", Options.of("source-highlighter", "github")));
            assertNotNull(doc.getSyntaxHighlighter());
            assertInstanceOf(HtmlPipelineAdapter.class, doc.getSyntaxHighlighter());
            assertInclude("<pre lang=\"ruby\"><code>", doc.convert());
        }

        @Test
        @DisplayName("can substitute an extended syntax highlighter factory implementation using the :syntax_highlighters option")
        void canSubstituteAnExtendedSyntaxHighlighterFactoryImplementationUsingTheSyntax_highlightersOption() throws Exception {
            var input = """
                    [source,ruby]
                    ----
                    puts 'Hello, World!'
                    ----
                    """;
            var syntaxHlFactory = new SyntaxHighlighterFactory() {
                @Override
                protected Map<String, Optional<Object>> registry() {
                    return Map.of();
                }

                @Override
                public Object forName(String name) {
                    return SyntaxHighlighterFactory.getDefault().forName("highlight.js");
                }
            };
            var doc = Asciidoctor.load(input, Options.of("safe", "safe", "syntax_highlighter_factory", syntaxHlFactory, "attributes", Options.of("source-highlighter", "coderay")));
            assertNotNull(doc.getSyntaxHighlighter());
            var output = doc.convert();
            refuteInclude("CodeRay", output);
            assertInclude("hljs", output);
        }
    }

    @Nested
    @DisplayName("Convert")
    class Convert {

        @Test
        @Disabled("method aliases are specific to the Ruby implementation")
        @DisplayName("render_file is aliased to convert_file")
        void render_fileIsAliasedToConvert_file() throws Exception {
        }

        @Test
        @Disabled("method aliases are specific to the Ruby implementation")
        @DisplayName("render is aliased to convert")
        void renderIsAliasedToConvert() throws Exception {
        }

        @Test
        @DisplayName("should convert source document to embedded document when header_footer is false")
        void shouldConvertSourceDocumentToEmbeddedDocumentWhenHeader_footerIsFalse() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var sampleOutputPath = fixturePath("sample.html");
            for (var opts : list(Options.of("header_footer", false), Options.of("header_footer", false, "to_file", sampleOutputPath))) {
                try {
                    Asciidoctor.convertFile(sampleInputPath, opts);
                    assertPathExists(sampleOutputPath);
                    var output = readFile(sampleOutputPath);
                    refuteEmpty(output);
                    assertXpath("/html", output, 0);
                    assertCss("#preamble", output, 1);
                } finally {
                    removeFile(sampleOutputPath);
                }
            }
        }

        @Test
        @DisplayName("should convert source document to standalone document string when to_file is false and standalone is true")
        void shouldConvertSourceDocumentToStandaloneDocumentStringWhenTo_fileIsFalseAndStandaloneIsTrue() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("standalone", true, "to_file", false));
            refuteEmpty(output);
            assertXpath("/html", output, 1);
            assertXpath("/html/head", output, 1);
            assertXpath("/html/body", output, 1);
            assertXpath("/html/head/title[text() = \"Document Title\"]", output, 1);
            assertXpath("/html/body/*[@id=\"header\"]/h1[text() = \"Document Title\"]", output, 1);
        }

        @Test
        @DisplayName("should convert source document to standalone document string when to_file is false and header_footer is true")
        void shouldConvertSourceDocumentToStandaloneDocumentStringWhenTo_fileIsFalseAndHeader_footerIsTrue() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("header_footer", true, "to_file", false));
            refuteEmpty(output);
            assertXpath("/html", output, 1);
            assertXpath("/html/head", output, 1);
            assertXpath("/html/body", output, 1);
            assertXpath("/html/head/title[text() = \"Document Title\"]", output, 1);
            assertXpath("/html/body/*[@id=\"header\"]/h1[text() = \"Document Title\"]", output, 1);
        }

        @Test
        @DisplayName("lines in output should be separated by line feed (universal newline)")
        void linesInOutputShouldBeSeparatedByLineFeedUniversalNewline() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("standalone", true, "to_file", false));
            refuteEmpty(output);
            refuteIncludes(output, "\r");
            var lines = split(output, "\n");
            assertEquals(map(lines, it -> size(it)), map(map(lines, (String it) -> Strings.rstrip(it)), it -> size(it)));
        }

        @Test
        @DisplayName("should accept attributes as array")
        void shouldAcceptAttributesAsArray2() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("attributes", list("sectnums", "idprefix", "idseparator=-"), "to_file", false));
            assertCss("#section-a", output, 1);
        }

        @Test
        @DisplayName("should accept attributes as string")
        void shouldAcceptAttributesAsString2() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("attributes", "sectnums idprefix idseparator=-", "to_file", false));
            assertCss("#section-a", output, 1);
        }

        @Test
        @DisplayName("should link to default stylesheet by default when safe mode is SECURE or greater")
        void shouldLinkToDefaultStylesheetByDefaultWhenSafeModeIsSECUREOrGreater() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("standalone", true, "to_file", false));
            assertCss("html:root > head > link[rel=\"stylesheet\"][href^=\"https://fonts.googleapis.com\"]", output, 1);
            assertCss("html:root > head > link[rel=\"stylesheet\"][href=\"./asciidoctor.css\"]", output, 1);
        }

        @Test
        @DisplayName("should embed default stylesheet by default if SafeMode is less than SECURE")
        void shouldEmbedDefaultStylesheetByDefaultIfSafeModeIsLessThanSECURE() throws Exception {
            var input = """
                    = Document Title

                    text
                    """;
            var output = Asciidoctor.convert(input, Options.of("safe", SafeMode.SERVER.getLevel(), "standalone", true));
            assertCss("html:root > head > link[rel=\"stylesheet\"][href^=\"https://fonts.googleapis.com\"]", output, 1);
            assertCss("html:root > head > link[rel=\"stylesheet\"][href=\"./asciidoctor.css\"]", output, 0);
            var stylenode = xmlnodesAtCss("html:root > head > style", output, 1);
            var styles = stylenode.getContent();
            assertNotNull(styles);
            refuteEmpty(Strings.strip(styles));
        }

        @Test
        @DisplayName("should embed remote stylesheet by default if SafeMode is less than SECURE and allow-uri-read is set")
        void shouldEmbedRemoteStylesheetByDefaultIfSafeModeIsLessThanSECUREAndAllowUriReadIsSet() throws Exception {
            var input = """
                    = Document Title

                    text
                    """;
            var output = usingTestWebserver(() -> Asciidoctor.convert(input, Options.of("safe", SafeMode.SERVER.getLevel(), "standalone", true, "attributes", Options.of("allow-uri-read", "", "stylesheet", ("http://" + str(resolveLocalhost()) + ":9876/fixtures/custom.css")))));
            var stylenode = xmlnodesAtCss("html:root > head > style", output, 1);
            var styles = stylenode.getContent();
            assertNotNull(styles);
            refuteEmpty(Strings.strip(styles));
            assertInclude("color: green", styles);
        }

        @Test
        @DisplayName("should not allow linkcss be unset from document if SafeMode is SECURE or greater")
        void shouldNotAllowLinkcssBeUnsetFromDocumentIfSafeModeIsSECUREOrGreater() throws Exception {
            var input = """
                    = Document Title
                    :linkcss!:

                    text
                    """;
            var output = Asciidoctor.convert(input, Options.of("standalone", true));
            assertCss("html:root > head > link[rel=\"stylesheet\"][href^=\"https://fonts.googleapis.com\"]", output, 1);
            assertCss("html:root > head > link[rel=\"stylesheet\"][href=\"./asciidoctor.css\"]", output, 1);
        }

        @Test
        @DisplayName("should embed default stylesheet if linkcss is unset from API and SafeMode is SECURE or greater")
        void shouldEmbedDefaultStylesheetIfLinkcssIsUnsetFromAPIAndSafeModeIsSECUREOrGreater() throws Exception {
            var input = """
                    = Document Title

                    text
                    """;
            for (var attrs : list(Options.of("linkcss!", ""), Options.of("linkcss", null), Options.of("linkcss", false))) {
                var output = Asciidoctor.convert(input, Options.of("standalone", true, "attributes", attrs));
                assertCss("html:root > head > link[rel=\"stylesheet\"][href^=\"https://fonts.googleapis.com\"]", output, 1);
                assertCss("html:root > head > link[rel=\"stylesheet\"][href=\"./asciidoctor.css\"]", output, 0);
                var stylenode = xmlnodesAtCss("html:root > head > style", output, 1);
                var styles = stylenode.getContent();
                assertNotNull(styles);
                refuteEmpty(Strings.strip(styles));
            }
        }

        @Test
        @DisplayName("should embed default stylesheet if safe mode is less than SECURE and linkcss is unset from API")
        void shouldEmbedDefaultStylesheetIfSafeModeIsLessThanSECUREAndLinkcssIsUnsetFromAPI() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("standalone", true, "to_file", false, "safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("linkcss!", "")));
            assertCss("html:root > head > style", output, 1);
            var stylenode = xmlnodesAtCss("html:root > head > style", output, 1);
            var styles = stylenode.getContent();
            assertNotNull(styles);
            refuteEmpty(Strings.strip(styles));
        }

        @Test
        @DisplayName("should not link to stylesheet if stylesheet is unset")
        void shouldNotLinkToStylesheetIfStylesheetIsUnset() throws Exception {
            var input = """
                    = Document Title

                    text
                    """;
            var output = Asciidoctor.convert(input, Options.of("standalone", true, "attributes", Options.of("stylesheet!", "")));
            assertCss("html:root > head > link[rel=\"stylesheet\"][href^=\"https://fonts.googleapis.com\"]", output, 0);
            assertCss("html:root > head > link[rel=\"stylesheet\"]", output, 0);
        }

        @Test
        @DisplayName("should link to custom stylesheet if specified in stylesheet attribute")
        void shouldLinkToCustomStylesheetIfSpecifiedInStylesheetAttribute() throws Exception {
            var input = """
                    = Document Title

                    text
                    """;
            var output = Asciidoctor.convert(input, Options.of("standalone", true, "attributes", Options.of("stylesheet", "./custom.css")));
            assertCss("html:root > head > link[rel=\"stylesheet\"][href^=\"https://fonts.googleapis.com\"]", output, 0);
            assertCss("html:root > head > link[rel=\"stylesheet\"][href=\"./custom.css\"]", output, 1);
            output = Asciidoctor.convert(input, Options.of("standalone", true, "attributes", Options.of("stylesheet", "file:///home/username/custom.css")));
            assertCss("html:root > head > link[rel=\"stylesheet\"][href=\"file:///home/username/custom.css\"]", output, 1);
        }

        @Test
        @DisplayName("should resolve custom stylesheet relative to stylesdir")
        void shouldResolveCustomStylesheetRelativeToStylesdir() throws Exception {
            var input = """
                    = Document Title

                    text
                    """;
            var output = Asciidoctor.convert(input, Options.of("standalone", true, "attributes", Options.of("stylesheet", "custom.css", "stylesdir", "./stylesheets")));
            assertCss("html:root > head > link[rel=\"stylesheet\"][href=\"./stylesheets/custom.css\"]", output, 1);
        }

        @Test
        @DisplayName("should resolve custom stylesheet to embed relative to stylesdir")
        void shouldResolveCustomStylesheetToEmbedRelativeToStylesdir() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var output = Asciidoctor.convertFile(sampleInputPath, Options.of("standalone", true, "safe", SafeMode.SAFE.getLevel(), "to_file", false, "attributes", Options.of("stylesheet", "custom.css", "stylesdir", "./stylesheets", "linkcss!", "")));
            var stylenode = xmlnodesAtCss("html:root > head > style", output, 1);
            var styles = stylenode.getContent();
            assertNotNull(styles);
            refuteEmpty(Strings.strip(styles));
        }

        @Test
        @DisplayName("should embed custom remote stylesheet if SafeMode is less than SECURE and allow-uri-read is set")
        void shouldEmbedCustomRemoteStylesheetIfSafeModeIsLessThanSECUREAndAllowUriReadIsSet() throws Exception {
            var input = """
                    = Document Title

                    text
                    """;
            var output = usingTestWebserver(() -> Asciidoctor.convert(input, Options.of("safe", SafeMode.SERVER.getLevel(), "standalone", true, "attributes", Options.of("allow-uri-read", "", "stylesheet", ("http://" + str(resolveLocalhost()) + ":9876/fixtures/custom.css")))));
            var stylenode = xmlnodesAtCss("html:root > head > style", output, 1);
            var styles = stylenode.getContent();
            assertNotNull(styles);
            refuteEmpty(Strings.strip(styles));
            assertInclude("color: green", styles);
        }

        @Test
        @Disabled("JRuby only")
        @DisplayName("should embed custom stylesheet read from classloader URI")
        void shouldEmbedCustomStylesheetReadFromClassloaderURI() throws Exception {
        }

        @Test
        @DisplayName("should embed custom stylesheet in remote stylesdir if SafeMode is less than SECURE and allow-uri-read is set")
        void shouldEmbedCustomStylesheetInRemoteStylesdirIfSafeModeIsLessThanSECUREAndAllowUriReadIsSet() throws Exception {
            var input = """
                    = Document Title

                    text
                    """;
            var output = usingTestWebserver(() -> Asciidoctor.convert(input, Options.of("safe", SafeMode.SERVER.getLevel(), "standalone", true, "attributes", Options.of("allow-uri-read", "", "stylesdir", ("http://" + str(resolveLocalhost()) + ":9876/fixtures"), "stylesheet", "custom.css"))));
            var stylenode = xmlnodesAtCss("html:root > head > style", output, 1);
            var styles = stylenode.getContent();
            assertNotNull(styles);
            refuteEmpty(Strings.strip(styles));
            assertInclude("color: green", styles);
        }

        @Test
        @DisplayName("should copy custom stylesheet in folder to same folder in destination dir if copycss is set")
        void shouldCopyCustomStylesheetInFolderToSameFolderInDestinationDirIfCopycssIsSet() throws Exception {
            var outputDir = fixturePath("output");
            try {
                var sampleInputPath = fixturePath("sample.adoc");
                var sampleOutputPath = joinPath(outputDir, "sample.html");
                var customStylesheetOutputPath = joinPath(outputDir, "stylesheets", "custom.css");
                Asciidoctor.convertFile(sampleInputPath, Options.of("safe", "safe", "to_dir", outputDir, "mkdirs", true, "attributes", Options.of("stylesheet", "stylesheets/custom.css", "linkcss", "", "copycss", "")));
                assertPathExists(sampleOutputPath);
                assertPathExists(customStylesheetOutputPath);
                var output = readFile(sampleOutputPath);
                assertXpath("/html/head/link[@rel=\"stylesheet\"][@href=\"./stylesheets/custom.css\"]", output, 1);
                assertXpath("style", output, 0);
            } finally {
                removeDir(outputDir);
            }
        }

        @Test
        @DisplayName("should copy custom stylesheet to destination dir if copycss is true")
        void shouldCopyCustomStylesheetToDestinationDirIfCopycssIsTrue() throws Exception {
            var outputDir = fixturePath("output");
            try {
                var sampleInputPath = fixturePath("sample.adoc");
                var sampleOutputPath = joinPath(outputDir, "sample.html");
                var customStylesheetOutputPath = joinPath(outputDir, "custom.css");
                Asciidoctor.convertFile(sampleInputPath, Options.of("safe", "safe", "to_dir", outputDir, "mkdirs", true, "attributes", Options.of("stylesheet", "custom.css", "linkcss", true, "copycss", true)));
                assertPathExists(sampleOutputPath);
                assertPathExists(customStylesheetOutputPath);
                var output = readFile(sampleOutputPath);
                assertXpath("/html/head/link[@rel=\"stylesheet\"][@href=\"./custom.css\"]", output, 1);
                assertXpath("style", output, 0);
            } finally {
                removeDir(outputDir);
            }
        }

        @Test
        @DisplayName("should copy custom stylesheet to destination dir if copycss is a path string")
        void shouldCopyCustomStylesheetToDestinationDirIfCopycssIsAPathString() throws Exception {
            var outputDir = fixturePath("output");
            try {
                var sampleInputPath = fixturePath("sample.adoc");
                var sampleOutputPath = joinPath(outputDir, "sample.html");
                var customStylesheetOutputPath = joinPath(outputDir, "styles.css");
                Asciidoctor.convertFile(sampleInputPath, Options.of("safe", "safe", "to_dir", outputDir, "mkdirs", true, "attributes", Options.of("stylesheet", "styles.css", "linkcss", true, "copycss", "custom.css")));
                assertPathExists(sampleOutputPath);
                assertPathExists(customStylesheetOutputPath);
                var output = readFile(sampleOutputPath);
                assertXpath("/html/head/link[@rel=\"stylesheet\"][@href=\"./styles.css\"]", output, 1);
                assertXpath("style", output, 0);
            } finally {
                removeDir(outputDir);
            }
        }

        @Test
        @DisplayName("should copy custom stylesheet to destination dir if copycss is a Pathname object")
        void shouldCopyCustomStylesheetToDestinationDirIfCopycssIsAPathnameObject() throws Exception {
            var outputDir = fixturePath("output");
            try {
                var sampleInputPath = fixturePath("sample.adoc");
                var sampleOutputPath = joinPath(outputDir, "sample.html");
                var customStylesheetSrcPath = Path.of(fixturePath("custom.css"));
                var customStylesheetOutputPath = joinPath(outputDir, "styles.css");
                Asciidoctor.convertFile(sampleInputPath, Options.of("safe", "safe", "to_dir", outputDir, "mkdirs", true, "attributes", Options.of("stylesheet", "styles.css", "linkcss", true, "copycss", customStylesheetSrcPath)));
                assertPathExists(sampleOutputPath);
                assertPathExists(customStylesheetOutputPath);
                var output = readFile(sampleOutputPath);
                assertXpath("/html/head/link[@rel=\"stylesheet\"][@href=\"./styles.css\"]", output, 1);
                assertXpath("style", output, 0);
            } finally {
                removeDir(outputDir);
            }
        }

        @Test
        @Disabled("JRuby only")
        @DisplayName("should copy custom stylesheet to destination dir if copycss is a classloader URI")
        void shouldCopyCustomStylesheetToDestinationDirIfCopycssIsAClassloaderURI() throws Exception {
        }

        @Test
        @DisplayName("should convert source file and write result to adjacent file by default")
        void shouldConvertSourceFileAndWriteResultToAdjacentFileByDefault() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var sampleOutputPath = fixturePath("sample.html");
            try {
                Asciidoctor.convertFile(sampleInputPath);
                assertPathExists(sampleOutputPath);
                var output = readFile(sampleOutputPath);
                refuteEmpty(output);
                assertXpath("/html", output, 1);
                assertXpath("/html/head", output, 1);
                assertXpath("/html/body", output, 1);
                assertXpath("/html/head/title[text() = \"Document Title\"]", output, 1);
                assertXpath("/html/body/*[@id=\"header\"]/h1[text() = \"Document Title\"]", output, 1);
            } finally {
                removeFile(sampleOutputPath);
            }
        }

        @Test
        @DisplayName("should convert source file specified by pathname and write result to adjacent file by default")
        void shouldConvertSourceFileSpecifiedByPathnameAndWriteResultToAdjacentFileByDefault() throws Exception {
            var sampleInputPath = Path.of(fixturePath("sample.adoc"));
            var sampleOutputPath = Path.of(fixturePath("sample.html"));
            try {
                var doc = (Document) Asciidoctor.convertFile(sampleInputPath, Options.of("safe", "safe"));
                assertEquals(Helpers.absolutePath(sampleOutputPath), doc.getAttribute("outfile"));
                assertTruthy(Files.isRegularFile(sampleOutputPath));
                var output = Files.readString(sampleOutputPath);
                refuteEmpty(output);
                assertXpath("/html", output, 1);
                assertXpath("/html/head", output, 1);
                assertXpath("/html/body", output, 1);
                assertXpath("/html/head/title[text() = \"Document Title\"]", output, 1);
                assertXpath("/html/body/*[@id=\"header\"]/h1[text() = \"Document Title\"]", output, 1);
            } finally {
                Files.deleteIfExists(sampleOutputPath);
            }
        }

        @Test
        @DisplayName("should convert source file and write to specified file")
        void shouldConvertSourceFileAndWriteToSpecifiedFile() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var sampleOutputPath = fixturePath("result.html");
            try {
                Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", sampleOutputPath));
                assertPathExists(sampleOutputPath);
                var output = readFile(sampleOutputPath);
                refuteEmpty(output);
                assertXpath("/html", output, 1);
                assertXpath("/html/head", output, 1);
                assertXpath("/html/body", output, 1);
                assertXpath("/html/head/title[text() = \"Document Title\"]", output, 1);
                assertXpath("/html/body/*[@id=\"header\"]/h1[text() = \"Document Title\"]", output, 1);
            } finally {
                removeFile(sampleOutputPath);
            }
        }

        @Test
        @DisplayName("should convert source file and write to specified file in base_dir")
        void shouldConvertSourceFileAndWriteToSpecifiedFileInBase_dir() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var sampleOutputPath = fixturePath("result.html");
            var fixtureDir = fixturePath("");
            try {
                Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", "result.html", "base_dir", fixtureDir));
                assertPathExists(sampleOutputPath);
                var output = readFile(sampleOutputPath);
                refuteEmpty(output);
                assertXpath("/html", output, 1);
                assertXpath("/html/head", output, 1);
                assertXpath("/html/body", output, 1);
                assertXpath("/html/head/title[text() = \"Document Title\"]", output, 1);
                assertXpath("/html/body/*[@id=\"header\"]/h1[text() = \"Document Title\"]", output, 1);
            } finally {
                removeFile(sampleOutputPath);
            }
        }

        @Test
        @DisplayName("should write file in bin mode and thus not convert line feeds to system-dependent newline")
        void shouldWriteFileInBinModeAndThusNotConvertLineFeedsToSystemDependentNewline() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var sampleOutputPath = fixturePath("sample.html");
            try {
                Asciidoctor.convertFile(sampleInputPath);
                assertPathExists(sampleOutputPath);
                var output = readFile(sampleOutputPath);
                refuteEmpty(output);
                assertIncludes(output, "\n");
                refuteIncludes(output, "\r");
                assertIncludes(output, "</body>\n</html>");
                assertFalsy(endsWith(output, "\n"));
            } finally {
                removeFile(sampleOutputPath);
            }
        }

        @Test
        @DisplayName("should resolve :to_dir option correctly when both :to_dir and :to_file options are set to an absolute path")
        void shouldResolveTo_dirOptionCorrectlyWhenBothTo_dirAndTo_fileOptionsAreSetToAnAbsolutePath() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var sampleOutputFile = Files.createTempFile("out-", ".html");
            try {
                // NOTE like a Ruby Tempfile, use a posix path
                var sampleOutputPath = Helpers.posixify(sampleOutputFile.toString());
                var sampleOutputDir = fileDirname(sampleOutputPath);
                var doc = (Document) Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", sampleOutputPath, "to_dir", sampleOutputDir, "safe", "unsafe"));
                assertPathExists(sampleOutputPath);
                assertEquals(sampleOutputPath, get(doc.getOptions(), "to_file"));
                assertEquals(sampleOutputDir, get(doc.getOptions(), "to_dir"));
            } finally {
                Files.deleteIfExists(sampleOutputFile);
            }
        }

        @Test
        @DisplayName("in_place option is ignored when to_file is specified")
        void in_placeOptionIsIgnoredWhenTo_fileIsSpecified() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var sampleOutputPath = fixturePath("result.html");
            try {
                Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", sampleOutputPath, "in_place", true));
                assertPathExists(sampleOutputPath);
            } finally {
                if (fileExists(sampleOutputPath)) {
                    removeFile(sampleOutputPath);
                }
            }
        }

        @Test
        @DisplayName("in_place option is ignored when to_dir is specified")
        void in_placeOptionIsIgnoredWhenTo_dirIsSpecified() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var sampleOutputPath = fixturePath("sample.html");
            try {
                Asciidoctor.convertFile(sampleInputPath, Options.of("to_dir", fileDirname(sampleOutputPath), "in_place", true));
                assertPathExists(sampleOutputPath);
            } finally {
                if (fileExists(sampleOutputPath)) {
                    removeFile(sampleOutputPath);
                }
            }
        }

        @Test
        @DisplayName("should set outfilesuffix to match file extension of target file")
        void shouldSetOutfilesuffixToMatchFileExtensionOfTargetFile() throws Exception {
            var sampleInput = "{outfilesuffix}";
            var sampleOutputPath = fixturePath("result.htm");
            try {
                Asciidoctor.convert(sampleInput, Options.of("to_file", sampleOutputPath));
                assertPathExists(sampleOutputPath);
                var output = readFile(sampleOutputPath);
                refuteEmpty(output);
                assertInclude("<p>.htm</p>", output);
            } finally {
                removeFile(sampleOutputPath);
            }
        }

        @Test
        @DisplayName("should respect outfilesuffix soft set from API")
        void shouldRespectOutfilesuffixSoftSetFromAPI() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var sampleOutputPath = fixturePath("sample.htm");
            try {
                Asciidoctor.convertFile(sampleInputPath, Options.of("to_dir", fileDirname(sampleInputPath), "attributes", Options.of("outfilesuffix", ".htm@")));
                assertPathExists(sampleOutputPath);
            } finally {
                removeFile(sampleOutputPath);
            }
        }

        @Test
        @DisplayName("output should be relative to to_dir option")
        void outputShouldBeRelativeToTo_dirOption() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var outputDir = joinPath(fileDirname(sampleInputPath), "test_output");
            if (!(fileExists(outputDir))) {
                makeDir(outputDir);
            }
            var sampleOutputPath = joinPath(outputDir, "sample.html");
            try {
                Asciidoctor.convertFile(sampleInputPath, Options.of("to_dir", outputDir));
                assertPathExists(sampleOutputPath);
            } finally {
                if (fileExists(sampleOutputPath)) {
                    removeFile(sampleOutputPath);
                }
                removeDir(outputDir);
            }
        }

        @Test
        @DisplayName("missing directories should be created if mkdirs is enabled")
        void missingDirectoriesShouldBeCreatedIfMkdirsIsEnabled() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var outputDir = joinPath(joinPath(fileDirname(sampleInputPath), "test_output"), "subdir");
            var sampleOutputPath = joinPath(outputDir, "sample.html");
            try {
                Asciidoctor.convertFile(sampleInputPath, Options.of("to_dir", outputDir, "mkdirs", true));
                assertPathExists(sampleOutputPath);
            } finally {
                if (fileExists(sampleOutputPath)) {
                    removeFile(sampleOutputPath);
                }
                removeDir(outputDir);
                removeDir(fileDirname(outputDir));
            }
        }

        @Test
        @DisplayName("should raise exception if an attempt is made to overwrite input file")
        void shouldRaiseExceptionIfAnAttemptIsMadeToOverwriteInputFile() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            assertThrows(AsciidoctorException.class, () -> {
                Asciidoctor.convertFile(sampleInputPath, Options.of("attributes", Options.of("outfilesuffix", ".adoc")));
            });
        }

        @Test
        @DisplayName("to_file should be relative to to_dir when both given")
        void to_fileShouldBeRelativeToTo_dirWhenBothGiven() throws Exception {
            var sampleInputPath = fixturePath("sample.adoc");
            var baseDir = fileDirname(sampleInputPath);
            var sampleRelOutputPath = joinPath("test_output", "result.html");
            var outputDir = fileDirname(joinPath(baseDir, sampleRelOutputPath));
            if (!(fileExists(outputDir))) {
                makeDir(outputDir);
            }
            var sampleOutputPath = joinPath(baseDir, sampleRelOutputPath);
            try {
                Asciidoctor.convertFile(sampleInputPath, Options.of("to_dir", baseDir, "to_file", sampleRelOutputPath));
                assertPathExists(sampleOutputPath);
            } finally {
                if (fileExists(sampleOutputPath)) {
                    removeFile(sampleOutputPath);
                }
                removeDir(outputDir);
            }
        }

        @Test
        @DisplayName("should not modify options argument")
        void shouldNotModifyOptionsArgument2() throws Exception {
            var output = new StringWriter();
            var options = Options.of("safe", SafeMode.SAFE.getLevel(), "to_file", output);
            var sampleInputPath = fixturePath("sample.adoc");
            var doc = (Document) Asciidoctor.convertFile(sampleInputPath, options);
            assertNotSame(options, doc.getOptions());
        }

        @Test
        @DisplayName("should set to_dir option to parent directory of specified output file")
        void shouldSetTo_dirOptionToParentDirectoryOfSpecifiedOutputFile() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var sampleOutputPath = fixturePath("basic.html");
            try {
                var doc = (Document) Asciidoctor.convertFile(sampleInputPath, Options.of("to_file", sampleOutputPath));
                assertEquals(fileDirname(sampleOutputPath), get(doc.getOptions(), "to_dir"));
            } finally {
                removeFile(sampleOutputPath);
            }
        }

        @Test
        @DisplayName("should set to_dir option to parent directory of specified output directory and file")
        void shouldSetTo_dirOptionToParentDirectoryOfSpecifiedOutputDirectoryAndFile() throws Exception {
            var sampleInputPath = fixturePath("basic.adoc");
            var sampleOutputPath = fixturePath("basic.html");
            var fixtureBasePath = fileDirname(sampleOutputPath);
            var fixtureParentPath = fileDirname(fixtureBasePath);
            var sampleOutputRelpath = joinPath("fixtures", "basic.html");
            try {
                var doc = (Document) Asciidoctor.convertFile(sampleInputPath, Options.of("to_dir", fixtureParentPath, "to_file", sampleOutputRelpath));
                assertEquals(fixtureBasePath, get(doc.getOptions(), "to_dir"));
            } finally {
                removeFile(sampleOutputPath);
            }
        }

        @Test
        @DisplayName("timings are recorded for each step")
        void timingsAreRecordedForEachStep() throws Exception {
            var sampleInputPath = fixturePath("asciidoc_index.txt");
            var timings = new Timings();
            Asciidoctor.convertFile(sampleInputPath, Options.of("timings", timings, "to_file", false));
            assertNotEquals("0.00000", sprintf("%05.5f", timings.getReadParse()));
            assertNotEquals("0.00000", sprintf("%05.5f", timings.getConvert()));
            assertNotEquals(timings.getReadParse(), timings.getTotal());
        }

        @Test
        @DisplayName("can override syntax highlighter using syntax_highlighters option")
        void canOverrideSyntaxHighlighterUsingSyntax_highlightersOption() throws Exception {
            class CustomSyntaxHighlighter extends AbstractSyntaxHighlighter {
                public CustomSyntaxHighlighter(String name, String backend, Map<String, Object> opts) {
                    super(name, backend, opts);
                }

                @Override
                public boolean isHighlight() {
                    return true;
                }

                @Override
                public HighlightResult highlight(AbstractBlock node, String source, String lang, Map<String, Object> opts) {
                    return new HighlightResult("highlighted");
                }
            }

            var input = """
                    [source,ruby]
                    ----
                    puts 'Hello, World!'
                    ----
                    """;
            var output = Asciidoctor.convert(input, Options.of("safe", "safe", "syntax_highlighters", Options.of("coderay", (SyntaxHighlighter.Constructor) CustomSyntaxHighlighter::new), "attributes", Options.of("source-highlighter", "coderay")));
            assertCss("pre.highlight > code[data-lang=\"ruby\"]", output, 1);
            assertXpath("//pre[@class=\"coderay highlight\"]/code[text()=\"highlighted\"]", output, 1);
        }
    }

    @Nested
    @DisplayName("AST")
    class AST {

        @Test
        @DisplayName("with no author")
        void withNoAuthor() throws Exception {
            var input = """
                    = Getting Real: The Smarter, Faster, Easier Way to Build a Successful Web Application

                    Getting Real details the business, design, programming, and marketing principles of 37signals.
                    """;
            var doc = documentFromString(input);
            assertEquals(0, size(doc.getAuthors()));
        }

        @Test
        @DisplayName("with one author")
        void withOneAuthor() throws Exception {
            var input = """
                    = Getting Real: The Smarter, Faster, Easier Way to Build a Successful Web Application
                    David Heinemeier Hansson <david@37signals.com>

                    Getting Real details the business, design, programming, and marketing principles of 37signals.
                    """;
            var doc = documentFromString(input);
            var authors = doc.getAuthors();
            assertEquals(1, size(authors));
            var author1 = authors.get(0);
            assertEquals("david@37signals.com", author1.getEmail());
            assertEquals("David Heinemeier Hansson", author1.getFullName());
            assertEquals("David", author1.getFirstName());
            assertEquals("Heinemeier", author1.getMiddleName());
            assertEquals("Hansson", author1.getLastName());
            assertEquals("DHH", author1.getInitials());
        }

        @Test
        @DisplayName("with two authors")
        void withTwoAuthors() throws Exception {
            var input = """
                    = Getting Real: The Smarter, Faster, Easier Way to Build a Successful Web Application
                    David Heinemeier Hansson <david@37signals.com>; Jason Fried <jason@37signals.com>

                    Getting Real details the business, design, programming, and marketing principles of 37signals.
                    """;
            var doc = documentFromString(input);
            var authors = doc.getAuthors();
            assertEquals(2, size(authors));
            var author1 = authors.get(0);
            assertEquals("david@37signals.com", author1.getEmail());
            assertEquals("David Heinemeier Hansson", author1.getFullName());
            assertEquals("David", author1.getFirstName());
            assertEquals("Heinemeier", author1.getMiddleName());
            assertEquals("Hansson", author1.getLastName());
            assertEquals("DHH", author1.getInitials());
            var author2 = authors.get(1);
            assertEquals("jason@37signals.com", author2.getEmail());
            assertEquals("Jason Fried", author2.getFullName());
            assertEquals("Jason", author2.getFirstName());
            assertNull(author2.getMiddleName());
            assertEquals("Fried", author2.getLastName());
            assertEquals("JF", author2.getInitials());
        }

        @Test
        @DisplayName("with authors as attributes")
        void withAuthorsAsAttributes() throws Exception {
            var input = """
                    = Getting Real: The Smarter, Faster, Easier Way to Build a Successful Web Application
                    :author_1: David Heinemeier Hansson
                    :email_1: david@37signals.com
                    :author_2: Jason Fried
                    :email_2: jason@37signals.com

                    Getting Real details the business, design, programming, and marketing principles of 37signals.
                    """;
            var doc = documentFromString(input);
            var authors = doc.getAuthors();
            assertEquals(2, size(authors));
            var author1 = authors.get(0);
            assertEquals("david@37signals.com", author1.getEmail());
            assertEquals("David Heinemeier Hansson", author1.getFullName());
            assertEquals("David", author1.getFirstName());
            assertEquals("Heinemeier", author1.getMiddleName());
            assertEquals("Hansson", author1.getLastName());
            assertEquals("DHH", author1.getInitials());
            var author2 = authors.get(1);
            assertEquals("jason@37signals.com", author2.getEmail());
            assertEquals("Jason Fried", author2.getFullName());
            assertEquals("Jason", author2.getFirstName());
            assertNull(author2.getMiddleName());
            assertEquals("Fried", author2.getLastName());
            assertEquals("JF", author2.getInitials());
        }

        @Test
        @DisplayName("should not crash if nil cell text is passed to Cell constructor")
        void shouldNotCrashIfNilCellTextIsPassedToCellConstructor() throws Exception {
            var input = """
                    |===
                    |a
                    |===
                    """;
            var table = documentFromString(input).getBlocks().get(0);
            var cell = new Table.Cell(((Table) table).getRows().getBody().get(0).get(0).getColumn(), null);
            assertFalsy(cell.getStyle());
            assertEquals(AbstractNode.NORMAL_SUBS, cell.getSubs());
            assertEquals("", cell.getText());
        }

        @Test
        @DisplayName("should set option on node when set_option is called")
        void shouldSetOptionOnNodeWhenSet_optionIsCalled() throws Exception {
            var input = """
                    . three
                    . two
                    . one
                    """;
            var block = documentFromString(input).getBlocks().get(0);
            block.setOption("reversed");
            assertTruthy(block.isOption("reversed"));
            assertEquals("", get(block.getAttributes(), "reversed-option"));
        }

        @Test
        @DisplayName("enabled_options should return all options which are set")
        void enabled_optionsShouldReturnAllOptionsWhichAreSet() throws Exception {
            var input = """
                    [%interactive]
                    * [x] code
                    * [ ] test
                    * [ ] profit
                    """;
            var block = documentFromString(input).getBlocks().get(0);
            assertEquals(Set.of("checklist", "interactive"), block.getEnabledOptions());
        }

        @Test
        @DisplayName("should append option to existing options")
        void shouldAppendOptionToExistingOptions() throws Exception {
            var input = """
                    [%fancy]
                    . three
                    . two
                    . one
                    """;
            var block = documentFromString(input).getBlocks().get(0);
            block.setOption("reversed");
            assertTruthy(block.isOption("fancy"));
            assertTruthy(block.isOption("reversed"));
        }

        @Test
        @DisplayName("should not append option if option is already set")
        void shouldNotAppendOptionIfOptionIsAlreadySet() throws Exception {
            var input = """
                    [%reversed]
                    . three
                    . two
                    . one
                    """;
            var block = documentFromString(input).getBlocks().get(0);
            block.setOption("reversed");
            assertEquals("", get(block.getAttributes(), "reversed-option"));
        }

        @Test
        @DisplayName("should return set of option names")
        void shouldReturnSetOfOptionNames() throws Exception {
            var input = """
                    [%compact%reversed]
                    . three
                    . two
                    . one
                    """;
            var block = documentFromString(input).getBlocks().get(0);
            assertEquals(Set.of("compact", "reversed"), block.getEnabledOptions());
        }

        @Test
        @DisplayName("should set linenums option if linenums enabled on source block")
        void shouldSetLinenumsOptionIfLinenumsEnabledOnSourceBlock() throws Exception {
            var input = """
                    [,ruby,linenums]
                    ----
                    puts "Hello, World!"
                    ----
                    """;
            var block = documentFromString(input).getBlocks().get(0);
            assertTruthy(block.hasAttribute("linenums"));
            assertTruthy(block.isOption("linenums"));
        }

        @Test
        @DisplayName("should set linenums option if linenums enabled on fenced code block")
        void shouldSetLinenumsOptionIfLinenumsEnabledOnFencedCodeBlock() throws Exception {
            var input = """
                    ```ruby,linenums
                    puts "Hello, World!"
                    ```
                    """;
            var block = documentFromString(input).getBlocks().get(0);
            assertTruthy(block.hasAttribute("linenums"));
            assertTruthy(block.isOption("linenums"));
        }

        @Test
        @DisplayName("should not set linenums attribute if linenums option is enabled on source block")
        void shouldNotSetLinenumsAttributeIfLinenumsOptionIsEnabledOnSourceBlock() throws Exception {
            var input = """
                    [%linenums,ruby]
                    ----
                    puts "Hello, World!"
                    ----
                    """;
            var block = documentFromString(input).getBlocks().get(0);
            assertFalsy(block.hasAttribute("linenums"));
            assertTruthy(block.isOption("linenums"));
        }

        @Test
        @DisplayName("should not set linenums attribute if linenums option is enabled on fenced code block")
        void shouldNotSetLinenumsAttributeIfLinenumsOptionIsEnabledOnFencedCodeBlock() throws Exception {
            var input = """
                    :source-linenums-option:

                    ```ruby
                    puts "Hello, World!"
                    ```
                    """;
            var block = documentFromString(input).getBlocks().get(0);
            assertFalsy(block.hasAttribute("linenums"));
            assertTruthy(block.isOption("linenums"));
        }

        @Test
        @DisplayName("table column should not be a block or inline")
        void tableColumnShouldNotBeABlockOrInline() throws Exception {
            var input = """
                    |===
                    |a
                    |===
                    """;
            var col = ((Table) documentFromString(input).getBlocks().get(0)).getColumns().get(0);
            assertFalsy(col.isBlock());
            assertFalsy(col.isInline());
        }

        @Test
        @DisplayName("table cell should be a block")
        void tableCellShouldBeABlock() throws Exception {
            var input = """
                    |===
                    |a
                    |===
                    """;
            var cell = ((Table) documentFromString(input).getBlocks().get(0)).getRows().getBody().get(0).get(0);
            assertInstanceOf(AbstractBlock.class, cell);
            assertTruthy(cell.isBlock());
            assertFalsy(cell.isInline());
        }

        @Test
        @DisplayName("next_adjacent_block should return next block")
        void next_adjacent_blockShouldReturnNextBlock() throws Exception {
            var input = """
                    first

                    second
                    """;
            var doc = documentFromString(input);
            assertEquals(doc.getBlocks().get(1), doc.getBlocks().get(0).getNextAdjacentBlock());
        }

        @Test
        @DisplayName("next_adjacent_block should return next sibling of parent if called on last sibling")
        void next_adjacent_blockShouldReturnNextSiblingOfParentIfCalledOnLastSibling() throws Exception {
            var input = """
                    --
                    first
                    --

                    second
                    """;
            var doc = documentFromString(input);
            assertEquals(doc.getBlocks().get(1), doc.getBlocks().get(0).getBlocks().get(0).getNextAdjacentBlock());
        }

        @Test
        @DisplayName("next_adjacent_block should return next sibling of list if called on last item")
        void next_adjacent_blockShouldReturnNextSiblingOfListIfCalledOnLastItem() throws Exception {
            var input = """
                    * first

                    second
                    """;
            var doc = documentFromString(input);
            assertEquals(doc.getBlocks().get(1), doc.getBlocks().get(0).getBlocks().get(0).getNextAdjacentBlock());
        }

        @Test
        @DisplayName("next_adjacent_block should return next item in dlist if called on last block of list item")
        void next_adjacent_blockShouldReturnNextItemInDlistIfCalledOnLastBlockOfListItem() throws Exception {
            var input = """
                    first::
                    desc
                    +
                    more desc

                    second::
                    desc
                    """;
            var doc = documentFromString(input);
            assertEquals(((List) doc.getBlocks().get(0)).getItems().get(1), ((DescriptionListEntry) ((List) doc.getBlocks().get(0)).getItems().get(0)).getDescription().getBlocks().get(0).getNextAdjacentBlock());
        }

        @Test
        @DisplayName("should return true when sections? is called on a document or section that has sections")
        void shouldReturnTrueWhenSectionsIsCalledOnADocumentOrSectionThatHasSections() throws Exception {
            var input = """
                    = Document Title

                    == First Section

                    === First subsection

                    content
                    """;
            var doc = documentFromString(input);
            assertTruthy(doc.hasSections());
            assertTruthy(doc.getBlocks().get(0).hasSections());
        }

        @Test
        @DisplayName("should return false when sections? is called on a document with no sections")
        void shouldReturnFalseWhenSectionsIsCalledOnADocumentWithNoSections() throws Exception {
            var input = """
                    = Document Title

                    content
                    """;
            var doc = documentFromString(input);
            assertFalsy(doc.hasSections());
        }

        @Test
        @DisplayName("should return false when sections? is called on a section with no sections")
        void shouldReturnFalseWhenSectionsIsCalledOnASectionWithNoSections() throws Exception {
            var input = """
                    = Document Title

                    == First Section
                    """;
            var doc = documentFromString(input);
            assertFalsy(doc.getBlocks().get(0).hasSections());
        }

        @Test
        @DisplayName("should return false when sections? is called on anything that is not a section")
        void shouldReturnFalseWhenSectionsIsCalledOnAnythingThatIsNotASection() throws Exception {
            var input = """
                    .Title
                    ====
                    I'm not section!
                    ====

                    [NOTE]
                    I'm not a section either!
                    """;
            var doc = documentFromString(input);
            assertFalsy(doc.getBlocks().get(0).hasSections());
            assertFalsy(doc.getBlocks().get(1).hasSections());
        }
    }

    @Nested
    @DisplayName("SafeMode")
    class SafeModeTests {

        @Test
        @DisplayName("should return nil if safe mode value not recognized")
        void shouldReturnNilIfSafeModeValueNotRecognized() throws Exception {
            assertNull(SafeMode.nameForValue(99));
        }

        @Test
        @DisplayName("should return nil if safe mode name not recognized")
        void shouldReturnNilIfSafeModeNameNotRecognized() throws Exception {
            assertNull(SafeMode.valueForName("unknown"));
        }
    }
}
