/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.dawnsci.plotting.tools";
    
	private static Activator staticActivator;
	public void start(BundleContext context) throws Exception {
		super.start(context);
        staticActivator = this;
    }
	
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin("org.dawnsci.plotting.tools", path);
    }
    		 
    public static Image getImage(String path) {
        return getImageDescriptor(path).createImage();
    }

    private static IPreferenceStore plottingPreferences;
	public static IPreferenceStore getPlottingPreferenceStore() {
		if (plottingPreferences!=null) return plottingPreferences;
		plottingPreferences = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
		return plottingPreferences;
	}
    		 
	public static ILog getPluginLog() {
		return staticActivator.getLog();
	}

	/**
	 * Used for transient local properties that are not designed to be read outside.
	 * @return
	 */
	public static IPreferenceStore getLocalPreferenceStore() {
		return staticActivator.getPreferenceStore();
	}
	

	public static Object getService(Class<?> serviceClass) {
		ServiceReference<?> ref = staticActivator.getBundle().getBundleContext().getServiceReference(serviceClass);
		return staticActivator.getBundle().getBundleContext().getService(ref);
	}

}
