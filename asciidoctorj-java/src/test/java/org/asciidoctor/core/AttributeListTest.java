package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

@DisplayName("AttributeList")
public class AttributeListTest extends TestHelper {

    @Test
    @DisplayName("collect unnamed attribute")
    void collectUnnamedAttribute() throws Exception {
        var attributes = Options.of();
        var line = "quote";
        var expected = Options.of("1", "quote");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect unnamed attribute double-quoted")
    void collectUnnamedAttributeDoubleQuoted() throws Exception {
        var attributes = Options.of();
        var line = "\"quote\"";
        var expected = Options.of("1", "quote");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect empty unnamed attribute double-quoted")
    void collectEmptyUnnamedAttributeDoubleQuoted() throws Exception {
        var attributes = Options.of();
        var line = "\"\"";
        var expected = Options.of("1", "");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect unnamed attribute double-quoted containing escaped quote")
    void collectUnnamedAttributeDoubleQuotedContainingEscapedQuote() throws Exception {
        var attributes = Options.of();
        var line = "\"ba\\\"zaar\"";
        var expected = Options.of("1", "ba\"zaar");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect unnamed attribute single-quoted")
    void collectUnnamedAttributeSingleQuoted() throws Exception {
        var attributes = Options.of();
        var line = "'quote'";
        var expected = Options.of("1", "quote");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect empty unnamed attribute single-quoted")
    void collectEmptyUnnamedAttributeSingleQuoted() throws Exception {
        var attributes = Options.of();
        var line = "''";
        var expected = Options.of("1", "");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect isolated single quote positional attribute")
    void collectIsolatedSingleQuotePositionalAttribute() throws Exception {
        var attributes = Options.of();
        var line = "'";
        var expected = Options.of("1", "'");
        var doc = new Document(new ArrayList<String>(), new LinkedHashMap<>()) {
            @Override
            public String applySubs(String text) {
                throw new AssertionError("apply_subs should not be called");
            }
        };
        new AttributeList(line, doc).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect isolated single quote attribute value")
    void collectIsolatedSingleQuoteAttributeValue() throws Exception {
        var attributes = Options.of();
        var line = "name='";
        var expected = Options.of("name", "'");
        var doc = new Document(new ArrayList<String>(), new LinkedHashMap<>()) {
            @Override
            public String applySubs(String text) {
                throw new AssertionError("apply_subs should not be called");
            }
        };
        new AttributeList(line, doc).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect attribute value as is if it has only leading single quote")
    void collectAttributeValueAsIsIfItHasOnlyLeadingSingleQuote() throws Exception {
        var attributes = Options.of();
        var line = "name='{val}";
        var expected = Options.of("name", "'{val}");
        var doc = new Document(new ArrayList<String>(), Options.of("attributes", Options.of("val", "val"))) {
            @Override
            public String applySubs(String text) {
                throw new AssertionError("apply_subs should not be called");
            }
        };
        new AttributeList(line, doc).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect unnamed attribute single-quoted containing escaped quote")
    void collectUnnamedAttributeSingleQuotedContainingEscapedQuote() throws Exception {
        var attributes = Options.of();
        var line = "'ba\\'zaar'";
        var expected = Options.of("1", "ba'zaar");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect unnamed attribute with dangling delimiter")
    void collectUnnamedAttributeWithDanglingDelimiter() throws Exception {
        var attributes = Options.of();
        var line = "quote , ";
        var expected = Options.of("1", "quote", "2", null);
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect unnamed attribute in second position after empty attribute")
    void collectUnnamedAttributeInSecondPositionAfterEmptyAttribute() throws Exception {
        var attributes = Options.of();
        var line = ", John Smith";
        var expected = Options.of("1", null, "2", "John Smith");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect unnamed attributes")
    void collectUnnamedAttributes() throws Exception {
        var attributes = Options.of();
        var line = "first, second one, third";
        var expected = Options.of("1", "first", "2", "second one", "3", "third");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect blank unnamed attributes")
    void collectBlankUnnamedAttributes() throws Exception {
        var attributes = Options.of();
        var line = "first,,third,";
        var expected = Options.of("1", "first", "2", null, "3", "third", "4", null);
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect unnamed attribute enclosed in equal signs")
    void collectUnnamedAttributeEnclosedInEqualSigns() throws Exception {
        var attributes = Options.of();
        var line = "=foo=";
        var expected = Options.of("1", "=foo=");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect named attribute")
    void collectNamedAttribute() throws Exception {
        var attributes = Options.of();
        var line = "foo=bar";
        var expected = Options.of("foo", "bar");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect named attribute double-quoted")
    void collectNamedAttributeDoubleQuoted() throws Exception {
        var attributes = Options.of();
        var line = "foo=\"bar\"";
        var expected = Options.of("foo", "bar");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect named attribute with double-quoted empty value")
    void collectNamedAttributeWithDoubleQuotedEmptyValue() throws Exception {
        var attributes = Options.of();
        var line = "height=100,caption=\"\",link=\"images/octocat.png\"";
        var expected = Options.of("height", "100", "caption", "", "link", "images/octocat.png");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect named attribute single-quoted")
    void collectNamedAttributeSingleQuoted() throws Exception {
        var attributes = Options.of();
        var line = "foo='bar'";
        var expected = Options.of("foo", "bar");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect named attribute with single-quoted empty value")
    void collectNamedAttributeWithSingleQuotedEmptyValue() throws Exception {
        var attributes = Options.of();
        var line = "height=100,caption='',link='images/octocat.png'";
        var expected = Options.of("height", "100", "caption", "", "link", "images/octocat.png");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect single named attribute with empty value")
    void collectSingleNamedAttributeWithEmptyValue() throws Exception {
        var attributes = Options.of();
        var line = "foo=";
        var expected = Options.of("foo", "");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect single named attribute with empty value when followed by other attributes")
    void collectSingleNamedAttributeWithEmptyValueWhenFollowedByOtherAttributes() throws Exception {
        var attributes = Options.of();
        var line = "foo=,bar=baz";
        var expected = Options.of("foo", "", "bar", "baz");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect named attributes unquoted")
    void collectNamedAttributesUnquoted() throws Exception {
        var attributes = Options.of();
        var line = "first=value, second=two, third=3";
        var expected = Options.of("first", "value", "second", "two", "third", "3");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect named attributes quoted")
    void collectNamedAttributesQuoted() throws Exception {
        var attributes = Options.of();
        var line = "first='value', second=\"value two\", third=three";
        var expected = Options.of("first", "value", "second", "value two", "third", "three");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect named attributes quoted containing non-semantic spaces")
    void collectNamedAttributesQuotedContainingNonSemanticSpaces() throws Exception {
        var attributes = Options.of();
        var line = "     first    =     'value', second     =\"value two\"     , third=       three      ";
        var expected = Options.of("first", "value", "second", "value two", "third", "three");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect mixed named and unnamed attributes")
    void collectMixedNamedAndUnnamedAttributes() throws Exception {
        var attributes = Options.of();
        var line = "first, second=\"value two\", third=three, Sherlock Holmes";
        var expected = Options.of("1", "first", "second", "value two", "third", "three", "4", "Sherlock Holmes");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect mixed empty named and blank unnamed attributes")
    void collectMixedEmptyNamedAndBlankUnnamedAttributes() throws Exception {
        var attributes = Options.of();
        var line = "first,,third=,,fifth=five";
        var expected = Options.of("1", "first", "2", null, "third", "", "4", null, "fifth", "five");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect options attribute")
    void collectOptionsAttribute() throws Exception {
        var attributes = Options.of();
        var line = "quote, options='opt1,,opt2 , opt3'";
        var expected = Options.of("1", "quote", "opt1-option", "", "opt2-option", "", "opt3-option", "");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect opts attribute as options")
    void collectOptsAttributeAsOptions() throws Exception {
        var attributes = Options.of();
        var line = "quote, opts='opt1,,opt2 , opt3'";
        var expected = Options.of("1", "quote", "opt1-option", "", "opt2-option", "", "opt3-option", "");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("should ignore options attribute if empty")
    void shouldIgnoreOptionsAttributeIfEmpty() throws Exception {
        var attributes = Options.of();
        var line = "quote, opts=";
        var expected = Options.of("1", "quote");
        new AttributeList(line).parseInto(attributes);
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("collect and rekey unnamed attributes")
    void collectAndRekeyUnnamedAttributes() throws Exception {
        var attributes = Options.of();
        var line = "first, second one, third, fourth";
        var expected = Options.of("1", "first", "2", "second one", "3", "third", "4", "fourth", "a", "first", "b", "second one", "c", "third");
        new AttributeList(line).parseInto(attributes, list("a", "b", "c"));
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("should not assign nil to attribute mapped to missing positional attribute")
    void shouldNotAssignNilToAttributeMappedToMissingPositionalAttribute() throws Exception {
        var attributes = Options.of();
        var line = "alt text,,100";
        var expected = Options.of("1", "alt text", "2", null, "3", "100", "alt", "alt text", "height", "100");
        new AttributeList(line).parseInto(attributes, list("alt", "width", "height"));
        assertEquals(expected, attributes);
    }

    @Test
    @DisplayName("rekey positional attributes")
    void rekeyPositionalAttributes() throws Exception {
        var attributes = Options.of("1", "source", "2", "java");
        var expected = Options.of("1", "source", "2", "java", "style", "source", "language", "java");
        AttributeList.rekey(attributes, list("style", "language", "linenums"));
        assertEquals(expected, attributes);
    }
}
