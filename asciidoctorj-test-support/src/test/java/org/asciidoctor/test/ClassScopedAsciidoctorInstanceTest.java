package org.asciidoctor.test;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(AsciidoctorExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class ClassScopedAsciidoctorInstanceTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    private static Asciidoctor copy;

    @Order(1)
    @Test
    void should_inject_asciidoctor() {
        assertThat(asciidoctor).isNotNull();
        copy = asciidoctor;
    }

    @Order(2)
    @Test
    void should_not_initialize_asciidoctor() {
        assertThat(asciidoctor).isNotNull();
        assertThat(asciidoctor).isSameAs(copy);
    }
}
