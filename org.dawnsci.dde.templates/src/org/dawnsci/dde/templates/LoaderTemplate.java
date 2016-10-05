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
package org.dawnsci.dde.templates;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelFactory;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.ui.templates.PluginReference;

/**
 * This type is used to set parameters for the "Loader" extension point. A
 * wizard page will be generated for the user to set values. Obtained values are
 * inserted into "plugin.xml" when this template's wizard is executing and also
 * used when code and other files are generated.
 * 
 * @author Torkild U. Resheim
 * 
 * @see org.eclipse.dawnsci.analysis.api.io.IFileLoader
 * @see org.eclipse.dawnsci.analysis.api.metadata.IMetaLoader
 */
public class LoaderTemplate extends DAWNTemplateSection {

	private static final String EXTENSION_POINT = "uk.ac.diamond.scisoft.analysis.io.loader";
	private static final String KEY_EXTENSIONS = "extensions";
	private static final String KEY_HIGH_PRIORITY = "highPriority";

	@Override
	protected String getClassName() {
		return "Loader";
	}

	public IPluginReference[] getDependencies(String schemaVersion) {
		// add _all_ required dependencies, no particular version 
		return new IPluginReference[] {
				new PluginReference("org.eclipse.january", null, 0),
				new PluginReference("org.eclipse.dawnsci.analysis.api", null, 0),
				new PluginReference("uk.ac.diamond.scisoft.analysis", null, 0),
				new PluginReference("org.eclipse.core.runtime", null, 0),
				new PluginReference("org.eclipse.core.resources", null, 0),
				};
	}

	@Override
	public String[] getNewFiles() {
		return null;
	}

	@Override
	protected String getPageDescription() {
		return "Please specify parameters for the new file loader implementation";
	}

	@Override
	protected String getPageTitle() {
		return "File Loader Extension";
	}

	@Override
	public String getSectionId() {
		return "loader";
	}
	@Override
	public String getUsedExtensionPoint() {
		return EXTENSION_POINT;
	}

	@Override
	protected void setOptions() {
		// add all the options we need and set default values
		addOption(KEY_PACKAGE_NAME, "Java package name", (String)null, 0);
		addOption(KEY_CLASS_NAME, "Java class name", (String)null, 0);
		addOption(KEY_EXTENSIONS, "File extensions", (String)null, 0);
		addOption(KEY_HIGH_PRIORITY, "High priority", Boolean.TRUE, 0);
	}

	@Override
	protected void updateModel(IProgressMonitor monitor) throws CoreException {
		IPluginBase plugin = model.getPluginBase();
		IPluginExtension extension = createExtension(EXTENSION_POINT, true);
		IPluginModelFactory factory = model.getPluginFactory();

		IPluginElement setElement = factory.createElement(extension);
		setElement.setName("loader");
		setElement.setAttribute("class", getStringOption(KEY_PACKAGE_NAME) + "." + getStringOption(KEY_CLASS_NAME));
		setElement.setAttribute("file_extension", getStringOption(KEY_EXTENSIONS));
		setElement.setAttribute("high_priority", Boolean.toString(getBooleanOption(KEY_HIGH_PRIORITY)));

		extension.add(setElement);
		if (!extension.isInTheModel()) {
			plugin.add(extension);
		}
	}

}
