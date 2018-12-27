package org.asciidoctor.integrationguide;

import org.apache.commons.io.IOUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.api.Attributes;
import org.asciidoctor.api.AttributesBuilder;
import org.asciidoctor.api.OptionsBuilder;
import org.asciidoctor.api.SafeMode;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileReader;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class OptionsTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;

    @ArquillianResource
    private TemporaryFolder temporaryFolder;

    @Test
    public void simple_options_example_embeddable_document() {
//tag::simpleOptionsExampleEmbeddableDocument[]
        String result =
                asciidoctor.convert(
                        "Hello World",
                        OptionsBuilder.options()     // <1>
                                .headerFooter(false) // <2>
                                .get());             // <3>

        assertThat(result, startsWith("<div "));
//end::simpleOptionsExampleEmbeddableDocument[]
    }

    @Test
    public void options_for_pdf_document() throws Exception {
//tag::optionsPDFBackend[]
        File targetFile = // ...
//end::optionsPDFBackend[]
        temporaryFolder.newFile("test.pdf");
        assertTrue(targetFile.exists());
//tag::optionsPDFBackend[]
        asciidoctor.convert(
                "Hello World",
                OptionsBuilder.options()
                        .backend("pdf")
                        .toFile(targetFile)
                        .safe(SafeMode.UNSAFE)
                        .get());

        assertThat(targetFile.length(), greaterThan(0L));
//end::optionsPDFBackend[]
    }

    @Test
    public void convert_in_unsafe_mode() throws Exception {
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
                OptionsBuilder.options()
                        .safe(SafeMode.UNSAFE) // <1>
                        .toFile(false)         // <2>
                        .get());

        assertThat(result, containsString("This is included content"));
//end::unsafeConversion[]
    }

    @Test
    public void convert_to_dedicated_file() throws Exception {
//tag::optionToFile[]
        File targetFile = //...
//end::optionToFile[]
        temporaryFolder.newFile("toFileExample.html");

//tag::optionToFile[]
        asciidoctor.convert(
                "Hello World",
                OptionsBuilder.options()
                        .toFile(targetFile)    // <1>
                        .safe(SafeMode.UNSAFE) // <2>
                        .get());

        assertTrue(targetFile.exists());
        assertThat(
                IOUtils.toString(new FileReader(targetFile)),
                containsString("<p>Hello World"));
//end::optionToFile[]
    }

    @Test
    public void use_font_awesome_icons() throws Exception {
//tag::attributeFontIcons[]
        String result =
            asciidoctor.convert(
                "NOTE: Asciidoctor supports font-based admonition icons!\n" +
                    "\n" +
                    "{foo}",
                OptionsBuilder.options()
                        .toFile(false)
                        .headerFooter(false)
                        .attributes(
                                AttributesBuilder.attributes()        // <1>
                                        .icons(Attributes.FONT_ICONS) // <2>
                                        .attribute("foo", "bar")      // <3>
                                        .get())
                        .get());
        assertThat(result, containsString("<i class=\"fa icon-note\" title=\"Note\"></i>"));
        assertThat(result, containsString("<p>bar</p>"));
//end::attributeFontIcons[]
    }

}
