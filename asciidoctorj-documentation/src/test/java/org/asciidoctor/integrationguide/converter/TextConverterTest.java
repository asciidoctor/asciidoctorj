package org.asciidoctor.integrationguide.converter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class TextConverterTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("textconvertertest.adoc")
    private File testDocument;


    @Test
    public void should_use_text_converter_for_conversion() {

//tag::include[]
        File test_adoc = //...
//end::include[]
                testDocument;

//tag::include[]

        asciidoctor.javaConverterRegistry().register(TextConverter.class); // <1>

        String result = asciidoctor.convertFile(
                test_adoc,
                OptionsBuilder.options()
                        .backend("text")                                   // <2>
                        .toFile(false));

//end::include[]

        verifyResult(result);
    }

    @Test
    public void should_use_text_converter_for_conversion_registered_as_service_impl(
            @ClasspathResource("converterregistry") File extensionRegistryDir) throws Exception {

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();

        try {
            URL serviceDir = extensionRegistryDir.toURI().toURL();
            URLClassLoader tccl = new URLClassLoader(new URL[]{serviceDir});
            Thread.currentThread().setContextClassLoader(tccl);

            Asciidoctor asciidoctor = Asciidoctor.Factory.create();
            File test_adoc = //...
                    testDocument;

            String result = asciidoctor.convertFile(
                    test_adoc,
                    OptionsBuilder.options()
                            .backend("text")
                            .toFile(false));

            verifyResult(result);
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }

    private void verifyResult(String result) {
        var lines = result.split("\\n");
        assertThat(lines)
                .contains("== This is section 1 ==")
                .contains("Paragraph 1")
                .contains("== This is section 2 ==")
                .contains("Paragraph 2");
    }
}
