package org.asciidoctor.test.extension;

import org.asciidoctor.Asciidoctor;

import java.lang.reflect.Field;
import java.util.List;

import static org.asciidoctor.test.extension.ReflectionUtils.injectValue;


public class TestContextManager {

    private final List<Field> sharedFields;
    private final List<Field> testFields;
    private final Asciidoctor sharedInstance;

    public TestContextManager(List<Field> sharedFields, List<Field> testFields) {
        this.sharedFields = sharedFields;
        this.testFields = testFields;
        this.sharedInstance = Asciidoctor.Factory.create();
    }

    public void initTestFields(Object testInstance) {
        this.testFields.forEach(field -> injectValue(testInstance, field, Asciidoctor.Factory.create()));
    }

    public void initSharedFields(Object testInstance) {
        this.sharedFields.forEach(field -> injectValue(testInstance, field, sharedInstance));
    }
}
