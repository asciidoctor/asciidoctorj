package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;


public class YellBlockProcessorTest {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

    @Test
    public void should_invoke_block_processor() {
//tag::include[]
        File yellblock_adoc = //...
//end::include[]
            classpathResources.getResource("yell-block.adoc");

        //tag::include[]

        asciidoctor.javaExtensionRegistry().block(YellBlockProcessor.class); // <1>

        String result = asciidoctor.convertFile(yellblock_adoc, OptionsBuilder.options().toFile(false));

        assertThat(result, containsString("I REALLY MEAN IT"));              // <2>
//end::include[]
    }

    @Test
    public void should_invoke_block_processor_with_attributes() {
        File yellblock_adoc = //...
            classpathResources.getResource("yell-block-attributes.adoc");

        asciidoctor.javaExtensionRegistry().block(YellBlockProcessorWithAttributes.class);

        String result = asciidoctor.convertFile(yellblock_adoc, Options.builder().toFile(false).build());

        assertThat(result, containsString("I REALLY MEAN IT!!!"));
    }

    @Test
    public void should_invoke_block_processor_with_positional_attributes() {
        File yellblock_adoc = //...
            classpathResources.getResource("yell-block-positional.adoc");

        asciidoctor.javaExtensionRegistry().block(YellBlockProcessorWithPositionalAttributes.class);

        String result = asciidoctor.convertFile(yellblock_adoc, Options.builder().toFile(false).build());

        assertThat(result, containsString("I REALLY MEAN IT!!!!!"));
    }
}
