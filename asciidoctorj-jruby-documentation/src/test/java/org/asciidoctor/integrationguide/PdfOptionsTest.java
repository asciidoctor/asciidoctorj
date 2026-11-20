package org.asciidoctor.integrationguide;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

/**
 * Documents the options that rely on Ruby gems, which are only available with the JRuby implementation.
 */
@ExtendWith(AsciidoctorExtension.class)
public class PdfOptionsTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @TempDir(cleanup = CleanupMode.NEVER)
    public File tempDir;

    @Test
    public void options_for_pdf_document() {
//tag::optionsPDFBackend[]
        File targetFile = // ...
//end::optionsPDFBackend[]
                new File(tempDir, "test.pdf");

//tag::optionsPDFBackend[]
        asciidoctor.convert(
                "Hello World",
                Options.builder()
                        .backend("pdf")
                        .toFile(targetFile)
                        .safe(SafeMode.UNSAFE)
                        .build());

        assertThat(targetFile.length()).isGreaterThan(0L);
//end::optionsPDFBackend[]
    }
}
