package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class YellBlockProcessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;

    @Test
    public void should_invoke_block_processor() throws Exception {
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
    public void should_invoke_block_processor_with_attributes() throws Exception {
        File yellblock_adoc = //...
            classpathResources.getResource("yell-block-attributes.adoc");

        asciidoctor.javaExtensionRegistry().block(YellBlockProcessorWithAttributes.class);

        String result = asciidoctor.convertFile(yellblock_adoc, Options.builder().toFile(false).build());

        assertThat(result, containsString("I REALLY MEAN IT!!!"));
    }

    @Test
    public void should_invoke_block_processor_with_positional_attributes() throws Exception {
        File yellblock_adoc = //...
            classpathResources.getResource("yell-block-positional.adoc");

        asciidoctor.javaExtensionRegistry().block(YellBlockProcessorWithPositionalAttributes.class);

        String result = asciidoctor.convertFile(yellblock_adoc, Options.builder().toFile(false).build());

        assertThat(result, containsString("I REALLY MEAN IT!!!!!"));
    }
}
