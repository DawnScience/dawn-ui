/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui;

import java.util.Hashtable;

import org.dawb.workbench.ui.transferable.TransferableDataService;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "org.dawb.workbench.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	private BundleContext context;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.context = context;
		
		Hashtable<String, String> props = new Hashtable<String, String>(1);
		props.put("description", "A service used to encapsulate data in a transferable object which can be used anywhere in the user interface.");
		context.registerService(ITransferableDataService.class, new TransferableDataService(), props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		this.context = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}


	/**
	 * Creates the image, this should be disposed later.
	 * @param path
	 * @return Image
	 */
	public static Image getImage(String path) {
		ImageDescriptor des = imageDescriptorFromPlugin(PLUGIN_ID, path);
		return des.createImage();
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}


	/**
	 * Looks for OSGI service, used by ServiceManager
	 * 
	 * @param clazz
	 * @return
	 */
	public static <T> T getService(Class<T> clazz) {
		if (plugin.context==null) return null;
		ServiceReference<T> ref = plugin.context.getServiceReference(clazz);
		if (ref==null) return null;
		return plugin.context.getService(ref);
	}
}
