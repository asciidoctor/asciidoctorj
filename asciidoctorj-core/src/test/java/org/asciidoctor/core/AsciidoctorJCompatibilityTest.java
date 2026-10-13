package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import org.asciidoctor.core.extension.BlockProcessor;
import org.asciidoctor.core.extension.Contexts;
import org.asciidoctor.core.extension.DefaultAttribute;
import org.asciidoctor.core.extension.DocinfoProcessor;
import org.asciidoctor.core.extension.Extensions;
import org.asciidoctor.core.extension.Format;
import org.asciidoctor.core.extension.FormatType;
import org.asciidoctor.core.extension.Group;
import org.asciidoctor.core.extension.InlineMacroProcessor;
import org.asciidoctor.core.extension.Location;
import org.asciidoctor.core.extension.LocationType;
import org.asciidoctor.core.extension.Name;
import org.asciidoctor.core.extension.PositionalAttributes;
import org.asciidoctor.core.log.LogHandlerLogger;
import org.asciidoctor.core.log.LogRecord;

/**
 * Tests of the API that mirrors AsciidoctorJ (which is not covered by the tests of the Ruby implementation).
 */
public class AsciidoctorJCompatibilityTest extends TestHelper {

    @Test
    void shouldReportVersionOfArtifact() {
        // NOTE the version is injected into the resources by Maven
        assertTrue(Asciidoctor.VERSION.matches("\\d+\\.\\d+\\.\\d+.*"), "unexpected version: " + Asciidoctor.VERSION);
        assertEquals(Asciidoctor.VERSION, Asciidoctor.load("").getAttribute("asciidoctor-version"));
    }

    @Test
    void shouldConvertUsingOptionsBuilder() {
        var options = Options.builder()
                .safe(SafeMode.SAFE)
                .standalone(false)
                .attributes(Attributes.builder().attribute("product", "Asciidoctor").sectionNumbers(true).build())
                .build();
        var output = (String) Asciidoctor.convert("== Section\n\n{product}", options);
        assertXpath("//h2[text()=\"1. Section\"]", output, 1);
        assertXpath("//p[text()=\"Asciidoctor\"]", output, 1);
    }

    @Test
    void shouldUnsetAttributeUsingFalseFlag() {
        var options = Options.builder().attributes(Attributes.builder().sectionNumbers(false).build()).build();
        var doc = Asciidoctor.load("= Title", options);
        assertFalse(doc.hasAttribute("sectnums"));
    }

    @Test
    void shouldSetTableOfContentsPlacement() {
        var attributes = Attributes.builder().tableOfContents(Placement.LEFT).build();
        assertEquals("left", attributes.map().get("toc"));
    }

    @Test
    void shouldResolveSafeModeByLevel() {
        assertEquals(SafeMode.SERVER, SafeMode.safeMode(10));
        assertThrows(IllegalArgumentException.class, () -> SafeMode.safeMode(5));
    }

    @Name("shout")
    @Contexts({ Contexts.PARAGRAPH })
    @org.asciidoctor.core.extension.ContentModel(org.asciidoctor.core.extension.ContentModel.SIMPLE)
    @PositionalAttributes("suffix")
    @DefaultAttribute(key = "suffix", value = "!")
    public static class ShoutBlock extends BlockProcessor {
        @Override
        public AbstractBlock process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {
            var lines = reader.readLines().stream().map(line -> line.toUpperCase() + attributes.get("suffix")).toList();
            return createParagraph(parent, lines, Map.of());
        }
    }

    @Name("emoji")
    @Format(FormatType.SHORT)
    public static class EmojiMacro extends InlineMacroProcessor {
        @Override
        public Object process(AbstractNode parent, String target, Map<String, Object> attributes) {
            return createInline(parent, "quoted", "[" + attributes.get("1") + "]", Map.of());
        }
    }

    @Location(LocationType.FOOTER)
    public static class FooterDocinfo extends DocinfoProcessor {
        @Override
        public String process(Document document) {
            return "<!-- footer -->";
        }
    }

    @Test
    void shouldConfigureExtensionsUsingAnnotations() {
        var registry = Extensions.create((Group) reg -> {
            reg.block(ShoutBlock.class);
            reg.inlineMacro(EmojiMacro.class);
            reg.docinfoProcessor(FooterDocinfo.class);
        });
        var input = """
                [shout]
                hello

                [shout,?]
                hi

                emoji:[smile]
                """;
        var output = (String) Asciidoctor.convert(input, Options.of("extension_registry", registry, "standalone", true));
        assertXpath("//p[text()=\"HELLO!\"]", output, 1);
        assertXpath("//p[text()=\"HI?\"]", output, 1);
        assertXpath("//p[text()=\"[smile]\"]", output, 1);
        assertTrue(output.contains("<!-- footer -->\n</body>"));
    }

