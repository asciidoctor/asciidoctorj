package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.junit.jupiter.api.*;

@DisplayName("Attributes")
public class AttributesTest extends TestHelper {
    MemoryLogger logger;
    Logger defaultLogger = LoggerManager.getLogger();

    @BeforeEach
    void setup() throws Exception {
        LoggerManager.setLogger((logger = new MemoryLogger()));
    }

    @AfterEach
    void teardown() throws Exception {
        LoggerManager.setLogger(defaultLogger);
    }

    @Nested
    @DisplayName("Assignment")
    class Assignment {

        @Test
        @DisplayName("creates an attribute")
        void createsAnAttribute() throws Exception {
            var doc = documentFromString(":frog: Tanglefoot");
            assertEquals("Tanglefoot", get(doc.getAttributes(), "frog"));
        }

        @Test
        @DisplayName("requires a space after colon following attribute name")
        void requiresASpaceAfterColonFollowingAttributeName() throws Exception {
            var doc = documentFromString("foo:bar");
            assertNull(get(doc.getAttributes(), "foo"));
        }

        @Test
        @DisplayName("does not recognize attribute entry if name contains colon")
        void doesNotRecognizeAttributeEntryIfNameContainsColon() throws Exception {
            var input = ":foo:bar: baz";
            var doc = documentFromString(input);
            assertFalsy(doc.hasAttribute("foo:bar"));
            assertEquals(1, size(doc.getBlocks()));
            assertEquals("paragraph", doc.getBlocks().get(0).getContext());
        }

        @Test
        @DisplayName("does not recognize attribute entry if name ends with colon")
        void doesNotRecognizeAttributeEntryIfNameEndsWithColon() throws Exception {
            var input = ":foo:: bar";
            var doc = documentFromString(input);
            assertFalsy(doc.hasAttribute("foo:"));
            assertEquals(1, size(doc.getBlocks()));
            assertEquals("dlist", doc.getBlocks().get(0).getContext());
        }

        @Test
        @DisplayName("allows any word character defined by Unicode in an attribute name")
        void allowsAnyWordCharacterDefinedByUnicodeInAnAttributeName() throws Exception {
            for (var it : list(list("café", "a coffee shop"), list("سمن", "سازمان مردمنهاد"))) {
                var name = get(it, 0);
                var value = get(it, 1);
                var str = (":" + str(name) + ": " + str(value) + "\n\n{" + str(name) + "}\n");
                var result = convertStringToEmbedded(str);
                assertIncludes(result, ("<p>" + str(value) + "</p>"));
            }
        }

        @Test
        @DisplayName("creates an attribute by fusing a legacy multi-line value")
        void createsAnAttributeByFusingALegacyMultiLineValue() throws Exception {
            var str = """
                    :description: This is the first      +
                                  Ruby implementation of +
                                  AsciiDoc.
                    """;
            var doc = documentFromString(str);
            assertEquals("This is the first Ruby implementation of AsciiDoc.", get(doc.getAttributes(), "description"));
        }

        @Test
        @DisplayName("creates an attribute by fusing a multi-line value")
        void createsAnAttributeByFusingAMultiLineValue() throws Exception {
            var str = """
                    :description: This is the first \\
                                  Ruby implementation of \\
                                  AsciiDoc.
                    """;
            var doc = documentFromString(str);
            assertEquals("This is the first Ruby implementation of AsciiDoc.", get(doc.getAttributes(), "description"));
        }

        @Test
        @DisplayName("honors line break characters in multi-line values")
        void honorsLineBreakCharactersInMultiLineValues() throws Exception {
            var str = """
                    :signature: Linus Torvalds + \\
                    Linux Hacker + \\
                    linus.torvalds@example.com
                    """;
            var doc = documentFromString(str);
            assertEquals("Linus Torvalds +\nLinux Hacker +\nlinus.torvalds@example.com", get(doc.getAttributes(), "signature"));
        }

        @Test
        @DisplayName("should allow pass macro to surround a multi-line value that contains line breaks")
        void shouldAllowPassMacroToSurroundAMultiLineValueThatContainsLineBreaks() throws Exception {
            var str = """
                    :signature: pass:a[{author} + \\
                    {title} + \\
                    {email}]
                    """;
            var doc = documentFromString(str, Options.of("attributes", Options.of("author", "Linus Torvalds", "title", "Linux Hacker", "email", "linus.torvalds@example.com")));
            assertEquals("Linus Torvalds +\nLinux Hacker +\nlinus.torvalds@example.com", doc.getAttribute("signature"));
        }

        @Test
        @DisplayName("should delete an attribute that ends with !")
        void shouldDeleteAnAttributeThatEndsWith() throws Exception {
            var doc = documentFromString(":frog: Tanglefoot\n:frog!:");
            assertNull(get(doc.getAttributes(), "frog"));
        }

        @Test
        @DisplayName("should delete an attribute that ends with ! set via API")
        void shouldDeleteAnAttributeThatEndsWithSetViaAPI() throws Exception {
            var doc = documentFromString(":frog: Tanglefoot", Options.of("attributes", Options.of("frog!", "")));
            assertNull(get(doc.getAttributes(), "frog"));
        }

        @Test
        @DisplayName("should delete an attribute that begins with !")
        void shouldDeleteAnAttributeThatBeginsWith() throws Exception {
            var doc = documentFromString(":frog: Tanglefoot\n:!frog:");
            assertNull(get(doc.getAttributes(), "frog"));
        }

        @Test
        @DisplayName("should delete an attribute that begins with ! set via API")
        void shouldDeleteAnAttributeThatBeginsWithSetViaAPI() throws Exception {
            var doc = documentFromString(":frog: Tanglefoot", Options.of("attributes", Options.of("!frog", "")));
            assertNull(get(doc.getAttributes(), "frog"));
        }

        @Test
        @DisplayName("should delete an attribute set via API to nil value")
        void shouldDeleteAnAttributeSetViaAPIToNilValue() throws Exception {
            var doc = documentFromString(":frog: Tanglefoot", Options.of("attributes", Options.of("frog", null)));
            assertNull(get(doc.getAttributes(), "frog"));
        }

        @Test
        @DisplayName("should not choke when deleting a non-existing attribute")
        void shouldNotChokeWhenDeletingANonExistingAttribute() throws Exception {
            var doc = documentFromString(":frog!:");
            assertNull(get(doc.getAttributes(), "frog"));
        }

        @Test
        @DisplayName("replaces special characters in attribute value")
        void replacesSpecialCharactersInAttributeValue() throws Exception {
            var doc = documentFromString(":xml-busters: <>&");
            assertEquals("&lt;&gt;&amp;", get(doc.getAttributes(), "xml-busters"));
        }

        @Test
        @DisplayName("performs attribute substitution on attribute value")
        void performsAttributeSubstitutionOnAttributeValue() throws Exception {
            var doc = documentFromString(":version: 1.0\n:release: Asciidoctor {version}");
            assertEquals("Asciidoctor 1.0", get(doc.getAttributes(), "release"));
        }

        @Test
        @DisplayName("assigns attribute to empty string if substitution fails to resolve attribute")
        void assignsAttributeToEmptyStringIfSubstitutionFailsToResolveAttribute() throws Exception {
            var input = ":release: Asciidoctor {version}";
            documentFromString(input, Options.of("attributes", Options.of("attribute-missing", "drop-line")));
            assertMessage(logger, "INFO", "dropping line containing reference to missing attribute: version");
        }

        @Test
        @DisplayName("assigns multi-line attribute to empty string if substitution fails to resolve attribute")
        void assignsMultiLineAttributeToEmptyStringIfSubstitutionFailsToResolveAttribute() throws Exception {
            var input = """
                    :release: Asciidoctor +
                              {version}
                    """;
            var doc = documentFromString(input, Options.of("attributes", Options.of("attribute-missing", "drop-line")));
            assertEquals("", get(doc.getAttributes(), "release"));
            assertMessage(logger, "INFO", "dropping line containing reference to missing attribute: version");
        }

        @Test
        @DisplayName("resolves attributes inside attribute value within header")
        void resolvesAttributesInsideAttributeValueWithinHeader() throws Exception {
            var input = """
                    = Document Title
                    :big: big
                    :bigfoot: {big}foot

                    {bigfoot}
                    """;
            var result = convertStringToEmbedded(input);
            assertIncludes(result, "bigfoot");
        }

        @Test
        @DisplayName("resolves attributes and pass macro inside attribute value outside header")
        void resolvesAttributesAndPassMacroInsideAttributeValueOutsideHeader() throws Exception {
            var input = """
                    = Document Title

                    content

                    :big: pass:a,q[_big_]
                    :bigfoot: {big}foot
                    {bigfoot}
                    """;
            var result = convertStringToEmbedded(input);
            assertIncludes(result, "<em>big</em>foot");
        }

