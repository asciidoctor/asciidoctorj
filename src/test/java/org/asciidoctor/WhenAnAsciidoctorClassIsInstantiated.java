package org.asciidoctor;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.util.HashMap;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.Test;

public class WhenAnAsciidoctorClassIsInstantiated {

	private Asciidoctor asciidoctor = JRubyAsciidoctor.create();
	
	private static final String EXPECTED_GENERATED_FILE = "<div id=\"preamble\">\n" + 
			"  <div class=\"sectionbody\">\n" + 
			"<div class=\"paragraph\">\n" + 
			"    \n" + 
			"  <p>Preamble paragraph.</p>\n" + 
			"</div>\n" + 
			"<div class=\"admonitionblock\">\n" + 
			"  <table>\n" + 
			"    <tr>\n" + 
			"      <td class=\"icon\">\n" + 
			"        \n" + 
			"        <div class=\"title\">Note</div>\n" + 
			"        \n" + 
			"      </td>\n" + 
			"      <td class=\"content\">\n" + 
			"        \n" + 
			"        This is test, only a test.\n" + 
			"\n" + 
			"      </td>\n" + 
			"    </tr>\n" + 
			"  </table>\n" + 
			"</div>\n" + 
			"\n" + 
			"  </div>\n" + 
			"</div>\n" + 
			"\n" + 
			"<div class=\"sect1\">\n" + 
			"  <h2 id=\"id_section_a\">Section A</h2>\n" + 
			"  \n" + 
			"  <div class=\"sectionbody\">\n" + 
			"<div class=\"paragraph\">\n" + 
			"    \n" + 
			"  <p><strong>Section A</strong> paragraph.</p>\n" + 
			"</div>\n" + 
			"\n" + 
			"<div class=\"sect2\">\n" + 
			"  <h3 id=\"id_section_a_subsection\">Section A Subsection</h3>\n" + 
			"  \n" + 
			"<div class=\"paragraph\">\n" + 
			"    \n" + 
			"  <p><strong>Section A</strong> <em>subsection</em> paragraph.</p>\n" + 
			"</div>\n" + 
			"\n" + 
			"  \n" + 
			"</div>\n" + 
			"\n" + 
			"\n" + 
			"  </div>\n" + 
			"  \n" + 
			"</div>\n" + 
			"\n" + 
			"\n" + 
			"<div class=\"sect1\">\n" + 
			"  <h2 id=\"id_section_b\">Section B</h2>\n" + 
			"  \n" + 
			"  <div class=\"sectionbody\">\n" + 
			"<div class=\"paragraph\">\n" + 
			"    \n" + 
			"  <p><strong>Section B</strong> paragraph.</p>\n" + 
			"</div>\n" + 
			"<div class=\"ulist\">\n" + 
			"  <div class=\"title\">Section B list</div>\n" + 
			"  <ul>\n" + 
			"  \n" + 
			"    <li>\n" + 
			"      <p>Item 1</p>\n" + 
			"      \n" + 
			"    </li>\n" + 
			"  \n" + 
			"    <li>\n" + 
			"      <p>Item 2</p>\n" + 
			"      \n" + 
			"    </li>\n" + 
			"  \n" + 
			"    <li>\n" + 
			"      <p>Item 3</p>\n" + 
			"      \n" + 
			"    </li>\n" + 
			"  \n" + 
			"  </ul>\n" + 
			"</div>\n" + 
			"\n" + 
			"  </div>\n" + 
			"  \n" + 
			"</div>";
	
	@Test
	public void file_document_should_be_rendered_into_default_backend() {
		
		String render_file = asciidoctor.render_file("build/resources/test/src/asciidoc/sample.asciidoc", new HashMap<Object, Object>());
		assertThat(render_file.trim(), is(EXPECTED_GENERATED_FILE));
	}
	
	@Test
	public void string_content_document_should_be_rendered_into_default_backend() {
		
		String content = "Document Title\r\n" + 
				"==============\r\n" + 
				"Doc Writer <thedoc@asciidoctor.org>\r\n" + 
				":idprefix: id_\r\n" + 
				"\r\n" + 
				"Preamble paragraph.\r\n" + 
				"\r\n" + 
				"NOTE: This is test, only a test.\r\n" + 
				"\r\n" + 
				"== Section A\r\n" + 
				"\r\n" + 
				"*Section A* paragraph.\r\n" + 
				"\r\n" + 
				"=== Section A Subsection\r\n" + 
				"\r\n" + 
				"*Section A* 'subsection' paragraph.\r\n" + 
				"\r\n" + 
				"== Section B\r\n" + 
				"\r\n" + 
				"*Section B* paragraph.\r\n" + 
				"\r\n" + 
				".Section B list\r\n" + 
				"* Item 1\r\n" + 
				"* Item 2\r\n" + 
				"* Item 3";
		
		
		
		String render_file = asciidoctor.render(content, new HashMap<Object, Object>());
		assertThat(render_file.trim(), is(EXPECTED_GENERATED_FILE));
	}
	
	@Test
	public void document_object_should_be_loaded_from_file_and_be_accessible_() {
		Document document = asciidoctor.load_file("build/resources/test/src/asciidoc/sample.asciidoc", new HashMap<Object, Object>());
		assertThat(document.author(), is("Doc Writer"));
	}

	@Test
	public void document_object_should_be_loaded_from_string_content_and_be_accessible_() {
		
		String content = "Document Title\r\n" + 
				"==============\r\n" + 
				"Doc Writer <thedoc@asciidoctor.org>\r\n" + 
				":idprefix: id_\r\n" + 
				"\r\n" + 
				"Preamble paragraph.\r\n" + 
				"\r\n" + 
				"NOTE: This is test, only a test.\r\n" + 
				"\r\n" + 
				"== Section A\r\n" + 
				"\r\n" + 
				"*Section A* paragraph.\r\n" + 
				"\r\n" + 
				"=== Section A Subsection\r\n" + 
				"\r\n" + 
				"*Section A* 'subsection' paragraph.\r\n" + 
				"\r\n" + 
				"== Section B\r\n" + 
				"\r\n" + 
				"*Section B* paragraph.\r\n" + 
				"\r\n" + 
				".Section B list\r\n" + 
				"* Item 1\r\n" + 
				"* Item 2\r\n" + 
				"* Item 3";
		
		Document document = asciidoctor.load(content, new HashMap<Object, Object>());
		assertThat(document.author(), is("Doc Writer"));
	}
	
}
