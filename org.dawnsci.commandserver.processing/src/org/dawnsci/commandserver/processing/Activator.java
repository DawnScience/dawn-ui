package org.dawnsci.commandserver.processing;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

	private static BundleContext bundleContext;
	
	@Override
	public void start(BundleContext context) throws Exception {
		bundleContext = context;

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		bundleContext = null;

	}
	
	public static <T> T getService(final Class<T> serviceClass) {
		if (bundleContext == null) return null;
		ServiceReference<T> ref = bundleContext.getServiceReference(serviceClass);
		if (ref==null) return null;
		return bundleContext.getService(ref);
	}

}
