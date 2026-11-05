package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.io.File;

import org.junit.jupiter.api.*;

@DisplayName("Options")
public class OptionsTest extends CliTestHelper {

    @Test
    @DisplayName("should print usage and return error code 0 when help flag is present")
    void shouldPrintUsageAndReturnErrorCode0WhenHelpFlagIsPresent() throws Exception {
        try (var streams = redirectStreams()) {
            var stdout = streams.out();
            var exitval = org.asciidoctor.cli.Options.parse(list("-h"));
            assertEquals(0, exitval);
            assertMatch(rx("^Usage:"), stdout.getString());
        }
    }

    @Test
    @DisplayName("should show safe modes in severity order")
    void shouldShowSafeModesInSeverityOrder() throws Exception {
        try (var streams = redirectStreams()) {
            var stdout = streams.out();
            var exitval = org.asciidoctor.cli.Options.parse(list("-h"));
            assertEquals(0, exitval);
            assertMatch(rx("unsafe, safe, server, secure"), stdout.getString());
        }
    }

    @Test
    @DisplayName("should print usage and return error code 0 when help flag is unknown")
    void shouldPrintUsageAndReturnErrorCode0WhenHelpFlagIsUnknown() throws Exception {
        var masgn = redirectStreams((out, ignored) -> list(org.asciidoctor.cli.Options.parse(list("-h", "unknown")), out.getString()));
        var exitval = get(masgn, 0);
        var output = get(masgn, 1);
        assertEquals(0, exitval);
        assertMatch(rx("^Usage:"), output);
    }

    @Test
    @DisplayName("should dump man page and return error code 0 when help topic is manpage")
    void shouldDumpManPageAndReturnErrorCode0WhenHelpTopicIsManpage() throws Exception {
        var masgn = redirectStreams((out, ignored) -> list(org.asciidoctor.cli.Options.parse(list("-h", "manpage")), out.getString()));
        var exitval = get(masgn, 0);
        var output = get(masgn, 1);
        assertEquals(0, exitval);
        assertIncludes(output, "Manual: Asciidoctor Manual");
        assertIncludes(output, ".TH \"ASCIIDOCTOR\"");
    }

    @Test
    @DisplayName("should an overview of the AsciiDoc syntax and return error code 0 when help topic is syntax")
    void shouldAnOverviewOfTheAsciiDocSyntaxAndReturnErrorCode0WhenHelpTopicIsSyntax() throws Exception {
        var masgn = redirectStreams((out, ignored) -> list(org.asciidoctor.cli.Options.parse(list("-h", "syntax")), out.getString()));
        var exitval = get(masgn, 0);
        var output = get(masgn, 1);
        assertEquals(0, exitval);
        assertIncludes(output, "= AsciiDoc Syntax");
        assertIncludes(output, "== Text Formatting");
    }

    @Test
    @Disabled("environment variables cannot be modified in Java")
    @DisplayName("should print message and return error code 1 when manpage is not found")
    void shouldPrintMessageAndReturnErrorCode1WhenManpageIsNotFound() throws Exception {
    }

    @Test
    @DisplayName("should return error code 1 when invalid option present")
    void shouldReturnErrorCode1WhenInvalidOptionPresent() throws Exception {
        try (var streams = redirectStreams()) {
            streams.out();
            var stderr = streams.err();
            var exitval = org.asciidoctor.cli.Options.parse(list("--foobar"));
            assertEquals(1, exitval);
            assertEquals("asciidoctor: invalid option: --foobar", Strings.chomp(stderr.getString()));
        }
    }

    @Test
    @DisplayName("should return error code 1 when option has invalid argument")
    void shouldReturnErrorCode1WhenOptionHasInvalidArgument() throws Exception {
        try (var streams = redirectStreams()) {
            streams.out();
            var stderr = streams.err();
            var exitval = org.asciidoctor.cli.Options.parse(list("-d", "chapter", "input.ad"));
            assertEquals(1, exitval);
            assertEquals("asciidoctor: invalid argument: -d chapter", Strings.chomp(stderr.getString()));
        }
    }

    @Test
    @DisplayName("should return error code 1 when option is missing required argument")
    void shouldReturnErrorCode1WhenOptionIsMissingRequiredArgument() throws Exception {
        try (var streams = redirectStreams()) {
            streams.out();
            var stderr = streams.err();
            var exitval = org.asciidoctor.cli.Options.parse(list("-b"));
            assertEquals(1, exitval);
            assertEquals("asciidoctor: option missing argument: -b", Strings.chomp(stderr.getString()));
        }
    }

