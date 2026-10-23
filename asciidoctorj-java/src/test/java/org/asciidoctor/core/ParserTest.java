package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

@DisplayName("Parser")
public class ParserTest extends TestHelper {

    @Test
    @DisplayName("is_section_title?")
    void is_section_title() throws Exception {
        assertTruthy(Parser.isSectionTitle("AsciiDoc Home Page", "=================="));
        assertTruthy(Parser.isSectionTitle("=== AsciiDoc Home Page"));
    }

    @Test
    @DisplayName("sanitize attribute name")
    void sanitizeAttributeName() throws Exception {
        assertEquals("foobar", Parser.sanitizeAttributeName("Foo Bar"));
        assertEquals("foo", Parser.sanitizeAttributeName("foo"));
        assertEquals("foo3-bar", Parser.sanitizeAttributeName("Foo 3^ # - Bar["));
    }

    @Test
    @DisplayName("store attribute with value")
    void storeAttributeWithValue() throws Exception {
        var masgn = Parser.storeAttribute("foo", "bar");
        var attrName = get(masgn, 0);
        var attrValue = get(masgn, 1);
        assertEquals("foo", attrName);
        assertEquals("bar", attrValue);
    }

    @Test
    @DisplayName("store attribute with negated value")
    void storeAttributeWithNegatedValue() throws Exception {
        for (var entry : entries(Options.of("foo!", null, "!foo", null, "foo", null))) {
            var name = entry.getKey();
            var value = entry.getValue();
            var masgn = Parser.storeAttribute(name, (String) value);
            var attrName = get(masgn, 0);
            var attrValue = get(masgn, 1);
            assertEquals(sub(name, "!", ""), attrName);
            assertNull(attrValue);
        }
    }

    @Test
    @DisplayName("store accessible attribute on document with value")
    void storeAccessibleAttributeOnDocumentWithValue() throws Exception {
        var doc = emptyDocument();
        doc.setDocumentAttribute("foo", "baz");
        var attrs = Options.of();
        var masgn = Parser.storeAttribute("foo", "bar", doc, attrs);
        var attrName = get(masgn, 0);
        var attrValue = get(masgn, 1);
        assertEquals("foo", attrName);
        assertEquals("bar", attrValue);
        assertEquals("bar", doc.getAttribute("foo"));
        assertTruthy(attrs.containsKey(Document.ATTRIBUTE_ENTRIES_KEY));
        assertEquals(1, size(attributeEntries(attrs)));
        assertEquals("foo", attributeEntries(attrs).get(0).getName());
        assertEquals("bar", attributeEntries(attrs).get(0).getValue());
    }

    @Test
    @DisplayName("store accessible attribute on document with value that contains attribute reference")
    void storeAccessibleAttributeOnDocumentWithValueThatContainsAttributeReference() throws Exception {
        var doc = emptyDocument();
        doc.setDocumentAttribute("foo", "baz");
        doc.setDocumentAttribute("release", "ultramega");
        var attrs = Options.of();
        var masgn = Parser.storeAttribute("foo", "{release}", doc, attrs);
        var attrName = get(masgn, 0);
        var attrValue = get(masgn, 1);
        assertEquals("foo", attrName);
        assertEquals("ultramega", attrValue);
        assertEquals("ultramega", doc.getAttribute("foo"));
        assertTruthy(attrs.containsKey(Document.ATTRIBUTE_ENTRIES_KEY));
        assertEquals(1, size(attributeEntries(attrs)));
        assertEquals("foo", attributeEntries(attrs).get(0).getName());
        assertEquals("ultramega", attributeEntries(attrs).get(0).getValue());
    }

    @Test
    @DisplayName("store inaccessible attribute on document with value")
    void storeInaccessibleAttributeOnDocumentWithValue() throws Exception {
        var doc = emptyDocument(Options.of("attributes", Options.of("foo", "baz")));
        var attrs = Options.of();
        var masgn = Parser.storeAttribute("foo", "bar", doc, attrs);
        var attrName = get(masgn, 0);
        var attrValue = get(masgn, 1);
        assertEquals("foo", attrName);
        assertEquals("bar", attrValue);
        assertEquals("baz", doc.getAttribute("foo"));
        assertFalsy(attrs.containsKey(Document.ATTRIBUTE_ENTRIES_KEY));
    }

