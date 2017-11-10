package org.asciidoctor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.ast.Author;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.ast.RevisionInfo;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WhenDocumentHeaderIsRequired {

    @ArquillianResource
    private ClasspathResources classpath;
    
	@ArquillianResource
    public TemporaryFolder testFolder;

	@ArquillianResource(Unshared.class)
	private Asciidoctor asciidoctor;
	
	@Test
	public void doctitle_blocks_and_attributes_should_be_returned() {
		
		DocumentHeader header = asciidoctor.readDocumentHeader(classpath.getResource("documentheaders.asciidoc"));
		
		
		assertThat(header.getDocumentTitle().getMain(), is("Sample Document"));
		assertThat(header.getPageTitle(), is("Sample Document"));
		
		Map<String, Object> attributes = header.getAttributes();
		assertThat((String)attributes.get("revdate"), is("2013-05-20"));
		assertThat((String)attributes.get("revnumber"), is("1.0"));
		assertThat((String)attributes.get("revremark"), is("First draft"));
		//attributes should be incasesensitive
		assertThat((String)attributes.get("tags"), is("[document, example]"));
		assertThat((String)attributes.get("Tags"), is("[document, example]"));
		assertThat((String)attributes.get("author"), is("Doc Writer"));
		assertThat((String)attributes.get("email"), is("doc.writer@asciidoc.org"));
		
	}
	
	@Test
	public void author_info_should_be_bound_into_author_class() {
		
		DocumentHeader header = asciidoctor.readDocumentHeader(classpath.getResource("documentheaders.asciidoc"));
		
		Author author = header.getAuthor();
		assertThat(author.getEmail(), is("doc.writer@asciidoc.org"));
		assertThat(author.getFullName(), is("Doc Writer"));
		assertThat(author.getFirstName(), is("Doc"));
		assertThat(author.getLastName(), is("Writer"));
		assertThat(author.getInitials(), is("DW"));
		
	}
	
	@Test
	public void revision_info_should_be_bound_into_revision_info_class() {
		
		DocumentHeader header = asciidoctor.readDocumentHeader(classpath.getResource("documentheaders.asciidoc"));
		
		RevisionInfo revisionInfo = header.getRevisionInfo();
		
		assertThat(revisionInfo.getDate(), is("2013-05-20"));
		assertThat(revisionInfo.getNumber(), is("1.0"));
		assertThat(revisionInfo.getRemark(), is("First draft"));
		
	}
	
	
	@Test
	public void multiple_authors_should_be_bound_into_list_of_authors() {
		
		DocumentHeader header = asciidoctor.readDocumentHeader(classpath.getResource("documentheaders.asciidoc"));
		
		List<? extends Author> authors = header.getAuthors();
		assertThat(authors, hasSize(2));
		
		Author author1 = authors.get(0);
		
		assertThat(author1.getEmail(), is("doc.writer@asciidoc.org"));
		assertThat(author1.getFullName(), is("Doc Writer"));
		assertThat(author1.getFirstName(), is("Doc"));
		assertThat(author1.getLastName(), is("Writer"));
		assertThat(author1.getInitials(), is("DW"));
		
		Author author2 = authors.get(1);
		
		assertThat(author2.getEmail(), is("john.smith@asciidoc.org"));
		assertThat(author2.getFullName(), is("John Smith"));
		assertThat(author2.getFirstName(), is("John"));
		assertThat(author2.getLastName(), is("Smith"));
		assertThat(author2.getInitials(), is("JS"));
		
	}
	
}
