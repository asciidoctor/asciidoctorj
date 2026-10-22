package org.asciidoctor.integrationguide;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;



public class AsciidoctorInterfaceTest {

    @Test
    public void testIntroductoryExample() {
        SimpleAsciidoctorRendering.main(new String[]{"build/resources/test/document.adoc"});

        File resultFile = new File("build/resources/test/document.html");

        assertThat(resultFile.exists()).isTrue();
    }

    @Test
    public void createAsciidoctorInstance() {
//tag::create[]
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
//end::create[]
        assertThat(asciidoctor).isNotNull();
    }

    @Test
    public void destroyAsciidoctorInstance() {
//tag::shutdown[]
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.shutdown();
//end::shutdown[]
    }

    @Test
    public void autocloseAsciidoctorInstance() {
//tag::autoclose[]
        try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
            Options emptyOptions = Options.builder().build();
            asciidoctor.convert("Hello World", emptyOptions);
        }
//end::autoclose[]
    }
}
