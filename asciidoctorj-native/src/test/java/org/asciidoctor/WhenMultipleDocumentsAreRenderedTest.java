package org.asciidoctor;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenMultipleDocumentsAreRenderedTest {

    @TempDir
    public File tempDir;

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("documenttitle.adoc")
    private Path documentTitleDocument;

    @Test
    public void should_render_the_same_document_a_100_times() throws IOException {
//        final MemoryLogHandler memoryLogHandler = registerMemoryLogHandler();

        int i = 0;
        String renderContent = null;
        for (; i < 100; i++) {
            renderContent = asciidoctor.convert(Files.readString(documentTitleDocument),
                    Options.builder()
                            .safe(SafeMode.UNSAFE)
                            .build());
        }

        assertThat(i).isEqualTo(100);
        assertThat(renderContent).isNotEmpty();
    }

}
