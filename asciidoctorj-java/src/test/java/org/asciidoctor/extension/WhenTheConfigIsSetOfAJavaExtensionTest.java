package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.jruby.internal.AsciidoctorCoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenTheConfigIsSetOfAJavaExtensionTest {

    String document = """
            [modify]
            Parsing will crash when processing this block
            """;

    @Test
    @DisplayName("setConfig should throw no Exception as long as the processor is not used by Asciidoctor")
    public void setConfigShouldThrowNoExceptionAsLongAsTheProcessorIsNotUsedByAsciidoctor() {

        // given:
        BlockProcessor blockProcessor = new ConfigModifyingBlockProcessor();
        Map<String, Object> config = new HashMap<>();
        config.put(Contexts.KEY, List.of(Contexts.PARAGRAPH));
        config.put(ContentModel.KEY, ContentModel.SIMPLE);
        blockProcessor.setConfig(config);

        // expect:
        assertThat(blockProcessor.getConfig().get(ContentModel.KEY)).isEqualTo(ContentModel.SIMPLE);
    }

    @Test
    @DisplayName("setConfig should throw an IllegalStateException in Processor_process when a processor instance is registered")
    public void setConfigShouldThrowAnIllegalStateExceptionWhenAProcessorInstanceIsRegistered() {

        // given:
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        BlockProcessor blockProcessor = new ConfigModifyingBlockProcessor();
        Map<String, Object> config = new HashMap<>();
        config.put(Contexts.KEY, List.of(Contexts.PARAGRAPH));
        config.put(ContentModel.KEY, ContentModel.SIMPLE);
        blockProcessor.setConfig(config);

        asciidoctor.javaExtensionRegistry().block(blockProcessor);

        // when:
        Exception e = assertThrows(Exception.class, () -> asciidoctor.convert(document, Options.builder().toFile(false).build()));

        // then:
        assertThat(e instanceof AsciidoctorCoreException || e instanceof IllegalStateException).isTrue();
    }

    @Test
    @DisplayName("setConfig should throw an IllegalStateException in Processor_process when a processor class is registered")
    public void setConfigShouldThrowAnIllegalStateExceptionWhenAProcessorClassIsRegistered() {

        // given:
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        asciidoctor.javaExtensionRegistry().block("modify", ConfigModifyingBlockProcessor.class);

        // when:
        Exception e = assertThrows(Exception.class, () -> asciidoctor.convert(document, Options.builder().toFile(false).build()));

        // then:
        assertThat(e instanceof AsciidoctorCoreException || e instanceof IllegalStateException).isTrue();
    }

}
