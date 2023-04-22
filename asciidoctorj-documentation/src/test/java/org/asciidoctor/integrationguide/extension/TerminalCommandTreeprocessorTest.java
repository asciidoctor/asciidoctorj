package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class TerminalCommandTreeprocessorTest {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

    @Test
    public void should_process_terminal_listings() {

        File referenceDocument = classpathResources.getResource("treeprocessorresult.adoc");

        String referenceResult = asciidoctor.convertFile(
                referenceDocument,
                OptionsBuilder.options()
                        .standalone(false)
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
                        .standalone(false)
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
                        .standalone(false)
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
                    Options.builder()
                            .standalone(false)
                            .toFile(false)
                            .build());
//end::include-extension-registry[]
            assertThat(result, is(referenceResult));
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }

}
