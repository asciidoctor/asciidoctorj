package org.asciidoctor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.xpath.HasXPath.hasXPath;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.common.io.CharStreams;

public class WhenAnAsciidoctorClassIsInstantiated {

	private Asciidoctor asciidoctor = JRubyAsciidoctor.create();
	
	@Test
	public void file_document_should_be_rendered_into_default_backend() throws IOException, SAXException, ParserConfigurationException {
		
		String render_file = asciidoctor.renderFile("target/test-classes/rendersample.asciidoc", new HashMap<Object, Object>());
		assertRenderedFile(render_file);
		
	}
	
	@Test
	public void string_content_document_should_be_rendered_into_default_backend() throws IOException, SAXException, ParserConfigurationException {
		
		InputStream content = new FileInputStream("target/test-classes/rendersample.asciidoc");
		String render_file = asciidoctor.render(toString(content), new HashMap<Object, Object>());
		
		assertRenderedFile(render_file);
	}
	
	@Test
	public void document_object_should_be_loaded_from_file_and_be_accessible_() throws IOException, SAXException, ParserConfigurationException {
		Document document = asciidoctor.loadFile("target/test-classes/rendersample.asciidoc", new HashMap<Object, Object>());
		String render_file = document.render(new HashMap<Object, Object>());
		
		assertRenderedFile(render_file);
	}

	@Test
	public void document_object_should_be_loaded_from_string_content_and_be_accessible_() throws IOException, SAXException, ParserConfigurationException {
	
		InputStream content = new FileInputStream("target/test-classes/rendersample.asciidoc");
		Document document = asciidoctor.load(toString(content), new HashMap<Object, Object>());
		
		String render_file = document.render(new HashMap<Object, Object>());
		assertRenderedFile(render_file);
		
	}
	
	private void assertRenderedFile(String render_file) throws IOException, SAXException, ParserConfigurationException {
		Source renderFileSource = new DOMSource(inputStream2Document(new ByteArrayInputStream(render_file.getBytes())));
		
		assertThat(renderFileSource, hasXPath("/div[@class='sect1']"));
		assertThat(renderFileSource, hasXPath("/div/h2[@id='_section_a']"));
		assertThat(renderFileSource, hasXPath("/div/h2", is("Section A")));
		assertThat(renderFileSource, hasXPath("/div/div[@class='sectionbody']"));
	}
	
	private static String toString(InputStream inputStream) throws IOException {
		return CharStreams.toString( new InputStreamReader( inputStream ));
	}
	
	private static org.w3c.dom.Document inputStream2Document(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
	    DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
	    newInstance.setNamespaceAware(true);
	    org.w3c.dom.Document parse = newInstance.newDocumentBuilder().parse(inputStream);
	    return parse;
	}
	
}
