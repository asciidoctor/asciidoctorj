package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.api.OptionsBuilder;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class TerminalCommandTreeprocessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;

    @Test
    public void should_process_terminal_listings() {

        File referenceDocument = classpathResources.getResource("treeprocessorresult.adoc");

        String referenceResult = asciidoctor.convertFile(
                referenceDocument,
                OptionsBuilder.options()
                        .headerFooter(false)
                        .toFile(false));

//tag::include[]
        File src = //...
//end::include[]
            classpathResources.getResource("treeprocessorcontent.adoc");

//tag::include[]
        asciidoctor.javaExtensionRegistry()
                .treeprocessor(TerminalCommandTreeprocessor.class); // <1>
        String result = asciidoctor.convertFile(
                src,
                OptionsBuilder.options()
                        .headerFooter(false)
                        .toFile(false));
//end::include[]

        assertThat(result, is(referenceResult));
    }


}
