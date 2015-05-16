package org.asciidoctor;

import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;

import static org.asciidoctor.OptionsBuilder.options;

@RunWith(Arquillian.class)
public class WhenAsciidoctorLogsToConsole {

    @ArquillianResource
    private ClasspathResources classpath = new ClasspathResources();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @Test
    public void shouldBeRedirectToAsciidoctorJLoggerSystem() {

        File inputFile = classpath.getResource("documentwithnotexistingfile.adoc");
        String renderContent = asciidoctor.renderFile(inputFile, options()
                .inPlace(true).safe(SafeMode.SERVER).asMap());

        File expectedFile = new File(inputFile.getParent(), "documentwithnotexistingfile.html");
        expectedFile.delete();
    }

}
