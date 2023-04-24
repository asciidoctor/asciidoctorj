package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.Document
import org.jsoup.Jsoup
import spock.lang.Specification

class WhenABlockShouldBeDuplicated extends Specification {

    public static final String VERBATIM = 'verbatim'
    public static final String CLASS_LISTINGBLOCK = '.listingblock'

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    def 'it should be possible to copy the content_model'() {

        given:
        String newSource = 'a source'
        asciidoctor.javaExtensionRegistry().treeprocessor(new BlockDuplicator('paragraph', newSource))

        final String asciidoctorSource = '''

  This will be ignored

    '''

        when:
        Document document = asciidoctor.load(asciidoctorSource, OptionsBuilder.options().safe(SafeMode.SAFE).asMap())

        then:
        document.blocks.size() == 2
        document.blocks[0].contentModel == VERBATIM
        document.blocks[1].contentModel == VERBATIM
    }


    def 'it should be possible to copy substitutions'() {

        given:
        String newSource = 'Asciidoctor asciidoctor = ...'
        asciidoctor.javaExtensionRegistry().treeprocessor(new BlockDuplicator('listing', newSource))

        final String asciidoctorSource = '''
[source,java,subs="replacements"]
----
This will be ignored
----

    '''

        when:
        org.jsoup.nodes.Document html = Jsoup.parse(asciidoctor.convert(asciidoctorSource, OptionsBuilder.options().safe(SafeMode.SAFE).standalone(false).asMap()))

        then:
        html.select(CLASS_LISTINGBLOCK).get(0).text() == 'This will be ignored'
        html.select(CLASS_LISTINGBLOCK).get(1).text() == 'Asciidoctor asciidoctor = \u2026\u200B'
    }

    static class BlockDuplicator extends Treeprocessor {

        private final String newSource
        private final String newContext

        BlockDuplicator(String newContext, String newSource) {
            super(new HashMap<>())
            this.newContext = newContext
            this.newSource = newSource
        }

        @Override
        Document process(Document document) {

            String contentModel = document.blocks[0].contentModel
            Map<Object, Object> options = new HashMap<Object, Object>()
            options.put('subs', document.blocks[0].substitutions)
            options.put(ContentModel.KEY, contentModel)

            document.blocks << createBlock(document, newContext, newSource, [:], options)

            document
        }
    }

}
