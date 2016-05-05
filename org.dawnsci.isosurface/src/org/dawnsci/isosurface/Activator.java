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
		Activator.plugin = this;
	    super.start(bundleContext);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.plugin = null;
	    super.stop(bundleContext);
	}


	/**
	 * Looks for OSGI service, used by ServiceManager
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> clazz) {
		BundleContext context = plugin.getBundle().getBundleContext();
		if (context==null) return null;
		ServiceReference<?> ref = context.getServiceReference(clazz);
		if (ref==null) return null;
		return (T) context.getService(ref);
	}

	public static ImageDescriptor getImage(String path) {
		return imageDescriptorFromPlugin("org.dawnsci.isosurface", path);
	}

}
