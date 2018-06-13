package org.asciidoctor;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.TestLogHandlerService;
import org.asciidoctor.util.ClasspathResources;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.LogManager;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class WhenAsciidoctorWithLogHandlerServiceLogsToConsole {

    @Rule
    public ClasspathResources classpath = new ClasspathResources();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private Asciidoctor asciidoctor;

    @Before
    public void before() {
        asciidoctor = JRubyAsciidoctor.create();
    }

    @After
    public void cleanup() throws IOException {
        LogManager.getLogManager().readConfiguration();
        TestLogHandlerService.clear();
    }

    @Test
    public void shouldNotifyLogHandlerService() throws Exception {

        File inputFile = classpath.getResource("documentwithnotexistingfile.adoc");
        String renderContent = asciidoctor.renderFile(inputFile,
            options()
                .inPlace(true)
                .safe(SafeMode.SERVER)
                .attributes(
                    AttributesBuilder.attributes().allowUriRead(true))
                .asMap());

        File expectedFile = new File(inputFile.getParent(), "documentwithnotexistingfile.html");
        expectedFile.delete();

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        assertThat(logRecords, hasSize(4));
        assertThat(logRecords.get(0).getMessage(), containsString("include file not found"));
        final Cursor cursor = logRecords.get(0).getCursor();
        assertThat(cursor.getDir().replace('\\', '/'), is(inputFile.getParent().replace('\\', '/')));
        assertThat(cursor.getFile(), is(inputFile.getName()));
        assertThat(cursor.getLineNumber(), is(3));

        for (LogRecord logRecord: logRecords) {
            assertThat(logRecord.getCursor(), not(nullValue()));
            assertThat(logRecord.getCursor().getFile(), not(nullValue()));
            assertThat(logRecord.getCursor().getDir(), not(nullValue()));
        }
    }
}
