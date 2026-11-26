package org.asciidoctor.core;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.condition.EnabledInNativeImage;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.asciidoctor.core.Fixtures.relativeFixturePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class OptionsTest extends CliTestHelper {

    @Test
    @DisplayName("should print usage and return error code 0 when help flag is present")
    void shouldPrintUsageAndReturnErrorCode0WhenHelpFlagIsPresent() {
        try (var streams = redirectStreams()) {
            var stdout = streams.out();
            var exitval = org.asciidoctor.cli.Options.parse(list("-h"));
            assertEquals(0, exitval);
            assertMatch(rx("^Usage:"), stdout.getString());
        }
    }

    @Test
    @DisplayName("should show safe modes in severity order")
    void shouldShowSafeModesInSeverityOrder() {
        try (var streams = redirectStreams()) {
            var stdout = streams.out();
            var exitval = org.asciidoctor.cli.Options.parse(list("-h"));
            assertEquals(0, exitval);
            assertMatch(rx("unsafe, safe, server, secure"), stdout.getString());
        }
    }

    @Test
    @DisplayName("should print usage and return error code 0 when help flag is unknown")
    void shouldPrintUsageAndReturnErrorCode0WhenHelpFlagIsUnknown() {
        var masgn = redirectStreams((out, ignored) -> list(org.asciidoctor.cli.Options.parse(list("-h", "unknown")), out.getString()));
        var exitval = get(masgn, 0);
        var output = get(masgn, 1);
        assertEquals(0, exitval);
        assertMatch(rx("^Usage:"), output);
    }

    @Test
    @DisplayName("should dump man page and return error code 0 when help topic is manpage")
    void shouldDumpManPageAndReturnErrorCode0WhenHelpTopicIsManpage() {
        var masgn = redirectStreams((out, ignored) -> list(org.asciidoctor.cli.Options.parse(list("-h", "manpage")), out.getString()));
        var exitval = get(masgn, 0);
        var output = get(masgn, 1);
        assertEquals(0, exitval);
        assertIncludes(output, "Manual: Asciidoctor Manual");
        assertIncludes(output, ".TH \"ASCIIDOCTOR\"");
    }

    @Test
    @DisplayName("should an overview of the AsciiDoc syntax and return error code 0 when help topic is syntax")
    void shouldAnOverviewOfTheAsciiDocSyntaxAndReturnErrorCode0WhenHelpTopicIsSyntax() {
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
    void shouldPrintMessageAndReturnErrorCode1WhenManpageIsNotFound() {
    }

    @Test
    @DisplayName("should return error code 1 when invalid option present")
    void shouldReturnErrorCode1WhenInvalidOptionPresent() {
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
    void shouldReturnErrorCode1WhenOptionHasInvalidArgument() {
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
    void shouldReturnErrorCode1WhenOptionIsMissingRequiredArgument() {
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
    void shouldEmitWarningWhenUnparsedOptionsRemain() {
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
    void basicArgumentAssignment() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-w", "-v", "-e", "-d", "book", relativeFixturePath("sample.adoc")));
        assertEquals(2, get(options, "verbose"));
        assertFalsy(get(options, "standalone"));
        assertEquals("book", get(get(options, "attributes"), "doctype"));
        assertEquals(1, size(get(options, "input_files")));
        assertEquals(relativeFixturePath("sample.adoc"), get(get(options, "input_files"), 0));
    }

    @Test
    @DisplayName("supports legacy option for no header footer")
    void supportsLegacyOptionForNoHeaderFooter() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-s", relativeFixturePath("sample.adoc")));
        assertFalsy(get(options, "standalone"));
        assertEquals(1, size(get(options, "input_files")));
        assertEquals(relativeFixturePath("sample.adoc"), get(get(options, "input_files"), 0));
    }

    @Test
    @DisplayName("standard attribute assignment")
    void standardAttributeAssignment() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-a", "docinfosubs=attributes,replacements", "-a", "icons", relativeFixturePath("sample.adoc")));
        assertEquals("attributes,replacements", get(get(options, "attributes"), "docinfosubs"));
        assertEquals("", get(get(options, "attributes"), "icons"));
    }

    @Test
    @DisplayName("multiple attribute arguments")
    void multipleAttributeArguments() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-a", "imagesdir=images", "-a", "icons", relativeFixturePath("sample.adoc")));
        assertEquals("images", get(get(options, "attributes"), "imagesdir"));
        assertEquals("", get(get(options, "attributes"), "icons"));
    }

    @Test
    @DisplayName("should only split attribute key/value pairs on first equal sign")
    void shouldOnlySplitAttributeKeyValuePairsOnFirstEqualSign() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-a", "name=value=value", relativeFixturePath("sample.adoc")));
        assertEquals("value=value", get(get(options, "attributes"), "name"));
    }

    @Test
    @DisplayName("should not fail if value of attribute option is empty")
    void shouldNotFailIfValueOfAttributeOptionIsEmpty() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-a", "", relativeFixturePath("sample.adoc")));
        assertNull(get(options, "attributes"));
    }

    @Test
    @DisplayName("should not fail if value of attribute option is equal sign")
    void shouldNotFailIfValueOfAttributeOptionIsEqualSign() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-a", "=", relativeFixturePath("sample.adoc")));
        assertNull(get(options, "attributes"));
    }

    @Test
    @Disabled("Java strings are always Unicode")
    @DisplayName("should gracefully force encoding to UTF-8 if encoding on string is mislabeled")
    void shouldGracefullyForceEncodingToUTF8IfEncodingOnStringIsMislabeled() {
    }

    @Test
    @DisplayName("should allow safe mode to be specified")
    void shouldAllowSafeModeToBeSpecified() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-S", "safe", relativeFixturePath("sample.adoc")));
        assertEquals(SafeMode.SAFE.getLevel(), get(options, "safe"));
    }

    @Test
    @DisplayName("should allow any backend to be specified")
    void shouldAllowAnyBackendToBeSpecified() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-b", "my_custom_backend", relativeFixturePath("sample.adoc")));
        assertEquals("my_custom_backend", get(get(options, "attributes"), "backend"));
    }

    @Test
    @DisplayName("article doctype assignment")
    void articleDoctypeAssignment() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-d", "article", relativeFixturePath("sample.adoc")));
        assertEquals("article", get(get(options, "attributes"), "doctype"));
    }

    @Test
    @DisplayName("book doctype assignment")
    void bookDoctypeAssignment() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-d", "book", relativeFixturePath("sample.adoc")));
        assertEquals("book", get(get(options, "attributes"), "doctype"));
    }

    @Test
    @DisplayName("inline doctype assignment")
    void inlineDoctypeAssignment() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-d", "inline", relativeFixturePath("sample.adoc")));
        assertEquals("inline", get(get(options, "attributes"), "doctype"));
    }

    @Test
    @DisplayName("template engine assignment")
    void templateEngineAssignment() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-E", "haml", relativeFixturePath("sample.adoc")));
        assertEquals("haml", get(options, "template_engine"));
    }

    @Test
    @DisplayName("template directory assignment")
    void templateDirectoryAssignment() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-T", "custom-backend", relativeFixturePath("sample.adoc")));
        assertEquals(list("custom-backend"), get(options, "template_dirs"));
    }

    @Test
    @DisplayName("multiple template directory assignments")
    void multipleTemplateDirectoryAssignments() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-T", "custom-backend", "-T", "custom-backend-hacks", relativeFixturePath("sample.adoc")));
        assertEquals(list("custom-backend", "custom-backend-hacks"), get(options, "template_dirs"));
    }

    // The native executable can only load the classes it contains: the option -r fails for any other class
    @Nested
    @DisplayName("-r option")
    class RequireOption {

        @Test
        @DisplayName("multiple -r flags requires specified libraries")
        void multipleRFlagsRequiresSpecifiedLibraries() {
            var options = new org.asciidoctor.cli.Options();
            try (var streams = redirectStreams()) {
                streams.out();
                var stderr = streams.err();
                var exitval = options.parseArgs(list("-r", "foobar", "-r", "foobaz", relativeFixturePath("sample.adoc")));
                assertMatch("asciidoctor: FAILED: 'foobar' could not be loaded", stderr.getString());
                assertEquals(1, exitval);
                assertEquals(list("foobar", "foobaz"), get(options, "requires"));
            }
        }

        @Test
        @DisplayName("-r flag with multiple values separated by commas is treated as a single path")
        void rFlagWithMultipleValuesSeparatedByCommasIsTreatedAsASinglePath() {
            var options = new org.asciidoctor.cli.Options();
            try (var streams = redirectStreams()) {
                streams.out();
                var stderr = streams.err();
                var exitval = options.parseArgs(list("-r", "/no-such-folder/a,b,c/ext.rb", relativeFixturePath("sample.adoc")));
                assertInclude("asciidoctor: FAILED: '/no-such-folder/a,b,c/ext.rb' could not be loaded", stderr.getString());
                assertEquals(1, exitval);
                assertEquals(list("/no-such-folder/a,b,c/ext.rb"), get(options, "requires"));
            }
        }

        @Test
        @EnabledInNativeImage
        @DisplayName("-r flag fails in a native image when the class is not in the executable")
        void rFlagFailsInNativeImageWhenTheClassIsNotInTheExecutable() {
            var options = new org.asciidoctor.cli.Options();
            try (var streams = redirectStreams()) {
                streams.out();
                var stderr = streams.err();
                var exitval = options.parseArgs(list("-r", "foobar", relativeFixturePath("sample.adoc")));
                assertThat(exitval).isEqualTo(1);
                assertThat(stderr.getString()).contains(
                        "asciidoctor: FAILED: 'foobar' could not be loaded",
                        "The native executable can only load the classes it contains; run AsciidoctorJ with Java to load other classes",
                        "Use --trace to show backtrace");
                assertThat(TestHelper.<List<String>>get(options, "requires")).containsExactly("foobar");
            }
        }
    }

    // The native executable can't load classes at runtime: the option -I fails in a native image
    @Nested
    @DisplayName("-I option")
    class LoadPathOption {

        @Test
        // The native executable fails when the class path is not empty (see the native-only tests)as
        @DisabledInNativeImage
        @DisplayName("-I option appends paths to $LOAD_PATH")
        void iOptionAppendsPathsToLOAD_PATH() {
            // NOTE in Java, the load paths are added to the class path used to load the required classes
            var options = new org.asciidoctor.cli.Options();
            var exitval = options.parseArgs(list("-I", "foobar", "-I", "foobaz", relativeFixturePath("sample.adoc")));
            assertThat(exitval).isNotEqualTo(1);
            assertThat(TestHelper.<List<String>>get(options, "load_paths")).containsExactly("foobar", "foobaz");
        }

        @Test
        // The native executable fails when the class path is not empty (see the native-only tests)
        @DisabledInNativeImage
        @DisplayName("-I option appends multiple paths to $LOAD_PATH")
        void iOptionAppendsMultiplePathsToLOAD_PATH() {
            // NOTE in Java, the load paths are added to the class path used to load the required classes
            var options = new org.asciidoctor.cli.Options();
            var exitval = options.parseArgs(list("-I", "foobar" + File.pathSeparator + "foobaz", relativeFixturePath("sample.adoc")));
            assertThat(exitval).isNotEqualTo(1);
            assertThat(TestHelper.<List<String>>get(options, "load_paths")).containsExactly("foobar", "foobaz");
        }

        @Test
        @EnabledInNativeImage
        @DisplayName("-I option fails in a native image, which can't load classes at runtime")
        void iOptionFailsInNativeImage() {
            var options = new org.asciidoctor.cli.Options();
            try (var streams = redirectStreams()) {
                streams.out();
                var stderr = streams.err();
                var exitval = options.parseArgs(list("-I", "foobar", "-I", "foobaz", relativeFixturePath("sample.adoc")));
                assertThat(exitval).isEqualTo(1);
                assertThat(stderr.getString()).contains("asciidoctor: FAILED: the native executable can't load classes at runtime (-I DIRECTORY, -r FILE.jar)");
                assertThat(TestHelper.<List<String>>get(options, "load_paths")).containsExactly("foobar", "foobaz");
            }
        }

        @Test
        @EnabledInNativeImage
        @DisplayName("-I option with multiple paths fails in a native image, which can't load classes at runtime")
        void iOptionWithMultiplePathsFailsInNativeImage() {
            var options = new org.asciidoctor.cli.Options();
            try (var streams = redirectStreams()) {
                streams.out();
                var stderr = streams.err();
                var exitval = options.parseArgs(list("-I", "foobar" + File.pathSeparator + "foobaz", relativeFixturePath("sample.adoc")));
                assertThat(exitval).isEqualTo(1);
                assertThat(stderr.getString()).contains("asciidoctor: FAILED: the native executable can't load classes at runtime (-I DIRECTORY, -r FILE.jar)");
                assertThat(TestHelper.<List<String>>get(options, "load_paths")).containsExactly("foobar", "foobaz");
            }
        }
    }

    @Test
    @DisplayName("should set failure level to FATAL by default")
    void shouldSetFailureLevelToFATALByDefault() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list(relativeFixturePath("sample.adoc")));
        assertEquals(org.asciidoctor.log.Severity.FATAL, get(options, "failure_level"));
    }

    @Test
    @DisplayName("should allow failure level to be set to FATAL using any recognized abbreviation")
    void shouldAllowFailureLevelToBeSetToFATALUsingAnyRecognizedAbbreviation() {
        for (var val : list("f", "fatal", "FATAL")) {
            var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list(("--failure-level=" + str(val)), relativeFixturePath("sample.adoc")));
            assertEquals(org.asciidoctor.log.Severity.FATAL, get(options, "failure_level"));
        }
    }

    @Test
    @DisplayName("should allow failure level to be set to ERROR using any recognized abbreviation")
    void shouldAllowFailureLevelToBeSetToERRORUsingAnyRecognizedAbbreviation() {
        for (var val : list("e", "err", "ERR", "error", "ERROR")) {
            var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list(("--failure-level=" + str(val)), relativeFixturePath("sample.adoc")));
            assertEquals(org.asciidoctor.log.Severity.ERROR, get(options, "failure_level"));
        }
    }

    @Test
    @DisplayName("should allow failure level to be set to WARN using any recognized abbreviation")
    void shouldAllowFailureLevelToBeSetToWARNUsingAnyRecognizedAbbreviation() {
        for (var val : list("w", "warn", "WARN", "warning", "WARNING")) {
            var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list(("--failure-level=" + str(val)), relativeFixturePath("sample.adoc")));
            assertEquals(org.asciidoctor.log.Severity.WARN, get(options, "failure_level"));
        }
    }

    @Test
    @DisplayName("should not allow failure level to be set to unknown value")
    void shouldNotAllowFailureLevelToBeSetToUnknownValue() {
        var masgn = redirectStreams((ignored, err) -> list(org.asciidoctor.cli.Options.parse(list("--failure-level=foobar", relativeFixturePath("sample.adoc"))), err.getString()));
        var exitCode = get(masgn, 0);
        var messages = get(masgn, 1);
        assertEquals(1, exitCode);
        assertIncludes(messages, "invalid argument: --failure-level=foobar");
    }

    @Test
    @DisplayName("should set log level to DEBUG when --log-level option is specified")
    void shouldSetLogLevelToDEBUGWhenLogLevelOptionIsSpecified() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("--log-level", "debug", relativeFixturePath("sample.adoc")));
        assertEquals(org.asciidoctor.log.Severity.DEBUG, get(options, "log_level"));
    }

    @Test
    @DisplayName("should allow log level to be set to WARN using any recognized abbreviation")
    void shouldAllowLogLevelToBeSetToWARNUsingAnyRecognizedAbbreviation() {
        for (var val : list("w", "warn", "WARN", "warning", "WARNING")) {
            var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list(("--log-level=" + str(val)), relativeFixturePath("sample.adoc")));
            assertEquals(org.asciidoctor.log.Severity.WARN, get(options, "log_level"));
        }
    }

    @Test
    @DisplayName("should set verbose to 2 when -v flag is specified")
    void shouldSetVerboseTo2WhenVFlagIsSpecified() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-v", relativeFixturePath("sample.adoc")));
        assertEquals(2, get(options, "verbose"));
    }

    @Test
    @DisplayName("should set verbose to 0 when -q flag is specified")
    void shouldSetVerboseTo0WhenQFlagIsSpecified() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-q", relativeFixturePath("sample.adoc")));
        assertEquals(0, get(options, "verbose"));
    }

    @Test
    @DisplayName("should set verbose to 2 when -v flag is specified after -q flag")
    void shouldSetVerboseTo2WhenVFlagIsSpecifiedAfterQFlag() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-q", "-v", relativeFixturePath("sample.adoc")));
        assertEquals(2, get(options, "verbose"));
    }

    @Test
    @DisplayName("should set verbose to 0 when -q flag is specified after -v flag")
    void shouldSetVerboseTo0WhenQFlagIsSpecifiedAfterVFlag() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-v", "-q", relativeFixturePath("sample.adoc")));
        assertEquals(0, get(options, "verbose"));
    }

    @Test
    @DisplayName("should enable warnings when -w flag is specified")
    void shouldEnableWarningsWhenWFlagIsSpecified() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-w", relativeFixturePath("sample.adoc")));
        assertTruthy(get(options, "warnings"));
    }

    @Test
    @DisplayName("should enable timings when -t flag is specified")
    void shouldEnableTimingsWhenTFlagIsSpecified() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("-t", relativeFixturePath("sample.adoc")));
        assertTruthy(get(options, "timings"));
    }

    @Test
    @DisplayName("timings option is disabled by default")
    void timingsOptionIsDisabledByDefault() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list(relativeFixturePath("sample.adoc")));
        assertFalsy(get(options, "timings"));
    }

    @Test
    @DisplayName("should enable sourcemap when --sourcemap flag is specified")
    void shouldEnableSourcemapWhenSourcemapFlagIsSpecified() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list("--sourcemap", relativeFixturePath("sample.adoc")));
        assertTruthy(get(options, "sourcemap"));
    }

    @Test
    @DisplayName("sourcemap option is disabled by default")
    void sourcemapOptionIsDisabledByDefault() {
        var options = (org.asciidoctor.cli.Options) org.asciidoctor.cli.Options.parse(list(relativeFixturePath("sample.adoc")));
        assertFalsy(get(options, "sourcemap"));
    }
}