    @Test
    @DisplayName("store accessible attribute on document with negated value")
    void storeAccessibleAttributeOnDocumentWithNegatedValue() throws Exception {
        for (var entry : entries(Options.of("foo!", null, "!foo", null, "foo", null))) {
            var name = entry.getKey();
            var value = entry.getValue();
            var doc = emptyDocument();
            doc.setDocumentAttribute("foo", "baz");
            var attrs = Options.of();
            var masgn = Parser.storeAttribute(name, (String) value, doc, attrs);
            var attrName = get(masgn, 0);
            var attrValue = get(masgn, 1);
            assertEquals(sub(name, "!", ""), attrName);
            assertNull(attrValue);
            assertTruthy(attrs.containsKey(Document.ATTRIBUTE_ENTRIES_KEY));
            assertEquals(1, size(attributeEntries(attrs)));
            assertEquals("foo", attributeEntries(attrs).get(0).getName());
            assertNull(attributeEntries(attrs).get(0).getValue());
        }
    }

    @Test
    @DisplayName("store inaccessible attribute on document with negated value")
    void storeInaccessibleAttributeOnDocumentWithNegatedValue() throws Exception {
        for (var entry : entries(Options.of("foo!", null, "!foo", null, "foo", null))) {
            var name = entry.getKey();
            var value = entry.getValue();
            var doc = emptyDocument(Options.of("attributes", Options.of("foo", "baz")));
            var attrs = Options.of();
            var masgn = Parser.storeAttribute(name, (String) value, doc, attrs);
            var attrName = get(masgn, 0);
            var attrValue = get(masgn, 1);
            assertEquals(sub(name, "!", ""), attrName);
            assertNull(attrValue);
            assertFalsy(attrs.containsKey(Document.ATTRIBUTE_ENTRIES_KEY));
        }
    }

    @Test
    @DisplayName("parse style attribute with id and role")
    void parseStyleAttributeWithIdAndRole() throws Exception {
        var attributes = Options.of("1", "style#id.role");
        var style = Parser.parseStyleAttribute(attributes);
        assertEquals("style", style);
        assertEquals("style", get(attributes, "style"));
        assertEquals("id", get(attributes, "id"));
        assertEquals("role", get(attributes, "role"));
        assertEquals("style#id.role", attributes.get("1"));
    }

    @Test
    @DisplayName("parse style attribute with style, role, id and option")
    void parseStyleAttributeWithStyleRoleIdAndOption() throws Exception {
        var attributes = Options.of("1", "style.role#id%fragment");
        var style = Parser.parseStyleAttribute(attributes);
        assertEquals("style", style);
        assertEquals("style", get(attributes, "style"));
        assertEquals("id", get(attributes, "id"));
        assertEquals("role", get(attributes, "role"));
        assertEquals("", get(attributes, "fragment-option"));
        assertEquals("style.role#id%fragment", attributes.get("1"));
        assertFalsy(attributes.containsKey("options"));
    }

    @Test
    @DisplayName("parse style attribute with style, id and multiple roles")
    void parseStyleAttributeWithStyleIdAndMultipleRoles() throws Exception {
        var attributes = Options.of("1", "style#id.role1.role2");
        var style = Parser.parseStyleAttribute(attributes);
        assertEquals("style", style);
        assertEquals("style", get(attributes, "style"));
        assertEquals("id", get(attributes, "id"));
        assertEquals("role1 role2", get(attributes, "role"));
        assertEquals("style#id.role1.role2", attributes.get("1"));
    }

    @Test
    @DisplayName("parse style attribute with style, multiple roles and id")
    void parseStyleAttributeWithStyleMultipleRolesAndId() throws Exception {
        var attributes = Options.of("1", "style.role1.role2#id");
        var style = Parser.parseStyleAttribute(attributes);
        assertEquals("style", style);
        assertEquals("style", get(attributes, "style"));
        assertEquals("id", get(attributes, "id"));
        assertEquals("role1 role2", get(attributes, "role"));
        assertEquals("style.role1.role2#id", attributes.get("1"));
    }

