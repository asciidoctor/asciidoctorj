package org.asciidoctor;

import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class WhenAsciidoctorLogsToConsole {

    @ArquillianResource
    private ClasspathResources classpath = new ClasspathResources();

    @ArquillianResource
    private TemporaryFolder testFolder;

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @After
    public void cleanup() throws IOException {
        LogManager.getLogManager().readConfiguration();
    }

    @Test
    public void shouldBeRedirectToAsciidoctorJLoggerSystem() throws Exception {
        LogManager.getLogManager().readConfiguration(getClass().getResourceAsStream("/testlogging.properties"));

        File inputFile = classpath.getResource("documentwithnotexistingfile.adoc");
        String renderContent = asciidoctor.renderFile(inputFile, options()
                .inPlace(true).safe(SafeMode.SERVER).asMap());

        File expectedFile = new File(inputFile.getParent(), "documentwithnotexistingfile.html");
        expectedFile.delete();

        final MemoryLogHandler memoryLogHandler = getMemoryLogHandler();
        assertEquals(1, memoryLogHandler.getLogRecords().size());
        assertThat(memoryLogHandler.getLogRecords().get(0).getMessage(),
            both(containsString("include file not found"))
                .and(containsString("documentwithnotexistingfile.adoc: line 3")));
    }

    private MemoryLogHandler getMemoryLogHandler() {
        final Logger logger = LogManager.getLogManager().getLogger("asciidoctor");
        assertThat(logger.getParent().getHandlers(), hasItemInArray(instanceOf(MemoryLogHandler.class)));

        MemoryLogHandler memoryLogHandler = null;
        for (Handler handler : logger.getParent().getHandlers()) {
            if (handler instanceof MemoryLogHandler) {
                memoryLogHandler = (MemoryLogHandler) handler;
            }
        }
        return memoryLogHandler;
    }

}
