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
 * This type is used to set parameters for the "Operation" extension point. A
 * wizard page will be generated for the user to set values. Obtained values are
 * inserted into "plugin.xml" when this template's wizard is executing and also
 * used when code and other files are generated.
 * 
 * @author Torkild U. Resheim
 */
public class OperationTemplate extends DAWNTemplateSection {

	private static final String EXTENSION_POINT = "org.eclipse.dawnsci.analysis.api.operation";
	private static final String KEY_DESCRIPTION = "description";
	
	@Override
	protected String getClassName() {
		return "Operation";
	}
		
	public IPluginReference[] getDependencies(String schemaVersion) {
		// add _all_ required dependencies, no particular version 
		return new IPluginReference[] {
				new PluginReference("org.eclipse.january", null, 0),
				new PluginReference("org.eclipse.dawnsci.analysis.api", null, 0),
				new PluginReference("uk.ac.diamond.scisoft.analysis.processing", null, 0),
				new PluginReference("uk.ac.diamond.scisoft.analysis", null, 0),
				new PluginReference("org.eclipse.core.runtime", null, 0),
				new PluginReference("org.eclipse.core.resources", null, 0),
				};
	}

	@Override
	public String[] getNewFiles() {
		return new String[0];
	}

	protected String getPageDescription() {
		return "Please specify parameters for the new operation extension.";
	}

	protected String getPageTitle() {
		return "Operation Extension";
	}

	@Override
	public String getSectionId() {
		return "operation";
	}

	@Override
	public String getUsedExtensionPoint() {
		return EXTENSION_POINT;
	}

	protected void setOptions() {
		// add all the options we need and set default values
		addOption(KEY_EXTENSION_ID, "Operation identifier", (String)null, 0);
		addOption(KEY_EXTENSION_NAME, "Operation name", (String)null, 0);
		addOption(KEY_PACKAGE_NAME, "Java package name", (String)null, 0);
		addOption(KEY_CLASS_NAME, "Java class name", (String)null, 0);
		addOption(KEY_DESCRIPTION, "Operation description", (String)null, 0);
	}

	@Override
	protected void updateModel(IProgressMonitor monitor) throws CoreException {
		IPluginBase plugin = model.getPluginBase();
		IPluginExtension extension = createExtension(EXTENSION_POINT, true);
		IPluginModelFactory factory = model.getPluginFactory();

		IPluginElement setElement = factory.createElement(extension);
		setElement.setName("operation");
		setElement.setAttribute("class", getStringOption(KEY_PACKAGE_NAME) + "." + getStringOption(KEY_CLASS_NAME));
		setElement.setAttribute(KEY_DESCRIPTION, getStringOption(KEY_DESCRIPTION));
		setElement.setAttribute("id", getStringOption(KEY_EXTENSION_ID));
		setElement.setAttribute("name", getStringOption(KEY_EXTENSION_NAME));
		setElement.setAttribute("visible", "true");

		extension.add(setElement);
		if (!extension.isInTheModel()) {
			plugin.add(extension);
		}
	}

}
