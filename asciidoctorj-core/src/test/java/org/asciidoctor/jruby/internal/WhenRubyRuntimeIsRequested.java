package org.asciidoctor.jruby.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.junit.jupiter.api.Test;

class WhenRubyRuntimeIsRequested {

    @Test
    void when_JRubyRuntimeContext_get_is_called_it_should_return_Ruby_runtime() {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        assertThat(JRubyRuntimeContext.get(asciidoctor)).isNotNull();
    }

    @Test
    void when_JRubyRuntimeContext_is_called_for_two_Asciidoctor_instances_it_should_return_different_Ruby_runtimes() {
        Asciidoctor asciidoctor1 = Asciidoctor.Factory.create();
        Asciidoctor asciidoctor2 = Asciidoctor.Factory.create();

        assertThat(JRubyRuntimeContext.get(asciidoctor1)).isNotNull();
        assertThat(JRubyRuntimeContext.get(asciidoctor2)).isNotNull();
        assertThat(JRubyRuntimeContext.get(asciidoctor1)).isNotEqualTo(JRubyRuntimeContext.get(asciidoctor2));
    }

}
