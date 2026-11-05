package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

@DisplayName("Helpers")
public class HelpersTest extends TestHelper {

    @Nested
    @DisplayName("URI Encoding")
    class URIEncoding {

        @Test
        @DisplayName("should URI encode non-word characters generally")
        void shouldURIEncodeNonWordCharactersGenerally() throws Exception {
            var given = " !*/%&?\\=";
            var expect = "%20%21%2A%2F%25%26%3F%5C%3D";
            assertEquals(expect, Helpers.encodeUriComponent(given));
        }

        @Test
        @DisplayName("should not URI encode select non-word characters")
        void shouldNotURIEncodeSelectNonWordCharacters() throws Exception {
            var given = "-.";
            var expect = given;
            assertEquals(expect, Helpers.encodeUriComponent(given));
        }
    }

    @Nested
    @DisplayName("URIs and Paths")
    class URIsAndPaths {

        @Test
        @DisplayName("rootname should return file name without extension")
        void rootnameShouldReturnFileNameWithoutExtension() throws Exception {
            assertEquals("main", Helpers.rootname("main.adoc"));
            assertEquals("docs/main", Helpers.rootname("docs/main.adoc"));
        }

        @Test
        @DisplayName("rootname should file name if it has no extension")
        void rootnameShouldFileNameIfItHasNoExtension() throws Exception {
            assertEquals("main", Helpers.rootname("main"));
            assertEquals("docs/main", Helpers.rootname("docs/main"));
        }

        @Test
        @DisplayName("rootname should ignore dot not in last segment")
        void rootnameShouldIgnoreDotNotInLastSegment() throws Exception {
            assertEquals("include.d/main", Helpers.rootname("include.d/main"));
            assertEquals("include.d/main", Helpers.rootname("include.d/main.adoc"));
        }

        @Test
        @DisplayName("extname? should return whether path contains an extname")
        void extnameShouldReturnWhetherPathContainsAnExtname() throws Exception {
            assertTruthy(Helpers.hasExtname("document.adoc"));
            assertTruthy(Helpers.hasExtname("path/to/document.adoc"));
            assertFalse(Helpers.hasExtname("basename"));
            assertFalsy(Helpers.hasExtname("include.d/basename"));
        }

        @Test
        @DisplayName("UriSniffRx should detect URIs")
        void urisniffrxShouldDetectURIs() throws Exception {
            assertMatch(Rx.URI_SNIFF_RX, "http://example.com");
            assertMatch(Rx.URI_SNIFF_RX, "https://example.com");
            assertMatch(Rx.URI_SNIFF_RX, "data:image/gif;base64,R0lGODlhAQABAIAAAAUEBAAAACwAAAAAAQABAAACAkQBADs=");
        }

        @Test
        @DisplayName("UriSniffRx should not detect an absolute Windows path as a URI")
        void urisniffrxShouldNotDetectAnAbsoluteWindowsPathAsAURI() throws Exception {
            refuteMatch(Rx.URI_SNIFF_RX, "c:/sample.adoc");
            refuteMatch(Rx.URI_SNIFF_RX, "c:\\sample.adoc");
        }

        @Test
        @DisplayName("uriish? should not detect a classloader path as a URI on JRuby")
        void uriishShouldNotDetectAClassloaderPathAsAURIOnJRuby() throws Exception {
            var input = "uri:classloader:/sample.png";
            assertMatch(Rx.URI_SNIFF_RX, input);
            if (isJruby()) {
                assertFalsy(Helpers.isUriish(input));
            } else {
                assertTruthy(Helpers.isUriish(input));
            }
        }

        @Test
        @DisplayName("UriSniffRx should not detect URI that does not start on first line")
        void urisniffrxShouldNotDetectURIThatDoesNotStartOnFirstLine() throws Exception {
            refuteMatch(Rx.URI_SNIFF_RX, "text\nhttps://example.org");
        }
    }

    @Nested
    @DisplayName("Type Resolution")
    class TypeResolution {

        @Test
        @DisplayName("should get class for top-level class name")
        void shouldGetClassForTopLevelClassName() throws Exception {
            var clazz = Helpers.classForName("java.lang.String");
            assertNotNull(clazz);
            assertEquals(String.class, clazz);
        }

