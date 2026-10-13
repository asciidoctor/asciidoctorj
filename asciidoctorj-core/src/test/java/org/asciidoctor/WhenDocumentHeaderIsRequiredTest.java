package org.asciidoctor;

import static org.assertj.core.api.Assertions.assertThat;

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

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenDocumentHeaderIsRequiredTest {

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

        assertThat(document.getDoctitle()).isEqualTo("Sample Document");
        assertThat(document.getStructuredDoctitle().getMain()).isEqualTo("Sample Document");

        Map<String, Object> attributes = document.getAttributes();
        assertThat((String) attributes.get("revdate")).isEqualTo("2013-05-20");
        assertThat((String) attributes.get("revnumber")).isEqualTo("1.0");
        assertThat((String) attributes.get("revremark")).isEqualTo("First draft");
        //attributes should be incasesensitive
        assertThat((String) attributes.get("tags")).isEqualTo("[document, example]");
        assertThat((String) attributes.get("author")).isEqualTo("Doc Writer");
        assertThat((String) attributes.get("email")).isEqualTo("doc.writer@asciidoc.org");
    }

    @Test
    public void author_info_should_be_bound_into_author_class() {
        final Options options = parseHeaderOnlyOptions();
        final Document document = asciidoctor.loadFile(documentHeaders, options);

        List<Author> authors = document.getAuthors();
        assertThat(authors).hasSize(2);

        Author author = authors.get(0);
        assertThat(author.getEmail()).isEqualTo("doc.writer@asciidoc.org");
        assertThat(author.getFullName()).isEqualTo("Doc Writer");
        assertThat(author.getFirstName()).isEqualTo("Doc");
        assertThat(author.getLastName()).isEqualTo("Writer");
        assertThat(author.getInitials()).isEqualTo("DW");
    }

    @Test
    public void revision_info_should_be_bound_into_revision_info_class() {
        final Options options = parseHeaderOnlyOptions();
        final Document document = asciidoctor.loadFile(documentHeaders, options);

        RevisionInfo revisionInfo = document.getRevisionInfo();

        assertThat(revisionInfo.getDate()).isEqualTo("2013-05-20");
        assertThat(revisionInfo.getNumber()).isEqualTo("1.0");
        assertThat(revisionInfo.getRemark()).isEqualTo("First draft");
    }

    @Test
    public void multiple_authors_should_be_bound_into_list_of_authors() {

        final Options options = parseHeaderOnlyOptions();
        final Document document = asciidoctor.loadFile(documentHeaders, options);

        List<Author> authors = document.getAuthors();
        assertThat(authors).hasSize(2);

        Author author1 = authors.get(0);

        assertThat(author1.getEmail()).isEqualTo("doc.writer@asciidoc.org");
        assertThat(author1.getFullName()).isEqualTo("Doc Writer");
        assertThat(author1.getFirstName()).isEqualTo("Doc");
        assertThat(author1.getLastName()).isEqualTo("Writer");
        assertThat(author1.getInitials()).isEqualTo("DW");

        Author author2 = authors.get(1);

        assertThat(author2.getEmail()).isEqualTo("john.smith@asciidoc.org");
        assertThat(author2.getFullName()).isEqualTo("John Smith");
        assertThat(author2.getFirstName()).isEqualTo("John");
        assertThat(author2.getLastName()).isEqualTo("Smith");
        assertThat(author2.getInitials()).isEqualTo("JS");
    }

    private static Options parseHeaderOnlyOptions() {
        return Options.builder()
                .option("parse_header_only", true)
                .build();
    }
}
