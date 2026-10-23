package org.asciidoctor;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.ast.*;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.asciidoctor.util.OptionsTestHelper.emptyOptions;


@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenAsciiDocIsLoadedToDocumentTest {

    private static final String DOCUMENT = "= Document Title\n" +
            "\n" +
            "preamble\n" +
            "\n" +
            "== Section A\n" +
            "\n" +
            "paragraph\n" +
            "\n" +
            "--\n" +
            "Exhibit A::\n" +
            "+\n" +
            "[#tiger.animal]\n" +
            "image::tiger.png[Tiger]\n" +
            "--\n" +
            "\n" +
            "image::cat.png[Cat]\n" +
            "\n" +
            "== Section B\n" +
            "\n" +
            "paragraph\n" +
            "\n" +
            "|===\n" +
            "|A\n" +
            "|B\n" +
            "|===";

    private static final String ROLE = "[\"quote\", \"author\", \"source\", role=\"famous\"]\n" +
            "____\n" +
            "A famous quote.\n" +
            "____";

    private static final String REFTEXT = "[reftext=\"the first section\"]\n" +
            "== Section One\n" +
            "\n" +
            "content";

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;


    @Test
    public void should_return_empty_sources_when_document_is_null() {
        assertEmptySources(asciidoctor.load(null, emptyOptions()));
    }

    @Test
    public void should_return_empty_sources_when_document_is_empty() {
        assertEmptySources(asciidoctor.load(null, emptyOptions()));
    }

    private static void assertEmptySources(Document document) {
        String source = document.getSource();
        assertThat(source).isEmpty();
        List<String> sourceLines = document.getSourceLines();
        assertThat(sourceLines).hasSize(0);
    }

    @Test
    public void should_return_section_blocks() {
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        Section section = (Section) document.getBlocks().get(1);
        assertThat(section.getIndex()).isEqualTo(0);
        assertThat(section.getSectionName()).isIn("sect1", "section");
        assertThat(section.isSpecial()).isFalse();
    }

    @Test
    public void should_return_blocks_from_a_document() {
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        assertThat(document.getDoctitle()).isEqualTo("Document Title");
        assertThat(document.getBlocks()).hasSize(3);
    }

    @Test
    public void should_return_a_document_object_from_string() {
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        assertThat(document.getDoctitle()).isEqualTo("Document Title");
    }

    @Test
    public void should_find_all_nodes() {
        Document document = asciidoctor.load(DOCUMENT, Options.builder().sourcemap(true).build());
        List<StructuralNode> findBy = document.findBy(new HashMap<>());
        assertThat(findBy).hasSize(17);
        List<Cell> tableCells = findBy.stream()
                .filter(Cell.class::isInstance)
                .map(Cell.class::cast)
                .collect(toList());
        assertThat(tableCells).hasSize(2);
        assertThat(tableCells.get(0).getSourceLocation().getLineNumber()).isEqualTo(23);
        assertThat(tableCells.get(1).getSourceLocation().getLineNumber()).isEqualTo(24);
    }

    @Test
    public void should_find_elements_from_document() {
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        List<StructuralNode> findBy = document.findBy(Map.of("context", ":image"));

        assertThat(findBy).hasSize(2);
        assertThat((String) findBy.get(0).getAttributes().get("target")).isEqualTo("tiger.png");
        assertThat(findBy.get(0).getLevel()).isGreaterThan(0);
    }

    @Test
    public void should_return_options_from_parsed_string() {
        Options options = Options.builder()
                .compact(true)
                .build();
        Document document = asciidoctor.load(DOCUMENT, options);

        Map<Object, Object> documentOptions = document.getOptions();

        assertThat((Boolean) documentOptions.get("compact")).isTrue();
    }

    @Test
    public void should_return_options_from_parsed_file(
            @ClasspathResource("sourcelocation.adoc") File resource) {

        Options options = Options.builder()
                .compact(true)
                .build();
        Document document = asciidoctor.loadFile(resource, options);

        Map<Object, Object> documentOptions = document.getOptions();

        assertThat((Boolean) documentOptions.get("compact")).isTrue();
    }

    @Test
    public void should_return_node_name() {
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        assertThat(document.getNodeName()).isEqualTo("document");
    }

    @Test
    public void should_return_if_it_is_inline() {
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        assertThat(document.isInline()).isFalse();
    }

    @Test
    public void should_return_if_it_is_block() {
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        assertThat(document.isBlock()).isTrue();
    }

    @Test
    public void should_be_able_to_manipulate_attributes() {
        Options options = Options.builder()
                .attributes(Attributes.builder().dataUri(true).build())
                .compact(true)
                .build();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.getAttributes()).containsKey("toc-placement");
        assertThat(document.hasAttribute("toc-placement")).isTrue();
        assertThat(document.isAttribute("toc-placement", "auto", false)).isTrue();
        assertThat(document.getAttribute("toc-placement", "", false).toString()).isEqualTo("auto");
    }

    @Test
    public void should_be_able_to_get_roles() {
        Document document = asciidoctor.load(ROLE, emptyOptions());
        StructuralNode abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.getRole()).isEqualTo("famous");
        assertThat(abstractBlock.hasRole("famous")).isTrue();
        assertThat(abstractBlock.isRole()).isTrue();
        assertThat(abstractBlock.getRoles()).containsExactly("famous");
    }

    @Test
    public void should_be_able_to_add_role() {
        final String tmpRole = "tmpRole";
        Document document = asciidoctor.load(ROLE, emptyOptions());
        StructuralNode abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.hasRole(tmpRole)).isFalse();
        abstractBlock.addRole(tmpRole);
        assertThat(abstractBlock.hasRole(tmpRole)).isTrue();
    }

    @Test
    public void should_be_able_to_remove_role() {
        final String famousRole = "famous";
        Document document = asciidoctor.load(ROLE, emptyOptions());
        StructuralNode abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.hasRole(famousRole)).isTrue();
        abstractBlock.removeRole(famousRole);
        assertThat(abstractBlock.hasRole(famousRole)).isFalse();
    }

    @Test
    public void should_be_able_to_get_reftext() {
        Document document = asciidoctor.load(REFTEXT, emptyOptions());
        StructuralNode abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.getReftext()).isEqualTo("the first section");
        assertThat(abstractBlock.isReftext()).isTrue();
    }

    @Test
    public void should_be_able_to_get_icon_uri_string_reference() {
        Options options = Options.builder()
                .attributes(Attributes.builder().dataUri(false).build())
                .compact(true)
                .build();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.iconUri("note")).isEqualTo("./images/icons/note.png");
    }

    @Test
    public void should_be_able_to_get_icon_uri() {
        Options options = Options.builder().safe(SafeMode.SAFE)
                .attributes(Attributes.builder().dataUri(true).icons("font").build())
                .compact(true)
                .build();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.iconUri("note")).isIn(
                "data:image/png:base64,", // <= Asciidoctor 1.5.5
                "data:image/png;base64,"); // >= Asciidoctor 1.5.6
    }

    @Test
    public void should_be_able_to_get_media_uri() {
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        assertThat(document.mediaUri("target")).isEqualTo("target");
    }

    @Test
    public void should_be_able_to_get_image_uri() {
        Options options = Options.builder().safe(SafeMode.SAFE)
                .attributes(Attributes.builder().dataUri(false).build())
                .compact(true)
                .build();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.imageUri("target.jpg")).isEqualTo("target.jpg");
        assertThat(document.imageUri("target.jpg", "imagesdir")).isEqualTo("target.jpg");
    }

    @Test
    public void should_be_able_to_normalize_web_path() {
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        assertThat(document.normalizeWebPath("target", null, true)).isEqualTo("target");
    }

    @Test
    public void should_be_able_to_read_asset(
            @ClasspathResource("rendersample.asciidoc") File inputFile) throws IOException {

        Options options = Options.builder().safe(SafeMode.SAFE)
                .attributes(Attributes.builder().dataUri(false).build())
                .compact(true)
                .build();
        Document document = asciidoctor.load(DOCUMENT, options);

        String content = document.readAsset(inputFile.getAbsolutePath(), new HashMap<>());
        assertThat(content.replace("\r", "")).isEqualTo(Files.readString(inputFile.toPath()).replace("\r", ""));
    }

    @Test
    public void should_be_able_to_set_attribute() {
        final Object attributeName = "testattribute";
        final Object attributeValue = "testvalue";
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        assertThat(document.setAttribute(attributeName, attributeValue, true)).isTrue();
        assertThat(document.getAttribute(attributeName)).isEqualTo(attributeValue);
        assertThat(document.getAttributes().get(attributeName)).isEqualTo(attributeValue);
    }

    @Test
    public void should_be_able_to_set_attribute_on_attributes_map() {
        final String attributeName = "testattribute";
        final Object attributeValue = "testvalue";
        Document document = asciidoctor.load(DOCUMENT, emptyOptions());
        document.getAttributes().put(attributeName, attributeValue);
        assertThat(document.getAttribute(attributeName)).isEqualTo(attributeValue);
        assertThat(document.getAttributes().get(attributeName)).isEqualTo(attributeValue);
    }

    @Test
    public void should_be_able_to_get_source_location(
            @ClasspathResource("sourcelocation.adoc") File file) {

        // When
        Document document = asciidoctor.loadFile(file, Options.builder().sourcemap(true).docType("book").build());
        Map<Object, Object> selector = new HashMap<>();
        selector.put("context", ":paragraph");
        List<StructuralNode> findBy = document.findBy(selector);

        // Then
        Cursor sourceLocation1 = findBy.get(0).getSourceLocation();
        assertThat(sourceLocation1.getLineNumber()).isEqualTo(3);
        assertThat(sourceLocation1.getPath()).isEqualTo(file.getName());
        assertThat(sourceLocation1.getFile()).isEqualTo(file.getName());
        assertThat(sourceLocation1.getDir()).isEqualTo(file.getParent().replaceAll("\\\\", "/"));

        Cursor sourceLocation2 = findBy.get(1).getSourceLocation();
        assertThat(sourceLocation2.getLineNumber()).isEqualTo(8);
        assertThat(sourceLocation2.getPath()).isEqualTo(file.getName());
        assertThat(sourceLocation2.getFile()).isEqualTo(file.getName());
        assertThat(sourceLocation2.getDir()).isEqualTo(file.getParent().replaceAll("\\\\", "/"));
    }

    @Test
    public void should_get_attributes() {

        final String documentWithAttributes = "= Document Title\n" +
                ":docattr: docvalue\n" +
                "\n" +
                "preamble\n" +
                "\n" +
                "== Section A\n" +
                "\n" +
                "paragraph\n" +
                "\n";

        Document document = asciidoctor.load(documentWithAttributes, emptyOptions());
        List<StructuralNode> blocks = document.getBlocks();

        Section section = (Section) blocks.get(1);
        section.setAttribute("testattr", "testvalue", true);

        assertThat(document.hasAttribute("testattr")).isFalse();

        assertThat(section.hasAttribute("testattr")).isTrue();
        assertThat(section.hasAttribute("testattr", true)).isTrue();
        assertThat(section.hasAttribute("testattr", false)).isTrue();
        assertThat(section.isAttribute("testattr", "testvalue")).isTrue();

        assertThat(section.hasAttribute("docattr", true)).isTrue();
        assertThat(section.hasAttribute("docattr", false)).isFalse();
    }

    @Test
    public void should_get_content_model() {
        final String documentWithPreambleAndSection = ""
                + "= Document Title\n"
                + "\n"
                + "A test document with a preamble and a section.\n"
                + "\n"
                + "The preamble contains multiple paragraphs to force the outer block to be compound.\n"
                + "\n"
                + "== First Section\n"
                + "\n"
                + "And herein lies the problem.\n"
                + "\n";

        Document document = asciidoctor.load(documentWithPreambleAndSection, emptyOptions());
        List<StructuralNode> blocks = document.getBlocks();

        StructuralNode preambleContainer = blocks.get(0);
        assertThat(preambleContainer.getContentModel()).isEqualTo("compound");

        assertThat(preambleContainer.getBlocks().get(0).getContentModel()).isEqualTo("simple");
        assertThat(preambleContainer.getBlocks().get(1).getContentModel()).isEqualTo("simple");

        Section section = (Section) blocks.get(1);
        assertThat(section.getContentModel()).isEqualTo("compound");
        assertThat(section.getBlocks().get(0).getContentModel()).isEqualTo("simple");
    }

    @Test
    public void should_be_able_to_get_parent_from_document() {
        String content = "== A small Example\n" +
                "\n" +
                "Lorem ipsum dolor sit amet:\n";

        Document document = asciidoctor.load(content, emptyOptions());
        assertThat(document.getParent()).isNull();
    }

    @Test
    public void should_read_caption() {
        String content = "[caption=\"Table A. \"]\n" +
                ".A formal table\n" +
                "|===\n" +
                "|Cell in column 1, row 1\n" +
                "|Cell in column 2, row 1\n" +
                "|===";

        Document document = asciidoctor.load(content, emptyOptions());
        assertThat(document.getBlocks()).isNotNull();
        assertThat(document.getBlocks().size()).isEqualTo(1);
        assertThat(document.getBlocks().get(0).getCaption()).isEqualTo("Table A. ");
    }

    @Test
    public void should_get_empty_collection_when_no_author_is_set() {
        String content = "= Sample Document\n" +
                "\n" +
                "Preamble...";

        Document document = asciidoctor.load(content, emptyOptions());

        List<Author> authors = document.getAuthors();
        assertThat(authors).hasSize(0);
    }

    @Test
    public void should_get_single_author_when_only_one_is_set_in_author_line() {
        String content = "= Sample Document\n" +
                "Doc Writer <doc.writer@asciidoc.org>" +
                "\n" +
                "Preamble...";

        Document document = asciidoctor.load(content, emptyOptions());

        List<Author> authors = document.getAuthors();
        assertThat(authors).hasSize(1);

        Author author = authors.get(0);
        assertThat(author.getEmail()).isEqualTo("doc.writer@asciidoc.org");
        assertThat(author.getFullName()).isEqualTo("Doc Writer");
        assertThat(author.getFirstName()).isEqualTo("Doc");
        assertThat(author.getMiddleName()).isNull();
        assertThat(author.getLastName()).isEqualTo("Writer");
        assertThat(author.getInitials()).isEqualTo("DW");
    }

    @Test
    public void should_get_single_author_when_only_one_is_set_in_attribute() {
        String content = "= Sample Document\n" +
                ":author: Doc Writer\n" +
                ":email: doc.writer@asciidoc.org\n" +
                "\n" +
                "Preamble...";

        Document document = asciidoctor.load(content, emptyOptions());

        List<Author> authors = document.getAuthors();
        assertThat(authors).hasSize(1);

        Author author = authors.get(0);
        assertThat(author.getEmail()).isEqualTo("doc.writer@asciidoc.org");
        assertThat(author.getFullName()).isEqualTo("Doc Writer");
        assertThat(author.getFirstName()).isEqualTo("Doc");
        assertThat(author.getMiddleName()).isNull();
        assertThat(author.getLastName()).isEqualTo("Writer");
        assertThat(author.getInitials()).isEqualTo("DW");
    }

    @Test
    public void should_get_multiple_authors_when_they_arez_set_in_author_line() {
        String content = "= Sample Document\n" +
                "Doc Writer <doc.writer@asciidoc.org>; John A. Smith <john.smith@asciidoc.org>\n" +
                "\n" +
                "Preamble...";

        Document document = asciidoctor.load(content, emptyOptions());

        List<Author> authors = document.getAuthors();
        assertThat(authors).hasSize(2);

        Author firstAuthor = authors.get(0);
        assertThat(firstAuthor.getEmail()).isEqualTo("doc.writer@asciidoc.org");
        assertThat(firstAuthor.getFullName()).isEqualTo("Doc Writer");
        assertThat(firstAuthor.getFirstName()).isEqualTo("Doc");
        assertThat(firstAuthor.getMiddleName()).isNull();
        assertThat(firstAuthor.getLastName()).isEqualTo("Writer");
        assertThat(firstAuthor.getInitials()).isEqualTo("DW");

        Author secondAuthor = authors.get(1);
        assertThat(secondAuthor.getEmail()).isEqualTo("john.smith@asciidoc.org");
        assertThat(secondAuthor.getFullName()).isEqualTo("John A. Smith");
        assertThat(secondAuthor.getFirstName()).isEqualTo("John");
        assertThat(secondAuthor.getMiddleName()).isEqualTo("A.");
        assertThat(secondAuthor.getLastName()).isEqualTo("Smith");
        assertThat(secondAuthor.getInitials()).isEqualTo("JAS");
    }

    @Test
    public void should_get_revision_info() {
        String content = "= Sample Document\n" +
                "Doc Writer <doc.writer@asciidoc.org>\n" +
                "v1.0, 2013-05-20: First draft\n" +
                "\n" +
                "Preamble...";

        Document document = asciidoctor.load(content, emptyOptions());

        RevisionInfo revisionInfo = document.getRevisionInfo();

        assertThat(revisionInfo.getDate()).isEqualTo("2013-05-20");
        assertThat(revisionInfo.getNumber()).isEqualTo("1.0");
        assertThat(revisionInfo.getRemark()).isEqualTo("First draft");
    }

    @Test
    public void should_return_source_and_source_lines() {
        final String asciidoc = asciidocWithSections();

        Document document = loadDocument(asciidoc);

        String source = document.getSource();
        Assertions.assertThat(source).isEqualTo(asciidoc.trim());
        List<String> sourceLines = document.getSourceLines();
        Assertions.assertThat(sourceLines)
                .containsExactly("= Document Title",
                        "",
                        "== Section A",
                        "",
                        "Section A paragraph.",
                        "",
                        "=== Section A Subsection",
                        "",
                        "Section A 'subsection' paragraph.");
    }

    @Test
    public void should_return_source_and_source_lines_without_trailing() {
        final String asciidoc = "= Document Title\n\n" +
                "== Section\n\n" +
                "Hello\t  \n";

        Document document = loadDocument(asciidoc);

        String source = document.getSource();
        Assertions.assertThat(source)
                .isEqualTo("= Document Title\n\n== Section\n\nHello");
        List<String> sourceLines = document.getSourceLines();
        Assertions.assertThat(sourceLines)
                .containsExactly("= Document Title",
                        "",
                        "== Section",
                        "",
                        "Hello");
    }

    @Test
    public void should_return_source_and_source_lines_without_resolving_attributes() {
        final String asciidoc = "= Document Title\n" +
                ":an-attribute: a-value\n\n" +
                "This is {an-attribute}";

        Document document = loadDocument(asciidoc);

        String source = document.getSource();
        Assertions.assertThat(source)
                .isEqualTo("= Document Title\n:an-attribute: a-value\n\nThis is {an-attribute}");
        List<String> sourceLines = document.getSourceLines();
        Assertions.assertThat(sourceLines)
                .containsExactly("= Document Title",
                        ":an-attribute: a-value",
                        "",
                        "This is {an-attribute}");
    }

    @Test
    public void should_return_source_and_source_lines_without_resolving_includes() {
        final String asciidoc = "= Document Title\n\n" +
                "== Section\n\n" +
                "include::partial.adoc[]";

        Document document = loadDocument(asciidoc);

        String source = document.getSource();
        Assertions.assertThat(source)
                .isEqualTo("= Document Title\n\n== Section\n\ninclude::partial.adoc[]");
        List<String> sourceLines = document.getSourceLines();
        Assertions.assertThat(sourceLines)
                .containsExactly("= Document Title",
                        "",
                        "== Section",
                        "",
                        "include::partial.adoc[]");
    }

    private Document loadDocument(String source) {
        return asciidoctor.load(source, emptyOptions());
    }

    static String asciidocWithSections() {
        return "= Document Title\n\n" +
                "== Section A\n\n" +
                "Section A paragraph.\n\n" +
                "=== Section A Subsection\n\n" +
                "Section A 'subsection' paragraph.\n\n";
    }
}
