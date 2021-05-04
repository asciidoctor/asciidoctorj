package org.asciidoctor;

import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.ast.Author;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.RevisionInfo;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.internal.IOUtils;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasKey;


@RunWith(Arquillian.class)
public class WhenAsciiDocIsLoadedToDocument {

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
            "paragraph";

    private static final String ROLE = "[\"quote\", \"author\", \"source\", role=\"famous\"]\n" +
            "____\n" +
            "A famous quote.\n" +
            "____";

    private static final String REFTEXT = "[reftext=\"the first section\"]\n" +
            "== Section One\n" +
            "\n" +
            "content";

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpath = new ClasspathResources();

    @Test
    public void should_return_section_blocks() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<>());
        Section section = (Section) document.getBlocks().get(1);
        assertThat(section.getIndex(), is(0));
        assertThat(section.getSectionName(), either(is("sect1")).or(is("section")));
        assertThat(section.isSpecial(), is(false));
    }

    @Test
    public void should_return_blocks_from_a_document() {

        Document document = asciidoctor.load(DOCUMENT, new HashMap<>());
        assertThat(document.getDoctitle(), is("Document Title"));
        assertThat(document.getBlocks(), hasSize(3));
    }

    @Test
    public void should_return_a_document_object_from_string() {

        Document document = asciidoctor.load(DOCUMENT, new HashMap<>());
        assertThat(document.getDoctitle(), is("Document Title"));
    }

    @Test
    public void should_find_elements_from_document() {

        Document document = asciidoctor.load(DOCUMENT, new HashMap<>());
        Map<Object, Object> selector = new HashMap<>();
        selector.put("context", ":image");
        List<StructuralNode> findBy = document.findBy(selector);
        assertThat(findBy, hasSize(2));

        assertThat((String) findBy.get(0).getAttributes().get("target"), is("tiger.png"));
        assertThat(findBy.get(0).getLevel(), greaterThan(0));

    }

    @Test
    public void should_return_options_from_parsed_string_when_passed_as_map() {
        Map<String, Object> options = OptionsBuilder.options().compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);

        Map<Object, Object> documentOptions = document.getOptions();

        assertThat((Boolean) documentOptions.get("compact"), is(true));
    }

    @Test
    public void should_return_options_from_parsed_string_when_passed_as_options_object() {
        Options options = Options.builder()
                .compact(true)
                .build();
        Document document = asciidoctor.load(DOCUMENT, options);

        Map<Object, Object> documentOptions = document.getOptions();

        assertThat((Boolean) documentOptions.get("compact"), is(true));
    }

    @Test
    public void should_return_options_from_parsed_file_when_passed_as_options_object() {
        Options options = Options.builder()
                .compact(true)
                .build();
        File resource = classpath.getResource("sourcelocation.adoc");
        Document document = asciidoctor.loadFile(resource, options);

        Map<Object, Object> documentOptions = document.getOptions();

        assertThat((Boolean) documentOptions.get("compact"), is(true));
    }
    
    @Test
    public void should_return_node_name() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<>());
        assertThat(document.getNodeName(), is("document"));
    }

    @Test
    public void should_return_if_it_is_inline() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<>());
        assertThat(document.isInline(), is(false));
    }

    @Test
    public void should_return_if_it_is_block() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<>());
        assertThat(document.isBlock(), is(true));
    }

    @Test
    public void should_be_able_to_manipulate_attributes() {
        Map<String, Object> options = OptionsBuilder.options()
                .attributes(AttributesBuilder.attributes().dataUri(true))
                .compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.getAttributes(), hasKey("toc-placement"));
        assertThat(document.hasAttribute("toc-placement"), is(true));
        assertThat(document.isAttribute("toc-placement", "auto", false), is(true));
        assertThat(document.getAttribute("toc-placement", "", false).toString(), is("auto"));
    }

    @Test
    public void should_be_able_to_get_roles() {
        Document document = asciidoctor.load(ROLE, new HashMap<>());
        StructuralNode abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.getRole(), is("famous"));
        assertThat(abstractBlock.hasRole("famous"), is(true));
        assertThat(abstractBlock.isRole(), is(true));
        assertThat(abstractBlock.getRoles(), contains("famous"));
    }

    @Test
    public void should_be_able_to_add_role() {
        final String tmpRole = "tmpRole";
        Document document = asciidoctor.load(ROLE, new HashMap<>());
        StructuralNode abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.hasRole(tmpRole), is(false));
        abstractBlock.addRole(tmpRole);
        assertThat(abstractBlock.hasRole(tmpRole), is(true));
    }

    @Test
    public void should_be_able_to_remove_role() {
        final String famousRole = "famous";
        Document document = asciidoctor.load(ROLE, new HashMap<>());
        StructuralNode abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.hasRole(famousRole), is(true));
        abstractBlock.removeRole(famousRole);
        assertThat(abstractBlock.hasRole(famousRole), is(false));
    }

    @Test
    public void should_be_able_to_get_reftext() {
        Document document = asciidoctor.load(REFTEXT, new HashMap<>());
        StructuralNode abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.getReftext(), is("the first section"));
        assertThat(abstractBlock.isReftext(), is(true));
    }

    @Test
    public void should_be_able_to_get_icon_uri_string_reference() {
        Map<String, Object> options = OptionsBuilder.options()
                .attributes(AttributesBuilder.attributes().dataUri(false))
                .compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.iconUri("note"), is("./images/icons/note.png"));
    }

    @Test
    public void should_be_able_to_get_icon_uri() {
        Map<String, Object> options = OptionsBuilder.options().safe(SafeMode.SAFE)
                .attributes(AttributesBuilder.attributes().dataUri(true).icons("font"))
                .compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.iconUri("note"),
                either(
                        is("data:image/png:base64,") // <= Asciidoctor 1.5.5
                )
                        .or(is("data:image/png;base64,"))); // >= Asciidoctor 1.5.6
    }

    @Test
    public void should_be_able_to_get_media_uri() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<>());
        assertThat(document.mediaUri("target"), is("target"));
    }

    @Test
    public void should_be_able_to_get_image_uri() {
        Map<String, Object> options = OptionsBuilder.options().safe(SafeMode.SAFE)
                .attributes(AttributesBuilder.attributes().dataUri(false))
                .compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.imageUri("target.jpg"), is("target.jpg"));
        assertThat(document.imageUri("target.jpg", "imagesdir"), is("target.jpg"));
    }

    @Test
    public void should_be_able_to_normalize_web_path() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<>());
        assertThat(document.normalizeWebPath("target", null, true), is("target"));
    }

    @Test
    public void should_be_able_to_read_asset() throws FileNotFoundException {
        Map<String, Object> options = OptionsBuilder.options().safe(SafeMode.SAFE)
                .attributes(AttributesBuilder.attributes().dataUri(false))
                .compact(true)
                .asMap();
        Document document = asciidoctor.load(DOCUMENT, options);
        File inputFile = classpath.getResource("rendersample.asciidoc");
        String content = document.readAsset(inputFile.getAbsolutePath(), new HashMap<>());
        assertThat(content.replace("\r", ""), is(IOUtils.readFull(new FileReader(inputFile)).replace("\r", "")));
    }

    @Test
    public void should_be_able_to_set_attribute() {
        final Object attributeName = "testattribute";
        final Object attributeValue = "testvalue";
        Document document = asciidoctor.load(DOCUMENT, new HashMap<>());
        assertThat(document.setAttribute(attributeName, attributeValue, true), is(true));
        assertThat(document.getAttribute(attributeName), is(attributeValue));
        assertThat(document.getAttributes().get(attributeName), is(attributeValue));
    }

    @Test
    public void should_be_able_to_set_attribute_on_attributes_map() {
        final String attributeName = "testattribute";
        final Object attributeValue = "testvalue";
        Document document = asciidoctor.load(DOCUMENT, new HashMap<>());
        document.getAttributes().put(attributeName, attributeValue);
        assertThat(document.getAttribute(attributeName), is(attributeValue));
        assertThat(document.getAttributes().get(attributeName), is(attributeValue));
    }

    @Test
    public void should_be_able_to_get_source_location() {
        // Given
        File file = classpath.getResource("sourcelocation.adoc");

        // When
        Document document = asciidoctor.loadFile(file, OptionsBuilder.options().sourcemap(true).docType("book").asMap());
        Map<Object, Object> selector = new HashMap<>();
        selector.put("context", ":paragraph");
        List<StructuralNode> findBy = document.findBy(selector);

        // Then
        StructuralNode block1 = findBy.get(0);
        assertThat(block1.getSourceLocation().getLineNumber(), is(3));
        assertThat(block1.getSourceLocation().getPath(), is(file.getName()));
        assertThat(block1.getSourceLocation().getFile(), is(file.getName()));
        assertThat(block1.getSourceLocation().getDir(), is(file.getParent().replaceAll("\\\\", "/")));

        StructuralNode block2 = findBy.get(1);
        assertThat(block2.getSourceLocation().getLineNumber(), is(8));
        assertThat(block2.getSourceLocation().getPath(), is(file.getName()));
        assertThat(block2.getSourceLocation().getFile(), is(file.getName()));
        assertThat(block2.getSourceLocation().getDir(), is(file.getParent().replaceAll("\\\\", "/")));
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

        Document document = asciidoctor.load(documentWithAttributes, new HashMap<>());
        List<StructuralNode> blocks = document.getBlocks();

        Section section = (Section) blocks.get(1);
        section.setAttribute("testattr", "testvalue", true);

        assertThat(document.hasAttribute("testattr"), is(false));

        assertThat(section.hasAttribute("testattr"), is(true));
        assertThat(section.hasAttribute("testattr", true), is(true));
        assertThat(section.hasAttribute("testattr", false), is(true));
        assertThat(section.isAttribute("testattr", "testvalue"), is(true));

        assertThat(section.hasAttribute("docattr", true), is(true));
        assertThat(section.hasAttribute("docattr", false), is(false));
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

        Document document = asciidoctor.load(documentWithPreambleAndSection, new HashMap<>());
        List<StructuralNode> blocks = document.getBlocks();

        StructuralNode preambleContainer = blocks.get(0);
        assertThat(preambleContainer.getContentModel(), is("compound"));

        assertThat(preambleContainer.getBlocks().get(0).getContentModel(), is("simple"));
        assertThat(preambleContainer.getBlocks().get(1).getContentModel(), is("simple"));

        Section section = (Section) blocks.get(1);
        assertThat(section.getContentModel(), is("compound"));
        assertThat(section.getBlocks().get(0).getContentModel(), is("simple"));
    }

    @Test
    public void should_be_able_to_get_parent_from_document() {
        String s = "== A small Example\n" +
                "\n" +
                "Lorem ipsum dolor sit amet:\n";

        Document document = asciidoctor.load(s, new HashMap<>());
        assertThat(document.getParent(), nullValue());
    }

    @Test
    public void should_read_caption() {
        String s = "[caption=\"Table A. \"]\n" +
                ".A formal table\n" +
                "|===\n" +
                "|Cell in column 1, row 1\n" +
                "|Cell in column 2, row 1\n" +
                "|===";

        Document document = asciidoctor.load(s, new HashMap<>());
        assertThat(document.getBlocks(), notNullValue());
        assertThat(document.getBlocks().size(), is(1));
        assertThat(document.getBlocks().get(0).getCaption(), is("Table A. "));
    }

    @Test
    public void should_get_empty_collection_when_no_author_is_set() {
        String content = "= Sample Document\n" +
                "\n" +
                "Preamble...";

        Document document = asciidoctor.load(content, emptyMap());

        List<Author> authors = document.getAuthors();
        assertThat(authors, hasSize(0));
    }

    @Test
    public void should_get_single_author_when_only_one_is_set_in_author_line() {
        String content = "= Sample Document\n" +
                "Doc Writer <doc.writer@asciidoc.org>" +
                "\n" +
                "Preamble...";

        Document document = asciidoctor.load(content, emptyMap());

        List<Author> authors = document.getAuthors();
        assertThat(authors, hasSize(1));

        Author author = authors.get(0);
        assertThat(author.getEmail(), is("doc.writer@asciidoc.org"));
        assertThat(author.getFullName(), is("Doc Writer"));
        assertThat(author.getFirstName(), is("Doc"));
        assertThat(author.getMiddleName(), nullValue());
        assertThat(author.getLastName(), is("Writer"));
        assertThat(author.getInitials(), is("DW"));
    }

    @Test
    public void should_get_single_author_when_only_one_is_set_in_attribute() {
        String content = "= Sample Document\n" +
                ":author: Doc Writer\n" +
                ":email: doc.writer@asciidoc.org\n" +
                "\n" +
                "Preamble...";

        Document document = asciidoctor.load(content, emptyMap());

        List<Author> authors = document.getAuthors();
        assertThat(authors, hasSize(1));

        Author author = authors.get(0);
        assertThat(author.getEmail(), is("doc.writer@asciidoc.org"));
        assertThat(author.getFullName(), is("Doc Writer"));
        assertThat(author.getFirstName(), is("Doc"));
        assertThat(author.getMiddleName(), nullValue());
        assertThat(author.getLastName(), is("Writer"));
        assertThat(author.getInitials(), is("DW"));
    }

    @Test
    public void should_get_multiple_authors_when_they_arez_set_in_author_line() {
        String content = "= Sample Document\n" +
                "Doc Writer <doc.writer@asciidoc.org>; John A. Smith <john.smith@asciidoc.org>\n" +
                "\n" +
                "Preamble...";

        Document document = asciidoctor.load(content, emptyMap());

        List<Author> authors = document.getAuthors();
        assertThat(authors, hasSize(2));

        Author firstAuthor = authors.get(0);
        assertThat(firstAuthor.getEmail(), is("doc.writer@asciidoc.org"));
        assertThat(firstAuthor.getFullName(), is("Doc Writer"));
        assertThat(firstAuthor.getFirstName(), is("Doc"));
        assertThat(firstAuthor.getMiddleName(), nullValue());
        assertThat(firstAuthor.getLastName(), is("Writer"));
        assertThat(firstAuthor.getInitials(), is("DW"));

        Author secondAuthor = authors.get(1);
        assertThat(secondAuthor.getEmail(), is("john.smith@asciidoc.org"));
        assertThat(secondAuthor.getFullName(), is("John A. Smith"));
        assertThat(secondAuthor.getFirstName(), is("John"));
        assertThat(secondAuthor.getMiddleName(), is("A."));
        assertThat(secondAuthor.getLastName(), is("Smith"));
        assertThat(secondAuthor.getInitials(), is("JAS"));
    }

    @Test
    public void should_get_revision_info() {
        String content = "= Sample Document\n" +
                "Doc Writer <doc.writer@asciidoc.org>\n" +
                "v1.0, 2013-05-20: First draft\n" +
                "\n" +
                "Preamble...";

        Document document = asciidoctor.load(content, emptyMap());

        RevisionInfo revisionInfo = document.getRevisionInfo();

        assertThat(revisionInfo.getDate(), is("2013-05-20"));
        assertThat(revisionInfo.getNumber(), is("1.0"));
        assertThat(revisionInfo.getRemark(), is("First draft"));
    }
}
