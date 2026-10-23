package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.time.ZonedDateTime;

import org.asciidoctor.cli.Invoker;
import org.asciidoctor.core.converter.Converter;

import org.junit.jupiter.api.*;

@DisplayName("Invoker")
public class InvokerTest extends CliTestHelper {

    @Test
    @DisplayName("should allow Options to be passed as first argument of constructor")
    void shouldAllowOptionsToBePassedAsFirstArgumentOfConstructor() throws Exception {
        var opts = new org.asciidoctor.cli.Options(Options.of("attributes", Options.of("toc", ""), "doctype", "book", "sourcemap", true));
        var invoker = new Invoker(opts);
        assertSame(invoker.getOptions(), opts);
    }

    @Test
    @DisplayName("should allow options Hash to be passed as first argument of constructor")
    void shouldAllowOptionsHashToBePassedAsFirstArgumentOfConstructor() throws Exception {
        var opts = Options.of("attributes", Options.of("toc", ""), "doctype", "book", "sourcemap", true);
        var invoker = new Invoker(opts);
        var resolvedOpts = invoker.getOptions();
        assertEquals(get(opts, "attributes"), get(resolvedOpts, "attributes"));
        assertEquals("book", get(get(resolvedOpts, "attributes"), "doctype"));
        assertTruthy(get(resolvedOpts, "sourcemap"));
    }

    @Test
    @DisplayName("should parse options from array passed as first argument of constructor")
    void shouldParseOptionsFromArrayPassedAsFirstArgumentOfConstructor() throws Exception {
        var inputFile = fixturePath("basic.adoc");
        var invoker = new Invoker(list("-s", inputFile));
        var resolvedOptions = invoker.getOptions();
        assertFalsy(get(resolvedOptions, "standalone"));
        assertEquals(list(inputFile), get(resolvedOptions, "input_files"));
    }

    @Test
    @DisplayName("should parse options from multiple arguments passed to constructor")
    void shouldParseOptionsFromMultipleArgumentsPassedToConstructor() throws Exception {
        var inputFile = fixturePath("basic.adoc");
        var invoker = new Invoker("-s", inputFile);
        var resolvedOptions = invoker.getOptions();
        assertFalsy(get(resolvedOptions, "standalone"));
        assertEquals(list(inputFile), get(resolvedOptions, "input_files"));
    }

    @Test
    @DisplayName("should parse source and convert to html5 article by default")
    void shouldParseSourceAndConvertToHtml5ArticleByDefault() throws Exception {
        var invoker = new Ref<Invoker>(null);
        var output = new Ref<String>(null);
        try (var streams = redirectStreams()) {
            var out = streams.out();
            invoker.set(invokeCli(list("-o", "-")));
            output.set(out.getString());
        }
        assertNotNull(invoker.get());
        var doc = (Document) ((Document) unwrap(invoker.get().getDocument()));
        assertNotNull(doc);
        assertEquals("Document Title", doc.getDoctitle());
        assertEquals("Doc Writer", doc.getAttribute("author"));
        assertEquals("html5", doc.getAttribute("backend"));
        assertEquals(".html", doc.getAttribute("outfilesuffix"));
        assertEquals("article", doc.getAttribute("doctype"));
        assertTruthy(doc.hasBlocks());
        assertEquals("preamble", first(doc.getBlocks()).getContext());
        refuteEmpty(output.get());
        assertXpath("/html", output.get(), 1);
        assertXpath("/html/head", output.get(), 1);
        assertXpath("/html/body", output.get(), 1);
        assertXpath("/html/head/title[text() = \"Document Title\"]", output.get(), 1);
        assertXpath("/html/body[@class=\"article\"]/*[@id=\"header\"]/h1[text() = \"Document Title\"]", output.get(), 1);
    }

    @Test
    @DisplayName("should set implicit doc info attributes")
    void shouldSetImplicitDocInfoAttributes() throws Exception {
        var sampleFilepath = fixturePath("sample.adoc");
        var sampleFiledir = fixturedir();
        var invoker = invokeCliToBuffer(list("-o", "/dev/null"), sampleFilepath);
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals("sample", doc.getAttribute("docname"));
        assertEquals(sampleFilepath, doc.getAttribute("docfile"));
        assertEquals(sampleFiledir, doc.getAttribute("docdir"));
        assertTruthy(doc.hasAttribute("docdate"));
        assertTruthy(doc.hasAttribute("docyear"));
        assertTruthy(doc.hasAttribute("doctime"));
        assertTruthy(doc.hasAttribute("docdatetime"));
        assertEmpty(invoker.readOutput());
    }

    @Test
    @DisplayName("should allow docdate and doctime to be overridden")
    void shouldAllowDocdateAndDoctimeToBeOverridden() throws Exception {
        var sampleFilepath = fixturePath("sample.adoc");
        var invoker = invokeCliToBuffer(list("-o", "/dev/null", "-a", "docdate=2015-01-01", "-a", "doctime=10:00:00-0700"), sampleFilepath);
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertTruthy(doc.isAttribute("docdate", "2015-01-01"));
        assertTruthy(doc.isAttribute("docyear", "2015"));
        assertTruthy(doc.isAttribute("doctime", "10:00:00-0700"));
        assertTruthy(doc.isAttribute("docdatetime", "2015-01-01 10:00:00-0700"));
    }

