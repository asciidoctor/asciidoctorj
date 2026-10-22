package org.asciidoctor.jruby.internal;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public final class Extensions {

    private Extensions() {}

    public static <T> Constructor<T> findConstructorWithMostMatchingArguments(Class<T> clazz, Object... args) {
        int numberOfMatchingArguments = -1;
        Constructor<?> bestConstructor = null;
        for (Constructor<?> constructor : clazz.getConstructors()) {
            int currentNumberOfArguments = constructor.getParameterTypes().length;
            if (currentNumberOfArguments > numberOfMatchingArguments && isConstructorCandidate(constructor, args)) {
                numberOfMatchingArguments = currentNumberOfArguments;
                bestConstructor = constructor;
            }
        }
        if (bestConstructor != null) {
            return (Constructor<T>) bestConstructor;
        } else {
            List<Class<?>> expectedTypes = new ArrayList<Class<?>>(args.length);
            for (Object arg : args) {
                if (arg == null) {
                    expectedTypes.add(Void.class);
                } else {
                    expectedTypes.add(arg.getClass());
                }
            }
            throw new IllegalArgumentException(
                "Extension class " + clazz.getName() + " does not provide a constructor accepting " + expectedTypes);
        }
    }

    private static boolean isConstructorCandidate(Constructor<?> constructor, Object... args) {
        if (constructor.getParameterTypes().length > args.length) {
            return false;
        }
        Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
        for (int i = 0; i < constructorParameterTypes.length; i++) {
            if (args[i] != null && !constructorParameterTypes[i].isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }
}
