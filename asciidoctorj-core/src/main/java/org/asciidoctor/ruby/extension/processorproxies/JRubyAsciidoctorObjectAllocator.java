package org.asciidoctor.ruby.extension.processorproxies;

import org.asciidoctor.ruby.internal.JRubyAsciidoctor;
import org.jruby.runtime.ObjectAllocator;

public abstract class JRubyAsciidoctorObjectAllocator implements ObjectAllocator {

    protected JRubyAsciidoctor asciidoctor;

    public JRubyAsciidoctorObjectAllocator(JRubyAsciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }
}
