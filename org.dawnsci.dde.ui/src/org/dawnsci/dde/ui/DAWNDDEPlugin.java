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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class DAWNDDEPlugin extends AbstractUIPlugin {

	public static final String WIZARD_BANNER = "WIZARD_BANNER";

	private static DAWNDDEPlugin plugin;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
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
		reg.put(WIZARD_BANNER, imageDescriptorFromPlugin("org.dawnsci.dde.io", "icons/wizban/project_wiz.gif"));
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

}
