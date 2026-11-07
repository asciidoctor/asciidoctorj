package org.asciidoctor;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.log.TestLogHandlerService;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenTwoAsciidoctorsLogToConsoleTest {

    @Execution(ExecutionMode.SAME_THREAD)
    @Test
    public void shouldCreateNewLogHandlersOnCreateAsciidoctor() {
        TestLogHandlerService.instancesCount.set(0);
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.convert("Test", Options.builder().build());
        Asciidoctor asciidoctor2 = Asciidoctor.Factory.create();
        asciidoctor2.convert("Test", Options.builder().build());
        assertThat(TestLogHandlerService.instancesCount.get()).isEqualTo(2);
    }
}
