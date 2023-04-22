package org.asciidoctor.integrationguide;

import org.apache.commons.io.IOUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OptionsTest {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @TempDir(cleanup = CleanupMode.NEVER)
    public File tempDir;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

    @Test
    public void simple_options_example_embeddable_document() {
//tag::simpleOptionsExampleEmbeddableDocument[]
        String result =
                asciidoctor.convert(
                        "Hello World",
                        Options.builder()            // <1>
                                .standalone(false)   // <2>
                                .build());           // <3>

        assertThat(result, startsWith("<div "));
//end::simpleOptionsExampleEmbeddableDocument[]
    }

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

        assertThat(targetFile.length(), greaterThan(0L));
//end::optionsPDFBackend[]
    }

    @Test
    public void convert_in_unsafe_mode() {
//tag::unsafeConversion[]
        File sourceFile =
//end::unsafeConversion[]
/*  We don't want to show the reader that we're getting the document via a classpath resources instance,
    nor do we want to expose the filesystem structure of our build.
    The reader should not care where the content comes from.
//tag::unsafeConversion[]
            new File("includingcontent.adoc");
//end::unsafeConversion[]
*/
                classpathResources.getResource("includingcontent.adoc");
//tag::unsafeConversion[]
        String result = asciidoctor.convertFile(
                sourceFile,
                Options.builder()
                        .safe(SafeMode.UNSAFE) // <1>
                        .toFile(false)         // <2>
                        .build());

        assertThat(result, containsString("This is included content"));
//end::unsafeConversion[]
    }

    @Test
    public void convert_to_dedicated_file() throws Exception {
//tag::optionToFile[]
        File targetFile = //...
//end::optionToFile[]
                new File(tempDir, "toFileExample.html");

//tag::optionToFile[]
        asciidoctor.convert(
                "Hello World",
                Options.builder()
                        .toFile(targetFile)    // <1>
                        .safe(SafeMode.UNSAFE) // <2>
                        .build());

        assertTrue(targetFile.exists());
        assertThat(
                IOUtils.toString(new FileReader(targetFile)),
                containsString("<p>Hello World"));
//end::optionToFile[]
    }

    @Test
    public void use_font_awesome_icons() {
//tag::attributeFontIcons[]
        String result =
            asciidoctor.convert(
                "NOTE: Asciidoctor supports font-based admonition icons!\n" +
                    "\n" +
                    "{foo}",
                    Options.builder()
                            .toFile(false)
                            .standalone(false)
                            .attributes(
                                    Attributes.builder()                                          // <1>        
                                            .icons(Attributes.FONT_ICONS)                         // <2>
                                            .attribute("foo", "bar")   // <3>
                                            .build())
                            .build());
        assertThat(result, containsString("<i class=\"fa icon-note\" title=\"Note\"></i>"));
        assertThat(result, containsString("<p>bar</p>"));
//end::attributeFontIcons[]
    }

}
