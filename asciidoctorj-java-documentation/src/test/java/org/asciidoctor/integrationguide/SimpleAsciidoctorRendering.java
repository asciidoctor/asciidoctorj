package org.asciidoctor.integrationguide;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;

import java.io.File;

public class SimpleAsciidoctorRendering {

    public static void main(String[] args) {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create(); // <1>
        asciidoctor.convertFile(                                // <2>
                new File(args[0]),
                Options.builder()                               // <3>
                        .toFile(true)
                        .safe(SafeMode.UNSAFE)
                        .build());
    }
}
