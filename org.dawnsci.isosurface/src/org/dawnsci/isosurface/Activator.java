/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.isosurface;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends AbstractUIPlugin {

	private static AbstractUIPlugin plugin;

	static BundleContext getContext() {
		return plugin.getBundle().getBundleContext();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
	    super.start(bundleContext);
		Activator.plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
	    super.stop(bundleContext);
		Activator.plugin = null;
	}


	/**
	 * Looks for OSGI service, used by ServiceManager
	 * 
	 * @param clazz
	 * @return
	 */
	public static Object getService(Class<?> clazz) {
		BundleContext context = plugin.getBundle().getBundleContext();
		if (context==null) return null;
		ServiceReference<?> ref = context.getServiceReference(clazz);
		if (ref==null) return null;
		return context.getService(ref);
	}

	public static ImageDescriptor getImage(String path) {
		return imageDescriptorFromPlugin("org.dawnsci.isosurface", path);
	}

}
