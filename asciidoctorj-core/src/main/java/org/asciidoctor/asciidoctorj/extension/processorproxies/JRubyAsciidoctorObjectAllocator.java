package org.asciidoctor.asciidoctorj.extension.processorproxies;

import org.asciidoctor.asciidoctorj.internal.JRubyAsciidoctor;
import org.jruby.runtime.ObjectAllocator;

public abstract class JRubyAsciidoctorObjectAllocator implements ObjectAllocator {

    protected JRubyAsciidoctor asciidoctor;

    public JRubyAsciidoctorObjectAllocator(JRubyAsciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }
}
