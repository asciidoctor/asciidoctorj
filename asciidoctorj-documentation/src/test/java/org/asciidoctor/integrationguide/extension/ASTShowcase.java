package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.Document;
import org.asciidoctor.jruby.internal.IOUtils;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileReader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class ASTShowcase {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;


    @Test
    public void createAstStructure() throws Exception {

        asciidoctor.javaExtensionRegistry().treeprocessor(ASTExtractorTreeprocessor.class);

        Document document = asciidoctor.loadFile(classpathResources.getResource("ast-demo.adoc"), OptionsBuilder.options().asMap());

        assertThat(
                ASTExtractorTreeprocessor.result.toString(),
                is(IOUtils.readFull(new FileReader(classpathResources.getResource("ast-demo-result.txt"))).replaceAll("\\r", "")));


    }

}
