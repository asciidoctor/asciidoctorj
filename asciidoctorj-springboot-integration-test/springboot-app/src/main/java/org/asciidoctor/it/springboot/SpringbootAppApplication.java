package org.asciidoctor.it.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is a test application for testing purposes.
 * It does not in any represent the recommended way to implement an
 * Asciidoctorj conversion service with SpringBoot.
 */
@SpringBootApplication
public class SpringbootAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootAppApplication.class, args);
    }

}
