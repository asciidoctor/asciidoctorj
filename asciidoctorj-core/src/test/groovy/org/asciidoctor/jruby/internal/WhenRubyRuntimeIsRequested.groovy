package org.asciidoctor.jruby.internal

import org.asciidoctor.Asciidoctor
import spock.lang.Specification

class WhenRubyRuntimeIsRequested extends Specification {

    def "when JRubyRuntimeContext get is called it should return Ruby runtime"() {
        given:
        Asciidoctor asciidoctor = Asciidoctor.Factory.create()

        expect:
        JRubyRuntimeContext.get(asciidoctor) != null
    }

    def "when JRubyRuntimeContext is called for two Asciidoctor instances it should return different Ruby runtimes"() {
        given:
        Asciidoctor asciidoctor1 = Asciidoctor.Factory.create()
        Asciidoctor asciidoctor2 = Asciidoctor.Factory.create()

        expect:
        JRubyRuntimeContext.get(asciidoctor1) != null
        JRubyRuntimeContext.get(asciidoctor2) != null
        JRubyRuntimeContext.get(asciidoctor1) != JRubyRuntimeContext.get(asciidoctor2)
    }

}
