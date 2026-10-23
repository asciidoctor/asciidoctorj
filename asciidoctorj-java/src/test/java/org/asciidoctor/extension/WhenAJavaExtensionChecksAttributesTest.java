package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Issue: https://github.com/asciidoctor/asciidoctorj/issues/450
public class WhenAJavaExtensionChecksAttributesTest {

    private static final String DOCUMENT = """
            = Test document

            [checkattributes,avalue]
            Check me

            """;

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Test
    @DisplayName("a BlockProcessor should only get String attribute keys")
    public void aBlockProcessorShouldOnlyGetStringAttributeKeys() {
        assertThatCode(() -> {
            // when:
            asciidoctor.javaExtensionRegistry().block(AttributeCheckingBlockProcessor.class);
            asciidoctor.convert(DOCUMENT, Options.builder().standalone(true).safe(SafeMode.SERVER).build());
        // then:
        }).doesNotThrowAnyException();
    }
}
