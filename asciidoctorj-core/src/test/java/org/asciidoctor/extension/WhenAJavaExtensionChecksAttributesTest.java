package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

// Issue: https://github.com/asciidoctor/asciidoctorj/issues/450
class WhenAJavaExtensionChecksAttributesTest {

    private static final String DOCUMENT = """
            = Test document
            
            [checkattributes,avalue]
            Check me
            
            """;

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    @DisplayName("a BlockProcessor should only get String attribute keys")
    void aBlockProcessorShouldOnlyGetStringAttributeKeys() {
        // given:
        AttributeCheckingBlockProcessor processor = new AttributeCheckingBlockProcessor();
        asciidoctor.javaExtensionRegistry().block(processor);

        // when:
        assertThatCode(() -> asciidoctor.convert(DOCUMENT, Options.builder().standalone(true).safe(SafeMode.SERVER).build()))
                .doesNotThrowAnyException();

        // then:
        assertThat(processor.getAttributeKeys())
                .isNotEmpty()
                .allSatisfy(key -> assertThat(key).isInstanceOf(String.class));
    }
}
