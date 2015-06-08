package org.asciidoctor.arquillian;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public final class AnnotationUtils {

    private AnnotationUtils() {}

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T filterAnnotation(Annotation[] annotations, Class<T> annotationClass) {
        if(annotations == null) {
            return null;
        }
        List<Annotation> filtered = new ArrayList<Annotation>();
        for(Annotation annotation : annotations) {
            if(annotationClass.isInstance(annotation)) {
                return (T) annotation;
            }
        }
        return null;
    }

}
