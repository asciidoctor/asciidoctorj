package org.asciidoctor;

import org.asciidoctor.ast.Author;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.RevisionInfo;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenDocumentHeaderIsRequired {

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    @ClasspathResource("documentheaders.asciidoc")
    private File documentHeaders;

    @TempDir
    public File tempFolder;


    @Test
    public void doctitle_blocks_and_attributes_should_be_returned() {
        final Options options = parseHeaderOnlyOptions();
        final Document document = asciidoctor.loadFile(documentHeaders, options);

        assertThat(document.getDoctitle(), is("Sample Document"));
        assertThat(document.getStructuredDoctitle().getMain(), is("Sample Document"));

        Map<String, Object> attributes = document.getAttributes();
        assertThat((String) attributes.get("revdate"), is("2013-05-20"));
        assertThat((String) attributes.get("revnumber"), is("1.0"));
        assertThat((String) attributes.get("revremark"), is("First draft"));
        //attributes should be incasesensitive
        assertThat((String) attributes.get("tags"), is("[document, example]"));
        assertThat((String) attributes.get("author"), is("Doc Writer"));
        assertThat((String) attributes.get("email"), is("doc.writer@asciidoc.org"));
    }

    @Test
    public void author_info_should_be_bound_into_author_class() {
        final Options options = parseHeaderOnlyOptions();
        final Document document = asciidoctor.loadFile(documentHeaders, options);

        List<Author> authors = document.getAuthors();
        assertThat(authors, hasSize(2));

        Author author = authors.get(0);
        assertThat(author.getEmail(), is("doc.writer@asciidoc.org"));
        assertThat(author.getFullName(), is("Doc Writer"));
        assertThat(author.getFirstName(), is("Doc"));
        assertThat(author.getLastName(), is("Writer"));
        assertThat(author.getInitials(), is("DW"));
    }

    @Test
    public void revision_info_should_be_bound_into_revision_info_class() {
        final Options options = parseHeaderOnlyOptions();
        final Document document = asciidoctor.loadFile(documentHeaders, options);

        RevisionInfo revisionInfo = document.getRevisionInfo();

        assertThat(revisionInfo.getDate(), is("2013-05-20"));
        assertThat(revisionInfo.getNumber(), is("1.0"));
        assertThat(revisionInfo.getRemark(), is("First draft"));
    }

    @Test
    public void multiple_authors_should_be_bound_into_list_of_authors() {

        final Options options = parseHeaderOnlyOptions();
        final Document document = asciidoctor.loadFile(documentHeaders, options);

        List<Author> authors = document.getAuthors();
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

    private static Options parseHeaderOnlyOptions() {
        return Options.builder()
                .option("parse_header_only", true)
                .build();
    }
}
