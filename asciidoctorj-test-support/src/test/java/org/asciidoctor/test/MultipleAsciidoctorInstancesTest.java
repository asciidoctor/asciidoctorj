package org.asciidoctor.test;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_CLASS;
import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(AsciidoctorExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class MultipleAsciidoctorInstancesTest {

    @AsciidoctorInstance
    private Asciidoctor firstSharedAsciidoctor;
    @AsciidoctorInstance(scope = PER_CLASS)
    private Asciidoctor secondSharedAsciidoctor;

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor firstMethodAsciidoctor;
    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor secondMethodAsciidoctor;

    private static Asciidoctor copy;

    public MultipleAsciidoctorInstancesTest() {
    }

    @Order(1)
    @Test
    void should_inject_asciidoctor() {
        assertThat(firstSharedAsciidoctor).isNotNull();
        assertThat(secondSharedAsciidoctor)
                .isNotNull()
                .isSameAs(firstSharedAsciidoctor);

        assertThat(firstMethodAsciidoctor)
                .isNotNull()
                .isNotSameAs(firstSharedAsciidoctor)
                .isNotSameAs(secondSharedAsciidoctor);
        assertThat(secondMethodAsciidoctor)
                .isNotNull()
                .isNotSameAs(firstSharedAsciidoctor)
                .isNotSameAs(secondSharedAsciidoctor);
        assertThat(firstMethodAsciidoctor).isNotSameAs(secondMethodAsciidoctor);

        copy = firstSharedAsciidoctor;
    }

    @Order(2)
    @Test
    void should_only_initialize_new_shared_asciidoctor_instances() {
        assertThat(firstSharedAsciidoctor)
                .isNotNull()
                .isSameAs(secondSharedAsciidoctor)
                .isSameAs(copy);
        assertThat(secondSharedAsciidoctor)
                .isNotNull()
                .isSameAs(copy);

        assertThat(firstMethodAsciidoctor)
                .isNotNull()
                .isNotSameAs(copy);
        assertThat(secondMethodAsciidoctor)
                .isNotNull()
                .isNotSameAs(copy);
        assertThat(firstMethodAsciidoctor).isNotSameAs(secondMethodAsciidoctor);
    }
}
