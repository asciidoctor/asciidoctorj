package org.asciidoctor.test;

import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ClasspathExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class ClassPathResourceTest {

    private static final String RESOURCE_PATH = "test.txt";


    @Nested
    class WhenResourceIsDeclaredAsFile {

        @ClasspathResource(RESOURCE_PATH)
        private File resource;

        @Order(1)
        @Test
        void should_load_resource_from_classpath_as_field() {
            assertResourceContent(resource.toPath());
        }

        @Order(2)
        @Test
        void should_load_resource_classpath_as_parameter(@ClasspathResource(RESOURCE_PATH) File param) {
            assertResourceContent(param.toPath());
            assertResourceContent(resource.toPath());
        }
    }

    @Nested
    class WhenResourceIsDeclaredAsPath {

        @ClasspathResource(RESOURCE_PATH)
        private Path resource;

        @Order(1)
        @Test
        void should_load_resource_from_classpath_as_field() {
            assertResourceContent(resource);
        }

        @Order(2)
        @Test
        void should_load_resource_classpath_as_parameter(@ClasspathResource(RESOURCE_PATH) Path param) {
            assertResourceContent(param);
            assertResourceContent(resource);
        }

    }

    private void assertResourceContent(Path resource) {
        assertThat(resource).content().contains("Simple resource located in the classpath");
    }
}
