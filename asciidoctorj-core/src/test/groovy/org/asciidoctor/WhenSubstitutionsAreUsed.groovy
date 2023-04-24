package org.asciidoctor

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Document
import org.asciidoctor.extension.Treeprocessor
import spock.lang.Specification

import static org.asciidoctor.ast.StructuralNode.SUBSTITUTION_SPECIAL_CHARACTERS
import static org.asciidoctor.ast.StructuralNode.SUBSTITUTION_QUOTES
import static org.asciidoctor.ast.StructuralNode.SUBSTITUTION_ATTRIBUTES
import static org.asciidoctor.ast.StructuralNode.SUBSTITUTION_REPLACEMENTS
import static org.asciidoctor.ast.StructuralNode.SUBSTITUTION_MACROS
import static org.asciidoctor.ast.StructuralNode.SUBSTITUTION_POST_REPLACEMENTS

class WhenSubstitutionsAreUsed extends Specification {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    def 'a node should return its substitutions'() {

        given:
        String document = '''
= Test document

== Test section

Test paragraph

[source,java]
----
System.out.println("Hello World");
----
'''

        when:
        Document doc = asciidoctor.load(document, OptionsBuilder.options().asMap())
        Block paragraph = doc.blocks[0].blocks[0]
        Block source = doc.blocks[0].blocks[1]

        then:
        paragraph.substitutions == [SUBSTITUTION_SPECIAL_CHARACTERS, SUBSTITUTION_QUOTES, SUBSTITUTION_ATTRIBUTES, SUBSTITUTION_REPLACEMENTS, SUBSTITUTION_MACROS, SUBSTITUTION_POST_REPLACEMENTS]
        source.substitutions == [SUBSTITUTION_SPECIAL_CHARACTERS]
    }

    def 'it should be possible to remove a substitution'() {
        given:
        String document = '''
= Test document
:foo: bar

== Test section

First test paragraph {foo}.

Second test paragraph {foo}

'''

        when:
        asciidoctor.javaExtensionRegistry().treeprocessor(TestTreeprocessor)
        Document doc = asciidoctor.load(document, OptionsBuilder.options().asMap())
        Block firstparagraph = doc.blocks[0].blocks[0]
        Block secondparagraph = doc.blocks[0].blocks[1]

        String html = asciidoctor.convert(document, OptionsBuilder.options().asMap())

        then:
        firstparagraph.substitutions == [SUBSTITUTION_SPECIAL_CHARACTERS, SUBSTITUTION_QUOTES, SUBSTITUTION_ATTRIBUTES, SUBSTITUTION_REPLACEMENTS, SUBSTITUTION_MACROS, SUBSTITUTION_POST_REPLACEMENTS]
        firstparagraph.isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)
        secondparagraph.substitutions == [SUBSTITUTION_SPECIAL_CHARACTERS, SUBSTITUTION_QUOTES, SUBSTITUTION_REPLACEMENTS, SUBSTITUTION_MACROS, SUBSTITUTION_POST_REPLACEMENTS]
        !secondparagraph.isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)

        html.contains('First test paragraph bar.')
        html.contains('Second test paragraph {foo}')
    }

    def 'it should be possible to add a substitution'() {
        given:
        String document = '''
= Test document
:foo: bar

== Test section

First test paragraph {foo}

[source,java]
----
System.out.println("{foo}");
----

'''

        when:
        asciidoctor.javaExtensionRegistry().treeprocessor(TestTreeprocessor)
        Document doc = asciidoctor.load(document, OptionsBuilder.options().asMap())
        Block firstparagraph = doc.blocks[0].blocks[0]
        Block secondparagraph = doc.blocks[0].blocks[1]

        String html = asciidoctor.convert(document, OptionsBuilder.options().asMap())

        then:
        firstparagraph.substitutions == [SUBSTITUTION_SPECIAL_CHARACTERS, SUBSTITUTION_QUOTES, SUBSTITUTION_ATTRIBUTES, SUBSTITUTION_REPLACEMENTS, SUBSTITUTION_MACROS, SUBSTITUTION_POST_REPLACEMENTS]
        firstparagraph.isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)
        secondparagraph.substitutions == [SUBSTITUTION_SPECIAL_CHARACTERS, SUBSTITUTION_ATTRIBUTES]
        secondparagraph.isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)

        html.contains('First test paragraph bar')
        html.contains('System.out.println("bar");')
    }

    static class TestTreeprocessor extends Treeprocessor {
        @Override
        Document process(Document document) {
            if (document.blocks()[0].blocks[1].isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)) {
                document.blocks()[0].blocks[1].removeSubstitution(SUBSTITUTION_ATTRIBUTES)
            } else {
                document.blocks()[0].blocks[1].addSubstitution(SUBSTITUTION_ATTRIBUTES)
            }
            document
        }
    }


    def 'it should be possible to prepend a substitution'() {
        given:
        // Usually *{foo} should not be rendered as bold text as attributes are substituted after quotes.
        // But by prepending the attributes substitution we can accomplish that
        String document = '''
= Test document
:foo: bar*

== Test section

First test paragraph *{foo}


'''

        when:
        asciidoctor.javaExtensionRegistry().treeprocessor(PrependSubstitutionTestTreeprocessor)
        Document doc = asciidoctor.load(document, OptionsBuilder.options().asMap())
        Block firstparagraph = doc.blocks()[0].blocks[0]

        String html = asciidoctor.convert(document, OptionsBuilder.options().asMap())

        then:
        firstparagraph.substitutions == [SUBSTITUTION_ATTRIBUTES, SUBSTITUTION_SPECIAL_CHARACTERS, SUBSTITUTION_QUOTES, SUBSTITUTION_ATTRIBUTES, SUBSTITUTION_REPLACEMENTS, SUBSTITUTION_MACROS, SUBSTITUTION_POST_REPLACEMENTS]
        firstparagraph.isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)

        html.contains('First test paragraph <strong>bar</strong>')
    }

    static class PrependSubstitutionTestTreeprocessor extends Treeprocessor {
        @Override
        Document process(Document document) {
            document.blocks()[0].blocks[0].prependSubstitution(SUBSTITUTION_ATTRIBUTES)
            document
        }
    }

    def 'it should be possible to set a substitution'() {
        given:
        // By default in the listing < and > should be substituted by &lt; and &gt; due to the special characters substitution.
        // We set only the attributes substitution instead and expect literally 'bar <=> bar' in the output.
        String document = '''
= Test document
:foo: bar

== Test section

----
{foo} <=> bar
----

'''

        when:
        asciidoctor.javaExtensionRegistry().treeprocessor(SetSubstitutionTestTreeprocessor)
        Document doc = asciidoctor.load(document, Options.builder().build())
        Block firstparagraph = doc.blocks[0].blocks[0]

        String html = asciidoctor.convert(document, Options.builder().build())

        then:
        firstparagraph.substitutions == [SUBSTITUTION_ATTRIBUTES]
        firstparagraph.isSubstitutionEnabled(SUBSTITUTION_ATTRIBUTES)

        html.contains('bar <=> bar')
    }

    static class SetSubstitutionTestTreeprocessor extends Treeprocessor {
        @Override
        Document process(Document document) {
            document.blocks[0].blocks[0].substitutions = [SUBSTITUTION_ATTRIBUTES]
            document
        }
    }

}
