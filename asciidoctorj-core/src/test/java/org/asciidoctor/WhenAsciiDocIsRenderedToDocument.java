package org.asciidoctor;

import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.Table;
import org.asciidoctor.internal.IOUtils;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.asciidoctor.util.ClasspathResources;
import org.junit.Rule;
import org.junit.Test;

public class WhenAsciiDocIsRenderedToDocument {

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

    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

    @Rule
    public ClasspathResources classpath = new ClasspathResources();

    @Test
    public void should_return_section_blocks() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        Section section = (Section) document.getBlocks().get(1);
        assertThat(section.index(), is(0));
        assertThat(section.sectname(), either(is("sect1")).or(is("section")));
        assertThat(section.special(), is(false));
    }
    
    @Test
    public void should_return_blocks_from_a_document() {
        
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.doctitle(), is("Document Title"));
        
    }
    
    @Test
    public void should_return_a_document_object_from_string() {
        
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.doctitle(), is("Document Title"));
    }
    
    @Test
    public void should_find_elements_from_document() {
        
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        Map<Object, Object> selector = new HashMap<Object, Object>();
        selector.put("context", ":image");
        List<AbstractBlock> findBy = document.findBy(selector);
        assertThat(findBy, hasSize(2));
        
        assertThat((String)findBy.get(0).getAttributes().get("target"), is("tiger.png"));
        
    }

    @Test
    public void should_return_options_from_document() {
        Map<String, Object> options = OptionsBuilder.options().compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);

        Map<Object, Object> documentOptions = document.getOptions();

        assertThat((Boolean) documentOptions.get("compact"), is(true));
    }

    @Test
    public void should_return_node_name() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.getNodeName(), is("document"));
    }

    @Test
    public void should_return_if_it_is_inline() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.isInline(), is(false));
    }

    @Test
    public void should_return_if_it_is_block() {
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.isBlock(), is(true));
    }

    @Test
    public void should_be_able_to_manipulate_attributes() {
        Map<String, Object> options = OptionsBuilder.options()
                                                    .attributes(AttributesBuilder.attributes().dataUri(true))
                                                    .compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);
        assertThat(document.getAttributes(), hasKey("toc-placement"));
        assertThat(document.isAttr("toc-placement", "auto", false), is(true));
        assertThat(document.getAttr("toc-placement", "", false).toString(), is("auto"));
    }

    @Test
    public void should_be_able_to_get_roles() {
        Document document = asciidoctor.load(ROLE, new HashMap<String, Object>());
        AbstractBlock abstractBlock = document.getBlocks().get(0);
        assertThat(abstractBlock.getRole(), is("famous"));
        assertThat(abstractBlock.hasRole("famous"), is(true));
        //assertThat(abstractBlock.isRole(), is(true));
        assertThat(abstractBlock.getRoles(), contains("famous"));
    }

    @Test
    public void should_be_able_to_get_reftext() {
        Document document = asciidoctor.load(REFTEXT, new HashMap<String, Object>());
        AbstractBlock abstractBlock = document.getBlocks().get(0);
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
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
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
        Document document = asciidoctor.load(DOCUMENT, new HashMap<String, Object>());
        assertThat(document.normalizeWebPath("target", null, true), is("target"));
    }

    @Test
    public void should_be_able_to_read_asset() throws FileNotFoundException {
        Map<String, Object> options = OptionsBuilder.options().safe(SafeMode.SAFE)
                .attributes(AttributesBuilder.attributes().dataUri(false))
                .compact(true).asMap();
        Document document = asciidoctor.load(DOCUMENT, options);
        File inputFile = classpath.getResource("rendersample.asciidoc");
        String content = document.readAsset(inputFile.getAbsolutePath(), new HashMap<Object, Object>());
        assertThat(content, is(IOUtils.readFull(new FileReader(inputFile))));
    }

    @Test
    public void should_load_a_document_from_File() throws FileNotFoundException {
        Map<String, Object> options = OptionsBuilder.options().safe(SafeMode.SAFE)
            .attributes(AttributesBuilder.attributes().dataUri(false))
            .compact(true).asMap();
        File inputFile = classpath.getResource("rendersample.asciidoc");
        Document document = asciidoctor.loadFile(inputFile, options);
        assertEquals(1, document.getBlocks().size());
        assertThat(document.getBlocks().get(0), instanceOf(Section.class));
        Section section = (Section) document.getBlocks().get(0);
        assertEquals(1, section.getBlocks().size());
        assertEquals("<strong>Section A</strong> paragraph.", section.getBlocks().get(0).getContent());
    }

    @Test
    public void should_load_document_with_table() throws Exception {

        final String doc = "= Test\n" +
            "\n" +
            "== Section \n" +
            "\n" +
            "[options=\"header,footer\"]\n" +
            "[cols=\"<,>\"]\n" +
            "|===\n" +
            "| A | B\n" +
            "\n" +
            "| C\n" +
            "| D\n" +
            "\n" +
            "| E\n" +
            "| F\n" +
            "\n" +
            "2+| G\n" +
            "\n" +
            "| H | I\n" +
            "|===\n";

        Document document = asciidoctor.load(doc, OptionsBuilder.options().asMap());

        Table table = (Table) document.getBlocks().get(0).getBlocks().get(0);

        List<Row> header = table.getHeader();
        assertThat(header, hasSize(1));

        Row headerRow = header.get(0);
        assertThat(headerRow.getCells(), hasSize(2));
        assertThat(headerRow.getCells().get(0).getText(), is("A"));
        assertThat(headerRow.getCells().get(1).getText(), is("B"));

        List<Row> footer = table.getFooter();
        assertThat(footer, hasSize(1));

        Row footerRow = footer.get(0);
        assertThat(footerRow.getCells(), hasSize(2));
        assertThat(footerRow.getCells().get(0).getText(), is("H"));
        assertThat(footerRow.getCells().get(1).getText(), is("I"));

        List<Row> body = table.getBody();
        assertThat(body, hasSize(3));

        Row firstBodyRow = body.get(0);
        assertThat(firstBodyRow.getCells(), hasSize(2));
        assertThat(firstBodyRow.getCells().get(0).getText(), is("C"));

        assertThat(firstBodyRow.getCells().get(1).getText(), is("D"));

        Row secondBodyRow = body.get(1);
        assertThat(secondBodyRow.getCells(), hasSize(2));
        assertThat(secondBodyRow.getCells().get(0).getText(), is("E"));
        assertThat(secondBodyRow.getCells().get(1).getText(), is("F"));
        assertThat(secondBodyRow.getCells().get(0).getColspan(), is(0));

        Row thirdBodyRow = body.get(2);
        assertThat(thirdBodyRow.getCells(), hasSize(1));
        assertThat(thirdBodyRow.getCells().get(0).getText(), is("G"));
        assertThat(thirdBodyRow.getCells().get(0).getColspan(), is(2));
        assertThat(thirdBodyRow.getCells().get(0).getRowspan(), is(0));

        List<Column> columns = table.getColumns();
        assertThat(columns, hasSize(2));

        assertThat(columns.get(0).getHorizontalAlignment(), is(Table.HorizontalAlignment.LEFT));
        assertThat(columns.get(1).getHorizontalAlignment(), is(Table.HorizontalAlignment.RIGHT));
    }

}
