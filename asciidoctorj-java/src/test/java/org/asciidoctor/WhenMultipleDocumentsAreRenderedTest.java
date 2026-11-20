package org.asciidoctor;

import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenMultipleDocumentsAreRenderedTest {

    private static final int RENDER_COUNT = 100;

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("documenttitle.adoc")
    private Path documentTitleDocument;

    @Test
    public void should_render_the_same_document_a_100_times() throws IOException {
        final String source = Files.readString(documentTitleDocument);
        final Options options = Options.builder()
                .safe(SafeMode.UNSAFE)
                .build();

        final String firstContent = asciidoctor.convert(source, options);

        assertThat(firstContent).isNotEmpty();
        for (int i = 1; i < RENDER_COUNT; i++) {
            String content = asciidoctor.convert(source, options);
            assertThat(content)
                    .as("render %d", i + 1)
                    .isEqualTo(firstContent);
        }
    }

}