    @Test
    void shouldForwardMessagesToLogHandlers() {
        var records = new ArrayList<LogRecord>();
        var oldLogger = LoggerManager.getLogger();
        try {
            LoggerManager.setLogger(new LogHandlerLogger(List.of(records::add)));
            Asciidoctor.convert("[#a]\nfirst\n\n[#a]\nsecond", Options.builder().build());
        } finally {
            LoggerManager.setLogger(oldLogger);
        }
        assertEquals(1, records.size());
        assertEquals(Logger.Severity.WARN, records.get(0).getSeverity());
        assertEquals("id assigned to block already in use: a", records.get(0).getMessage());
        assertEquals(5, records.get(0).getCursor().getLineNumber());
    }

    @Test
    void shouldManageSubstitutionsByName() {
        var block = (Block) Asciidoctor.load("*bold* <tag>", Options.builder().build()).getBlocks().get(0);
        assertEquals(List.of("specialcharacters", "quotes", "attributes", "replacements", "macros", "post_replacements"),
                block.getSubstitutions());
        assertTrue(block.isSubstitutionEnabled("quotes"));
        block.removeSubstitution("quotes");
        assertFalse(block.isSubstitutionEnabled("quotes"));
        assertEquals("*bold* &lt;tag&gt;", block.getContent());
        block.setSubstitutions("quotes");
        assertEquals("<strong>bold</strong> <tag>", block.getContent());
        assertThrows(IllegalArgumentException.class, () -> block.addSubstitution("unknown"));
    }

    @Test
    void shouldFindBlocksUsingSelectorMap() {
        var doc = Asciidoctor.load("[.lead]\nfirst\n\nsecond\n\n----\ncode\n----", Map.of());
        assertEquals(2, doc.findBy(Map.of("context", ":paragraph")).size());
        assertEquals(1, doc.findBy(Map.of(":context", "paragraph", "role", "lead")).size());
        assertEquals(1, doc.findBy(Map.of("context", "listing")).size());
    }

    @Test
    void shouldProvideDocumentInformation() {
        var input = """
                = Main Title: Subtitle
                Doc Writer <doc@example.org>
                v1.0, 2024-01-01: First release
                """;
        var doc = Asciidoctor.load(input, Map.of());
        assertEquals("Main Title", doc.getStructuredDoctitle().getMain());
        assertEquals("Subtitle", doc.getStructuredDoctitle().getSubtitle());
        assertEquals("1.0", doc.getRevisionInfo().getNumber());
        assertEquals("2024-01-01", doc.getRevisionInfo().getDate());
        assertEquals("First release", doc.getRevisionInfo().getRemark());
        var author = doc.getAuthors().get(0);
        assertEquals("Doc Writer", author.getFullName());
        assertEquals("Doc", author.getFirstName());
        assertEquals("Writer", author.getLastName());
        assertEquals("doc@example.org", author.getEmail());
        assertEquals("DW", author.getInitials());
        assertEquals(1, doc.getAndIncrementCounter("count"));
        assertEquals(2, doc.getAndIncrementCounter("count"));
        assertEquals(10, doc.getAndIncrementCounter("other", 10));
    }

    @Name("kbd2")
    @Format(FormatType.LONG)
    public static class PhraseMacro extends InlineMacroProcessor {
        @Override
        public Object process(AbstractNode parent, String target, Map<String, Object> attributes) {
            return createPhraseNode(parent, "kbd", target, Map.of("keys", List.of(target)));
        }
    }

    public static class SigningTreeprocessor extends org.asciidoctor.core.extension.TreeProcessor {
        @Override
        public Document process(Document document) {
            document.append(createParagraph(document, "signed", new java.util.LinkedHashMap<>()));
            return document;
        }
    }

    @Test
    void shouldSupportAsciidoctorJExtensionApi() {
        var registry = Extensions.create((Group) reg -> {
            reg.inlineMacro(PhraseMacro.class);
            reg.treeprocessor(SigningTreeprocessor.class);
        });
        var output = (String) Asciidoctor.convert("press kbd2:Enter[]", Options.of("extension_registry", registry));
        assertXpath("//kbd[text()=\"Enter\"]", output, 1);
        assertXpath("//p[text()=\"signed\"]", output, 1);
    }

    @org.asciidoctor.core.converter.ConverterFor(value = "texty", format = "text", suffix = ".txt")
    public static class TextyConverter extends org.asciidoctor.core.converter.AbstractConverter<String> {
        public TextyConverter(String backend, Map<String, Object> opts) {
            super(backend, opts);
            handle("document", AbstractBlock.class, node -> node.getContent());
            handle("embedded", AbstractBlock.class, node -> node.getContent());
            handle("paragraph", Block.class, node -> node.getSource());
        }
    }

    @Test
    void shouldRegisterConverterUsingConverterForAnnotation() {
        try {
            org.asciidoctor.core.converter.Converter.register(TextyConverter.class);
            var doc = Asciidoctor.load("hello", Map.of("backend", "texty"));
            assertEquals(".txt", doc.getOutfilesuffix());
            assertEquals("text", doc.getAttribute("filetype"));
            assertEquals("hello", doc.convert());
        } finally {
            org.asciidoctor.core.converter.Converter.getFactory().unregisterAll();
        }
        assertThrows(IllegalArgumentException.class,
                () -> org.asciidoctor.core.converter.Converter.register(ShoutBlock.class));
    }

