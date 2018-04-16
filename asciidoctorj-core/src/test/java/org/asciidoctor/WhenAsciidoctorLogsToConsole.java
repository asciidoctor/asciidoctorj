package org.asciidoctor;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.asciidoctor.util.ClasspathResources;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WhenAsciidoctorLogsToConsole {

    @Rule
    public ClasspathResources classpath = new ClasspathResources();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

    @After
    public void cleanup() throws IOException {
        LogManager.getLogManager().readConfiguration();
    }

    @Test
    public void shouldBeRedirectToAsciidoctorJLoggerSystem() throws Exception {
        final MemoryLogHandler memoryLogHandler = registerMemoryLogHandler();

        File inputFile = classpath.getResource("documentwithnotexistingfile.adoc");
        String renderContent = asciidoctor.renderFile(inputFile, options()
                .inPlace(true).safe(SafeMode.SERVER).asMap());

        File expectedFile = new File(inputFile.getParent(), "documentwithnotexistingfile.html");
        expectedFile.delete();

        assertEquals(1, memoryLogHandler.getLogRecords().size());
        assertThat(memoryLogHandler.getLogRecords().get(0).getMessage(),
            both(containsString("include file not found"))
                .and(containsString("documentwithnotexistingfile.adoc: line 3")));
        assertThat(memoryLogHandler.getLogRecords().get(0).getParameters()[0], instanceOf(Cursor.class));
        final Cursor cursor = (Cursor) memoryLogHandler.getLogRecords().get(0).getParameters()[0];
        assertThat(cursor.getDir(), is(inputFile.getParent()));
        assertThat(cursor.getFile(), is(inputFile.getName()));
        assertThat(cursor.getLineNumber(), is(3));
    }

    private MemoryLogHandler registerMemoryLogHandler() {
        final Logger logger = Logger.getLogger("asciidoctor");
        final MemoryLogHandler handler = new MemoryLogHandler();
        logger.addHandler(handler);
        return handler;
    }

}