    @Test
    @DisplayName("parse style attribute with positional and original style")
    void parseStyleAttributeWithPositionalAndOriginalStyle() throws Exception {
        var attributes = Options.of("1", "new_style", "style", "original_style");
        var style = Parser.parseStyleAttribute(attributes);
        assertEquals("new_style", style);
        assertEquals("new_style", get(attributes, "style"));
        assertEquals("new_style", attributes.get("1"));
    }

    @Test
    @DisplayName("parse style attribute with id and role only")
    void parseStyleAttributeWithIdAndRoleOnly() throws Exception {
        var attributes = Options.of("1", "#id.role");
        var style = Parser.parseStyleAttribute(attributes);
        assertNull(style);
        assertEquals("id", get(attributes, "id"));
        assertEquals("role", get(attributes, "role"));
        assertEquals("#id.role", attributes.get("1"));
    }

    @Test
    @DisplayName("parse empty style attribute")
    void parseEmptyStyleAttribute() throws Exception {
        var attributes = Options.of("1", null);
        var style = Parser.parseStyleAttribute(attributes);
        assertNull(style);
        assertNull(get(attributes, "id"));
        assertNull(get(attributes, "role"));
        assertNull(attributes.get("1"));
    }

    @Test
    @DisplayName("parse style attribute with option should preserve existing options")
    void parseStyleAttributeWithOptionShouldPreserveExistingOptions() throws Exception {
        var attributes = Options.of("1", "%header", "footer-option", "");
        var style = Parser.parseStyleAttribute(attributes);
        assertNull(style);
        assertEquals("", get(attributes, "header-option"));
        assertEquals("", get(attributes, "footer-option"));
    }