    @Test
    @DisplayName("should accept document from stdin and write to stdout")
    void shouldAcceptDocumentFromStdinAndWriteToStdout() throws Exception {
        var invoker = invokeCliToBuffer(list("-e"), "-", () -> "content");
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertFalsy(doc.hasAttribute("docname"));
        assertFalsy(doc.hasAttribute("docfile"));
        assertEquals(Helpers.workingDirectory(), doc.getAttribute("docdir"));
        assertEquals(doc.getAttribute("docdate"), doc.getAttribute("localdate"));
        assertEquals(doc.getAttribute("docyear"), doc.getAttribute("localyear"));
        assertEquals(doc.getAttribute("doctime"), doc.getAttribute("localtime"));
        assertEquals(doc.getAttribute("docdatetime"), doc.getAttribute("localdatetime"));
        assertFalsy(doc.hasAttribute("outfile"));
        var output = invoker.readOutput();
        refuteEmpty(output);
        assertXpath("/*[@class=\"paragraph\"]/p[text()=\"content\"]", output, 1);
    }

    @Test
    @DisplayName("should not fail to rewind input if reading document from stdin")
    void shouldNotFailToRewindInputIfReadingDocumentFromStdin() throws Exception {
        var oldStdin = System.in;
        try {
            System.setIn(new ByteArrayInputStream("paragraph".getBytes(StandardCharsets.UTF_8)));
            var invoker = invokeCliToBuffer(list("-e"), "-");
            assertEquals(0, invoker.getCode());
            assertEquals(1, size(((Document) unwrap(invoker.getDocument())).getBlocks()));
        } finally {
            System.setIn(oldStdin);
        }
    }

    @Test
    @DisplayName("should accept document from stdin and write to output file")
    void shouldAcceptDocumentFromStdinAndWriteToOutputFile() throws Exception {
        var sampleOutpath = fixturePath("sample-output.html");
        try {
            var invoker = invokeCli(list("-e", "-o", (str(sampleOutpath))), "-", () -> "content");
            var doc = (Document) ((Document) unwrap(invoker.getDocument()));
            assertFalsy(doc.hasAttribute("docname"));
            assertFalsy(doc.hasAttribute("docfile"));
            assertEquals(Helpers.workingDirectory(), doc.getAttribute("docdir"));
            assertEquals(doc.getAttribute("docdate"), doc.getAttribute("localdate"));
            assertEquals(doc.getAttribute("docyear"), doc.getAttribute("localyear"));
            assertEquals(doc.getAttribute("doctime"), doc.getAttribute("localtime"));
            assertEquals(doc.getAttribute("docdatetime"), doc.getAttribute("localdatetime"));
            assertTruthy(doc.hasAttribute("outfile"));
            assertEquals(sampleOutpath, doc.getAttribute("outfile"));
            assertPathExists(sampleOutpath);
        } finally {
            removeFile(sampleOutpath);
        }
    }

    @Test
    @DisplayName("should fail if input file matches resolved output file")
    void shouldFailIfInputFileMatchesResolvedOutputFile() throws Exception {
        var invoker = invokeCliToBuffer(list("-a", "outfilesuffix=.adoc"), "sample.adoc");
        assertMatch(rx("input file and output file cannot be the same"), invoker.readError());
    }

    @Test
    @DisplayName("should fail if input file matches specified output file")
    void shouldFailIfInputFileMatchesSpecifiedOutputFile() throws Exception {
        var sampleOutpath = fixturePath("sample.adoc");
        var invoker = invokeCliToBuffer(list("-o", (str(sampleOutpath))), "sample.adoc");
        assertMatch(rx("input file and output file cannot be the same"), invoker.readError());
    }

    @Test
    @DisplayName("should accept input from named pipe and output to stdout")
    void shouldAcceptInputFromNamedPipeAndOutputToStdout() throws Exception {
        assumeTrue(!isWindows());
        var sampleInpath = fixturePath("sample-pipe.adoc");
        try {
            new ProcessBuilder("mkfifo", sampleInpath).start().waitFor();
            var writeThread = new Thread(() -> Helpers.writeFile(sampleInpath, "pipe content"));
            writeThread.start();
            var invoker = invokeCliToBuffer(list("-a", "stylesheet!"), sampleInpath);
            var result = invoker.readOutput();
            assertMatch(rx("pipe content"), result);
            writeThread.join();
        } finally {
            removeFile(sampleInpath);
        }
    }

    @Test
    @DisplayName("should allow docdir to be specified when input is a string")
    void shouldAllowDocdirToBeSpecifiedWhenInputIsAString() throws Exception {
        var expectedDocdir = fixturedir();
        var invoker = invokeCliToBuffer(list("-e", "--base-dir", "src/test/resources/fixtures", "-o", "/dev/null"), "-", () -> "content");
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals(expectedDocdir, doc.getAttribute("docdir"));
        assertEquals(expectedDocdir, doc.getBaseDir());
    }

    @Test
    @DisplayName("should display version and exit")
    void shouldDisplayVersionAndExit() throws Exception {
        var expected = ("AsciidoctorJ " + str(Asciidoctor.VERSION) + " [https://asciidoctor.org]\nRuntime Environment (" + str(runtimeDescription()) + ")");
        for (var switch_ : list("--version", "-V")) {
            var actual = new Ref<String>(null);
            try (var streams = redirectStreams()) {
                var out = streams.out();
                invokeCli(list(switch_));
                actual.set(Strings.rstrip(out.getString()));
            }
            assertNotNull(actual.get());
            assertTruthy(startsWith(actual.get(), expected), ("Expected to print version when using " + str(switch_) + " switch"));
        }
    }