    @Test
    @DisplayName("should emit warning when unparsed options remain")
    void shouldEmitWarningWhenUnparsedOptionsRemain() throws Exception {
        try (var streams = redirectStreams()) {
            streams.out();
            var stderr = streams.err();
            var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-b", "docbook", "-", "-"));
            assertInstanceOf(Map.class, options);
            assertMatch(rx("asciidoctor: WARNING: extra arguments .*"), Strings.chomp(stderr.getString()));
        }
    }

    @Test
    @DisplayName("basic argument assignment")
    void basicArgumentAssignment() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-w", "-v", "-e", "-d", "book", "src/test/resources/fixtures/sample.adoc"));
        assertEquals(2, get(options, "verbose"));
        assertFalsy(get(options, "standalone"));
        assertEquals("book", get(get(options, "attributes"), "doctype"));
        assertEquals(1, size(get(options, "input_files")));
        assertEquals("src/test/resources/fixtures/sample.adoc", get(get(options, "input_files"), 0));
    }

    @Test
    @DisplayName("supports legacy option for no header footer")
    void supportsLegacyOptionForNoHeaderFooter() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-s", "src/test/resources/fixtures/sample.adoc"));
        assertFalsy(get(options, "standalone"));
        assertEquals(1, size(get(options, "input_files")));
        assertEquals("src/test/resources/fixtures/sample.adoc", get(get(options, "input_files"), 0));
    }

    @Test
    @DisplayName("standard attribute assignment")
    void standardAttributeAssignment() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-a", "docinfosubs=attributes,replacements", "-a", "icons", "src/test/resources/fixtures/sample.adoc"));
        assertEquals("attributes,replacements", get(get(options, "attributes"), "docinfosubs"));
        assertEquals("", get(get(options, "attributes"), "icons"));
    }

    @Test
    @DisplayName("multiple attribute arguments")
    void multipleAttributeArguments() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-a", "imagesdir=images", "-a", "icons", "src/test/resources/fixtures/sample.adoc"));
        assertEquals("images", get(get(options, "attributes"), "imagesdir"));
        assertEquals("", get(get(options, "attributes"), "icons"));
    }

    @Test
    @DisplayName("should only split attribute key/value pairs on first equal sign")
    void shouldOnlySplitAttributeKeyValuePairsOnFirstEqualSign() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-a", "name=value=value", "src/test/resources/fixtures/sample.adoc"));
        assertEquals("value=value", get(get(options, "attributes"), "name"));
    }

    @Test
    @DisplayName("should not fail if value of attribute option is empty")
    void shouldNotFailIfValueOfAttributeOptionIsEmpty() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-a", "", "src/test/resources/fixtures/sample.adoc"));
        assertNull(get(options, "attributes"));
    }

    @Test
    @DisplayName("should not fail if value of attribute option is equal sign")
    void shouldNotFailIfValueOfAttributeOptionIsEqualSign() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-a", "=", "src/test/resources/fixtures/sample.adoc"));
        assertNull(get(options, "attributes"));
    }

    @Test
    @Disabled("Java strings are always Unicode")
    @DisplayName("should gracefully force encoding to UTF-8 if encoding on string is mislabeled")
    void shouldGracefullyForceEncodingToUTF8IfEncodingOnStringIsMislabeled() throws Exception {
    }

    @Test
    @DisplayName("should allow safe mode to be specified")
    void shouldAllowSafeModeToBeSpecified() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-S", "safe", "src/test/resources/fixtures/sample.adoc"));
        assertEquals(SafeMode.SAFE.getLevel(), get(options, "safe"));
    }

    @Test
    @DisplayName("should allow any backend to be specified")
    void shouldAllowAnyBackendToBeSpecified() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-b", "my_custom_backend", "src/test/resources/fixtures/sample.adoc"));
        assertEquals("my_custom_backend", get(get(options, "attributes"), "backend"));
    }

    @Test
    @DisplayName("article doctype assignment")
    void articleDoctypeAssignment() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-d", "article", "src/test/resources/fixtures/sample.adoc"));
        assertEquals("article", get(get(options, "attributes"), "doctype"));
    }

    @Test
    @DisplayName("book doctype assignment")
    void bookDoctypeAssignment() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-d", "book", "src/test/resources/fixtures/sample.adoc"));
        assertEquals("book", get(get(options, "attributes"), "doctype"));
    }

    @Test
    @DisplayName("inline doctype assignment")
    void inlineDoctypeAssignment() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-d", "inline", "src/test/resources/fixtures/sample.adoc"));
        assertEquals("inline", get(get(options, "attributes"), "doctype"));
    }

    @Test
    @DisplayName("template engine assignment")
    void templateEngineAssignment() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-E", "haml", "src/test/resources/fixtures/sample.adoc"));
        assertEquals("haml", get(options, "template_engine"));
    }

    @Test
    @DisplayName("template directory assignment")
    void templateDirectoryAssignment() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-T", "custom-backend", "src/test/resources/fixtures/sample.adoc"));
        assertEquals(list("custom-backend"), get(options, "template_dirs"));
    }

    @Test
    @DisplayName("multiple template directory assignments")
    void multipleTemplateDirectoryAssignments() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-T", "custom-backend", "-T", "custom-backend-hacks", "src/test/resources/fixtures/sample.adoc"));
        assertEquals(list("custom-backend", "custom-backend-hacks"), get(options, "template_dirs"));
    }

    @Test
    @DisplayName("multiple -r flags requires specified libraries")
    void multipleRFlagsRequiresSpecifiedLibraries() throws Exception {
        var options = new org.asciidoctor.cli.Options();
        try (var streams = redirectStreams()) {
            streams.out();
            var stderr = streams.err();
            var exitval = options.parseArgs(list("-r", "foobar", "-r", "foobaz", "src/test/resources/fixtures/sample.adoc"));
            assertMatch("asciidoctor: FAILED: 'foobar' could not be loaded", stderr.getString());
            assertEquals(1, exitval);
            assertEquals(list("foobar", "foobaz"), get(options, "requires"));
        }
    }

    @Test
    @DisplayName("-r flag with multiple values separated by commas is treated as a single path")
    void rFlagWithMultipleValuesSeparatedByCommasIsTreatedAsASinglePath() throws Exception {
        var options = new org.asciidoctor.cli.Options();
        try (var streams = redirectStreams()) {
            streams.out();
            var stderr = streams.err();
            var exitval = options.parseArgs(list("-r", "/no-such-folder/a,b,c/ext.rb", "src/test/resources/fixtures/sample.adoc"));
            assertInclude("asciidoctor: FAILED: '/no-such-folder/a,b,c/ext.rb' could not be loaded", stderr.getString());
            assertEquals(1, exitval);
            assertEquals(list("/no-such-folder/a,b,c/ext.rb"), get(options, "requires"));
        }
    }

    @Test
    @DisplayName("-I option appends paths to $LOAD_PATH")
    void iOptionAppendsPathsToLOAD_PATH() throws Exception {
        // NOTE in Java, the load paths are added to the class path used to load the required classes
        var options = new org.asciidoctor.cli.Options();
        var exitval = options.parseArgs(list("-I", "foobar", "-I", "foobaz", "src/test/resources/fixtures/sample.adoc"));
        assertNotEquals(1, exitval);
        assertEquals(list("foobar", "foobaz"), get(options, "load_paths"));
    }

    @Test
    @DisplayName("-I option appends multiple paths to $LOAD_PATH")
    void iOptionAppendsMultiplePathsToLOAD_PATH() throws Exception {
        // NOTE in Java, the load paths are added to the class path used to load the required classes
        var options = new org.asciidoctor.cli.Options();
        var exitval = options.parseArgs(list("-I", "foobar" + File.pathSeparator + "foobaz", "src/test/resources/fixtures/sample.adoc"));
        assertNotEquals(1, exitval);
        assertEquals(list("foobar", "foobaz"), get(options, "load_paths"));
    }

    @Test
    @DisplayName("should set failure level to FATAL by default")
    void shouldSetFailureLevelToFATALByDefault() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("src/test/resources/fixtures/sample.adoc"));
        assertEquals(org.asciidoctor.log.Severity.FATAL, get(options, "failure_level"));
    }

    @Test
    @DisplayName("should allow failure level to be set to FATAL using any recognized abbreviation")
    void shouldAllowFailureLevelToBeSetToFATALUsingAnyRecognizedAbbreviation() throws Exception {
        for (var val : list("f", "fatal", "FATAL")) {
            var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list(("--failure-level=" + str(val)), "src/test/resources/fixtures/sample.adoc"));
            assertEquals(org.asciidoctor.log.Severity.FATAL, get(options, "failure_level"));
        }
    }

    @Test
    @DisplayName("should allow failure level to be set to ERROR using any recognized abbreviation")
    void shouldAllowFailureLevelToBeSetToERRORUsingAnyRecognizedAbbreviation() throws Exception {
        for (var val : list("e", "err", "ERR", "error", "ERROR")) {
            var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list(("--failure-level=" + str(val)), "src/test/resources/fixtures/sample.adoc"));
            assertEquals(org.asciidoctor.log.Severity.ERROR, get(options, "failure_level"));
        }
    }

    @Test
    @DisplayName("should allow failure level to be set to WARN using any recognized abbreviation")
    void shouldAllowFailureLevelToBeSetToWARNUsingAnyRecognizedAbbreviation() throws Exception {
        for (var val : list("w", "warn", "WARN", "warning", "WARNING")) {
            var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list(("--failure-level=" + str(val)), "src/test/resources/fixtures/sample.adoc"));
            assertEquals(org.asciidoctor.log.Severity.WARN, get(options, "failure_level"));
        }
    }

    @Test
    @DisplayName("should not allow failure level to be set to unknown value")
    void shouldNotAllowFailureLevelToBeSetToUnknownValue() throws Exception {
        var masgn = redirectStreams((ignored, err) -> list(org.asciidoctor.cli.Options.parse(list("--failure-level=foobar", "src/test/resources/fixtures/sample.adoc")), err.getString()));
        var exitCode = get(masgn, 0);
        var messages = get(masgn, 1);
        assertEquals(1, exitCode);
        assertIncludes(messages, "invalid argument: --failure-level=foobar");
    }

    @Test
    @DisplayName("should set log level to DEBUG when --log-level option is specified")
    void shouldSetLogLevelToDEBUGWhenLogLevelOptionIsSpecified() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("--log-level", "debug", "src/test/resources/fixtures/sample.adoc"));
        assertEquals(org.asciidoctor.log.Severity.DEBUG, get(options, "log_level"));
    }

    @Test
    @DisplayName("should allow log level to be set to WARN using any recognized abbreviation")
    void shouldAllowLogLevelToBeSetToWARNUsingAnyRecognizedAbbreviation() throws Exception {
        for (var val : list("w", "warn", "WARN", "warning", "WARNING")) {
            var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list(("--log-level=" + str(val)), "src/test/resources/fixtures/sample.adoc"));
            assertEquals(org.asciidoctor.log.Severity.WARN, get(options, "log_level"));
        }
    }

    @Test
    @DisplayName("should set verbose to 2 when -v flag is specified")
    void shouldSetVerboseTo2WhenVFlagIsSpecified() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-v", "src/test/resources/fixtures/sample.adoc"));
        assertEquals(2, get(options, "verbose"));
    }

    @Test
    @DisplayName("should set verbose to 0 when -q flag is specified")
    void shouldSetVerboseTo0WhenQFlagIsSpecified() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-q", "src/test/resources/fixtures/sample.adoc"));
        assertEquals(0, get(options, "verbose"));
    }

    @Test
    @DisplayName("should set verbose to 2 when -v flag is specified after -q flag")
    void shouldSetVerboseTo2WhenVFlagIsSpecifiedAfterQFlag() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-q", "-v", "src/test/resources/fixtures/sample.adoc"));
        assertEquals(2, get(options, "verbose"));
    }

    @Test
    @DisplayName("should set verbose to 0 when -q flag is specified after -v flag")
    void shouldSetVerboseTo0WhenQFlagIsSpecifiedAfterVFlag() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-v", "-q", "src/test/resources/fixtures/sample.adoc"));
        assertEquals(0, get(options, "verbose"));
    }

    @Test
    @DisplayName("should enable warnings when -w flag is specified")
    void shouldEnableWarningsWhenWFlagIsSpecified() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-w", "src/test/resources/fixtures/sample.adoc"));
        assertTruthy(get(options, "warnings"));
    }

    @Test
    @DisplayName("should enable timings when -t flag is specified")
    void shouldEnableTimingsWhenTFlagIsSpecified() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-t", "src/test/resources/fixtures/sample.adoc"));
        assertTruthy(get(options, "timings"));
    }

    @Test
    @DisplayName("timings option is disabled by default")
    void timingsOptionIsDisabledByDefault() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("src/test/resources/fixtures/sample.adoc"));
        assertFalsy(get(options, "timings"));
    }

    @Test
    @DisplayName("should enable sourcemap when --sourcemap flag is specified")
    void shouldEnableSourcemapWhenSourcemapFlagIsSpecified() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("--sourcemap", "src/test/resources/fixtures/sample.adoc"));
        assertTruthy(get(options, "sourcemap"));
    }

    @Test
    @DisplayName("sourcemap option is disabled by default")
    void sourcemapOptionIsDisabledByDefault() throws Exception {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("src/test/resources/fixtures/sample.adoc"));
        assertFalsy(get(options, "sourcemap"));
    }
}
