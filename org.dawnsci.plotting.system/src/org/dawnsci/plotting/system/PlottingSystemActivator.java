/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class PlottingSystemActivator extends AbstractUIPlugin {

	private final static String ID = "org.dawnsci.plotting.system";

	private static PlottingSystemActivator activator;
	private static BundleContext           context;

	private static IPreferenceStore plottingPreferenceStore;
	private static IPreferenceStore analysisRCPPreferenceStore;

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(ID, path);
	}

	public static Image getImage(String path) {
		return getImageDescriptor(path).createImage();
	}

	public static IPreferenceStore getPlottingPreferenceStore() {
		if (plottingPreferenceStore == null)
			plottingPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
		return plottingPreferenceStore;
	}

	public static IPreferenceStore getAnalysisRCPPreferenceStore() {
		if (analysisRCPPreferenceStore == null)
			analysisRCPPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
		return analysisRCPPreferenceStore;
	}

	public void start(BundleContext c) throws Exception {
		super.start(c);
		activator = this;
		context   = c;
	}

	public static IPreferenceStore getLocalPreferenceStore() {
		return activator.getPreferenceStore();
	}


	/**
	 * Looks for OSGI service, used by ServiceManager
	 * 
	 * @param clazz
	 * @return
	 */
	public static Object getService(Class<?> clazz) {
		if (context==null) return null;
		ServiceReference<?> ref = context.getServiceReference(clazz);
		if (ref==null) return null;
		return context.getService(ref);
	}

}
