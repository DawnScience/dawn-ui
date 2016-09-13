package org.dawnsci.javafx.starter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bundle used to set the osgi.framework.extension property to the javafx osgi
 * bundle so javafx can be properly found by OSGi
 * 
 * @author wqk87977
 *
 */
public class Activator implements BundleActivator {

	private static BundleContext context;
	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		System.setProperty("osgi.framework.extensions", "org.eclipse.fx.osgi");
		logger.debug("System property \"osgi.framework.extensions\" set to \"org.eclipse.fx.osgi\"");
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
