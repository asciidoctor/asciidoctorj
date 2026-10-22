package org.asciidoctor.it.springboot;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.springframework.stereotype.Component;

@Component
public class AsciidoctorService {

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();


    public String convert(String source) {
        return asciidoctor.convert(source, defaultOptions());
    }

    private Options defaultOptions() {
        return Options.builder()
                .backend("html5")
                .safe(SafeMode.SAFE)
                .standalone(true)
                .toFile(false)
                .build();
    }
}
