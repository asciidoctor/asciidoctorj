package org.asciidoctor.integrationguide.converter;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class TextConverterTest {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

    @Test
    public void should_use_text_converter_for_conversion() {

//tag::include[]
        File test_adoc = //...
//end::include[]
                classpathResources.getResource("textconvertertest.adoc");

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

    private void verifyResult(String result) {
        List<String> lines = Arrays.asList(result.split("\\n"));
        assertThat(lines, hasItem("== This is section 1 =="));
        assertThat(lines, hasItem("Paragraph 1"));
        assertThat(lines, hasItem("== This is section 2 =="));
        assertThat(lines, hasItem("Paragraph 2"));
    }


    @Test
    public void should_use_text_converter_for_conversion_registered_as_service_impl() throws Exception {

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();

        try {
            URL serviceDir = classpathResources.getResource("converterregistry").toURI().toURL();
            URLClassLoader tccl = new URLClassLoader(new URL[]{serviceDir});
            Thread.currentThread().setContextClassLoader(tccl);

            Asciidoctor asciidoctor = Asciidoctor.Factory.create();
            File test_adoc = //...
                    classpathResources.getResource("textconvertertest.adoc");

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

}
