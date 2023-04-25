package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class YellBlockProcessorTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;


    @Test
    public void should_invoke_block_processor(@ClasspathResource("yell-block.adoc") File yellBlock) {
//tag::include[]
        File yellblock_adoc = //...
//end::include[]
            yellBlock;

        //tag::include[]

        asciidoctor.javaExtensionRegistry().block(YellBlockProcessor.class); // <1>

        String result = asciidoctor.convertFile(yellblock_adoc, OptionsBuilder.options().toFile(false));

        assertThat(result, containsString("I REALLY MEAN IT"));              // <2>
//end::include[]
    }

    @Test
    public void should_invoke_block_processor_with_attributes(@ClasspathResource("yell-block-attributes.adoc") File yellBlock) {
        File yellblock_adoc = //...
            yellBlock;

        asciidoctor.javaExtensionRegistry().block(YellBlockProcessorWithAttributes.class);

        String result = asciidoctor.convertFile(yellblock_adoc, Options.builder().toFile(false).build());

        assertThat(result, containsString("I REALLY MEAN IT!!!"));
    }

    @Test
    public void should_invoke_block_processor_with_positional_attributes(@ClasspathResource("yell-block-positional.adoc") File yellBlock) {
        File yellblock_adoc = //...
            yellBlock;

        asciidoctor.javaExtensionRegistry().block(YellBlockProcessorWithPositionalAttributes.class);

        String result = asciidoctor.convertFile(yellblock_adoc, Options.builder().toFile(false).build());

        assertThat(result, containsString("I REALLY MEAN IT!!!!!"));
    }
}