    @Test
    @DisplayName("should print warnings to stderr by default")
    void shouldPrintWarningsToStderrByDefault() throws Exception {
        var input = """
                1. first
                3. third
                """;
        var warnings = new Ref<String>(null);
        try (var streams = redirectStreams()) {
            streams.out();
            var err = streams.err();
            invokeCliToBuffer(list("-o", "/dev/null"), "-", () -> input);
            warnings.set(err.getString());
        }
        assertMatch(rx("WARNING"), warnings.get());
    }

    @Test
    @DisplayName("should not emit any unexpected warnings")
    void shouldNotEmitAnyUnexpectedWarnings() throws Exception {
        var inputPath = fixturePath("basic.adoc");
        var output = runCommand(list(asciidoctorCmd(), "-o", "/dev/null", "-w", inputPath), out -> out.read());
        assertEmpty(output);
    }

    @Test
    @DisplayName("should change level on logger when --log-level is specified")
    void shouldChangeLevelOnLoggerWhenLogLevelIsSpecified() throws Exception {
        var input = """
                skip to <<install>>

                . download
                . install[[install]]
                . run
                """;
        var output = new Ref<String>(null);
        try (var streams = redirectStreams()) {
            streams.out();
            var err = streams.err();
            invokeCli(list("--log-level", "info"), "-", () -> input);
            output.set(err.getString());
        }
        assertEquals("asciidoctor: INFO: possible invalid reference: install", Strings.chomp(output.get()));
    }

    @Test
    @DisplayName("should not log when --log-level and -q are both specified")
    void shouldNotLogWhenLogLevelAndQAreBothSpecified() throws Exception {
        var input = """
                skip to <<install>>

                . download
                . install[[install]]
                . run
                """;
        var output = new Ref<String>(null);
        try (var streams = redirectStreams()) {
            streams.out();
            var err = streams.err();
            invokeCli(list("--log-level", "info", "-q"), "-", () -> input);
            output.set(err.getString());
        }
        assertEmpty(output.get());
    }

    @Test
    @DisplayName("should use specified log level when --log-level and -v are both specified")
    void shouldUseSpecifiedLogLevelWhenLogLevelAndVAreBothSpecified() throws Exception {
        var input = """
                skip to <<install>>

                . download
                . install[[install]]
                . run
                """;
        var output = new Ref<String>(null);
        try (var streams = redirectStreams()) {
            streams.out();
            var err = streams.err();
            invokeCli(list("--log-level", "warn", "-v"), "-", () -> input);
            output.set(err.getString());
        }
        assertEmpty(output.get());
    }

    @Test
    @Disabled("script warnings are specific to Ruby")
    @DisplayName("should enable script warnings if -w flag is specified")
    void shouldEnableScriptWarningsIfWFlagIsSpecified() throws Exception {
    }

    @Test
    @DisplayName("should silence warnings if -q flag is specified")
    void shouldSilenceWarningsIfQFlagIsSpecified() throws Exception {
        var input = """
                2. second
                3. third
                """;
        var warnings = new Ref<String>(null);
        try (var streams = redirectStreams()) {
            streams.out();
            var err = streams.err();
            invokeCliToBuffer(list("-q", "-o", "/dev/null"), "-", () -> input);
            warnings.set(err.getString());
        }
        assertEquals("", warnings.get());
    }

    @Test
    @DisplayName("should not fail to check log level when -q flag is specified")
    void shouldNotFailToCheckLogLevelWhenQFlagIsSpecified() throws Exception {
        var input = """
                skip to <<install>>

                . download
                . install[[install]]
                . run
                """;
        try (var streams = redirectStreams()) {
            var invoker = invokeCli(list("-q"), "-", () -> input);
            assertEquals(0, invoker.getCode());
        }
    }

    @Test
    @DisplayName("should return non-zero exit code if failure level is reached")
    void shouldReturnNonZeroExitCodeIfFailureLevelIsReached() throws Exception {
        var input = """
                1. first
                3. third
                """;
        var masgn = redirectStreams((ignored, err) -> list(invokeCli(list("-q", "--failure-level=WARN", "-o", "/dev/null"), "-", () -> input).getCode(), err.getString()));
        var exitCode = get(masgn, 0);
        var messages = get(masgn, 1);
        assertEquals(1, exitCode);
        assertEmpty(messages);
    }

    @Test
    @DisplayName("should report usage if no input file given")
    void shouldReportUsageIfNoInputFileGiven() throws Exception {
        try (var streams = redirectStreams()) {
            streams.out();
            var err = streams.err();
            invokeCli(new ArrayList<String>(), (String) null);
            assertMatch(rx("Usage:"), err.getString());
        }
    }

    @Test
    @DisplayName("should report error if input file does not exist")
    void shouldReportErrorIfInputFileDoesNotExist() throws Exception {
        try (var streams = redirectStreams()) {
            streams.out();
            var err = streams.err();
            var invoker = invokeCli(new ArrayList<String>(), "missing_file.adoc");
            assertMatch(rx("input file .* is missing"), err.getString());
            assertEquals(1, invoker.getCode());
        }
    }

    @Test
    @DisplayName("should suggest --trace option if not present when program raises error")
    void shouldSuggestTraceOptionIfNotPresentWhenProgramRaisesError() throws Exception {
        try (var streams = redirectStreams()) {
            streams.out();
            var err = streams.err();
            var sampleFilepath = fixturePath("sample.adoc");
            var invoker = invokeCli(list("-r", "no-such-module"), sampleFilepath);
            assertMatch(rx("'no-such-module' could not be loaded\\n *Use --trace to show backtrace"), err.getString());
            assertEquals(1, invoker.getCode());
        }
    }

