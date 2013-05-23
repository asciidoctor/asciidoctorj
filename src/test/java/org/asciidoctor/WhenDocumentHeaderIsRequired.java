package org.asciidoctor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Map;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WhenDocumentHeaderIsRequired {

	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
	
	private Asciidoctor asciidoctor = JRubyAsciidoctor.create();
	
	@Test
	public void doctitle_blocks_and_attributes_should_be_returned() {
		
		DocumentHeader header = asciidoctor.readHeader(new File("target/test-classes/documentheaders.asciidoc"));
		
		
		assertThat(header.getDocumentTitle(), is("Sample Document"));
		assertThat(header.getNumberOfBlocks(), is(0));
		
		Map<String, Object> attributes = header.getAttributes();
		assertThat((String)attributes.get("revdate"), is("2013-05-20"));
		assertThat((String)attributes.get("revnumber"), is("1.0"));
		assertThat((String)attributes.get("revremark"), is("First draft"));
		assertThat((String)attributes.get("tags"), is("[document, example]"));
		assertThat((String)attributes.get("author"), is("Doc Writer"));
		assertThat((String)attributes.get("email"), is("doc.writer@example.com"));
		
	}
	
}