    /** A converter for a binary format (the UTF-16 encoding of the text). */
    @org.asciidoctor.core.converter.ConverterFor("utf16")
    public static class BinaryConverter extends org.asciidoctor.core.converter.AbstractConverter<byte[]> {
        public BinaryConverter(String backend, Map<String, Object> opts) {
            super(backend, opts);
            handle("document", AbstractBlock.class, node -> encode(node.getBlocks()));
            handle("embedded", AbstractBlock.class, node -> encode(node.getBlocks()));
        }

        private static byte[] encode(List<AbstractBlock> blocks) {
            var text = blocks.stream().map(block -> ((Block) block).getSource()).reduce("", (a, b) -> a + b);
            return text.getBytes(java.nio.charset.StandardCharsets.UTF_16BE);
        }
    }

    /** The output of a converter that is neither a String nor a byte array. */
    public record Slides(List<String> titles) {
    }

    /** A converter that produces a custom object and writes it. */
    public static class SlidesConverter extends org.asciidoctor.core.converter.AbstractConverter<Slides>
            implements org.asciidoctor.core.Writer<Slides> {
        public SlidesConverter(String backend, Map<String, Object> opts) {
            super(backend, opts);
            handle("document", Document.class, doc -> new Slides(doc.getSections().stream().map(AbstractBlock::getTitle).toList()));
        }

        @Override
        public void write(Slides output, Object target) {
            org.asciidoctor.core.Helpers.writeFile(String.valueOf(target), String.join("|", output.titles()));
        }
    }

    @Test
    void shouldConvertToBinaryOutput(@TempDir java.nio.file.Path tmp) throws Exception {
        try {
            org.asciidoctor.core.converter.Converter.register(BinaryConverter.class);
            var bytes = Asciidoctor.convert("ab", Options.of("backend", "utf16"), byte[].class);
            assertArrayEquals(new byte[] { 0, 'a', 0, 'b' }, bytes);
            assertArrayEquals(bytes, Asciidoctor.load("ab", Map.of("backend", "utf16")).convert(byte[].class));
            // a binary output is written as is
            var out = tmp.resolve("out.bin");
            Asciidoctor.convert("ab", Options.of("backend", "utf16", "to_file", out.toString(), "safe", "unsafe"));
            assertArrayEquals(bytes, java.nio.file.Files.readAllBytes(out));
            var stream = new java.io.ByteArrayOutputStream();
            Asciidoctor.convert("ab", Options.of("backend", "utf16", "to_file", stream));
            assertArrayEquals(bytes, stream.toByteArray());
        } finally {
            org.asciidoctor.core.converter.Converter.getFactory().unregisterAll();
        }
    }

    @Test
    void shouldWriteCustomOutputUsingWriter(@TempDir java.nio.file.Path tmp) throws Exception {
        try {
            org.asciidoctor.core.converter.Converter.register(SlidesConverter.class, "slides");
            var input = "= Deck\n\n== One\n\n== Two";
            var slides = Asciidoctor.convert(input, Options.of("backend", "slides", "standalone", true), Slides.class);
            assertEquals(List.of("One", "Two"), slides.titles());
            var out = tmp.resolve("deck.txt");
            Asciidoctor.convert(input, Options.of("backend", "slides", "standalone", true, "to_file", out.toString(), "safe", "unsafe"));
            assertEquals("One|Two", java.nio.file.Files.readString(out));
        } finally {
            org.asciidoctor.core.converter.Converter.getFactory().unregisterAll();
        }
    }

    @Test
    void shouldNotWriteCustomOutputWithoutWriter(@TempDir java.nio.file.Path tmp) {
        try {
            org.asciidoctor.core.converter.Converter.register((org.asciidoctor.core.converter.Converter.Constructor) (backend, opts) ->
                    (node, transform, o) -> new Slides(List.of()), "slides");
            var out = tmp.resolve("deck.txt").toString();
            assertThrows(IllegalStateException.class,
                    () -> Asciidoctor.convert("text", Options.of("backend", "slides", "to_file", out, "safe", "unsafe")));
        } finally {
            org.asciidoctor.core.converter.Converter.getFactory().unregisterAll();
        }
    }

    @Test
    void shouldGroupCellsInRows() {
        var input = """
                [%header%footer]
                |===
                |A |B
                |1 |2
                |x |y
                |===
                """;
        var table = (Table) Asciidoctor.load(input, Map.of()).getBlocks().get(0);
        assertEquals(1, table.getHeader().size());
        assertEquals(1, table.getBody().size());
        assertEquals(1, table.getFooter().size());
        Table.Row row = table.getBody().get(0);
        assertEquals(List.of("1", "2"), row.getCells().stream().map(Table.Cell::getText).toList());
        assertEquals("A", table.getHeader().get(0).get(0).getText());
    }
}
