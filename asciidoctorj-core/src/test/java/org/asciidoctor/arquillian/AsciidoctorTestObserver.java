package org.asciidoctor.arquillian;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.arquillian.api.Shared;
import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AsciidoctorTestObserver {

    @Inject @ApplicationScoped
    private InstanceProducer<ScopedAsciidoctor> scopedAsciidoctor;

    @Inject @ApplicationScoped
    private InstanceProducer<ClasspathResources> classpathResourcesInstanceProducer;


    public void beforeTestClassCreateScopedResourceHolder(@Observes(precedence = 100) BeforeClass beforeClass) {
        scopedAsciidoctor.set(new ScopedAsciidoctor());
    }

    public void beforeTestClassCreateSharedAsciidoctorInstance(@Observes(precedence = -100) BeforeClass beforeClass) {
        if (isSharedAsciidoctorInstanceRequired(beforeClass.getTestClass().getJavaClass())) {
            scopedAsciidoctor.get().setSharedAsciidoctor(
                    Asciidoctor.Factory.create());
        }
    }

    public void beforeTestClassCreateClasspathResources(@Observes BeforeClass beforeClass) {
        ClasspathResources classpathResources = new ClasspathResources(beforeClass.getTestClass().getJavaClass());
        classpathResourcesInstanceProducer.set(classpathResources);
    }


    public void beforeTestCreateUnsharedAsciidoctorInstance(@Observes(precedence = 5) Before before) {

        if (isUnsharedAsciidoctorInstanceRequired(before.getTestClass().getJavaClass())
                || isUnsharedAsciidoctorInstanceRequired(before.getTestMethod())) {
            scopedAsciidoctor.get().setUnsharedAsciidoctor(
                    Asciidoctor.Factory.create());
        }

    }

    public void afterTestShutdownUnsharedAsciidoctorInstance(@Observes After after) {
        Asciidoctor asciidoctor = scopedAsciidoctor.get().getUnsharedAsciidoctor();
        if (asciidoctor != null) {
            asciidoctor.shutdown();
            scopedAsciidoctor.get().setUnsharedAsciidoctor(null);
        }
    }

    public void afterTestClassShutdownSharedAsciidoctorInstance(@Observes AfterClass afterClass) {
        Asciidoctor asciidoctor = scopedAsciidoctor.get().getSharedAsciidoctor();
        if (asciidoctor != null) {
            asciidoctor.shutdown();
            scopedAsciidoctor.get().setSharedAsciidoctor(null);
        }

    }

    private boolean isSharedAsciidoctorInstanceRequired(Class<?> testClass) {
        for (Field f: SecurityActions.getFieldsWithAnnotation(testClass, ArquillianResource.class)) {
            ArquillianResource arquillianResource = SecurityActions.getAnnotation(f, ArquillianResource.class);
            if (f.getType() == Asciidoctor.class && arquillianResource.value() == Shared.class) {
                return true;
            }
        }
        return false;
    }

    private boolean isUnsharedAsciidoctorInstanceRequired(Class<?> testClass) {
        for (Field f: SecurityActions.getFieldsWithAnnotation(testClass, ArquillianResource.class)) {
            ArquillianResource arquillianResource = SecurityActions.getAnnotation(f, ArquillianResource.class);
            if (f.getType() == Asciidoctor.class &&
                    (arquillianResource.value() == ArquillianResource.class || arquillianResource.value() == Unshared.class)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSharedAsciidoctorInstanceRequired(Method testMethod) {
        for (int i = 0; i < testMethod.getParameterTypes().length; i++) {
            if (testMethod.getParameterTypes()[i] == Asciidoctor.class) {
                ArquillianResource arquillianResource =
                        AnnotationUtils.filterAnnotation(testMethod.getParameterAnnotations()[i], ArquillianResource.class);
                if (arquillianResource != null
                        && arquillianResource.value() == Unshared.class) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean isUnsharedAsciidoctorInstanceRequired(Method testMethod) {
        for (int i = 0; i < testMethod.getParameterTypes().length; i++) {
            if (testMethod.getParameterTypes()[i] == Asciidoctor.class) {
                ArquillianResource arquillianResource =
                        AnnotationUtils.filterAnnotation(testMethod.getParameterAnnotations()[i], ArquillianResource.class);
                if (arquillianResource != null &&
                        (arquillianResource.value() == ArquillianResource.class // Default value is ArquillianResoure.class is Unshared
                                || arquillianResource.value() == Unshared.class)) {
                    return true;
                }
            }
        }
        return false;
    }
}