        @Test
        @DisplayName("should get class for class name in module")
        void shouldGetClassForClassNameInModule() throws Exception {
            var clazz = Helpers.classForName("org.asciidoctor.core.Document");
            assertNotNull(clazz);
            assertEquals(Document.class, clazz);
        }

        @Test
        @DisplayName("should get class for class name resolved from root")
        void shouldGetClassForClassNameResolvedFromRoot() throws Exception {
            var clazz = Helpers.classForName("org.asciidoctor.core.Document");
            assertNotNull(clazz);
            assertEquals(Document.class, clazz);
        }

        @Test
        @DisplayName("should raise exception if cannot find class for name")
        void shouldRaiseExceptionIfCannotFindClassForName() throws Exception {
            var ex = assertThrows(IllegalArgumentException.class, () -> Helpers.classForName("InvalidModule::InvalidClass"));
            assertMatch(rx("^Could not resolve class for name: InvalidModule::InvalidClass$"), ex.getMessage());
        }

        @Test
        @DisplayName("should raise exception if constant name is invalid")
        void shouldRaiseExceptionIfConstantNameIsInvalid() throws Exception {
            var ex = assertThrows(IllegalArgumentException.class, () -> Helpers.classForName("foobar"));
            assertMatch(rx("^Could not resolve class for name: foobar$"), ex.getMessage());
        }

        @Test
        @DisplayName("should raise exception if class not found in scope")
        void shouldRaiseExceptionIfClassNotFoundInScope() throws Exception {
            var ex = assertThrows(IllegalArgumentException.class, () -> Helpers.classForName("org.asciidoctor.core.extension.String"));
            assertMatch(rx("^Could not resolve class for name: org.asciidoctor.core.extension.String"), ex.getMessage());
        }

        @Test
        @DisplayName("should raise exception if name resolves to module")
        void shouldRaiseExceptionIfNameResolvesToModule() throws Exception {
            var ex = assertThrows(IllegalArgumentException.class, () -> Helpers.classForName("org.asciidoctor.core.Logging"));
            assertMatch(rx("^Could not resolve class for name: org.asciidoctor.core.Logging"), ex.getMessage());
        }

        @Test
        @DisplayName("should resolve class if class is given")
        void shouldResolveClassIfClassIsGiven() throws Exception {
            var clazz = Helpers.resolveClass(Document.class);
            assertNotNull(clazz);
            assertEquals(Document.class, clazz);
        }

        @Test
        @DisplayName("should resolve class if class from string")
        void shouldResolveClassIfClassFromString() throws Exception {
            var clazz = Helpers.resolveClass("org.asciidoctor.core.Document");
            assertNotNull(clazz);
            assertEquals(Document.class, clazz);
        }

        @Test
        @DisplayName("should not resolve class if not in scope")
        void shouldNotResolveClassIfNotInScope() throws Exception {
            var ex = assertThrows(IllegalArgumentException.class, () -> Helpers.resolveClass("org.asciidoctor.core.extension.String"));
            assertMatch(rx("^Could not resolve class for name: org.asciidoctor.core.extension.String$"), ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Require Library")
    class RequireLibrary {

        @Test
        @Disabled("gems are specific to the Ruby implementation")
        @DisplayName("should include backtrace in LoadError thrown by Helpers.require_library")
        void shouldIncludeBacktraceInLoadErrorThrownByHelpersRequire_library() throws Exception {
        }
    }

    @Nested
    @DisplayName("Roman Numeral Conversion")
    class RomanNumeralConversion {

        @Test
        @DisplayName("should convert integer to roman numeral")
        void shouldConvertIntegerToRomanNumeral() throws Exception {
            assertEquals("I", Helpers.intToRoman(1));
            assertEquals("IV", Helpers.intToRoman(4));
            assertEquals("LXIV", Helpers.intToRoman(64));
        }

        @Test
        @DisplayName("should convert roman numeral to integer")
        void shouldConvertRomanNumeralToInteger() throws Exception {
            assertEquals(1, Helpers.romanToInt("I"));
            assertEquals(4, Helpers.romanToInt("IV"));
            assertEquals(64, Helpers.romanToInt("LXIV"));
        }
    }
}
