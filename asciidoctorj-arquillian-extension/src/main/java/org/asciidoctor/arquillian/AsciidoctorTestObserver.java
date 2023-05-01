package org.asciidoctor.arquillian;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.arquillian.api.Shared;
import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.junit.ClasspathResources;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AsciidoctorTestObserver {

    @Inject @ApplicationScoped
    private InstanceProducer<ScopedAsciidoctor> scopedAsciidoctor;

    @Inject @ApplicationScoped
    private InstanceProducer<ScopedTemporaryFolder> scopedTemporaryFolder;

    @Inject @ApplicationScoped
    private InstanceProducer<ClasspathResources> classpathResourcesInstanceProducer;


    public void beforeTestClassCreateScopedResourceHolder(@Observes(precedence = 100) BeforeClass beforeClass) {
        scopedAsciidoctor.set(new ScopedAsciidoctor());
        scopedTemporaryFolder.set(new ScopedTemporaryFolder());
    }

    public void beforeTestClassCreateSharedAsciidoctorInstance(@Observes(precedence = -100) BeforeClass beforeClass) {
        if (isSharedInstanceRequired(beforeClass.getTestClass().getJavaClass(), AsciidoctorJRuby.class)) {
            scopedAsciidoctor.get().setSharedAsciidoctor(
                    AsciidoctorJRuby.Factory.create());
        } else if (isSharedInstanceRequired(beforeClass.getTestClass().getJavaClass(), Asciidoctor.class)) {
            scopedAsciidoctor.get().setSharedAsciidoctor(
                    Asciidoctor.Factory.create());
        }
    }

    public void beforeTestClassCreateSharedTemporaryFolder(@Observes(precedence = -100) BeforeClass beforeClass) throws IOException {
        if (isSharedInstanceRequired(beforeClass.getTestClass().getJavaClass(), TemporaryFolder.class)) {
            TemporaryFolder temporaryFolder = new TemporaryFolder();
            temporaryFolder.create();
            scopedTemporaryFolder.get().setSharedTemporaryFolder(temporaryFolder);
        }
    }

    public void beforeTestClassCreateClasspathResources(@Observes BeforeClass beforeClass) {
        ClasspathResources classpathResources = new ClasspathResources(beforeClass.getTestClass().getJavaClass());
        classpathResourcesInstanceProducer.set(classpathResources);
    }


    public void beforeTestCreateUnsharedAsciidoctorInstance(@Observes(precedence = 5) Before before) {

        if (isUnsharedInstanceRequired(before.getTestClass().getJavaClass(), AsciidoctorJRuby.class)
                || isUnsharedInstanceRequired(before.getTestMethod(), Asciidoctor.class)) {
            scopedAsciidoctor.get().setUnsharedAsciidoctor(
                    AsciidoctorJRuby.Factory.create());
        } else if (isUnsharedInstanceRequired(before.getTestClass().getJavaClass(), Asciidoctor.class)
                || isUnsharedInstanceRequired(before.getTestMethod(), Asciidoctor.class)) {
            scopedAsciidoctor.get().setUnsharedAsciidoctor(
                    Asciidoctor.Factory.create());
        }

    }

    public void beforeTestCreateUnsharedTemporaryFolder(@Observes(precedence = 5) Before before) throws IOException {

        if (isUnsharedInstanceRequired(before.getTestClass().getJavaClass(), TemporaryFolder.class)
                || isUnsharedInstanceRequired(before.getTestMethod(), TemporaryFolder.class)) {
            TemporaryFolder temporaryFolder = new TemporaryFolder();
            temporaryFolder.create();
            scopedTemporaryFolder.get().setUnsharedTemporaryFolder(
                    temporaryFolder);
        }

    }

    public void afterTestShutdownUnsharedAsciidoctorInstance(@Observes After after) {
        Asciidoctor asciidoctor = scopedAsciidoctor.get().getUnsharedAsciidoctor();
        if (asciidoctor != null) {
            asciidoctor.shutdown();
            scopedAsciidoctor.get().setUnsharedAsciidoctor(null);
        }
    }

    public void afterTestShutdownUnsharedTemporaryFolderInstance(@Observes After after) {
        TemporaryFolder temporaryFolder = scopedTemporaryFolder.get().getUnsharedTemporaryFolder();
        if (temporaryFolder != null) {
            temporaryFolder.delete();
            scopedTemporaryFolder.get().setUnsharedTemporaryFolder(null);
        }
    }

    public void afterTestClassShutdownSharedAsciidoctorInstance(@Observes AfterClass afterClass) {
        Asciidoctor asciidoctor = scopedAsciidoctor.get().getSharedAsciidoctor();
        if (asciidoctor != null) {
            asciidoctor.shutdown();
            scopedAsciidoctor.get().setSharedAsciidoctor(null);
        }

    }

    public void afterTestClassShutdownSharedTemporaryFolderInstance(@Observes AfterClass afterClass) {
        TemporaryFolder temporaryFolder = scopedTemporaryFolder.get().getSharedTemporaryFolder();
        if (temporaryFolder != null) {
            temporaryFolder.delete();
            scopedTemporaryFolder.get().setSharedTemporaryFolder(null);
        }

    }

    private boolean isSharedInstanceRequired(Class<?> testClass, Class<?> resourceClass) {
        for (Field f : SecurityActions.getFieldsWithAnnotation(testClass, ArquillianResource.class)) {
            ArquillianResource arquillianResource = SecurityActions.getAnnotation(f, ArquillianResource.class);
            if (f.getType() == resourceClass && arquillianResource.value() == Shared.class) {
                return true;
            }
        }
        return false;
    }

    private boolean isUnsharedInstanceRequired(Class<?> testClass, Class<?> resourceClass) {
        for (Field f : SecurityActions.getFieldsWithAnnotation(testClass, ArquillianResource.class)) {
            ArquillianResource arquillianResource = SecurityActions.getAnnotation(f, ArquillianResource.class);
            if (f.getType() == resourceClass &&
                    (arquillianResource.value() == ArquillianResource.class || arquillianResource.value() == Unshared.class)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSharedInstanceRequired(Method testMethod, Class<?> resourceClass) {
        for (int i = 0; i < testMethod.getParameterTypes().length; i++) {
            if (testMethod.getParameterTypes()[i] == resourceClass) {
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


    private boolean isUnsharedInstanceRequired(Method testMethod, Class<?> resourceClass) {
        for (int i = 0; i < testMethod.getParameterTypes().length; i++) {
            if (testMethod.getParameterTypes()[i] == resourceClass) {
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
