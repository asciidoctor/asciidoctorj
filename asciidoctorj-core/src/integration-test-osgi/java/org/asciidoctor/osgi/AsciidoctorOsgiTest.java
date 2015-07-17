package org.asciidoctor.osgi;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@RunWith(Arquillian.class)
public class AsciidoctorOsgiTest {

    @Deployment(name = "AsciidoctorJ")
    public static JavaArchive createAsciidoctorDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(System.getProperty("asciidoctorJarName")));
    }

    @OperateOnDeployment("AsciidoctorJ")
    @Test
    public void shouldRenderSimpleDocumentToHtml(@ArquillianResource final BundleContext ctx, @ArquillianResource final Bundle asciidoctorBundle)
            throws Exception {

        Assert.assertNull(ctx.getServiceReference(Asciidoctor.class.getName()));

        asciidoctorBundle.start();

        final ServiceReference<?> reference = ctx.getServiceReference(Asciidoctor.class.getName());
        final Asciidoctor asciidoctor = (Asciidoctor) ctx.getService(reference);
        final String result = asciidoctor.convert("Hello World",
                OptionsBuilder.options().safe(SafeMode.UNSAFE).headerFooter(false).toFile(false));
        System.out.println("RESULT: " + result);
        assertTrue("Result does not have expected content: \n" + result,
                result.contains("Hello World"));

        asciidoctorBundle.stop();

        Assert.assertNull(ctx.getServiceReference(Asciidoctor.class.getName()));

    }

}
