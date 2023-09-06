package org.asciidoctor.test.extension;

import org.asciidoctor.test.ClasspathResource;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.asciidoctor.test.extension.ReflectionUtils.findFields;
import static org.asciidoctor.test.extension.ReflectionUtils.injectValue;

/**
 * Handles the initialization of {@link File} fields annotated with {@link ClasspathExtension}.
 * <p>
 * To prevent classpath traversing fields are parsed and values are initialized
 * only once and cached in {@link ClasspathExtension#postProcessTestInstance(Object, ExtensionContext)}.
 * Then assigned on every test {@link ClasspathExtension#beforeEach(ExtensionContext)}.
 * <p>
 * JUnit5's {@link org.junit.jupiter.api.extension.ExtensionContext.Store} is
 * used to hande initialization in a safe way.
 */
public class ClasspathExtension implements TestInstancePostProcessor, BeforeEachCallback, ParameterResolver {

    private static final Namespace TEST_CONTEXT_NAMESPACE = Namespace.create(ClasspathExtension.class);

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        Class<?> contextKey = context.getRequiredTestClass();
        getStore(context)
                .getOrComputeIfAbsent(contextKey,
                        keyType -> {
                            final List<Field> fileFields = findFields(testInstance, File.class, ClasspathResource.class);
                            final Map<Field, Resource> values = matchValues(testInstance, fileFields, File.class);

                            final List<Field> pathFields = findFields(testInstance, Path.class, ClasspathResource.class);
                            final Map<Field, Resource> values2 = matchValues(testInstance, pathFields, Path.class);

                            values2.forEach(values::put);

                            return new ResourcesCache(values);
                        },
                        ResourcesCache.class);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getTestInstance()
                .ifPresent(testInstance -> {
                    getStore(context)
                            .get(context.getRequiredTestClass(), ResourcesCache.class)
                            .initializeFields(testInstance);
                });
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(TEST_CONTEXT_NAMESPACE);
    }

    private Map<Field, Resource> matchValues(Object testInstance, List<Field> resourcesFields, Class<?> targetType) {
        final ClasspathHelper classpathHelper = new ClasspathHelper(testInstance.getClass());
        final Map<Field, Resource> assignedValues = new HashMap<>();
        for (Field field : resourcesFields) {
            final String path = getAnnotationValue(field);
            final File resource = classpathHelper.getResource(path);
            assignedValues.put(field, new Resource(resource, targetType));
        }
        return assignedValues;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context) {
        boolean annotated = parameterContext.isAnnotated(ClasspathResource.class);
        if (annotated && parameterContext.getDeclaringExecutable() instanceof Constructor) {
            throw new ParameterResolutionException("@ClasspathResource is not supported on constructor parameters.");
        }
        return annotated;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
        final Parameter parameter = parameterContext.getParameter();
        Class<?> parameterType = parameter.getType();
        assertSupportedType(parameterType);

        final String path = getAnnotationValue(parameter);
        final File resource = new ClasspathHelper(context.getRequiredTestClass()).getResource(path);
        return parameterType == File.class ? resource : resource.toPath();
    }

    private static String getAnnotationValue(AnnotatedElement parameter) {
        return parameter.getAnnotation(ClasspathResource.class).value();
    }

    private void assertSupportedType(Class<?> type) {
        if (type != File.class && type != Path.class) {
            throw new ExtensionConfigurationException("Only File or Path are supported");
        }
    }

    class Resource<T> {
        private final File file;
        private final Class<T> targetType;

        Resource(File file, Class<T> targetType) {
            this.file = file;
            this.targetType = targetType;
        }
    }

    class ResourcesCache {

        private final Map<Field, Resource> cache;

        ResourcesCache(Map<Field, Resource> cache) {
            this.cache = cache;
        }

        public void initializeFields(Object testInstance) {
            cache.forEach((field, resource) -> {
                final Object value = resource.targetType == File.class ? resource.file : resource.file.toPath();
                injectValue(testInstance, field, value);
            });
        }
    }
}