    @Test
    @DisplayName("should raise error when --trace option is specified and program raises error")
    void shouldRaiseErrorWhenTraceOptionIsSpecifiedAndProgramRaisesError() throws Exception {
        var sampleFilepath = fixturePath("sample.adoc");
        assertThrows(IllegalStateException.class, () -> {
            invokeCli(list("--trace", "-r", "no-such-module"), sampleFilepath);
        });
    }

    @Test
    @DisplayName("should show backtrace when --trace option is specified and program raises error")
    void shouldShowBacktraceWhenTraceOptionIsSpecifiedAndProgramRaisesError() throws Exception {
        var result = runCommand(list(asciidoctorCmd(), "-r", "no-such-module", "--trace", fixturePath("basic.adoc")), out -> out.read());
        assertMatch(rx("IllegalStateException: cannot load such class -- no-such-module\n\tat "), result);
    }

    @Test
    @DisplayName("should treat extra arguments as files")
    void shouldTreatExtraArgumentsAsFiles() throws Exception {
        try (var streams = redirectStreams()) {
            streams.out();
            var err = streams.err();
            var invoker = invokeCli(list("-o", "/dev/null", "extra", "arguments", "sample.adoc"), (String) null);
            assertMatch(rx("input file .* is missing"), err.getString());
            assertEquals(1, invoker.getCode());
        }
    }

    @Test
    @DisplayName("should output to file name based on input file name")
    void shouldOutputToFileNameBasedOnInputFileName() throws Exception {
        var sampleOutpath = fixturePath("sample.html");
        try {
            var invoker = invokeCli();
            var doc = (Document) ((Document) unwrap(invoker.getDocument()));
            assertEquals(sampleOutpath, doc.getAttribute("outfile"));
            assertPathExists(sampleOutpath);
            var output = readFile(sampleOutpath);
            refuteEmpty(output);
            assertXpath("/html", output, 1);
            assertXpath("/html/head", output, 1);
            assertXpath("/html/body", output, 1);
            assertXpath("/html/head/title[text() = \"Document Title\"]", output, 1);
            assertXpath("/html/body/*[@id=\"header\"]/h1[text() = \"Document Title\"]", output, 1);
        } finally {
            removeFile(sampleOutpath);
        }
    }

    @Test
    @DisplayName("should output to file in destination directory if set")
    void shouldOutputToFileInDestinationDirectoryIfSet() throws Exception {
        var destinationPath = joinPath(testdir(), "test_output");
        var sampleOutpath = joinPath(destinationPath, "sample.html");
        try {
            makeDir(destinationPath);
            var invoker = invokeCli(list("-D", "src/test/resources/test_output"));
            var doc = (Document) ((Document) unwrap(invoker.getDocument()));
            assertEquals(sampleOutpath, doc.getAttribute("outfile"));
            assertPathExists(sampleOutpath);
        } finally {
            removeFile(sampleOutpath);
            removeDir(destinationPath);
        }
    }

    @Test
    @DisplayName("should preserve directory structure in destination directory if source directory is set")
    void shouldPreserveDirectoryStructureInDestinationDirectoryIfSourceDirectoryIsSet() throws Exception {
        var sampleInpath = "subdir/index.adoc";
        var destinationPath = "test_output";
        var destinationSubdirPath = joinPath(destinationPath, "subdir");
        var sampleOutpath = joinPath(destinationSubdirPath, "index.html");
        try {
            makeDir(destinationPath);
            invokeCli(list("-D", (str(destinationPath)), "-R", "src/test/resources/fixtures"), sampleInpath);
            assertTruthy(fileExists(destinationSubdirPath));
            assertPathExists(sampleOutpath);
        } finally {
            removeFile(sampleOutpath);
            removeDir(destinationSubdirPath);
            removeDir(destinationPath);
        }
    }

    @Test
    @DisplayName("should output to file specified")
    void shouldOutputToFileSpecified() throws Exception {
        var sampleOutpath = fixturePath("sample-output.html");
        try {
            var invoker = invokeCli(list("-o", (str(sampleOutpath))));
            var doc = (Document) ((Document) unwrap(invoker.getDocument()));
            assertEquals(sampleOutpath, doc.getAttribute("outfile"));
            assertPathExists(sampleOutpath);
        } finally {
            removeFile(sampleOutpath);
        }
    }

    @Test
    @DisplayName("should copy default stylesheet to target directory if linkcss is specified")
    void shouldCopyDefaultStylesheetToTargetDirectoryIfLinkcssIsSpecified() throws Exception {
        // NOTE the CodeRay adapter is not ported (it requires a gem), so only the default stylesheet is verified
        var sampleOutpath = fixturePath("sample-output.html");
        var asciidoctorStylesheet = fixturePath("asciidoctor.css");
        try {
            invokeCli(list("-o", sampleOutpath, "-a", "linkcss"), "source-block.adoc");
            assertPathExists(sampleOutpath);
            assertPathExists(asciidoctorStylesheet);
            for (var path : list(sampleOutpath, asciidoctorStylesheet)) {
                var contents = readFile(path);
                assertIncludes(contents, "\n");
                refuteIncludes(contents, "\r");
                assertFalsy(contents.endsWith("\n"));
            }
        } finally {
            removeFile(sampleOutpath);
            removeFile(asciidoctorStylesheet);
        }
    }