        @Test
        @DisplayName("should limit maximum size of attribute value if safe mode is SECURE")
        void shouldLimitMaximumSizeOfAttributeValueIfSafeModeIsSECURE() throws Exception {
            var expected = "a".repeat(4096);
            var input = (":name: " + str("a".repeat(5000)) + "\n\n{name}\n");
            var result = convertInlineString(input);
            assertEquals(expected, result);
            assertEquals(4096, result.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        }

        @Test
        @DisplayName("should handle multibyte characters when limiting attribute value size")
        void shouldHandleMultibyteCharactersWhenLimitingAttributeValueSize() throws Exception {
            var expected = "日本";
            var input = """
                    :name: 日本語

                    {name}
                    """;
            var result = convertInlineString(input, Options.of("attributes", Options.of("max-attribute-value-size", 6)));
            assertEquals(expected, result);
            assertEquals(6, result.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        }

        @Test
        @DisplayName("should not mangle multibyte characters when limiting attribute value size")
        void shouldNotMangleMultibyteCharactersWhenLimitingAttributeValueSize() throws Exception {
            var expected = "日本";
            var input = """
                    :name: 日本語

                    {name}
                    """;
            var result = convertInlineString(input, Options.of("attributes", Options.of("max-attribute-value-size", 8)));
            assertEquals(expected, result);
            assertEquals(6, result.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        }

        @Test
        @DisplayName("should allow maximize size of attribute value to be disabled")
        void shouldAllowMaximizeSizeOfAttributeValueToBeDisabled() throws Exception {
            var expected = "a".repeat(5000);
            var input = (":name: " + str("a".repeat(5000)) + "\n\n{name}\n");
            var result = convertInlineString(input, Options.of("attributes", Options.of("max-attribute-value-size", null)));
            assertEquals(expected, result);
            assertEquals(5000, result.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        }

        @Test
        @DisplayName("resolves user-home attribute if safe mode is less than SERVER")
        void resolvesUserHomeAttributeIfSafeModeIsLessThanSERVER() throws Exception {
            var input = """
                    :imagesdir: {user-home}/etc/images

                    {imagesdir}
                    """;
            var output = convertInlineString(input, Options.of("safe", "safe"));
            assertEquals((str(Asciidoctor.USER_HOME) + "/etc/images"), output);
        }

        @Test
        @DisplayName("user-home attribute resolves to . if safe mode is SERVER or greater")
        void userHomeAttributeResolvesToIfSafeModeIsSERVEROrGreater() throws Exception {
            var input = """
                    :imagesdir: {user-home}/etc/images

                    {imagesdir}
                    """;
            var output = convertInlineString(input, Options.of("safe", "server"));
            assertEquals("./etc/images", output);
        }

        @Test
        @DisplayName("user-home attribute can be overridden by API if safe mode is less than SERVER")
        void userHomeAttributeCanBeOverriddenByAPIIfSafeModeIsLessThanSERVER() throws Exception {
            var input = "Go {user-home}!\n";
            var output = convertInlineString(input, Options.of("attributes", Options.of("user-home", "/home")));
            assertEquals("Go /home!", output);
        }

        @Test
        @DisplayName("user-home attribute can be overridden by API if safe mode is SERVER or greater")
        void userHomeAttributeCanBeOverriddenByAPIIfSafeModeIsSERVEROrGreater() throws Exception {
            var input = "Go {user-home}!\n";
            var output = convertInlineString(input, Options.of("safe", "server", "attributes", Options.of("user-home", "/home")));
            assertEquals("Go /home!", output);
        }

        @Test
        @DisplayName("apply custom substitutions to text in passthrough macro and assign to attribute")
        void applyCustomSubstitutionsToTextInPassthroughMacroAndAssignToAttribute() throws Exception {
            var doc = documentFromString(":xml-busters: pass:[<>&]");
            assertEquals("<>&", get(doc.getAttributes(), "xml-busters"));
            doc = documentFromString(":xml-busters: pass:none[<>&]");
            assertEquals("<>&", get(doc.getAttributes(), "xml-busters"));
            doc = documentFromString(":xml-busters: pass:specialcharacters[<>&]");
            assertEquals("&lt;&gt;&amp;", get(doc.getAttributes(), "xml-busters"));
            doc = documentFromString(":xml-busters: pass:n,-c[<(C)>]");
            assertEquals("<&#169;>", get(doc.getAttributes(), "xml-busters"));
        }

        @Test
        @DisplayName("should not recognize pass macro with invalid substitution list in attribute value")
        void shouldNotRecognizePassMacroWithInvalidSubstitutionListInAttributeValue() throws Exception {
            for (var subs : list(",", "42", "a,")) {
                var doc = documentFromString((":pass-fail: pass:" + str(subs) + "[whale]"));
                assertEquals(("pass:" + str(subs) + "[whale]"), get(doc.getAttributes(), "pass-fail"));
            }
        }

        @Test
        @DisplayName("attribute is treated as defined until it is unset")
        void attributeIsTreatedAsDefinedUntilItIsUnset() throws Exception {
            var input = """
                    :holygrail:
                    ifdef::holygrail[]
                    The holy grail has been found!
                    endif::holygrail[]

                    :holygrail!:
                    ifndef::holygrail[]
                    Buggers! What happened to the grail?
                    endif::holygrail[]
                    """;
            var output = convertString(input);
            assertXpath("//p", output, 2);
            assertXpath("(//p)[1][text() = \"The holy grail has been found!\"]", output, 1);
            assertXpath("(//p)[2][text() = \"Buggers! What happened to the grail?\"]", output, 1);
        }

        @Test
        @DisplayName("attribute set via API overrides attribute set in document")
        void attributeSetViaAPIOverridesAttributeSetInDocument() throws Exception {
            var doc = documentFromString(":cash: money", Options.of("attributes", Options.of("cash", "heroes")));
            assertEquals("heroes", get(doc.getAttributes(), "cash"));
        }

        @Test
        @DisplayName("attribute set via API cannot be unset by document")
        void attributeSetViaAPICannotBeUnsetByDocument() throws Exception {
            var doc = documentFromString(":cash!:", Options.of("attributes", Options.of("cash", "heroes")));
            assertEquals("heroes", get(doc.getAttributes(), "cash"));
        }

        @Test
        @DisplayName("attribute soft set via API using modifier on name can be overridden by document")
        void attributeSoftSetViaAPIUsingModifierOnNameCanBeOverriddenByDocument() throws Exception {
            var doc = documentFromString(":cash: money", Options.of("attributes", Options.of("cash@", "heroes")));
            assertEquals("money", get(doc.getAttributes(), "cash"));
        }

        @Test
        @DisplayName("attribute soft set via API using modifier on value can be overridden by document")
        void attributeSoftSetViaAPIUsingModifierOnValueCanBeOverriddenByDocument() throws Exception {
            var doc = documentFromString(":cash: money", Options.of("attributes", Options.of("cash", "heroes@")));
            assertEquals("money", get(doc.getAttributes(), "cash"));
        }

        @Test
        @DisplayName("attribute soft set via API using modifier on name can be unset by document")
        void attributeSoftSetViaAPIUsingModifierOnNameCanBeUnsetByDocument() throws Exception {
            var doc = documentFromString(":cash!:", Options.of("attributes", Options.of("cash@", "heroes")));
            assertNull(get(doc.getAttributes(), "cash"));
            doc = documentFromString(":cash!:", Options.of("attributes", Options.of("cash@", true)));
            assertNull(get(doc.getAttributes(), "cash"));
        }

        @Test
        @DisplayName("attribute soft set via API using modifier on value can be unset by document")
        void attributeSoftSetViaAPIUsingModifierOnValueCanBeUnsetByDocument() throws Exception {
            var doc = documentFromString(":cash!:", Options.of("attributes", Options.of("cash", "heroes@")));
            assertNull(get(doc.getAttributes(), "cash"));
        }

        @Test
        @DisplayName("attribute unset via API cannot be set by document")
        void attributeUnsetViaAPICannotBeSetByDocument() throws Exception {
            for (var attributes : list(Options.of("cash!", ""), Options.of("!cash", ""), Options.of("cash", null))) {
                var doc = documentFromString(":cash: money", Options.of("attributes", attributes));
                assertNull(get(doc.getAttributes(), "cash"));
            }
        }

        @Test
        @DisplayName("attribute soft unset via API can be set by document")
        void attributeSoftUnsetViaAPICanBeSetByDocument() throws Exception {
            for (var attributes : list(Options.of("cash!@", ""), Options.of("!cash@", ""), Options.of("cash!", "@"), Options.of("!cash", "@"), Options.of("cash", false))) {
                var doc = documentFromString(":cash: money", Options.of("attributes", attributes));
                assertEquals("money", get(doc.getAttributes(), "cash"));
            }
        }

        @Test
        @DisplayName("can soft unset built-in attribute from API and still override in document")
        void canSoftUnsetBuiltInAttributeFromAPIAndStillOverrideInDocument() throws Exception {
            for (var attributes : list(Options.of("sectids!@", ""), Options.of("!sectids@", ""), Options.of("sectids!", "@"), Options.of("!sectids", "@"), Options.of("sectids", false))) {
                var doc = documentFromString("== Heading", Options.of("attributes", attributes));
                assertFalsy(doc.hasAttribute("sectids"));
                assertCss("#_heading", doc.convert(Options.of("standalone", false)), 0);
                doc = documentFromString(":sectids:\n\n== Heading", Options.of("attributes", attributes));
                assertTruthy(doc.hasAttribute("sectids"));
                assertCss("#_heading", doc.convert(Options.of("standalone", false)), 1);
            }
        }

        @Test
        @DisplayName("backend and doctype attributes are set by default in default configuration")
        void backendAndDoctypeAttributesAreSetByDefaultInDefaultConfiguration() throws Exception {
            var input = """
                    = Document Title
                    Author Name

                    content
                    """;
            var doc = documentFromString(input);
            var expect = Options.of("backend", "html5", "backend-html5", "", "backend-html5-doctype-article", "", "outfilesuffix", ".html", "basebackend", "html", "basebackend-html", "", "basebackend-html-doctype-article", "", "doctype", "article", "doctype-article", "", "filetype", "html", "filetype-html", "");
            for (var entry : entries(expect)) {
                var key = entry.getKey();
                var val = entry.getValue();
                assertTruthy(doc.getAttributes().containsKey(key));
                assertEquals(val, get(doc.getAttributes(), key));
            }
        }

        @Test
        @DisplayName("backend and doctype attributes are set by default in custom configuration")
        void backendAndDoctypeAttributesAreSetByDefaultInCustomConfiguration() throws Exception {
            var input = """
                    = Document Title
                    Author Name

                    content
                    """;
            var doc = documentFromString(input, Options.of("doctype", "book", "backend", "docbook"));
            var expect = Options.of("backend", "docbook5", "backend-docbook5", "", "backend-docbook5-doctype-book", "", "outfilesuffix", ".xml", "basebackend", "docbook", "basebackend-docbook", "", "basebackend-docbook-doctype-book", "", "doctype", "book", "doctype-book", "", "filetype", "xml", "filetype-xml", "");
            for (var entry : entries(expect)) {
                var key = entry.getKey();
                var val = entry.getValue();
                assertTruthy(doc.getAttributes().containsKey(key));
                assertEquals(val, get(doc.getAttributes(), key));
            }
        }

        @Test
        @DisplayName("backend attributes are updated if backend attribute is defined in document and safe mode is less than SERVER")
        void backendAttributesAreUpdatedIfBackendAttributeIsDefinedInDocumentAndSafeModeIsLessThanSERVER() throws Exception {
            var input = """
                    = Document Title
                    Author Name
                    :backend: docbook
                    :doctype: book

                    content
                    """;
            var doc = documentFromString(input, Options.of("safe", SafeMode.SAFE.getLevel()));
            var expect = Options.of("backend", "docbook5", "backend-docbook5", "", "backend-docbook5-doctype-book", "", "outfilesuffix", ".xml", "basebackend", "docbook", "basebackend-docbook", "", "basebackend-docbook-doctype-book", "", "doctype", "book", "doctype-book", "", "filetype", "xml", "filetype-xml", "");
            for (var entry : entries(expect)) {
                var key = entry.getKey();
                var val = entry.getValue();
                assertTruthy(doc.getAttributes().containsKey(key));
                assertEquals(val, get(doc.getAttributes(), key));
            }
            assertFalsy(doc.getAttributes().containsKey("backend-html5"));
            assertFalsy(doc.getAttributes().containsKey("backend-html5-doctype-article"));
            assertFalsy(doc.getAttributes().containsKey("basebackend-html"));
            assertFalsy(doc.getAttributes().containsKey("basebackend-html-doctype-article"));
            assertFalsy(doc.getAttributes().containsKey("doctype-article"));
            assertFalsy(doc.getAttributes().containsKey("filetype-html"));
        }

        @Test
        @DisplayName("backend attributes defined in document options overrides backend attribute in document")
        void backendAttributesDefinedInDocumentOptionsOverridesBackendAttributeInDocument() throws Exception {
            var doc = documentFromString(":backend: docbook5", Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("backend", "html5")));
            assertEquals("html5", get(doc.getAttributes(), "backend"));
            assertTruthy(doc.getAttributes().containsKey("backend-html5"));
            assertEquals("html", get(doc.getAttributes(), "basebackend"));
            assertTruthy(doc.getAttributes().containsKey("basebackend-html"));
        }

        @Test
        @Disabled("positional attributes are stored with a String key in Java")
        @DisplayName("can only access a positional attribute from the attributes hash")
        void canOnlyAccessAPositionalAttributeFromTheAttributesHash() throws Exception {
        }

        @Test
        @DisplayName("attr should not retrieve attribute from document if not set on block")
        void attrShouldNotRetrieveAttributeFromDocumentIfNotSetOnBlock() throws Exception {
            var doc = documentFromString("paragraph", Options.of("attributes", Options.of("name", "value")));
            var para = doc.getBlocks().get(0);
            assertNull(para.getAttribute("name"));
        }

        @Test
        @DisplayName("attr looks for attribute on document if fallback name is true")
        void attrLooksForAttributeOnDocumentIfFallbackNameIsTrue() throws Exception {
            var doc = documentFromString("paragraph", Options.of("attributes", Options.of("name", "value")));
            var para = doc.getBlocks().get(0);
            assertEquals("value", para.getAttribute("name", null, "name"));
        }

        @Test
        @DisplayName("attr uses fallback name when looking for attribute on document")
        void attrUsesFallbackNameWhenLookingForAttributeOnDocument() throws Exception {
            var doc = documentFromString("paragraph", Options.of("attributes", Options.of("alt-name", "value")));
            var para = doc.getBlocks().get(0);
            assertEquals("value", para.getAttribute("name", null, "alt-name"));
        }

        @Test
        @DisplayName("attr? should not check for attribute on document if not set on block")
        void attrShouldNotCheckForAttributeOnDocumentIfNotSetOnBlock() throws Exception {
            var doc = documentFromString("paragraph", Options.of("attributes", Options.of("name", "value")));
            var para = doc.getBlocks().get(0);
            assertFalsy(para.hasAttribute("name"));
        }

        @Test
        @DisplayName("attr? checks for attribute on document if fallback name is true")
        void attrChecksForAttributeOnDocumentIfFallbackNameIsTrue() throws Exception {
            var doc = documentFromString("paragraph", Options.of("attributes", Options.of("name", "value")));
            var para = doc.getBlocks().get(0);
            assertTruthy(para.hasAttribute("name", null, "name"));
        }

        @Test
        @DisplayName("attr? checks for fallback name when looking for attribute on document")
        void attrChecksForFallbackNameWhenLookingForAttributeOnDocument() throws Exception {
            var doc = documentFromString("paragraph", Options.of("attributes", Options.of("alt-name", "value")));
            var para = doc.getBlocks().get(0);
            assertTruthy(para.hasAttribute("name", null, "alt-name"));
        }

        @Test
        @DisplayName("set_attr should set value to empty string if no value is specified")
        void set_attrShouldSetValueToEmptyStringIfNoValueIsSpecified() throws Exception {
            var node = new Block(null, "paragraph", Options.of("attributes", Options.of()));
            node.setAttribute("foo");
            assertEquals("", node.getAttribute("foo"));
        }

        @Test
        @DisplayName("remove_attr should remove attribute and return previous value")
        void remove_attrShouldRemoveAttributeAndReturnPreviousValue() throws Exception {
            var doc = emptyDocument();
            var node = new Block(doc, "paragraph", Options.of("attributes", Options.of("foo", "bar")));
            assertEquals("bar", node.removeAttribute("foo"));
            assertNull(node.getAttribute("foo"));
        }

        @Test
        @DisplayName("set_attr should not overwrite existing key if overwrite is false")
        void set_attrShouldNotOverwriteExistingKeyIfOverwriteIsFalse() throws Exception {
            var node = new Block(null, "paragraph", Options.of("attributes", Options.of("foo", "bar")));
            assertEquals("bar", node.getAttribute("foo"));
            node.setAttribute("foo", "baz", false);
            assertEquals("bar", node.getAttribute("foo"));
        }

        @Test
        @DisplayName("set_attr should overwrite existing key by default")
        void set_attrShouldOverwriteExistingKeyByDefault() throws Exception {
            var node = new Block(null, "paragraph", Options.of("attributes", Options.of("foo", "bar")));
            assertEquals("bar", node.getAttribute("foo"));
            node.setAttribute("foo", "baz");
            assertEquals("baz", node.getAttribute("foo"));
        }

        @Test
        @DisplayName("set_attr should set header attribute in loaded document")
        void set_attrShouldSetHeaderAttributeInLoadedDocument() throws Exception {
            var input = """
                    :uri: http://example.org

                    {uri}
                    """;
            var doc = Asciidoctor.load(input, Options.of("attributes", Options.of("uri", "https://github.com")));
            doc.setAttribute("uri", "https://google.com");
            var output = doc.convert();
            assertXpath("//a[@href=\"https://google.com\"]", output, 1);
        }

        @Test
        @DisplayName("set_attribute should set attribute if key is not locked")
        void set_attributeShouldSetAttributeIfKeyIsNotLocked() throws Exception {
            var doc = emptyDocument();
            assertFalsy(doc.hasAttribute("foo"));
            var res = doc.setDocumentAttribute("foo", "baz");
            assertTruthy(res);
            assertEquals("baz", doc.getAttribute("foo"));
        }

        @Test
        @DisplayName("set_attribute should not set key if key is locked")
        void set_attributeShouldNotSetKeyIfKeyIsLocked() throws Exception {
            var doc = emptyDocument(Options.of("attributes", Options.of("foo", "bar")));
            assertEquals("bar", doc.getAttribute("foo"));
            var res = doc.setDocumentAttribute("foo", "baz");
            assertFalsy(res);
            assertEquals("bar", doc.getAttribute("foo"));
        }

        @Test
        @DisplayName("set_attribute should update backend attributes")
        void set_attributeShouldUpdateBackendAttributes() throws Exception {
            var doc = emptyDocument(Options.of("attributes", Options.of("backend", "html5@")));
            assertEquals("", doc.getAttribute("backend-html5"));
            var res = doc.setDocumentAttribute("backend", "docbook5");
            assertTruthy(res);
            assertFalsy(doc.hasAttribute("backend-html5"));
            assertEquals("", doc.getAttribute("backend-docbook5"));
        }

        @Test
        @DisplayName("verify toc attribute matrix")
        void verifyTocAttributeMatrix() throws Exception {
            var expectedData = """
                    #attributes                               |toc|toc-position|toc-placement|toc-class
                    toc                                       |   |nil         |auto         |nil
                    toc=header                                |   |nil         |auto         |nil
                    toc=beeboo                                |   |nil         |auto         |nil
                    toc=left                                  |   |left        |auto         |toc2
                    toc2                                      |   |left        |auto         |toc2
                    toc=right                                 |   |right       |auto         |toc2
                    toc=preamble                              |   |content     |preamble     |nil
                    toc=macro                                 |   |content     |macro        |nil
                    toc toc-placement=macro toc-position=left |   |content     |macro        |nil
                    toc toc-placement!                        |   |content     |macro        |nil
                    """;
            for (var line : lines(expectedData)) {
                if (line.startsWith("#")) {
                    continue;
                }
                var expect = Arrays.stream(line.split("\\|")).map(String::strip).map(e -> e.equals("nil") ? null : e).toList();
                Map<String, Object> attrs = new LinkedHashMap<>();
                for (var e : Strings.splitOnWhitespace(expect.get(0))) {
                    var pair = e.split("=", 2);
                    attrs.put(pair[0], pair.length > 1 ? pair[1] : "");
                }
                var doc = documentFromString("", Options.of("attributes", attrs));
                var names = list("toc", "toc-position", "toc-placement", "toc-class");
                for (int i = 0; i < names.size(); i++) {
                    var name = names.get(i);
                    var value = expect.get(i + 1);
                    if (value != null) {
                        assertTruthy(doc.isAttribute(name, value));
                    } else {
                        assertFalsy(doc.hasAttribute(name));
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Interpolation")
    class Interpolation {

        @Test
        @DisplayName("convert properly with simple names")
        void convertProperlyWithSimpleNames() throws Exception {
            var html = convertString(":frog: Tanglefoot\n:my_super-hero: Spiderman\n\nYo, {frog}!\nBeat {my_super-hero}!");
            assertXpath("//p[text()=\"Yo, Tanglefoot!\nBeat Spiderman!\"]", html, 1);
        }

        @Test
        @DisplayName("attribute lookup is not case sensitive")
        void attributeLookupIsNotCaseSensitive() throws Exception {
            var input = """
                    :He-Man: The most powerful man in the universe

                    He-Man: {He-Man}

                    She-Ra: {She-Ra}
                    """;
            var result = convertStringToEmbedded(input, Options.of("attributes", Options.of("She-Ra", "The Princess of Power")));
            assertXpath("//p[text()=\"He-Man: The most powerful man in the universe\"]", result, 1);
            assertXpath("//p[text()=\"She-Ra: The Princess of Power\"]", result, 1);
        }

        @Test
        @DisplayName("convert properly with single character name")
        void convertProperlyWithSingleCharacterName() throws Exception {
            var html = convertString(":r: Ruby\n\nR is for {r}!");
            assertXpath("//p[text()=\"R is for Ruby!\"]", html, 1);
        }

        @Test
        @DisplayName("collapses spaces in attribute names")
        void collapsesSpacesInAttributeNames() throws Exception {
            var input = """
                    Main Header
                    ===========
                    :My frog: Tanglefoot

                    Yo, {myfrog}!
                    """;
            var output = convertString(input);
            assertXpath("(//p)[1][text()=\"Yo, Tanglefoot!\"]", output, 1);
        }

        @Test
        @DisplayName("ignores lines with bad attributes if attribute-missing is drop-line")
        void ignoresLinesWithBadAttributesIfAttributeMissingIsDropLine() throws Exception {
            var input = """
                    :attribute-missing: drop-line

                    This is
                    blah blah {foobarbaz}
                    all there is.
                    """;
            var output = convertStringToEmbedded(input);
            var para = xmlnodesAtCss("p", output, 1);
            refuteIncludes("blah blah", para.getContent());
            assertMessage(logger, "INFO", "dropping line containing reference to missing attribute: foobarbaz");
        }

        @Test
        @DisplayName("attribute value gets interpreted when converting")
        void attributeValueGetsInterpretedWhenConverting() throws Exception {
            var doc = documentFromString(":google: http://google.com[Google]\n\n{google}");
            assertEquals("http://google.com[Google]", get(doc.getAttributes(), "google"));
            var output = doc.convert();
            assertXpath("//a[@href=\"http://google.com\"][text() = \"Google\"]", output, 1);
        }

        @Test
        @DisplayName("should drop line with reference to missing attribute if attribute-missing attribute is drop-line")
        void shouldDropLineWithReferenceToMissingAttributeIfAttributeMissingAttributeIsDropLine() throws Exception {
            var input = """
                    :attribute-missing: drop-line

                    Line 1: This line should appear in the output.
                    Line 2: Oh no, a {bogus-attribute}! This line should not appear in the output.
                    """;
            var output = convertStringToEmbedded(input);
            assertMatch(rx("Line 1"), output);
            refuteMatch(rx("Line 2"), output);
            assertMessage(logger, "INFO", "dropping line containing reference to missing attribute: bogus-attribute");
        }

        @Test
        @DisplayName("should not drop line with reference to missing attribute by default")
        void shouldNotDropLineWithReferenceToMissingAttributeByDefault() throws Exception {
            var input = """
                    Line 1: This line should appear in the output.
                    Line 2: A {bogus-attribute}! This time, this line should appear in the output.
                    """;
            var output = convertStringToEmbedded(input);
            assertMatch(rx("Line 1"), output);
            assertMatch(rx("Line 2"), output);
            assertMatch(rx("\\{bogus-attribute\\}"), output);
        }

        @Test
        @DisplayName("should drop line with attribute unassignment by default")
        void shouldDropLineWithAttributeUnassignmentByDefault() throws Exception {
            var input = """
                    :a:

                    Line 1: This line should appear in the output.
                    Line 2: {set:a!}This line should not appear in the output.
                    """;
            var output = convertStringToEmbedded(input);
            assertMatch(rx("Line 1"), output);
            refuteMatch(rx("Line 2"), output);
        }

        @Test
        @DisplayName("should not drop line with attribute unassignment if attribute-undefined is drop")
        void shouldNotDropLineWithAttributeUnassignmentIfAttributeUndefinedIsDrop() throws Exception {
            var input = """
                    :attribute-undefined: drop
                    :a:

                    Line 1: This line should appear in the output.
                    Line 2: {set:a!}This line should appear in the output.
                    """;
            var output = convertStringToEmbedded(input);
            assertMatch(rx("Line 1"), output);
            assertMatch(rx("Line 2"), output);
            refuteMatch(rx("\\{set:a!\\}"), output);
        }

        @Test
        @DisplayName("should drop line that only contains attribute assignment")
        void shouldDropLineThatOnlyContainsAttributeAssignment() throws Exception {
            var input = """
                    Line 1
                    {set:a}
                    Line 2
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//p[text()=\"Line 1\nLine 2\"]", output, 1);
        }

        @Test
        @DisplayName("should drop line that only contains unresolved attribute when attribute-missing is drop")
        void shouldDropLineThatOnlyContainsUnresolvedAttributeWhenAttributeMissingIsDrop() throws Exception {
            var input = """
                    Line 1
                    {unresolved}
                    Line 2
                    """;
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("attribute-missing", "drop")));
            assertXpath("//p[text()=\"Line 1\nLine 2\"]", output, 1);
        }

        @Test
        @DisplayName("substitutes inside unordered list items")
        void substitutesInsideUnorderedListItems() throws Exception {
            var html = convertString(":foo: bar\n* snort at the {foo}\n* yawn");
            assertXpath("//li/p[text()=\"snort at the bar\"]", html, 1);
        }

        @Test
        @DisplayName("substitutes inside section title")
        void substitutesInsideSectionTitle() throws Exception {
            var output = convertString(":prefix: Cool\n\n== {prefix} Title\n\ncontent");
            assertXpath("//h2[text()=\"Cool Title\"]", output, 1);
            assertCss("h2#_cool_title", output, 1);
        }

        @Test
        @DisplayName("interpolates attribute defined in header inside attribute entry in header")
        void interpolatesAttributeDefinedInHeaderInsideAttributeEntryInHeader() throws Exception {
            var input = """
                    = Title
                    Author Name
                    :attribute-a: value
                    :attribute-b: {attribute-a}

                    preamble
                    """;
            var doc = documentFromString(input, Options.of("parse_header_only", true));
            assertEquals("value", get(doc.getAttributes(), "attribute-b"));
        }

        @Test
        @DisplayName("interpolates author attribute inside attribute entry in header")
        void interpolatesAuthorAttributeInsideAttributeEntryInHeader() throws Exception {
            var input = """
                    = Title
                    Author Name
                    :name: {author}

                    preamble
                    """;
            var doc = documentFromString(input, Options.of("parse_header_only", true));
            assertEquals("Author Name", get(doc.getAttributes(), "name"));
        }

        @Test
        @DisplayName("interpolates revinfo attribute inside attribute entry in header")
        void interpolatesRevinfoAttributeInsideAttributeEntryInHeader() throws Exception {
            var input = """
                    = Title
                    Author Name
                    2013-01-01
                    :date: {revdate}

                    preamble
                    """;
            var doc = documentFromString(input, Options.of("parse_header_only", true));
            assertEquals("2013-01-01", get(doc.getAttributes(), "date"));
        }

        @Test
        @DisplayName("attribute entries can resolve previously defined attributes")
        void attributeEntriesCanResolvePreviouslyDefinedAttributes() throws Exception {
            var input = """
                    = Title
                    Author Name
                    v1.0, 2010-01-01: First release!
                    :a: value
                    :a2: {a}
                    :revdate2: {revdate}

                    {a} == {a2}

                    {revdate} == {revdate2}
                    """;
            var doc = documentFromString(input);
            assertEquals("2010-01-01", doc.getAttribute("revdate"));
            assertEquals("2010-01-01", doc.getAttribute("revdate2"));
            assertEquals("value", doc.getAttribute("a"));
            assertEquals("value", doc.getAttribute("a2"));
            var output = doc.convert();
            assertIncludes(output, "value == value");
            assertIncludes(output, "2010-01-01 == 2010-01-01");
        }

        @Test
        @DisplayName("should warn if unterminated block comment is detected in document header")
        void shouldWarnIfUnterminatedBlockCommentIsDetectedInDocumentHeader() throws Exception {
            var input = """
                    = Document Title
                    :foo: bar
                    ////
                    :hey: there

                    content
                    """;
            var doc = documentFromString(input);
            assertNull(doc.getAttribute("hey"));
            assertMessage(logger, "WARN", "<stdin>: line 3: unterminated comment block", LogMessage.class);
        }

        @Test
        @DisplayName("substitutes inside block title")
        void substitutesInsideBlockTitle() throws Exception {
            var input = """
                    :gem_name: asciidoctor

                    .Require the +{gem_name}+ gem
                    To use {gem_name}, the first thing to do is to import it in your Ruby source file.
                    """;
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("compat-mode", "")));
            assertXpath("//*[@class=\"title\"]/code[text()=\"asciidoctor\"]", output, 1);
            input = """
                    :gem_name: asciidoctor

                    .Require the `{gem_name}` gem
                    To use {gem_name}, the first thing to do is to import it in your Ruby source file.
                    """;
            output = convertStringToEmbedded(input);
            assertXpath("//*[@class=\"title\"]/code[text()=\"asciidoctor\"]", output, 1);
        }

        @Test
        @DisplayName("sets attribute until it is deleted")
        void setsAttributeUntilItIsDeleted() throws Exception {
            var input = """
                    :foo: bar

                    Crossing the {foo}.

                    :foo!:

                    Belly up to the {foo}.
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//p[text()=\"Crossing the bar.\"]", output, 1);
            assertXpath("//p[text()=\"Belly up to the bar.\"]", output, 0);
        }

        @Test
        @DisplayName("should allow compat-mode to be set and unset in middle of document")
        void shouldAllowCompatModeToBeSetAndUnsetInMiddleOfDocument() throws Exception {
            var input = """
                    :foo: bar

                    [[paragraph-a]]
                    `{foo}`

                    :compat-mode!:

                    [[paragraph-b]]
                    `{foo}`

                    :compat-mode:

                    [[paragraph-c]]
                    `{foo}`
                    """;
            var result = convertStringToEmbedded(input, Options.of("attributes", Options.of("compat-mode", "@")));
            assertXpath("/*[@id=\"paragraph-a\"]//code[text()=\"{foo}\"]", result, 1);
            assertXpath("/*[@id=\"paragraph-b\"]//code[text()=\"bar\"]", result, 1);
            assertXpath("/*[@id=\"paragraph-c\"]//code[text()=\"{foo}\"]", result, 1);
        }

        @Test
        @DisplayName("does not disturb attribute-looking things escaped with backslash")
        void doesNotDisturbAttributeLookingThingsEscapedWithBackslash() throws Exception {
            var html = convertString(":foo: bar\nThis is a \\{foo} day.");
            assertXpath("//p[text()=\"This is a {foo} day.\"]", html, 1);
        }

        @Test
        @DisplayName("does not disturb attribute-looking things escaped with literals")
        void doesNotDisturbAttributeLookingThingsEscapedWithLiterals() throws Exception {
            var html = convertString(":foo: bar\nThis is a +++{foo}+++ day.");
            assertXpath("//p[text()=\"This is a {foo} day.\"]", html, 1);
        }

        @Test
        @DisplayName("does not substitute attributes inside listing blocks")
        void doesNotSubstituteAttributesInsideListingBlocks() throws Exception {
            var input = """
                    :forecast: snow

                    ----
                    puts 'The forecast for today is {forecast}'
                    ----
                    """;
            var output = convertString(input);
            assertMatch(rx("\\{forecast\\}"), output);
        }

        @Test
        @DisplayName("does not substitute attributes inside literal blocks")
        void doesNotSubstituteAttributesInsideLiteralBlocks() throws Exception {
            var input = """
                    :foo: bar

                    ....
                    You insert the text {foo} to expand the value
                    of the attribute named foo in your document.
                    ....
                    """;
            var output = convertString(input);
            assertMatch(rx("\\{foo\\}"), output);
        }

        @Test
        @DisplayName("does not show docdir and shows relative docfile if safe mode is SERVER or greater")
        void doesNotShowDocdirAndShowsRelativeDocfileIfSafeModeIsSERVEROrGreater() throws Exception {
            var input = """
                    * docdir: {docdir}
                    * docfile: {docfile}
                    """;
            var docdir = Helpers.workingDirectory();
            var docfile = joinPath(docdir, "sample.adoc");
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SERVER.getLevel(), "attributes", Options.of("docdir", docdir, "docfile", docfile)));
            assertXpath("//li[1]/p[text()=\"docdir: \"]", output, 1);
            assertXpath("//li[2]/p[text()=\"docfile: sample.adoc\"]", output, 1);
        }

        @Test
        @DisplayName("shows absolute docdir and docfile paths if safe mode is less than SERVER")
        void showsAbsoluteDocdirAndDocfilePathsIfSafeModeIsLessThanSERVER() throws Exception {
            var input = """
                    * docdir: {docdir}
                    * docfile: {docfile}
                    """;
            var docdir = Helpers.workingDirectory();
            var docfile = joinPath(docdir, "sample.adoc");
            var output = convertStringToEmbedded(input, Options.of("safe", SafeMode.SAFE.getLevel(), "attributes", Options.of("docdir", docdir, "docfile", docfile)));
            assertXpath(("//li[1]/p[text()=\"docdir: " + str(docdir) + "\"]"), output, 1);
            assertXpath(("//li[2]/p[text()=\"docfile: " + str(docfile) + "\"]"), output, 1);
        }

        @Test
        @DisplayName("assigns attribute defined in attribute reference with set prefix and value")
        void assignsAttributeDefinedInAttributeReferenceWithSetPrefixAndValue() throws Exception {
            var input = "{set:foo:bar}{foo}";
            var output = convertStringToEmbedded(input);
            assertXpath("//p", output, 1);
            assertXpath("//p[text()=\"bar\"]", output, 1);
        }

        @Test
        @DisplayName("assigns attribute defined in attribute reference with set prefix and no value")
        void assignsAttributeDefinedInAttributeReferenceWithSetPrefixAndNoValue() throws Exception {
            var input = "{set:foo}\n{foo}yes";
            var output = convertStringToEmbedded(input);
            assertXpath("//p", output, 1);
            assertXpath("//p[normalize-space(text())=\"yes\"]", output, 1);
        }

        @Test
        @DisplayName("assigns attribute defined in attribute reference with set prefix and empty value")
        void assignsAttributeDefinedInAttributeReferenceWithSetPrefixAndEmptyValue() throws Exception {
            var input = "{set:foo:}\n{foo}yes";
            var output = convertStringToEmbedded(input);
            assertXpath("//p", output, 1);
            assertXpath("//p[normalize-space(text())=\"yes\"]", output, 1);
        }

        @Test
        @DisplayName("unassigns attribute defined in attribute reference with set prefix")
        void unassignsAttributeDefinedInAttributeReferenceWithSetPrefix() throws Exception {
            var input = """
                    :attribute-missing: drop-line
                    :foo:

                    {set:foo!}
                    {foo}yes
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//p", output, 1);
            assertXpath("//p/child::text()", output, 0);
            assertMessage(logger, "INFO", "dropping line containing reference to missing attribute: foo");
        }
    }

    @Nested
    @DisplayName("Intrinsic attributes")
    class IntrinsicAttributes {

        @Test
        @DisplayName("substitute intrinsics")
        void substituteIntrinsics() throws Exception {
            for (var entry : entries(Asciidoctor.INTRINSIC_ATTRIBUTES)) {
                var key = entry.getKey();
                var value = entry.getValue();
                var html = convertString(("Look, a {" + str(key) + "} is here"));
                assertMatch(rx(("Look, a " + str(Pattern.quote(value)) + " is here")), html);
            }
        }

        @Test
        @DisplayName("do not escape intrinsic substitutions")
        void doNotEscapeIntrinsicSubstitutions() throws Exception {
            var html = convertString("happy{nbsp}together");
            assertMatch(rx("happy&#160;together"), html);
        }

        @Test
        @DisplayName("escape special characters")
        void escapeSpecialCharacters() throws Exception {
            var html = convertString("<node>&</node>");
            assertMatch(rx("&lt;node&gt;&amp;&lt;/node&gt;"), html);
        }

        @Test
        @DisplayName("creates counter")
        void createsCounter() throws Exception {
            var input = "{counter:mycounter}";
            var doc = documentFromString(input);
            var output = doc.convert();
            assertEquals(1, get(doc.getAttributes(), "mycounter"));
            assertXpath("//p[text()=\"1\"]", output, 1);
        }

        @Test
        @DisplayName("creates counter silently")
        void createsCounterSilently() throws Exception {
            var input = "{counter2:mycounter}";
            var doc = documentFromString(input);
            var output = doc.convert();
            assertEquals(1, get(doc.getAttributes(), "mycounter"));
            assertXpath("//p[text()=\"1\"]", output, 0);
        }

        @Test
        @DisplayName("creates counter with numeric seed value")
        void createsCounterWithNumericSeedValue() throws Exception {
            var input = "{counter2:mycounter:10}";
            var doc = documentFromString(input);
            doc.convert();
            assertEquals(10, get(doc.getAttributes(), "mycounter"));
        }

        @Test
        @DisplayName("creates counter with character seed value")
        void createsCounterWithCharacterSeedValue() throws Exception {
            var input = "{counter2:mycounter:A}";
            var doc = documentFromString(input);
            doc.convert();
            assertEquals("A", get(doc.getAttributes(), "mycounter"));
        }

        @Test
        @DisplayName("can seed counter to start at 1")
        void canSeedCounterToStartAt1() throws Exception {
            var input = """
                    :mycounter: 0

                    {counter:mycounter}
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//p[text()=\"1\"]", output, 1);
        }

        @Test
        @DisplayName("can seed counter to start at A")
        void canSeedCounterToStartAtA() throws Exception {
            var input = """
                    :mycounter: @

                    {counter:mycounter}
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//p[text()=\"A\"]", output, 1);
        }

        @Test
        @DisplayName("increments counter with positive numeric value")
        void incrementsCounterWithPositiveNumericValue() throws Exception {
            var input = """
                    [subs=attributes]
                    ++++
                    {counter:mycounter:1}
                    {counter:mycounter}
                    {counter:mycounter}
                    {mycounter}
                    ++++
                    """;
            var doc = documentFromString(input, Options.of("standalone", false));
            var output = doc.convert();
            assertEquals(3, get(doc.getAttributes(), "mycounter"));
            assertEquals(list("1", "2", "3", "3"), map(lines(output), (String it) -> Strings.rstrip(it)));
        }

        @Test
        @DisplayName("increments counter with negative numeric value")
        void incrementsCounterWithNegativeNumericValue() throws Exception {
            var input = """
                    [subs=attributes]
                    ++++
                    {counter:mycounter:-2}
                    {counter:mycounter}
                    {counter:mycounter}
                    {mycounter}
                    ++++
                    """;
            var doc = documentFromString(input, Options.of("standalone", false));
            var output = doc.convert();
            assertEquals(0, get(doc.getAttributes(), "mycounter"));
            assertEquals(list("-2", "-1", "0", "0"), map(lines(output), (String it) -> Strings.rstrip(it)));
        }

        @Test
        @DisplayName("increments counter with ASCII character value")
        void incrementsCounterWithASCIICharacterValue() throws Exception {
            var input = """
                    [subs=attributes]
                    ++++
                    {counter:mycounter:A}
                    {counter:mycounter}
                    {counter:mycounter}
                    {mycounter}
                    ++++
                    """;
            var output = convertStringToEmbedded(input);
            assertEquals(list("A", "B", "C", "C"), map(lines(output), (String it) -> Strings.rstrip(it)));
        }

        @Test
        @DisplayName("increments counter with non-ASCII character value")
        void incrementsCounterWithNonASCIICharacterValue() throws Exception {
            var input = """
                    [subs=attributes]
                    ++++
                    {counter:mycounter:é}
                    {counter:mycounter}
                    {counter:mycounter}
                    {mycounter}
                    ++++
                    """;
            var output = convertStringToEmbedded(input);
            assertEquals(list("é", "ê", "ë", "ë"), map(lines(output), (String it) -> Strings.rstrip(it)));
        }

        @Test
        @DisplayName("increments counter with emoji character value")
        void incrementsCounterWithEmojiCharacterValue() throws Exception {
            var input = """
                    [subs=attributes]
                    ++++
                    {counter:smiley:😋}
                    {counter:smiley}
                    {counter:smiley}
                    {smiley}
                    ++++
                    """;
            var output = convertStringToEmbedded(input);
            assertEquals(list("😋", "😌", "😍", "😍"), map(lines(output), (String it) -> Strings.rstrip(it)));
        }

        @Test
        @DisplayName("increments counter with multi-character value")
        void incrementsCounterWithMultiCharacterValue() throws Exception {
            var input = """
                    [subs=attributes]
                    ++++
                    {counter:math:1x}
                    {counter:math}
                    {counter:math}
                    {math}
                    ++++
                    """;
            var output = convertStringToEmbedded(input);
            assertEquals(list("1x", "1y", "1z", "1z"), map(lines(output), (String it) -> Strings.rstrip(it)));
        }

        @Test
        @DisplayName("counter uses 0 as seed value if seed attribute is nil")
        void counterUses0AsSeedValueIfSeedAttributeIsNil() throws Exception {
            var input = """
                    :mycounter:

                    {counter:mycounter}

                    {mycounter}
                    """;
            var doc = documentFromString(input);
            var output = doc.convert(Options.of("standalone", false));
            assertEquals(1, get(doc.getAttributes(), "mycounter"));
            assertXpath("//p[text()=\"1\"]", output, 2);
        }

        @Test
        @DisplayName("counter value can be reset by attribute entry")
        void counterValueCanBeResetByAttributeEntry() throws Exception {
            var input = """
                    :mycounter:

                    before: {counter:mycounter} {counter:mycounter} {counter:mycounter}

                    :mycounter!:

                    after: {counter:mycounter}
                    """;
            var doc = documentFromString(input);
            var output = doc.convert(Options.of("standalone", false));
            assertEquals(1, get(doc.getAttributes(), "mycounter"));
            assertXpath("//p[text()=\"before: 1 2 3\"]", output, 1);
            assertXpath("//p[text()=\"after: 1\"]", output, 1);
        }

        @Test
        @DisplayName("counter value can be advanced by attribute entry")
        void counterValueCanBeAdvancedByAttributeEntry() throws Exception {
            var input = """
                    before: {counter:mycounter}

                    :mycounter: 10

                    after: {counter:mycounter}
                    """;
            var doc = documentFromString(input);
            var output = doc.convert(Options.of("standalone", false));
            assertEquals(11, get(doc.getAttributes(), "mycounter"));
            assertXpath("//p[text()=\"before: 1\"]", output, 1);
            assertXpath("//p[text()=\"after: 11\"]", output, 1);
        }

        @Test
        @DisplayName("nested document should use counter from parent document")
        void nestedDocumentShouldUseCounterFromParentDocument() throws Exception {
            var input = """
                    .Title for Foo
                    image::foo.jpg[]

                    [cols="2*a"]
                    |===
                    |
                    .Title for Bar
                    image::bar.jpg[]

                    |
                    .Title for Baz
                    image::baz.jpg[]
                    |===

                    .Title for Qux
                    image::qux.jpg[]
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//div[@class=\"title\"]", output, 4);
            assertXpath("//div[@class=\"title\"][text() = \"Figure 1. Title for Foo\"]", output, 1);
            assertXpath("//div[@class=\"title\"][text() = \"Figure 2. Title for Bar\"]", output, 1);
            assertXpath("//div[@class=\"title\"][text() = \"Figure 3. Title for Baz\"]", output, 1);
            assertXpath("//div[@class=\"title\"][text() = \"Figure 4. Title for Qux\"]", output, 1);
        }

        @Test
        @DisplayName("should not allow counter to modify locked attribute")
        void shouldNotAllowCounterToModifyLockedAttribute() throws Exception {
            var input = "{counter:foo:ignored} is not {foo}\n";
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("foo", "bar")));
            assertXpath("//p[text()=\"bas is not bar\"]", output, 1);
        }

        @Test
        @DisplayName("should not allow counter2 to modify locked attribute")
        void shouldNotAllowCounter2ToModifyLockedAttribute() throws Exception {
            var input = "{counter2:foo:ignored}{foo}\n";
            var output = convertStringToEmbedded(input, Options.of("attributes", Options.of("foo", "bar")));
            assertXpath("//p[text()=\"bar\"]", output, 1);
        }

        @Test
        @DisplayName("should not allow counter to modify built-in locked attribute")
        void shouldNotAllowCounterToModifyBuiltInLockedAttribute() throws Exception {
            var input = "{counter:max-include-depth:128} is one more than {max-include-depth}\n";
            var doc = documentFromString(input, Options.of("standalone", false));
            var output = doc.convert();
            assertXpath("//p[text()=\"65 is one more than 64\"]", output, 1);
            assertEquals(64, get(doc.getAttributes(), "max-include-depth"));
        }

        @Test
        @DisplayName("should not allow counter2 to modify built-in locked attribute")
        void shouldNotAllowCounter2ToModifyBuiltInLockedAttribute() throws Exception {
            var input = "{counter2:max-include-depth:128}{max-include-depth}\n";
            var doc = documentFromString(input, Options.of("standalone", false));
            var output = doc.convert();
            assertXpath("//p[text()=\"64\"]", output, 1);
            assertEquals(64, get(doc.getAttributes(), "max-include-depth"));
        }
    }

    @Nested
    @DisplayName("Block attributes")
    class BlockAttributes {

        @Test
        @DisplayName("parses named attribute with valid name")
        void parsesNamedAttributeWithValidName() throws Exception {
            var input = """
                    [normal,foo="bar",_foo="_bar",foo1="bar1",foo-foo="bar-bar",foo.foo="bar.bar"]
                    content
                    """;
            var block = blockFromString(input);
            assertEquals("bar", block.getAttribute("foo"));
            assertEquals("_bar", block.getAttribute("_foo"));
            assertEquals("bar1", block.getAttribute("foo1"));
            assertEquals("bar-bar", block.getAttribute("foo-foo"));
        }

        @Test
        @DisplayName("does not parse named attribute if name is invalid")
        void doesNotParseNamedAttributeIfNameIsInvalid() throws Exception {
            var input = """
                    [normal,foo.foo="bar.bar",-foo-foo="-bar-bar"]
                    content
                    """;
            var block = blockFromString(input);
            assertEquals("foo.foo=\"bar.bar\"", block.getAttributes().get("2"));
            assertEquals("-foo-foo=\"-bar-bar\"", block.getAttributes().get("3"));
        }

        @Test
        @DisplayName("positional attributes assigned to block")
        void positionalAttributesAssignedToBlock() throws Exception {
            var input = """
                    [quote, author, source]
                    ____
                    A famous quote.
                    ____
                    """;
            var doc = documentFromString(input);
            var qb = first(doc.getBlocks());
            assertEquals("quote", qb.getStyle());
            assertEquals("author", qb.getAttribute("attribution"));
            assertEquals("author", qb.getAttribute("attribution"));
            assertEquals("author", get(qb.getAttributes(), "attribution"));
            assertEquals("source", get(qb.getAttributes(), "citetitle"));
        }

        @Test
        @DisplayName("normal substitutions are performed on single-quoted positional attribute")
        void normalSubstitutionsArePerformedOnSingleQuotedPositionalAttribute() throws Exception {
            var input = """
                    [quote, author, 'http://wikipedia.org[source]']
                    ____
                    A famous quote.
                    ____
                    """;
            var doc = documentFromString(input);
            var qb = first(doc.getBlocks());
            assertEquals("quote", qb.getStyle());
            assertEquals("author", qb.getAttribute("attribution"));
            assertEquals("author", qb.getAttribute("attribution"));
            assertEquals("author", get(qb.getAttributes(), "attribution"));
            assertEquals("<a href=\"http://wikipedia.org\">source</a>", get(qb.getAttributes(), "citetitle"));
        }

        @Test
        @DisplayName("normal substitutions are performed on single-quoted named attribute")
        void normalSubstitutionsArePerformedOnSingleQuotedNamedAttribute() throws Exception {
            var input = """
                    [quote, author, citetitle='http://wikipedia.org[source]']
                    ____
                    A famous quote.
                    ____
                    """;
            var doc = documentFromString(input);
            var qb = first(doc.getBlocks());
            assertEquals("quote", qb.getStyle());
            assertEquals("author", qb.getAttribute("attribution"));
            assertEquals("author", qb.getAttribute("attribution"));
            assertEquals("author", get(qb.getAttributes(), "attribution"));
            assertEquals("<a href=\"http://wikipedia.org\">source</a>", get(qb.getAttributes(), "citetitle"));
        }

        @Test
        @DisplayName("normal substitutions are performed once on single-quoted named title attribute")
        void normalSubstitutionsArePerformedOnceOnSingleQuotedNamedTitleAttribute() throws Exception {
            var input = """
                    [title='*title*']
                    content
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("//*[@class=\"title\"]/strong[text()=\"title\"]", output, 1);
        }

        @Test
        @DisplayName("attribute list may not begin with space")
        void attributeListMayNotBeginWithSpace() throws Exception {
            var input = """
                    [ quote]
                    ____
                    A famous quote.
                    ____
                    """;
            var doc = documentFromString(input);
            var b1 = first(doc.getBlocks());
            assertEquals(list("[ quote]"), lines(b1));
        }

        @Test
        @DisplayName("attribute list may begin with comma")
        void attributeListMayBeginWithComma() throws Exception {
            var input = """
                    [, author, source]
                    ____
                    A famous quote.
                    ____
                    """;
            var doc = documentFromString(input);
            var qb = first(doc.getBlocks());
            assertEquals("quote", qb.getStyle());
            assertEquals("author", get(qb.getAttributes(), "attribution"));
            assertEquals("source", get(qb.getAttributes(), "citetitle"));
        }

        @Test
        @DisplayName("first attribute in list may be double quoted")
        void firstAttributeInListMayBeDoubleQuoted() throws Exception {
            var input = """
                    ["quote", "author", "source", role="famous"]
                    ____
                    A famous quote.
                    ____
                    """;
            var doc = documentFromString(input);
            var qb = first(doc.getBlocks());
            assertEquals("quote", qb.getStyle());
            assertEquals("author", get(qb.getAttributes(), "attribution"));
            assertEquals("source", get(qb.getAttributes(), "citetitle"));
            assertEquals("famous", get(qb.getAttributes(), "role"));
        }

        @Test
        @DisplayName("first attribute in list may be single quoted")
        void firstAttributeInListMayBeSingleQuoted() throws Exception {
            var input = """
                    ['quote', 'author', 'source', role='famous']
                    ____
                    A famous quote.
                    ____
                    """;
            var doc = documentFromString(input);
            var qb = first(doc.getBlocks());
            assertEquals("quote", qb.getStyle());
            assertEquals("author", get(qb.getAttributes(), "attribution"));
            assertEquals("source", get(qb.getAttributes(), "citetitle"));
            assertEquals("famous", get(qb.getAttributes(), "role"));
        }

        @Test
        @DisplayName("attribute with value None without quotes is ignored")
        void attributeWithValueNoneWithoutQuotesIsIgnored() throws Exception {
            var input = """
                    [id=None]
                    paragraph
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            assertFalsy(para.getAttributes().containsKey("id"));
        }

        @Test
        @DisplayName("role? returns true if role is assigned")
        void roleReturnsTrueIfRoleIsAssigned() throws Exception {
            var input = """
                    [role="lead"]
                    A paragraph
                    """;
            var doc = documentFromString(input);
            var p = first(doc.getBlocks());
            assertTruthy(p.isRole());
        }

        @Test
        @DisplayName("role? does not return true if role attribute is set on document")
        void roleDoesNotReturnTrueIfRoleAttributeIsSetOnDocument() throws Exception {
            var input = """
                    :role: lead

                    A paragraph
                    """;
            var doc = documentFromString(input);
            var p = first(doc.getBlocks());
            assertFalsy(p.isRole());
        }

        @Test
        @DisplayName("role? can check for exact role name match")
        void roleCanCheckForExactRoleNameMatch() throws Exception {
            var input = """
                    [role="lead"]
                    A paragraph
                    """;
            var doc = documentFromString(input);
            var p = first(doc.getBlocks());
            assertTruthy(p.isRole("lead"));
            var p2 = last(doc.getBlocks());
            assertFalsy(p2.isRole("final"));
        }

        @Test
        @DisplayName("has_role? can check for presence of role name")
        void has_roleCanCheckForPresenceOfRoleName() throws Exception {
            var input = """
                    [role="lead abstract"]
                    A paragraph
                    """;
            var doc = documentFromString(input);
            var p = first(doc.getBlocks());
            assertFalsy(p.isRole("lead"));
            assertTruthy(p.hasRole("lead"));
        }

        @Test
        @DisplayName("has_role? does not look for role defined as document attribute")
        void has_roleDoesNotLookForRoleDefinedAsDocumentAttribute() throws Exception {
            var input = """
                    :role: lead abstract

                    A paragraph
                    """;
            var doc = documentFromString(input);
            var p = first(doc.getBlocks());
            assertFalsy(p.hasRole("lead"));
        }

        @Test
        @DisplayName("roles returns array of role names")
        void rolesReturnsArrayOfRoleNames() throws Exception {
            var input = """
                    [role="story lead"]
                    A paragraph
                    """;
            var doc = documentFromString(input);
            var p = first(doc.getBlocks());
            assertEquals(list("story", "lead"), p.getRoles());
        }

        @Test
        @DisplayName("roles returns empty array if role attribute is not set")
        void rolesReturnsEmptyArrayIfRoleAttributeIsNotSet() throws Exception {
            var input = "a paragraph";
            var doc = documentFromString(input);
            var p = first(doc.getBlocks());
            assertEmpty(p.getRoles());
        }

        @Test
        @DisplayName("roles does not return value of roles document attribute")
        void rolesDoesNotReturnValueOfRolesDocumentAttribute() throws Exception {
            var input = """
                    :role: story lead

                    A paragraph
                    """;
            var doc = documentFromString(input);
            var p = first(doc.getBlocks());
            assertEmpty(p.getRoles());
        }

        @Test
        @DisplayName("roles= sets the role attribute on the node")
        void rolesSetsTheRoleAttributeOnTheNode() throws Exception {
            var doc = documentFromString("a paragraph");
            var p = first(doc.getBlocks());
            p.setRole("foobar");
            assertEquals("foobar", p.getAttribute("role"));
        }

        @Test
        @DisplayName("roles= coerces array value to a space-separated string")
        void rolesCoercesArrayValueToASpaceSeparatedString() throws Exception {
            var doc = documentFromString("a paragraph");
            var p = first(doc.getBlocks());
            p.setRole(list("foo", "bar"));
            assertEquals("foo bar", p.getAttribute("role"));
        }

        @Test
        @DisplayName("Attribute substitutions are performed on attribute list before parsing attributes")
        void attributeSubstitutionsArePerformedOnAttributeListBeforeParsingAttributes() throws Exception {
            var input = """
                    :lead: role="lead"

                    [{lead}]
                    A paragraph
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            assertEquals("lead", get(para.getAttributes(), "role"));
        }

        @Test
        @DisplayName("id, role and options attributes can be specified on block style using shorthand syntax")
        void idRoleAndOptionsAttributesCanBeSpecifiedOnBlockStyleUsingShorthandSyntax() throws Exception {
            var input = """
                    [literal#first.lead%step]
                    A literal paragraph.
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            assertEquals("literal", para.getContext());
            assertEquals("first", get(para.getAttributes(), "id"));
            assertEquals("lead", get(para.getAttributes(), "role"));
            assertTruthy(para.getAttributes().containsKey("step-option"));
            assertFalsy(para.getAttributes().containsKey("options"));
        }

        @Test
        @DisplayName("id, role and options attributes can be specified using shorthand syntax on block style using multiple block attribute lines")
        void idRoleAndOptionsAttributesCanBeSpecifiedUsingShorthandSyntaxOnBlockStyleUsingMultipleBlockAttributeLines() throws Exception {
            var input = """
                    [literal]
                    [#first]
                    [.lead]
                    [%step]
                    A literal paragraph.
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            assertEquals("literal", para.getContext());
            assertEquals("first", get(para.getAttributes(), "id"));
            assertEquals("lead", get(para.getAttributes(), "role"));
            assertTruthy(para.getAttributes().containsKey("step-option"));
            assertFalsy(para.getAttributes().containsKey("options"));
        }

        @Test
        @DisplayName("multiple roles and options can be specified in block style using shorthand syntax")
        void multipleRolesAndOptionsCanBeSpecifiedInBlockStyleUsingShorthandSyntax() throws Exception {
            var input = """
                    [.role1%option1.role2%option2]
                    Text
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            assertEquals("role1 role2", get(para.getAttributes(), "role"));
            assertTruthy(para.getAttributes().containsKey("option1-option"));
            assertTruthy(para.getAttributes().containsKey("option2-option"));
            assertFalsy(para.getAttributes().containsKey("options"));
        }

        @Test
        @DisplayName("options specified using shorthand syntax on block style across multiple lines should be additive")
        void optionsSpecifiedUsingShorthandSyntaxOnBlockStyleAcrossMultipleLinesShouldBeAdditive() throws Exception {
            var input = """
                    [%option1]
                    [%option2]
                    Text
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            assertTruthy(para.getAttributes().containsKey("option1-option"));
            assertTruthy(para.getAttributes().containsKey("option2-option"));
            assertFalsy(para.getAttributes().containsKey("options"));
        }

        @Test
        @DisplayName("roles specified using shorthand syntax on block style across multiple lines should be additive")
        void rolesSpecifiedUsingShorthandSyntaxOnBlockStyleAcrossMultipleLinesShouldBeAdditive() throws Exception {
            var input = """
                    [.role1]
                    [.role2.role3]
                    Text
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            assertEquals("role1 role2 role3", get(para.getAttributes(), "role"));
        }

        @Test
        @DisplayName("setting a role using the role attribute replaces any existing roles")
        void settingARoleUsingTheRoleAttributeReplacesAnyExistingRoles() throws Exception {
            var input = """
                    [.role1]
                    [role=role2]
                    [.role3]
                    Text
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            assertEquals("role2 role3", get(para.getAttributes(), "role"));
        }

        @Test
        @DisplayName("setting a role using the shorthand syntax on block style should not clear the ID")
        void settingARoleUsingTheShorthandSyntaxOnBlockStyleShouldNotClearTheID() throws Exception {
            var input = """
                    [#id]
                    [.role]
                    Text
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            assertEquals("id", para.getId());
            assertEquals("role", para.getRole());
        }

        @Test
        @DisplayName("a role can be added using add_role when the node has no roles")
        void aRoleCanBeAddedUsingAdd_roleWhenTheNodeHasNoRoles() throws Exception {
            var input = "A normal paragraph";
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            var res = para.addRole("role1");
            assertTruthy(res);
            assertEquals("role1", get(para.getAttributes(), "role"));
            assertTruthy(para.hasRole("role1"));
        }

        @Test
        @DisplayName("a role can be added using add_role when the node already has a role")
        void aRoleCanBeAddedUsingAdd_roleWhenTheNodeAlreadyHasARole() throws Exception {
            var input = """
                    [.role1]
                    A normal paragraph
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            var res = para.addRole("role2");
            assertTruthy(res);
            assertEquals("role1 role2", get(para.getAttributes(), "role"));
            assertTruthy(para.hasRole("role1"));
            assertTruthy(para.hasRole("role2"));
        }

        @Test
        @DisplayName("a role is not added using add_role if the node already has that role")
        void aRoleIsNotAddedUsingAdd_roleIfTheNodeAlreadyHasThatRole() throws Exception {
            var input = """
                    [.role1]
                    A normal paragraph
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            var res = para.addRole("role1");
            assertFalsy(res);
            assertEquals("role1", get(para.getAttributes(), "role"));
            assertTruthy(para.hasRole("role1"));
        }

        @Test
        @DisplayName("an existing role can be removed using remove_role")
        void anExistingRoleCanBeRemovedUsingRemove_role() throws Exception {
            var input = """
                    [.role1.role2]
                    A normal paragraph
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            var res = para.removeRole("role1");
            assertTruthy(res);
            assertEquals("role2", get(para.getAttributes(), "role"));
            assertTruthy(para.hasRole("role2"));
            assertFalsy(para.hasRole("role1"));
        }

        @Test
        @DisplayName("roles are removed when last role is removed using remove_role")
        void rolesAreRemovedWhenLastRoleIsRemovedUsingRemove_role() throws Exception {
            var input = """
                    [.role1]
                    A normal paragraph
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            var res = para.removeRole("role1");
            assertTruthy(res);
            assertFalsy(para.isRole());
            assertNull(get(para.getAttributes(), "role"));
            assertFalsy(para.hasRole("role1"));
        }

        @Test
        @DisplayName("roles are not changed when a non-existent role is removed using remove_role")
        void rolesAreNotChangedWhenANonExistentRoleIsRemovedUsingRemove_role() throws Exception {
            var input = """
                    [.role1]
                    A normal paragraph
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            var res = para.removeRole("role2");
            assertFalsy(res);
            assertEquals("role1", get(para.getAttributes(), "role"));
            assertTruthy(para.hasRole("role1"));
            assertFalsy(para.hasRole("role2"));
        }

        @Test
        @DisplayName("roles are not changed when using remove_role if the node has no roles")
        void rolesAreNotChangedWhenUsingRemove_roleIfTheNodeHasNoRoles() throws Exception {
            var input = "A normal paragraph";
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            var res = para.removeRole("role1");
            assertFalsy(res);
            assertNull(get(para.getAttributes(), "role"));
            assertFalsy(para.hasRole("role1"));
        }

        @Test
        @DisplayName("option can be specified in first position of block style using shorthand syntax")
        void optionCanBeSpecifiedInFirstPositionOfBlockStyleUsingShorthandSyntax() throws Exception {
            var input = """
                    [%interactive]
                    - [x] checked
                    """;
            var doc = documentFromString(input);
            var list = first(doc.getBlocks());
            assertTruthy(list.getAttributes().containsKey("interactive-option"));
            assertFalsy(list.getAttributes().containsKey("options"));
        }

        @Test
        @DisplayName("id and role attributes can be specified on section style using shorthand syntax")
        void idAndRoleAttributesCanBeSpecifiedOnSectionStyleUsingShorthandSyntax() throws Exception {
            var input = """
                    [dedication#dedication.small]
                    == Section
                    Content.
                    """;
            var output = convertStringToEmbedded(input);
            assertXpath("/div[@class=\"sect1 small\"]", output, 1);
            assertXpath("/div[@class=\"sect1 small\"]/h2[@id=\"dedication\"]", output, 1);
        }

        @Test
        @DisplayName("id attribute specified using shorthand syntax should not create a special section")
        void idAttributeSpecifiedUsingShorthandSyntaxShouldNotCreateASpecialSection() throws Exception {
            var input = """
                    [#idname]
                    == Section

                    content
                    """;
            var doc = documentFromString(input, Options.of("backend", "docbook"));
            var section = (Section) doc.getBlocks().get(0);
            assertNotNull(section);
            assertEquals("section", section.getContext());
            assertFalsy(section.isSpecial());
            var output = doc.convert();
            assertCss("article:root > section", output, 1);
            assertCss("article:root > section[xml|id=\"idname\"]", output, 1);
        }

        @Test
        @DisplayName("Block attributes are additive")
        void blockAttributesAreAdditive() throws Exception {
            var input = """
                    [id='foo']
                    [role='lead']
                    A paragraph.
                    """;
            var doc = documentFromString(input);
            var para = first(doc.getBlocks());
            assertEquals("foo", para.getId());
            assertEquals("lead", get(para.getAttributes(), "role"));
        }

        @Test
        @DisplayName("Last wins for id attribute")
        void lastWinsForIdAttribute() throws Exception {
            var input = """
                    [[bar]]
                    [[foo]]
                    == Section

                    paragraph

                    [[baz]]
                    [id='coolio']
                    === Section
                    """;
            var doc = documentFromString(input);
            var sec = doc.getFirstSection();
            assertEquals("foo", sec.getId());
            var subsec = last(sec.getBlocks());
            assertEquals("coolio", subsec.getId());
        }

        @Test
        @DisplayName("trailing block attributes transfer to the following section")
        void trailingBlockAttributesTransferToTheFollowingSection() throws Exception {
            var input = """
                    [[one]]

                    == Section One

                    paragraph

                    [[sub]]
                    // try to mess this up!

                    === Sub-section

                    paragraph

                    [role='classy']

                    ////
                    block comment
                    ////

                    == Section Two

                    content
                    """;
            var doc = documentFromString(input);
            var sectionOne = first(doc.getBlocks());
            assertEquals("one", sectionOne.getId());
            var subsection = last(sectionOne.getBlocks());
            assertEquals("sub", subsection.getId());
            var sectionTwo = last(doc.getBlocks());
            assertEquals("classy", sectionTwo.getAttribute("role"));
        }
    }
}
