package org.asciidoctor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.Assert;
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
		assertThat((String) attributes.get("tags"), is("[document, example]"));
		assertThat((String) attributes.get("author"), is("Doc Writer"));
		assertThat((String) attributes.get("email"),
				is("doc.writer@asciidoc.org"));

		List<ContentPart> parts = document.getParts();

		Assert.assertNotNull(parts);
		Assert.assertEquals(3, parts.size());

		Assert.assertEquals("section", parts.get(0).getContext());
		Assert.assertEquals("Abstract", parts.get(0).getTitle());
		
		Assert.assertEquals(1, parts.get(0).getParts().size());
		Assert.assertEquals("abstract", parts.get(0).getParts().get(0).getStyle());
		Assert.assertEquals("open", parts.get(0).getParts().get(0).getContext());
		
		Assert.assertEquals("First Section", parts.get(1).getTitle());
		Assert.assertEquals("_first_section", parts.get(1).getId());
		Assert.assertEquals("section", parts.get(1).getContext());
		Assert.assertEquals(5, parts.get(1).getParts().size());

		
		Assert.assertEquals("blockid", parts.get(1).getParts().get(1).getId());
		Assert.assertEquals("blockStyle", parts.get(1).getParts().get(1).getStyle());
		Assert.assertEquals("Abraham Lincoln", parts.get(1).getParts().get(1).getAttributes()
				.get("attribution"));

		Assert.assertEquals("feature-list", parts.get(1).getParts().get(2).getRole());

		Assert.assertEquals("Second Section", parts.get(2).getTitle());
		Assert.assertEquals(2, parts.get(2).getParts().size());
		
		Assert.assertEquals("image", parts.get(2).getParts().get(0).getContext());
		Assert.assertEquals("image", parts.get(2).getParts().get(1).getContext());
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

		Assert.assertNotNull(parts);
		Assert.assertEquals(4, parts.size());

		
		
		Assert.assertEquals("TODO: This is description", document.getPartByStyle("Description").getContent());
		
		List<ContentPart> images = document.getPartsByContext("image");
		int imageCount = 2;
		Assert.assertEquals(imageCount, images.size());
		for (int i = 1; i<=imageCount; i++) {
			Assert.assertEquals("src/some image "+i+".JPG",(String) images.get(i-1).getAttributes().get("target"));
			Assert.assertEquals("TODO title"+i,(String) images.get(i-1).getAttributes().get("alt"));
			Assert.assertEquals("link"+i+".html",(String) images.get(i-1).getAttributes().get("link"));
		}
		Assert.assertTrue(document.getPartByStyle("Text").getContent().startsWith("<div class=\"paragraph text-center\">"));
	}

	@Test
	public void title_should_be_retrieved_from_simple_string() {

		StructuredDocument document = asciidoctor.readDocumentStructure(
				"= Sample Document", new HashMap<String, Object>());

		DocumentHeader header = document.getHeader();

		assertThat(header.getDocumentTitle(), is("Sample Document"));

		List<ContentPart> parts = document.getParts();

		Assert.assertNotNull(parts);
		Assert.assertEquals(0, parts.size());

	}

	@Test
	public void one_part_should_be_retrieved_from_simple_string() {

		StructuredDocument document = asciidoctor.readDocumentStructure(
				"Simple single paragraph", new HashMap<String, Object>());

		DocumentHeader header = document.getHeader();

		Assert.assertNotNull(header);

		List<ContentPart> parts = document.getParts();

		Assert.assertNotNull(parts);
		Assert.assertEquals(1, parts.size());
		Assert.assertEquals("Simple single paragraph", parts.get(0)
				.getContent());

	}
	
	@Test
	public void no_parts_should_be_retrieved_from_empty_string() {

		StructuredDocument document = asciidoctor.readDocumentStructure(
				"", new HashMap<String, Object>());

		DocumentHeader header = document.getHeader();

		Assert.assertNotNull(header);

		List<ContentPart> parts = document.getParts();

		Assert.assertNotNull(parts);
		Assert.assertEquals(0, parts.size());
	}
	
	@Test
	public void test_problematic_document() {
		StructuredDocument document = asciidoctor.readDocumentStructure(
				new File("target/test-classes/document-with-arrays.adoc"),
				new HashMap<String, Object>());
	}
	

}
