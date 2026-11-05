package org.asciidoctor.it.springboot;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Converts a document to a standalone HTML document (which embeds the default stylesheet, read from the classpath)
 * and prints it to the standard output.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
            String html = asciidoctor.convert("= Hello, Spring Boot!\n\nConverted by *AsciidoctorJ*.", Options.builder()
                    .safe(SafeMode.SAFE)
                    .standalone(true)
                    .build());
            System.out.println(html);
        }
    }
}