    @Test
    @DisplayName("parse author first")
    void parseAuthorFirst() throws Exception {
        var metadata = parseHeaderMetadata("Stuart");
        assertEquals(5, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("Stuart", get(metadata, "firstname"));
        assertEquals("S", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse author first last")
    void parseAuthorFirstLast() throws Exception {
        var metadata = parseHeaderMetadata("Yukihiro Matsumoto");
        assertEquals(6, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Yukihiro Matsumoto", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("Yukihiro", get(metadata, "firstname"));
        assertEquals("Matsumoto", get(metadata, "lastname"));
        assertEquals("YM", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse author first middle last")
    void parseAuthorFirstMiddleLast() throws Exception {
        var metadata = parseHeaderMetadata("David Heinemeier Hansson");
        assertEquals(7, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("David Heinemeier Hansson", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("David", get(metadata, "firstname"));
        assertEquals("Heinemeier", get(metadata, "middlename"));
        assertEquals("Hansson", get(metadata, "lastname"));
        assertEquals("DHH", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse author first middle last email")
    void parseAuthorFirstMiddleLastEmail() throws Exception {
        var metadata = parseHeaderMetadata("David Heinemeier Hansson <rails@ruby-lang.org>");
        assertEquals(8, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("David Heinemeier Hansson", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("David", get(metadata, "firstname"));
        assertEquals("Heinemeier", get(metadata, "middlename"));
        assertEquals("Hansson", get(metadata, "lastname"));
        assertEquals("rails@ruby-lang.org", get(metadata, "email"));
        assertEquals("DHH", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse author first email")
    void parseAuthorFirstEmail() throws Exception {
        var metadata = parseHeaderMetadata("Stuart <founder@asciidoc.org>");
        assertEquals(6, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Stuart", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("Stuart", get(metadata, "firstname"));
        assertEquals("founder@asciidoc.org", get(metadata, "email"));
        assertEquals("S", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse author first last email")
    void parseAuthorFirstLastEmail() throws Exception {
        var metadata = parseHeaderMetadata("Stuart Rackham <founder@asciidoc.org>");
        assertEquals(7, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Stuart Rackham", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("Stuart", get(metadata, "firstname"));
        assertEquals("Rackham", get(metadata, "lastname"));
        assertEquals("founder@asciidoc.org", get(metadata, "email"));
        assertEquals("SR", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse author with hyphen")
    void parseAuthorWithHyphen() throws Exception {
        var metadata = parseHeaderMetadata("Tim Berners-Lee <founder@www.org>");
        assertEquals(7, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Tim Berners-Lee", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("Tim", get(metadata, "firstname"));
        assertEquals("Berners-Lee", get(metadata, "lastname"));
        assertEquals("founder@www.org", get(metadata, "email"));
        assertEquals("TB", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse author with single quote")
    void parseAuthorWithSingleQuote() throws Exception {
        var metadata = parseHeaderMetadata("Stephen O'Grady <founder@redmonk.com>");
        assertEquals(7, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Stephen O'Grady", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("Stephen", get(metadata, "firstname"));
        assertEquals("O'Grady", get(metadata, "lastname"));
        assertEquals("founder@redmonk.com", get(metadata, "email"));
        assertEquals("SO", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse author with dotted initial")
    void parseAuthorWithDottedInitial() throws Exception {
        var metadata = parseHeaderMetadata("Heiko W. Rupp <hwr@example.de>");
        assertEquals(8, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Heiko W. Rupp", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("Heiko", get(metadata, "firstname"));
        assertEquals("W.", get(metadata, "middlename"));
        assertEquals("Rupp", get(metadata, "lastname"));
        assertEquals("hwr@example.de", get(metadata, "email"));
        assertEquals("HWR", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse author with underscore")
    void parseAuthorWithUnderscore() throws Exception {
        var metadata = parseHeaderMetadata("Tim_E Fella");
        assertEquals(6, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Tim E Fella", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("Tim E", get(metadata, "firstname"));
        assertEquals("Fella", get(metadata, "lastname"));
        assertEquals("TF", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse author name with letters outside basic latin")
    void parseAuthorNameWithLettersOutsideBasicLatin() throws Exception {
        var metadata = parseHeaderMetadata("Stéphane Brontë");
        assertEquals(6, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Stéphane Brontë", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("Stéphane", get(metadata, "firstname"));
        assertEquals("Brontë", get(metadata, "lastname"));
        assertEquals("SB", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse ideographic author names")
    void parseIdeographicAuthorNames() throws Exception {
        var metadata = parseHeaderMetadata("李 四 <si.li@example.com>");
        assertEquals(7, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("李 四", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("李", get(metadata, "firstname"));
        assertEquals("四", get(metadata, "lastname"));
        assertEquals("si.li@example.com", get(metadata, "email"));
        assertEquals("李四", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse author condenses whitespace")
    void parseAuthorCondensesWhitespace() throws Exception {
        var metadata = parseHeaderMetadata("Stuart       Rackham     <founder@asciidoc.org>");
        assertEquals(7, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Stuart Rackham", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("Stuart", get(metadata, "firstname"));
        assertEquals("Rackham", get(metadata, "lastname"));
        assertEquals("founder@asciidoc.org", get(metadata, "email"));
        assertEquals("SR", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse invalid author line becomes author")
    void parseInvalidAuthorLineBecomesAuthor() throws Exception {
        var metadata = parseHeaderMetadata("   Stuart       Rackham, founder of AsciiDoc   <founder@asciidoc.org>");
        assertEquals(5, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Stuart Rackham, founder of AsciiDoc <founder@asciidoc.org>", get(metadata, "author"));
        assertEquals(get(metadata, "author"), get(metadata, "authors"));
        assertEquals("Stuart Rackham, founder of AsciiDoc <founder@asciidoc.org>", get(metadata, "firstname"));
        assertEquals("S", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("parse multiple authors")
    void parseMultipleAuthors() throws Exception {
        var metadata = parseHeaderMetadata("Doc Writer <doc.writer@asciidoc.org>; John Smith <john.smith@asciidoc.org>");
        assertEquals(2, get(metadata, "authorcount"));
        assertEquals("Doc Writer, John Smith", get(metadata, "authors"));
        assertEquals("Doc Writer", get(metadata, "author"));
        assertEquals("Doc Writer", get(metadata, "author_1"));
        assertEquals("John Smith", get(metadata, "author_2"));
    }

    @Test
    @DisplayName("should not parse multiple authors if semi-colon is not followed by space")
    void shouldNotParseMultipleAuthorsIfSemiColonIsNotFollowedBySpace() throws Exception {
        var metadata = parseHeaderMetadata("Joe Doe;Smith Johnson");
        assertEquals(1, get(metadata, "authorcount"));
    }

    @Test
    @DisplayName("skips blank author entries in implicit author line")
    void skipsBlankAuthorEntriesInImplicitAuthorLine() throws Exception {
        var metadata = parseHeaderMetadata("Doc Writer; ; John Smith <john.smith@asciidoc.org>;");
        assertEquals(2, get(metadata, "authorcount"));
        assertEquals("Doc Writer", get(metadata, "author_1"));
        assertEquals("John Smith", get(metadata, "author_2"));
    }

    @Test
    @DisplayName("parse name with more than 3 parts in author attribute")
    void parseNameWithMoreThan3PartsInAuthorAttribute() throws Exception {
        var doc = emptyDocument();
        parseHeaderMetadata(":author: Leroy  Harold  Scherer,  Jr.", doc);
        assertEquals("Leroy Harold Scherer, Jr.", get(doc.getAttributes(), "author"));
        assertEquals("Leroy", get(doc.getAttributes(), "firstname"));
        assertEquals("Harold", get(doc.getAttributes(), "middlename"));
        assertEquals("Scherer, Jr.", get(doc.getAttributes(), "lastname"));
    }

    @Test
    @DisplayName("use explicit authorinitials if set after implicit author line")
    void useExplicitAuthorinitialsIfSetAfterImplicitAuthorLine() throws Exception {
        var input = """
                Jean-Claude Van Damme
                :authorinitials: JCVD
                """;
        var doc = emptyDocument();
        parseHeaderMetadata(input, doc);
        assertEquals("JCVD", get(doc.getAttributes(), "authorinitials"));
    }

    @Test
    @DisplayName("use explicit authorinitials if set after author attribute")
    void useExplicitAuthorinitialsIfSetAfterAuthorAttribute() throws Exception {
        var input = """
                :author: Jean-Claude Van Damme
                :authorinitials: JCVD
                """;
        var doc = emptyDocument();
        parseHeaderMetadata(input, doc);
        assertEquals("JCVD", get(doc.getAttributes(), "authorinitials"));
    }

    @Test
    @DisplayName("use implicit authors if value of authors attribute matches computed value")
    void useImplicitAuthorsIfValueOfAuthorsAttributeMatchesComputedValue() throws Exception {
        var input = """
                Doc Writer; Junior Writer
                :authors: Doc Writer, Junior Writer
                """;
        var doc = emptyDocument();
        parseHeaderMetadata(input, doc);
        assertEquals("Doc Writer, Junior Writer", get(doc.getAttributes(), "authors"));
        assertEquals("Doc Writer", get(doc.getAttributes(), "author_1"));
        assertEquals("Junior Writer", get(doc.getAttributes(), "author_2"));
    }

    @Test
    @DisplayName("replace implicit authors if value of authors attribute does not match computed value")
    void replaceImplicitAuthorsIfValueOfAuthorsAttributeDoesNotMatchComputedValue() throws Exception {
        var input = """
                Doc Writer; Junior Writer
                :authors: Stuart Rackham; Dan Allen; Sarah White
                """;
        var doc = emptyDocument();
        var metadata = parseHeaderMetadata(input, doc);
        assertEquals(3, get(metadata, "authorcount"));
        assertEquals(3, get(doc.getAttributes(), "authorcount"));
        assertEquals("Stuart Rackham, Dan Allen, Sarah White", get(doc.getAttributes(), "authors"));
        assertEquals("Stuart Rackham", get(doc.getAttributes(), "author_1"));
        assertEquals("Dan Allen", get(doc.getAttributes(), "author_2"));
        assertEquals("Sarah White", get(doc.getAttributes(), "author_3"));
    }

    @Test
    @DisplayName("sets authorcount to 0 if document has no authors")
    void setsAuthorcountTo0IfDocumentHasNoAuthors() throws Exception {
        var input = "";
        var doc = emptyDocument();
        var metadata = parseHeaderMetadata(input, doc);
        assertEquals(0, get(doc.getAttributes(), "authorcount"));
        assertEquals(0, get(metadata, "authorcount"));
    }

    @Test
    @DisplayName("returns empty hash if document has no authors and invoked without document")
    void returnsEmptyHashIfDocumentHasNoAuthorsAndInvokedWithoutDocument() throws Exception {
        var metadata = parseHeaderMetadata("");
        assertEmpty(metadata);
    }

    @Test
    @DisplayName("does not drop name joiner when using multiple authors")
    void doesNotDropNameJoinerWhenUsingMultipleAuthors() throws Exception {
        var input = "Kismet Chameleon; Lazarus het_Draeke";
        var doc = emptyDocument();
        parseHeaderMetadata(input, doc);
        assertEquals(2, get(doc.getAttributes(), "authorcount"));
        assertEquals("Kismet Chameleon, Lazarus het Draeke", get(doc.getAttributes(), "authors"));
        assertEquals("Kismet Chameleon", get(doc.getAttributes(), "author_1"));
        assertEquals("Lazarus het Draeke", get(doc.getAttributes(), "author_2"));
        assertEquals("het Draeke", get(doc.getAttributes(), "lastname_2"));
    }

    @Test
    @DisplayName("allows authors to be overridden using explicit author attributes")
    void allowsAuthorsToBeOverriddenUsingExplicitAuthorAttributes() throws Exception {
        var input = """
                Kismet Chameleon; Johnny Bravo; Lazarus het_Draeke
                :author_2: Danger Mouse
                """;
        var doc = emptyDocument();
        parseHeaderMetadata(input, doc);
        assertEquals(3, get(doc.getAttributes(), "authorcount"));
        assertEquals("Kismet Chameleon, Danger Mouse, Lazarus het Draeke", get(doc.getAttributes(), "authors"));
        assertEquals("Kismet Chameleon", get(doc.getAttributes(), "author_1"));
        assertEquals("Danger Mouse", get(doc.getAttributes(), "author_2"));
        assertEquals("Lazarus het Draeke", get(doc.getAttributes(), "author_3"));
        assertEquals("het Draeke", get(doc.getAttributes(), "lastname_3"));
    }

    @Test
    @DisplayName("removes formatting before partitioning author defined using author attribute")
    void removesFormattingBeforePartitioningAuthorDefinedUsingAuthorAttribute() throws Exception {
        var input = ":author: pass:n[http://example.org/community/team.html[Ze_**Project** team]]";
        var doc = emptyDocument();
        parseHeaderMetadata(input, doc);
        assertEquals(1, get(doc.getAttributes(), "authorcount"));
        assertEquals("<a href=\"http://example.org/community/team.html\">Ze <strong>Project</strong> team</a>", get(doc.getAttributes(), "authors"));
        assertEquals("Ze Project", get(doc.getAttributes(), "firstname"));
        assertEquals("team", get(doc.getAttributes(), "lastname"));
    }

    @Test
    @DisplayName("parse rev number date remark")
    void parseRevNumberDateRemark() throws Exception {
        var input = """
                Ryan Waldron
                v0.0.7, 2013-12-18: The first release you can stand on
                """;
        var metadata = parseHeaderMetadata(input);
        assertEquals(9, size(metadata));
        assertEquals("0.0.7", get(metadata, "revnumber"));
        assertEquals("2013-12-18", get(metadata, "revdate"));
        assertEquals("The first release you can stand on", get(metadata, "revremark"));
    }

    @Test
    @DisplayName("parse rev number, data, and remark as attribute references")
    void parseRevNumberDataAndRemarkAsAttributeReferences() throws Exception {
        var input = """
                Author Name
                v{project-version}, {release-date}: {release-summary}
                """;
        var metadata = parseHeaderMetadata(input);
        assertEquals(9, size(metadata));
        assertEquals("{project-version}", get(metadata, "revnumber"));
        assertEquals("{release-date}", get(metadata, "revdate"));
        assertEquals("{release-summary}", get(metadata, "revremark"));
    }

    @Test
    @DisplayName("should resolve attribute references in rev number, data, and remark")
    void shouldResolveAttributeReferencesInRevNumberDataAndRemark() throws Exception {
        var input = """
                = Document Title
                Author Name
                {project-version}, {release-date}: {release-summary}
                """;
        var doc = documentFromString(input, Options.of("attributes", Options.of("project-version", "1.0.1", "release-date", "2018-05-15", "release-summary", "The one you can count on!")));
        assertEquals("1.0.1", doc.getAttribute("revnumber"));
        assertEquals("2018-05-15", doc.getAttribute("revdate"));
        assertEquals("The one you can count on!", doc.getAttribute("revremark"));
    }

    @Test
    @DisplayName("parse rev date")
    void parseRevDate() throws Exception {
        var input = """
                Ryan Waldron
                2013-12-18
                """;
        var metadata = parseHeaderMetadata(input);
        assertEquals(7, size(metadata));
        assertEquals("2013-12-18", get(metadata, "revdate"));
    }

    @Test
    @DisplayName("parse rev number with trailing comma")
    void parseRevNumberWithTrailingComma() throws Exception {
        var input = """
                Stuart Rackham
                v8.6.8,
                """;
        var metadata = parseHeaderMetadata(input);
        assertEquals(7, size(metadata));
        assertEquals("8.6.8", get(metadata, "revnumber"));
        assertFalsy(metadata.containsKey("revdate"));
    }

    @Test
    @DisplayName("parse rev number")
    void parseRevNumber() throws Exception {
        var input = """
                Stuart Rackham
                v8.6.8
                """;
        var metadata = parseHeaderMetadata(input);
        assertEquals(7, size(metadata));
        assertEquals("8.6.8", get(metadata, "revnumber"));
        assertFalsy(metadata.containsKey("revdate"));
    }

    @Test
    @DisplayName("treats arbitrary text on rev line as revdate")
    void treatsArbitraryTextOnRevLineAsRevdate() throws Exception {
        var input = """
                Ryan Waldron
                foobar
                """;
        var metadata = parseHeaderMetadata(input);
        assertEquals(7, size(metadata));
        assertEquals("foobar", get(metadata, "revdate"));
    }

    @Test
    @DisplayName("parse rev date remark")
    void parseRevDateRemark() throws Exception {
        var input = """
                Ryan Waldron
                2013-12-18:  The first release you can stand on
                """;
        var metadata = parseHeaderMetadata(input);
        assertEquals(8, size(metadata));
        assertEquals("2013-12-18", get(metadata, "revdate"));
        assertEquals("The first release you can stand on", get(metadata, "revremark"));
    }

    @Test
    @DisplayName("should not mistake attribute entry as rev remark")
    void shouldNotMistakeAttributeEntryAsRevRemark() throws Exception {
        var input = """
                Joe Cool
                :page-layout: post
                """;
        var metadata = parseHeaderMetadata(input);
        assertNotEquals("page-layout: post", get(metadata, "revremark"));
        assertFalsy(metadata.containsKey("revdate"));
    }

    @Test
    @DisplayName("parse rev remark only")
    void parseRevRemarkOnly() throws Exception {
        var input = """
                Joe Cool
                 :Must start revremark-only line with space
                """;
        var metadata = parseHeaderMetadata(input);
        assertEquals("Must start revremark-only line with space", get(metadata, "revremark"));
        assertFalsy(metadata.containsKey("revdate"));
    }

    @Test
    @DisplayName("skip line comments before author")
    void skipLineCommentsBeforeAuthor() throws Exception {
        var input = """
                // Asciidoctor
                // release artist
                Ryan Waldron
                """;
        var metadata = parseHeaderMetadata(input);
        assertEquals(6, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Ryan Waldron", get(metadata, "author"));
        assertEquals("Ryan", get(metadata, "firstname"));
        assertEquals("Waldron", get(metadata, "lastname"));
        assertEquals("RW", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("skip block comment before author")
    void skipBlockCommentBeforeAuthor() throws Exception {
        var input = """
                ////
                Asciidoctor
                release artist
                ////
                Ryan Waldron
                """;
        var metadata = parseHeaderMetadata(input);
        assertEquals(6, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Ryan Waldron", get(metadata, "author"));
        assertEquals("Ryan", get(metadata, "firstname"));
        assertEquals("Waldron", get(metadata, "lastname"));
        assertEquals("RW", get(metadata, "authorinitials"));
    }

    @Test
    @DisplayName("skip block comment before rev")
    void skipBlockCommentBeforeRev() throws Exception {
        var input = """
                Ryan Waldron
                ////
                Asciidoctor
                release info
                ////
                v0.0.7, 2013-12-18
                """;
        var metadata = parseHeaderMetadata(input);
        assertEquals(8, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Ryan Waldron", get(metadata, "author"));
        assertEquals("0.0.7", get(metadata, "revnumber"));
        assertEquals("2013-12-18", get(metadata, "revdate"));
    }

    @Test
    @DisplayName("break header at line with three forward slashes")
    void breakHeaderAtLineWithThreeForwardSlashes() throws Exception {
        var input = """
                Joe Cool
                v1.0
                ///
                stuff
                """;
        var metadata = parseHeaderMetadata(input);
        assertEquals(7, size(metadata));
        assertEquals(1, get(metadata, "authorcount"));
        assertEquals("Joe Cool", get(metadata, "author"));
        assertEquals("1.0", get(metadata, "revnumber"));
    }

    @Test
    @DisplayName("attribute entry overrides generated author initials")
    void attributeEntryOverridesGeneratedAuthorInitials() throws Exception {
        var doc = emptyDocument();
        var metadata = parseHeaderMetadata("Stuart Rackham <founder@asciidoc.org>\n:Author Initials: SJR", doc);
        assertEquals("SR", get(metadata, "authorinitials"));
        assertEquals("SJR", get(doc.getAttributes(), "authorinitials"));
    }

    @Test
    @DisplayName("adjust indentation to 0")
    void adjustIndentationTo0() throws Exception {
        var input = """
                    def names

                      @name.split

                    end
                """;
        var expected = Strings.chop("""
                def names

                  @name.split

                end
                """);
        var lines = split(input, "\n");
        Parser.adjustIndentation(lines);
        assertEquals(expected, join(lines, "\n"));
    }

    @Test
    @DisplayName("adjust indentation mixed with tabs and spaces to 0")
    void adjustIndentationMixedWithTabsAndSpacesTo0() throws Exception {
        var input = """
                    def names

                	  @name.split

                    end
                """;
        var expected = Strings.chop("""
                def names

                  @name.split

                end
                """);
        var lines = split(input, "\n");
        Parser.adjustIndentation(lines, 0, 4);
        assertEquals(expected, join(lines, "\n"));
    }

    @Test
    @DisplayName("expands tabs to spaces")
    void expandsTabsToSpaces() throws Exception {
        var input = """
                Filesystem				Size	Used	Avail	Use%	Mounted on
                Filesystem              Size    Used    Avail   Use%    Mounted on
                devtmpfs				3.9G	   0	 3.9G	  0%	/dev
                /dev/mapper/fedora-root	 48G	 18G	  29G	 39%	/
                """;
        var expected = Strings.chop("""
                Filesystem              Size    Used    Avail   Use%    Mounted on
                Filesystem              Size    Used    Avail   Use%    Mounted on
                devtmpfs                3.9G       0     3.9G     0%    /dev
                /dev/mapper/fedora-root  48G     18G      29G    39%    /
                """);
        var lines = split(input, "\n");
        Parser.adjustIndentation(lines, 0, 4);
        assertEquals(expected, join(lines, "\n"));
    }

    @Test
    @DisplayName("adjust indentation to non-zero")
    void adjustIndentationToNonZero() throws Exception {
        var input = """
                    def names

                      @name.split

                    end
                """;
        var expected = Strings.chop("""
                  def names

                    @name.split

                  end
                """);
        var lines = split(input, "\n");
        Parser.adjustIndentation(lines, 2);
        assertEquals(expected, join(lines, "\n"));
    }

    @Test
    @DisplayName("preserve block indent if indent is -1")
    void preserveBlockIndentIfIndentIs1() throws Exception {
        var input = """
                    def names

                      @name.split

                    end
                """;
        var expected = input;
        var lines = lines(input);
        Parser.adjustIndentation(lines, -1);
        assertEquals(expected, join(lines));
    }

    @Test
    @DisplayName("adjust indentation handles empty lines gracefully")
    void adjustIndentationHandlesEmptyLinesGracefully() throws Exception {
        var input = new ArrayList<String>();
        var expected = input;
        var lines = copy(input);
        Parser.adjustIndentation(lines);
        assertEquals(expected, lines);
    }

    @Test
    @DisplayName("should warn if inline anchor is already in use")
    void shouldWarnIfInlineAnchorIsAlreadyInUse() throws Exception {
        var input = """
                [#in-use]
                A paragraph with an id.

                Another paragraph
                [[in-use]]that uses an id
                which is already in use.
                """;
        try (var logger = usingMemoryLogger()) {
            documentFromString(input);
            assertMessage(logger, "WARN", "<stdin>: line 5: id assigned to anchor already in use: in-use", LogMessage.class);
        }
    }
}
