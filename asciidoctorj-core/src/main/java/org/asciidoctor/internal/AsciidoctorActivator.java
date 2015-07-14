package org.asciidoctor.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Asciidoctor.Factory;
import org.jruby.embed.osgi.OSGiScriptingContainer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class AsciidoctorActivator implements BundleActivator {

	private Asciidoctor asciidoctor;
	private ServiceRegistration registration;

	@Override
	public void start(final BundleContext context) throws Exception {
		final OSGiScriptingContainer container = new OSGiScriptingContainer(context.getBundle());
		this.asciidoctor = Factory.create(container.getOSGiBundleClassLoader());
		this.registration = context.registerService(Asciidoctor.class.getName(), asciidoctor, null);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		if (registration != null) {
			registration.unregister();
			registration = null;
		}
		if (asciidoctor != null) {
			asciidoctor.shutdown();
			asciidoctor = null;
		}
	}

}
