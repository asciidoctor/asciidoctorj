package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.Document;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenABlockShouldBeDuplicatedTest {

    public static final String VERBATIM = "verbatim";
    public static final String CLASS_LISTINGBLOCK = ".listingblock";

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    @DisplayName("it should be possible to copy the content_model")
    public void itShouldBePossibleToCopyTheContentModel() {

        // given:
        String newSource = "a source";
        asciidoctor.javaExtensionRegistry().treeprocessor(new BlockDuplicator("paragraph", newSource));

        final String asciidoctorSource = "\n\n  This will be ignored\n\n    ";

        // when:
        Document document = asciidoctor.load(asciidoctorSource, Options.builder().safe(SafeMode.SAFE).build());

        // then:
        assertThat(document.getBlocks().size()).isEqualTo(2);
        assertThat(document.getBlocks().get(0).getContentModel()).isEqualTo(VERBATIM);
        assertThat(document.getBlocks().get(1).getContentModel()).isEqualTo(VERBATIM);
    }


    @Test
    @DisplayName("it should be possible to copy substitutions")
    public void itShouldBePossibleToCopySubstitutions() {

        // given:
        String newSource = "Asciidoctor asciidoctor = ...";
        asciidoctor.javaExtensionRegistry().treeprocessor(new BlockDuplicator("listing", newSource));

        final String asciidoctorSource = "\n[source,java,subs=\"replacements\"]\n----\nThis will be ignored\n----\n\n    ";

        // when:
        Options options = Options.builder().safe(SafeMode.SAFE).standalone(false).build();
        org.jsoup.nodes.Document html = Jsoup.parse(asciidoctor.convert(asciidoctorSource, options));

        // then:
        assertThat(html.select(CLASS_LISTINGBLOCK).get(0).text()).isEqualTo("This will be ignored");
        assertThat(html.select(CLASS_LISTINGBLOCK).get(1).text()).isEqualTo("Asciidoctor asciidoctor = …​");
    }

    public static class BlockDuplicator extends Treeprocessor {

        private final String newSource;
        private final String newContext;

        public BlockDuplicator(String newContext, String newSource) {
            super(new HashMap<>());
            this.newContext = newContext;
            this.newSource = newSource;
        }

        @Override
        public Document process(Document document) {

            String contentModel = document.getBlocks().get(0).getContentModel();
            Map<Object, Object> options = new HashMap<Object, Object>();
            options.put("subs", document.getBlocks().get(0).getSubstitutions());
            options.put(ContentModel.KEY, contentModel);

            document.getBlocks().add(createBlock(document, newContext, newSource, new HashMap<>(), options));

            return document;
        }
    }

}
