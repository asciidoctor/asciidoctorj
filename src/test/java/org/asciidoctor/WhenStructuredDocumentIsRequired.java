package org.asciidoctor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WhenStructuredDocumentIsRequired {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

	@Test
	public void structural_content_should_be_retrieved_from_file() {

		Map<String,Object> parameters = new HashMap<String, Object>();
		parameters.put(Asciidoctor.STRUCTURE_MAX_LEVEL, 2);
		StructuredDocument document = asciidoctor.readDocumentStructure(
				new File("target/test-classes/documentblocks.asciidoc"),parameters
				);

		DocumentHeader header = document.getHeader();

		assertThat(header.getDocumentTitle(), is("Sample Document"));
		assertThat(header.getPageTitle(), is("Sample Document"));

		Map<String, Object> attributes = header.getAttributes();
		assertThat((String) attributes.get("revdate"), is("2013-05-20"));
		assertThat((String) attributes.get("revnumber"), is("1.0"));
		assertThat((String) attributes.get("revremark"), is("First draft"));
		//attributes should be incasesensitive
		assertThat((String) attributes.get("tags"), is("[document, example]"));
		assertThat((String) attributes.get("Tags"), is("[document, example]"));
		assertThat((String) attributes.get("author"), is("Doc Writer"));
		assertThat((String) attributes.get("email"),
				is("doc.writer@asciidoc.org"));

		List<ContentPart> parts = document.getParts();

		assertThat(parts, notNullValue());
		assertThat(parts, hasSize(3));

		assertThat(parts.get(0).getContext(), is("section"));
		assertThat(parts.get(0).getTitle(), is("Abstract"));
		
		assertThat(parts.get(0).getParts(), hasSize(1));
		assertThat(parts.get(0).getParts().get(0).getStyle(), is("abstract"));
		assertThat(parts.get(0).getParts().get(0).getContext(), is("open"));
		
		assertThat(parts.get(1).getTitle(), is("First Section"));
		assertThat(parts.get(1).getId(), is("_first_section"));
		assertThat(parts.get(1).getContext(), is("section"));
		assertThat(parts.get(1).getParts(), hasSize(5));

		
		assertThat(parts.get(1).getParts().get(1).getId(), is("blockid"));
		assertThat(parts.get(1).getParts().get(1).getStyle(), is("quote"));
		assertThat((String)parts.get(1).getParts().get(1).getAttributes().get("attribution"), is("Abraham Lincoln"));


		assertThat(parts.get(1).getParts().get(2).getRole(), is("feature-list"));

		assertThat(parts.get(2).getTitle(), is("Second Section"));
		assertThat(parts.get(2).getParts(), hasSize(2));
		
		assertThat(parts.get(2).getParts().get(0).getContext(), is("image"));
		assertThat(parts.get(2).getParts().get(1).getContext(), is("image"));
	}
	
	@Test
	public void some_real_content(){
		StructuredDocument document = asciidoctor.readDocumentStructure(
				new File("target/test-classes/contentstructure.asciidoc"),
				new HashMap<String, Object>());

		DocumentHeader header = document.getHeader();

		assertThat(header.getDocumentTitle(), is("TODO: Document Title"));

		Map<String, Object> attributes = header.getAttributes();
		assertThat((String) attributes.get("type"), is("object.type"));

		List<ContentPart> parts = document.getParts();

		assertThat(parts, notNullValue());
		assertThat(parts, hasSize(4));

		
		
		assertThat(document.getPartByStyle("literal").getContent(), is("TODO: This is description"));
		
		List<ContentPart> images = document.getPartsByContext("image");
		assertThat(images, hasSize(2));
		for (int i = 0; i<2; i++) {
			assertThat((String) images.get(i).getAttributes().get("target"), is("src/some image "+(i+1)+".JPG"));
			assertThat((String) images.get(i).getAttributes().get("alt"), is("TODO title"+(i+1)));
			assertThat((String) images.get(i).getAttributes().get("link"), is("link"+(i+1)+".html"));
		}
		assertThat(document.getPartByStyle("Open").getContent(), startsWith("<div class=\"paragraph text-center\">"));
	}

	@Test
	public void title_should_be_retrieved_from_simple_string() {

		StructuredDocument document = asciidoctor.readDocumentStructure(
				"= Sample Document", new HashMap<String, Object>());

		DocumentHeader header = document.getHeader();

		assertThat(header.getDocumentTitle(), is("Sample Document"));

		List<ContentPart> parts = document.getParts();

		assertThat(parts, notNullValue());
		assertThat(parts, hasSize(0));

	}

	@Test
	public void one_part_should_be_retrieved_from_simple_string() {

		StructuredDocument document = asciidoctor.readDocumentStructure(
				"Simple single paragraph", new HashMap<String, Object>());

		DocumentHeader header = document.getHeader();

		assertThat(header, notNullValue());

		List<ContentPart> parts = document.getParts();

		assertThat(parts, notNullValue());
		assertThat(parts, hasSize(1));
		assertThat(parts.get(0)
				.getContent(), is("Simple single paragraph"));

	}
	
	@Test
	public void no_parts_should_be_retrieved_from_empty_string() {

		StructuredDocument document = asciidoctor.readDocumentStructure(
				"", new HashMap<String, Object>());

		DocumentHeader header = document.getHeader();

		assertThat(header, notNullValue());

		List<ContentPart> parts = document.getParts();

		assertThat(parts, notNullValue());
		assertThat(parts, hasSize(0));
	}
	
}
