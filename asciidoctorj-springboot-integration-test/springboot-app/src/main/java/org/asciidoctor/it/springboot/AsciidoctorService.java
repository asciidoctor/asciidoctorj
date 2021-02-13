package org.asciidoctor.it.springboot;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.springframework.stereotype.Component;

@Component
public class AsciidoctorService {

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();


    public String convert(String source) {
        return asciidoctor.convert(source, defaultOptions());
    }

    private OptionsBuilder defaultOptions() {
        return OptionsBuilder.options()
                .backend("html5")
                .safe(SafeMode.SAFE)
                .headerFooter(true)
                .toFile(false);
    }
}
