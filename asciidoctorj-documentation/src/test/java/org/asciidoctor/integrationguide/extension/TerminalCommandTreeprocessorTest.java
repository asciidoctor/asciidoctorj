package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

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

    @Test
    public void should_process_terminal_listings_after_registering_via_extension_registry() throws Exception {

        File referenceDocument = classpathResources.getResource("treeprocessorresult.adoc");

        String referenceResult = asciidoctor.convertFile(
                referenceDocument,
                OptionsBuilder.options()
                        .headerFooter(false)
                        .toFile(false));

//tag::include-extension-registry[]
        File src = //...
//end::include-extension-registry[]
            classpathResources.getResource("treeprocessorcontent.adoc");


        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();

        try {
            URL serviceDir = classpathResources.getResource("extensionregistry").toURI().toURL();
            URLClassLoader tccl = new URLClassLoader(new URL[]{serviceDir});
            Thread.currentThread().setContextClassLoader(tccl);

            Asciidoctor asciidoctor = Asciidoctor.Factory.create();
//tag::include-extension-registry[]
        String result = asciidoctor.convertFile(
            src,
            OptionsBuilder.options()
                .headerFooter(false)
                .toFile(false));
//end::include-extension-registry[]
            assertThat(result, is(referenceResult));
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


}