    @Test
    @DisplayName("should not copy coderay stylesheet to target directory when no source blocks where highlighted")
    void shouldNotCopyCoderayStylesheetToTargetDirectoryWhenNoSourceBlocksWhereHighlighted() throws Exception {
        var sampleOutpath = fixturePath("sample-output.html");
        var asciidoctorStylesheet = fixturePath("asciidoctor.css");
        var coderayStylesheet = fixturePath("coderay-asciidoctor.css");
        try {
            invokeCli(list("-o", (str(sampleOutpath)), "-a", "linkcss", "-a", "source-highlighter=coderay"));
            assertPathExists(sampleOutpath);
            assertPathExists(asciidoctorStylesheet);
            refutePathExists(coderayStylesheet);
        } finally {
            removeFile(sampleOutpath);
            removeFile(asciidoctorStylesheet);
            removeFile(coderayStylesheet);
        }
    }

    @Test
    @DisplayName("should not copy default stylesheet to target directory if linkcss is set and copycss is unset")
    void shouldNotCopyDefaultStylesheetToTargetDirectoryIfLinkcssIsSetAndCopycssIsUnset() throws Exception {
        var sampleOutpath = fixturePath("sample-output.html");
        var defaultStylesheet = fixturePath("asciidoctor.css");
        try {
            var invoker = invokeCli(list("-o", (str(sampleOutpath)), "-a", "linkcss", "-a", "copycss!"));
            assertPathExists(sampleOutpath);
            refutePathExists(defaultStylesheet);
        } finally {
            removeFile(sampleOutpath);
            removeFile(defaultStylesheet);
        }
    }

    @Test
    @DisplayName("should copy custom stylesheet to target directory if stylesheet and linkcss is specified")
    void shouldCopyCustomStylesheetToTargetDirectoryIfStylesheetAndLinkcssIsSpecified() throws Exception {
        var destdir = fixturePath("output");
        var sampleOutpath = joinPath(destdir, "sample-output.html");
        var stylesdir = joinPath(destdir, "styles");
        var customStylesheet = joinPath(stylesdir, "custom.css");
        try {
            var invoker = invokeCli(list("-o", (str(sampleOutpath)), "-a", "linkcss", "-a", "copycss=stylesheets/custom.css", "-a", "stylesdir=./styles", "-a", "stylesheet=custom.css"));
            assertPathExists(sampleOutpath);
            assertPathExists(customStylesheet);
        } finally {
            removeFile(sampleOutpath);
            removeFile(customStylesheet);
            removeDir(stylesdir);
            removeDir(destdir);
        }
    }

    @Test
    @DisplayName("should not copy custom stylesheet to target directory if stylesheet and linkcss are set and copycss is unset")
    void shouldNotCopyCustomStylesheetToTargetDirectoryIfStylesheetAndLinkcssAreSetAndCopycssIsUnset() throws Exception {
        var destdir = fixturePath("output");
        var sampleOutpath = joinPath(destdir, "sample-output.html");
        var stylesdir = joinPath(destdir, "styles");
        var customStylesheet = joinPath(stylesdir, "custom.css");
        try {
            var invoker = invokeCli(list("-o", (str(sampleOutpath)), "-a", "linkcss", "-a", "stylesdir=./styles", "-a", "stylesheet=custom.css", "-a", "copycss!"));
            assertPathExists(sampleOutpath);
            refutePathExists(customStylesheet);
        } finally {
            removeFile(sampleOutpath);
            removeFile(customStylesheet);
            if (fileExists(stylesdir)) {
                removeDir(stylesdir);
            }
            removeDir(destdir);
        }
    }

    @Test
    @DisplayName("should not copy custom stylesheet to target directory if stylesdir is a URI")
    void shouldNotCopyCustomStylesheetToTargetDirectoryIfStylesdirIsAURI() throws Exception {
        var destdir = fixturePath("output");
        var sampleOutpath = joinPath(destdir, "sample-output.html");
        var stylesdir = joinPath(destdir, "http:");
        try {
            var invoker = invokeCli(list("-o", (str(sampleOutpath)), "-a", "linkcss", "-a", "stylesdir=http://example.org/styles", "-a", "stylesheet=custom.css"));
            assertPathExists(sampleOutpath);
            refutePathExists(stylesdir);
        } finally {
            removeFile(sampleOutpath);
            if (fileExists(stylesdir)) {
                removeDir(stylesdir);
            }
            removeDir(destdir);
        }
    }

    @Test
    @DisplayName("should convert all passed files")
    void shouldConvertAllPassedFiles() throws Exception {
        var basicOutpath = fixturePath("basic.html");
        var sampleOutpath = fixturePath("sample.html");
        try {
            invokeCliWithFilenames(new ArrayList<String>(), list("basic.adoc", "sample.adoc"));
            assertPathExists(basicOutpath);
            assertPathExists(sampleOutpath);
        } finally {
            removeFile(basicOutpath);
            removeFile(sampleOutpath);
        }
    }

