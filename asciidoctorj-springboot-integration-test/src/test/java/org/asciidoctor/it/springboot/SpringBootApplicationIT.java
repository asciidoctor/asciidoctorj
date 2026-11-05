package org.asciidoctor.it.springboot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Runs the Spring Boot executable jar, where AsciidoctorJ is a nested jar, in a separate process.
 */
public class SpringBootApplicationIT {

    @Test
    public void shouldConvertDocumentFromSpringBootExecutableJar() throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(List.of(java, "-jar", System.getProperty("app.jar"),
                "--spring.main.banner-mode=off", "--logging.level.root=warn"))
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(0, process.waitFor(), output);
        assertTrue(output.contains("<h1>Hello, Spring Boot!</h1>"), output);
        assertTrue(output.contains("<strong>AsciidoctorJ</strong>"), output);
        // the default stylesheet is read from the classpath (i.e., from the nested jar)
        assertTrue(output.contains("Asciidoctor default stylesheet"), output);
    }
}
