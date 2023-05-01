package org.asciidoctor.test.extension;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.asciidoctor.test.extension.ReflectionUtils.injectValue;
import static org.assertj.core.api.Assertions.assertThat;

public class ReflectionUtilsTest {

    @Test
    void should_inject_a_private_field() throws NoSuchFieldException {
        final TestClass instance = new TestClass();
        final Field field = instance.getClass().getDeclaredField("privateText");
        final String value = "uno";

        injectValue(instance, field, value);

        assertThat(instance.privateText).isEqualTo(value);
    }

    @Test
    void should_inject_a_public_field() throws NoSuchFieldException {
        final TestClass instance = new TestClass();
        final Field field = instance.getClass().getDeclaredField("publicText");
        final String value = "dos";

        injectValue(instance, field, value);

        assertThat(instance.publicText).isEqualTo(value);
    }

    @Test
    void should_inject_a_protected_field() throws NoSuchFieldException {
        final TestClass instance = new TestClass();
        final Field field = instance.getClass().getDeclaredField("protectedText");
        final String value = "tres";

        injectValue(instance, field, value);

        assertThat(instance.protectedText).isEqualTo(value);
    }

    @Test
    void should_inject_a_package_protected_field() throws NoSuchFieldException {
        final TestClass instance = new TestClass();
        final Field field = instance.getClass().getDeclaredField("packageProtectedText");
        final String value = "cuatro";

        injectValue(instance, field, value);

        assertThat(instance.packageProtectedText).isEqualTo(value);
    }

    class TestClass {
        public String publicText;
        private String privateText;
        protected String protectedText;
        String packageProtectedText;
    }
}