    @Test
    @DisplayName("options should not be modified when processing multiple files")
    void optionsShouldNotBeModifiedWhenProcessingMultipleFiles() throws Exception {
        var destinationPath = joinPath(testdir(), "test_output");
        var basicOutpath = joinPath(destinationPath, "basic.htm");
        var sampleOutpath = joinPath(destinationPath, "sample.htm");
        try {
            invokeCliWithFilenames(list("-D", "src/test/resources/test_output", "-a", "outfilesuffix=.htm"), list("basic.adoc", "sample.adoc"));
            assertPathExists(basicOutpath);
            assertPathExists(sampleOutpath);
        } finally {
            removeFile(basicOutpath);
            removeFile(sampleOutpath);
            removeDir(destinationPath);
        }
    }

    @Test
    @DisplayName("should convert all files that matches a glob expression")
    void shouldConvertAllFilesThatMatchesAGlobExpression() throws Exception {
        var basicOutpath = fixturePath("basic.html");
        try {
            invokeCliToBuffer(new ArrayList<String>(), "ba*.adoc");
            assertPathExists(basicOutpath);
        } finally {
            removeFile(basicOutpath);
        }
    }

    @Test
    @DisplayName("should convert all files that matches an absolute path glob expression")
    void shouldConvertAllFilesThatMatchesAnAbsolutePathGlobExpression() throws Exception {
        var basicOutpath = fixturePath("basic.html");
        var glob = fixturePath("ba*.adoc");
        if (Objects.equals(altSeparator(), "\\")) {
            glob = Strings.tr(glob, "/", "\\");
        }
        try {
            invokeCliToBuffer(new ArrayList<String>(), glob);
            assertPathExists(basicOutpath);
        } finally {
            removeFile(basicOutpath);
        }
    }

    @Test
    @DisplayName("should suppress header footer if specified")
    void shouldSuppressHeaderFooterIfSpecified() throws Exception {
        for (var flags : list(list("-e", "-o", "-"), list("-s", "-o", "-"))) {
            var invoker = invokeCliToBuffer(flags);
            var output = invoker.readOutput();
            assertXpath("/html", output, 0);
            assertXpath("/*[@id=\"preamble\"]", output, 1);
        }
    }

    @Test
    @DisplayName("should write page for each alternate manname")
    void shouldWritePageForEachAlternateManname() throws Exception {
        var outdir = fixturedir();
        var outfile1 = joinPath(outdir, "eve.1");
        var outfile2 = joinPath(outdir, "islifeform.1");
        var input = """
                = eve(1)
                Andrew Stanton
                v1.0.0
                :doctype: manpage
                :manmanual: EVE
                :mansource: EVE

                == NAME

                eve, islifeform - analyzes an image to determine if it's a picture of a life form

                == SYNOPSIS

                *eve* ['OPTION']... 'FILE'...
                """;
        try {
            invokeCli(list("-b", "manpage", "-o", (str(outfile1))), "-", () -> input);
            assertPathExists(outfile1);
            assertPathExists(outfile2);
            assertEquals(".so eve.1", Strings.chomp(readFile(outfile2)));
        } finally {
            removeFile(outfile1);
            removeFile(outfile2);
        }
    }

    @Test
    @DisplayName("should output a trailing newline to stdout")
    void shouldOutputATrailingNewlineToStdout() throws Exception {
        var invoker = new Ref<Invoker>(null);
        var output = new Ref<String>(null);
        try (var streams = redirectStreams()) {
            var out = streams.out();
            invoker.set(invokeCli(list("-o", "-")));
            output.set(out.getString());
        }
        assertNotNull(invoker.get());
        assertNotNull(output.get());
        assertTruthy(endsWith(output.get(), "\n"));
    }

