package org.asciidoctor;

import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.xpath.HasXPath.hasXPath;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import com.google.common.io.CharStreams;

public class WhenAnAsciidoctorClassIsInstantiated {

	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
	
	private Asciidoctor asciidoctor = JRubyAsciidoctor.create();
	
	@Test
	public void file_document_should_be_rendered_into_default_backend() throws IOException, SAXException, ParserConfigurationException {
		
		String render_file = asciidoctor.renderFile("target/test-classes/rendersample.asciidoc", new HashMap<String, Object>());
		assertRenderedFile(render_file);
		
	}
	
	@Test
	public void file_document_should_be_rendered_into_current_directory() throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
		
		String renderContent = asciidoctor.renderFile("target/test-classes/rendersample.asciidoc", options().inPlace(true).asMap());

		File expectedFile = new File("target/test-classes/rendersample.html");
		
		//Bug in asciidoctor that do not close meta tag?¿
		//String renderedFileContent = toString(new FileInputStream(expectedFile));
		//assertRenderedFile(renderedFileContent);
		assertThat(expectedFile.exists(), is(true));
		assertThat(renderContent, is(nullValue()));
	}
	
	@Test
	public void file_document_should_be_rendered_into_foreign_directory() throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
		
		Map<String, Object> options = options()
										.inPlace(false)
										.safe(SafeMode.UNSAFE)
										.toDir(testFolder.getRoot())
									.asMap();
		String renderContent = asciidoctor.renderFile("target/test-classes/rendersample.asciidoc", options);

		File expectedFile = new File(testFolder.getRoot(),"rendersample.html");
		
		//Bug in asciidoctor that do not close meta tag?¿
		//String renderedFileContent = toString(new FileInputStream(expectedFile));
		//assertRenderedFile(renderedFileContent);
		assertThat(expectedFile.exists(), is(true));
		assertThat(renderContent, is(nullValue()));
	}
	
	@Test
	public void docbook_document_should_be_rendered_into_current_directory() throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
		
		
		
		Map<String, Object> attributes = attributes().backend("docbook").asMap();
		Map<String, Object> options = options()
										.inPlace(true)
										.attributes(attributes)
									  .asMap();
		
		
		String renderContent = asciidoctor.renderFile("target/test-classes/rendersample.asciidoc", options);

		File expectedFile = new File("target/test-classes/rendersample.xml");
		
		//Bug in asciidoctor that do not close meta tag?¿
		//String renderedFileContent = toString(new FileInputStream(expectedFile));
		//assertRenderedFile(renderedFileContent);
		assertThat(expectedFile.exists(), is(true));
		assertThat(renderContent, is(nullValue()));
	}
	
	@Test
	public void string_content_document_should_be_rendered_into_default_backend() throws IOException, SAXException, ParserConfigurationException {
		
		InputStream content = new FileInputStream("target/test-classes/rendersample.asciidoc");
		String render_file = asciidoctor.render(toString(content), new HashMap<String, Object>());
		
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
