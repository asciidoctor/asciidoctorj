package org.asciidoctor.integrationguide;

import java.io.File;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.api.OptionsBuilder;
import org.asciidoctor.api.SafeMode;

public class SimpleAsciidoctorRendering {

    public static void main(String[] args) {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create(); // <1>
        asciidoctor.convertFile(                                // <2>
                new File(args[0]),
                OptionsBuilder.options()                        // <3>
                        .toFile(true)
                        .safe(SafeMode.UNSAFE));
    }
}