    @Test
    @DisplayName("should set backend to html5 if specified")
    void shouldSetBackendToHtml5IfSpecified() throws Exception {
        var invoker = invokeCliToBuffer(list("-b", "html5", "-o", "-"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals("html5", doc.getAttribute("backend"));
        assertEquals(".html", doc.getAttribute("outfilesuffix"));
        var output = invoker.readOutput();
        assertXpath("/html", output, 1);
    }

    @Test
    @DisplayName("should set backend to docbook5 if specified")
    void shouldSetBackendToDocbook5IfSpecified() throws Exception {
        var invoker = invokeCliToBuffer(list("-b", "docbook5", "-a", "xmlns", "-o", "-"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals("docbook5", doc.getAttribute("backend"));
        assertEquals(".xml", doc.getAttribute("outfilesuffix"));
        var output = invoker.readOutput();
        assertXpath("/xmlns:article", output, 1);
    }

    @Test
    @DisplayName("should set doctype to article if specified")
    void shouldSetDoctypeToArticleIfSpecified() throws Exception {
        var invoker = invokeCliToBuffer(list("-d", "article", "-o", "-"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals("article", doc.getAttribute("doctype"));
        var output = invoker.readOutput();
        assertXpath("/html/body[@class=\"article\"]", output, 1);
    }

    @Test
    @DisplayName("should set doctype to book if specified")
    void shouldSetDoctypeToBookIfSpecified() throws Exception {
        var invoker = invokeCliToBuffer(list("-d", "book", "-o", "-"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals("book", doc.getAttribute("doctype"));
        var output = invoker.readOutput();
        assertXpath("/html/body[@class=\"book\"]", output, 1);
    }

    @Test
    @DisplayName("should warn if doctype is inline and the first block is not a candidate for inline conversion")
    void shouldWarnIfDoctypeIsInlineAndTheFirstBlockIsNotACandidateForInlineConversion() throws Exception {
        for (var input : list("== Section Title", "image::tiger.png[]")) {
            var warnings = redirectStreams((ignored, err) -> {
                invokeCliToBuffer(list("-d", "inline"), "-", () -> input);
                return err.getString();
            });
            assertMatch(rx("WARNING: no inline candidate"), warnings);
        }
    }

    @Test
    @DisplayName("should not warn if doctype is inline and the document has no blocks")
    void shouldNotWarnIfDoctypeIsInlineAndTheDocumentHasNoBlocks() throws Exception {
        var warnings = redirectStreams((ignored, err) -> {
            invokeCliToBuffer(list("-d", "inline"), "-", () -> "// comment");
            return err.getString();
        });
        refuteMatch(rx("WARNING"), warnings);
    }

    @Test
    @DisplayName("should not warn if doctype is inline and the document contains multiple blocks")
    void shouldNotWarnIfDoctypeIsInlineAndTheDocumentContainsMultipleBlocks() throws Exception {
        var warnings = redirectStreams((ignored, err) -> {
            invokeCliToBuffer(list("-d", "inline"), "-", () -> "paragraph one\n\nparagraph two\n\nparagraph three");
            return err.getString();
        });
        refuteMatch(rx("WARNING"), warnings);
    }

    @Test
    @DisplayName("should add source location to blocks when sourcemap option is specified")
    void shouldAddSourceLocationToBlocksWhenSourcemapOptionIsSpecified() throws Exception {
        var sampleFilepath = fixturePath("sample.adoc");
        var invoker = invokeCliToBuffer(list("--sourcemap", "-o", "-"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        var allBlocks = doc.findBy(AbstractBlock.Selector.any());
        assertNotEquals(0, size(allBlocks));
        for (var block : doc.findBy(AbstractBlock.Selector.any())) {
            assertNotNull(block.getSourceLocation());
        }
        assertEquals(sampleFilepath, doc.getBlocks().get(0).getSourceLocation().getFile());
        assertEquals(6, doc.getBlocks().get(0).getSourceLocation().getLineNumber());
    }

    @Test
    @Disabled("custom converter templates are not supported (they require the Tilt gem)")
    @DisplayName("should locate custom templates based on template dir, template engine and backend")
    void shouldLocateCustomTemplatesBasedOnTemplateDirTemplateEngineAndBackend() throws Exception {
    }

    @Test
    @Disabled("custom converter templates are not supported (they require the Tilt gem)")
    @DisplayName("should load custom templates from multiple template directories")
    void shouldLoadCustomTemplatesFromMultipleTemplateDirectories() throws Exception {
    }

    @Test
    @DisplayName("should set attribute with value")
    void shouldSetAttributeWithValue() throws Exception {
        var invoker = invokeCliToBuffer(list("--trace", "-a", "idprefix=id", "-e", "-o", "-"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals("id", doc.getAttribute("idprefix"));
        var output = invoker.readOutput();
        assertXpath("//h2[@id=\"idsection_a\"]", output, 1);
    }

    @Test
    @DisplayName("should set attribute with value containing equal sign")
    void shouldSetAttributeWithValueContainingEqualSign() throws Exception {
        var invoker = invokeCliToBuffer(list("--trace", "-a", "toc", "-a", "toc-title=t=o=c", "-o", "-"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals("t=o=c", doc.getAttribute("toc-title"));
        var output = invoker.readOutput();
        assertXpath("//*[@id=\"toctitle\"][text() = \"t=o=c\"]", output, 1);
    }

    @Test
    @DisplayName("should set attribute with quoted value containing a space")
    void shouldSetAttributeWithQuotedValueContainingASpace() throws Exception {
        var invoker = invokeCliToBuffer(list("--trace", "-a", "toc", "-a", "note-caption=Note to self:", "-o", "-"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals("Note to self:", doc.getAttribute("note-caption"));
        var output = invoker.readOutput();
        assertXpath(("//*[" + str(containsClass("admonitionblock")) + "]//*[@class='title'][text() = 'Note to self:']"), output, 1);
    }

    @Test
    @DisplayName("should not set attribute ending in @ if defined in document")
    void shouldNotSetAttributeEndingInIfDefinedInDocument() throws Exception {
        var invoker = invokeCliToBuffer(list("--trace", "-a", "idprefix=id@", "-e", "-o", "-"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals("id_", doc.getAttribute("idprefix"));
        var output = invoker.readOutput();
        assertXpath("//h2[@id=\"id_section_a\"]", output, 1);
    }

    @Test
    @DisplayName("should set attribute with no value")
    void shouldSetAttributeWithNoValue() throws Exception {
        var invoker = invokeCliToBuffer(list("-a", "icons", "-e", "-o", "-"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals("", doc.getAttribute("icons"));
        var output = invoker.readOutput();
        assertXpath("//*[@class=\"admonitionblock note\"]//img[@alt=\"Note\"]", output, 1);
    }

    @Test
    @DisplayName("should unset attribute ending in bang")
    void shouldUnsetAttributeEndingInBang() throws Exception {
        var invoker = invokeCliToBuffer(list("-a", "sectids!", "-e", "-o", "-"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertFalsy(doc.hasAttribute("sectids"));
        var output = invoker.readOutput();
        assertXpath("//h2[not(@id)]", output);
    }

    @Test
    @DisplayName("default mode for cli should be unsafe")
    void defaultModeForCliShouldBeUnsafe() throws Exception {
        var invoker = invokeCliToBuffer(list("-o", "/dev/null"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals(SafeMode.UNSAFE.getLevel(), doc.getSafe());
    }

    @Test
    @DisplayName("should set safe mode if specified")
    void shouldSetSafeModeIfSpecified() throws Exception {
        var invoker = invokeCliToBuffer(list("--safe", "-o", "/dev/null"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals(SafeMode.SAFE.getLevel(), doc.getSafe());
    }

    @Test
    @DisplayName("should set safe mode to specified level")
    void shouldSetSafeModeToSpecifiedLevel() throws Exception {
        for (var name : SafeMode.names()) {
            var invoker = invokeCliToBuffer(list("-S", (str(name)), "-o", "/dev/null"));
            var doc = (Document) ((Document) unwrap(invoker.getDocument()));
            assertEquals(SafeMode.valueForName(name), doc.getSafe());
        }
    }

    @Test
    @DisplayName("should set eRuby impl if specified")
    void shouldSetERubyImplIfSpecified() throws Exception {
        var invoker = invokeCliToBuffer(list("--eruby", "erubi", "-o", "/dev/null"));
        var doc = (Document) ((Document) unwrap(invoker.getDocument()));
        assertEquals("erubi", get(doc.getOptions(), "eruby"));
    }

    @Test
    @DisplayName("should force default external encoding to UTF-8")
    void shouldForceDefaultExternalEncodingToUTF8() throws Exception {
        var inputPath = fixturePath("encoding.adoc");
        // NOTE the output is always encoded as UTF-8, regardless of the encoding of the console
        java.util.List<String> stdoutLines = runCommand(list(asciidoctorCmd(), "-o", "-", "--trace", inputPath, Options.of("env", Options.of("LANG", "US-ASCII"))), out -> out.readLines());
        refuteEmpty(stdoutLines);
        var stdoutStr = join(stdoutLines);
        assertIncludes(stdoutStr, "Codierungen sind verrückt auf älteren Versionen von Ruby");
    }

    @Test
    @Disabled("the encoding of the standard streams and Dir.home are specific to Ruby")
    @DisplayName("should force stdio encoding to UTF-8")
    void shouldForceStdioEncodingToUTF8() throws Exception {
    }

    @Test
    @Disabled("the encoding of the standard streams and Dir.home are specific to Ruby")
    @DisplayName("should not fail to load if call to Dir.home fails")
    void shouldNotFailToLoadIfCallToDirHomeFails() throws Exception {
    }

    @Test
    @DisplayName("should print timings when -t flag is specified")
    void shouldPrintTimingsWhenTFlagIsSpecified() throws Exception {
        var input = "Sample *AsciiDoc*";
        var invoker = new Ref<Invoker>(null);
        var error = new Ref<String>(null);
        try (var streams = redirectStreams()) {
            streams.out();
            var err = streams.err();
            invoker.set(invokeCli(list("-t", "-o", "/dev/null"), "-", () -> input));
            error.set(err.getString());
        }
        assertNotNull(invoker.get());
        assertNotNull(error.get());
        assertMatch(rx("Total time"), error.get());
    }

    @Test
    @DisplayName("should show timezone as UTC if system TZ is set to UTC")
    void shouldShowTimezoneAsUTCIfSystemTZIsSetToUTC() throws Exception {
        var inputPath = fixturePath("doctime-localtime.adoc");
        var output = runCommand(list(asciidoctorCmd(), "-d", "inline", "-o", "-", "-e", inputPath, Options.of("env", Options.of("TZ", "UTC", "SOURCE_DATE_EPOCH", null, "IGNORE_SOURCE_DATE_EPOCH", "1"))), out -> out.read());
        var masgn = map(slice(lines(output), -2, 2), (String it) -> Strings.chomp(it));
        var doctime = get(masgn, 0);
        var localtime = get(masgn, 1);
        assertTruthy(endsWith(doctime, " UTC"));
        assertTruthy(endsWith(localtime, " UTC"));
    }

    @Test
    @DisplayName("should show timezone as offset if system TZ is not set to UTC")
    void shouldShowTimezoneAsOffsetIfSystemTZIsNotSetToUTC() throws Exception {
        assumeTrue(!isWindows());
        var inputPath = fixturePath("doctime-localtime.adoc");
        var output = runCommand(list(asciidoctorCmd(), "-d", "inline", "-o", "-", "-e", inputPath, Options.of("env", Options.of("TZ", "EST+5", "SOURCE_DATE_EPOCH", null, "IGNORE_SOURCE_DATE_EPOCH", "1"))), out -> out.read());
        var masgn = map(slice(lines(output), -2, 2), (String it) -> Strings.chomp(it));
        var doctime = get(masgn, 0);
        var localtime = get(masgn, 1);
        assertTruthy(endsWith(doctime, " -0500"));
        assertTruthy(endsWith(localtime, " -0500"));
    }

    @Test
    @Disabled("environment variables cannot be modified in Java")
    @DisplayName("should use SOURCE_DATE_EPOCH as modified time of input file and local time")
    void shouldUseSOURCE_DATE_EPOCHAsModifiedTimeOfInputFileAndLocalTime() throws Exception {
    }

    @Test
    @Disabled("environment variables cannot be modified in Java")
    @DisplayName("should ignore SOURCE_DATE_EPOCH is value is empty")
    void shouldIgnoreSOURCE_DATE_EPOCHIsValueIsEmpty() throws Exception {
    }

    @Test
    @Disabled("environment variables cannot be modified in Java")
    @DisplayName("should fail if SOURCE_DATE_EPOCH is malformed")
    void shouldFailIfSOURCE_DATE_EPOCHIsMalformed() throws Exception {
    }
}
