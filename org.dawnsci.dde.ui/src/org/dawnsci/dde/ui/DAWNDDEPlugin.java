/*-
 *******************************************************************************
 * Copyright (c) 2015 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package org.dawnsci.dde.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class DAWNDDEPlugin extends AbstractUIPlugin {

	private static final String LAUNCH_CONFIG_NAME = "Default DAWN Application";
	private static final String DAWN_PRODUCT_ID = "org.dawnsci.product.plugin.DAWN";
	public static final String WIZARD_BANNER = "WIZARD_BANNER";
	public static final String LAUNCH_CONFIG_ID = "org.dawnsci.dde.core.launchConfigurationType";

	private static DAWNDDEPlugin plugin;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		createLaunchConfiguration();
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static DAWNDDEPlugin getDefault() {
		return plugin;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		reg.put(WIZARD_BANNER, imageDescriptorFromPlugin("org.dawnsci.dde.ui", "icons/wizban/project_wiz.gif"));
	}

	/**
	 * Returns <code>true</code> if the given identifier matches one of those
	 * that should be supported by the DAWN extension project wizard.
	 * 
	 * @param id
	 *            the extension point identifier
	 * @return <code>true</code> if supported
	 */
	public static boolean isSupportedDAWNExtension(@NonNull String id) {
		if (!id.startsWith("org.dawnsci") && !id.startsWith("org.eclipse.dawnsci") && !id.startsWith("uk.ac.diamond")) {
			return false;
		}
		if (id.contains(".analysis.rpc.") || id.contains(".sda.")) {
			return false;
		}
		return true;
	}
	
	public static void createLaunchConfiguration() throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(LAUNCH_CONFIG_ID);
		ILaunchConfiguration[] lcs = manager.getLaunchConfigurations(type);
		boolean found = false;
		for (int i = 0; i < lcs.length; ++i) {
			if (lcs[i].getName().equals(LAUNCH_CONFIG_NAME)) {
				found = true;
			}
		}
		if (!found){
			ILaunchConfigurationWorkingCopy newInstance = type.newInstance(null, LAUNCH_CONFIG_NAME);
			newInstance.setAttribute(IPDELauncherConstants.USE_PRODUCT, true);
			newInstance.setAttribute(IPDELauncherConstants.PRODUCT, DAWN_PRODUCT_ID);
			// Set this argument so that javafx classes are found at runtime
			newInstance.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Dosgi.framework.extensions=org.eclipse.fx.osgi");
			newInstance.doSave();
		}
	}
}
