package org.dawnsci.osgi.test.application;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	public static <T> T getService(final Class<T> serviceClass) {
		if (context == null) return null;
		ServiceReference<T> ref = context.getServiceReference(serviceClass);
		if (ref==null) return null;
		return context.getService(ref);
	}
}
