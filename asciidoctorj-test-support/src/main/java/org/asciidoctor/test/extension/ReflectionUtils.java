package org.asciidoctor.test.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ReflectionUtils {

    static boolean hasAnnotation(AnnotatedElement field, Class<? extends Annotation> annotation) {
        return field.getAnnotation(annotation) != null;
    }

    static List<Field> findFields(Object instance, Class<?> assignable) {
        return Arrays.stream(instance.getClass().getDeclaredFields())
                .filter(field -> !Modifier.isFinal(field.getModifiers()))
                .filter(field -> assignable.isAssignableFrom((field.getType())))
                .collect(Collectors.toList());
    }

    static List<Field> findFields(Object instance, Class<?> assignable, Class<? extends Annotation> annotation) {
        return Arrays.stream(instance.getClass().getDeclaredFields())
                .filter(field -> !Modifier.isFinal(field.getModifiers()))
                .filter(field -> assignable.isAssignableFrom(field.getType()) && hasAnnotation(field, annotation))
                .collect(Collectors.toList());
    }

    static void injectValue(Object target, Field field, Object value) {
        try {
            if (Modifier.isPrivate(field.getModifiers())) {
                field.setAccessible(true);
                field.set(target, value);
                field.setAccessible(false);
            } else {
                field.set(target, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
