package org.asciidoctor.osgi;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.runner.RunWith;
import org.osgi.framework.ServiceReference;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class AsciidoctorOsgiTest {

    @Deployment(name = "AsciidoctorJ", order = 1)
    public static JavaArchive createAsciidoctorDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(System.getProperty("asciidoctorJarName")));
    }

    @Deployment(name = "Test", order = 2)
    public static JavaArchive createTestDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
            .setManifest(new ClassLoaderAsset("TestManifest.mf"));
    }

    @ArquillianResource
    private BundleContext ctx;

    @OperateOnDeployment("Test")
    @Test
    public void shouldRenderSimpleDocumentToHtml() throws Exception {
        Bundle bundle = getBundle("org.asciidoctor");
        bundle.start();

        ServiceReference serviceref = ctx.getServiceReference("org.asciidoctor.Asciidoctor");
        Asciidoctor asciidoctor = (Asciidoctor) ctx.getService(serviceref);

        String result = asciidoctor.convert("Hello World", OptionsBuilder.options().safe(SafeMode.UNSAFE).headerFooter(false).toFile(false));
        System.out.println("RESULT: " + result);
        assertTrue("Result does not have expected content: \n" + result, result.contains("Hello World"));

    }

    public Bundle getBundle(String bundleName) {
        for (Bundle bundle: ctx.getBundles()) {
            if (bundle.getSymbolicName().equals(bundleName)) {
                return bundle;
            }
        }
        return null;
    }

}
