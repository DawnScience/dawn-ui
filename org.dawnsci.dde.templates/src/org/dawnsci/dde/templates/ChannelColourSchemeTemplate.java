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
 * This type is used to set parameters for the "ToolPage" extension point. A
 * wizard page will be generated for the user to set values. Obtained values are
 * inserted into "plugin.xml" when this template's wizard is executing and also
 * used when code and other files are generated.
 * 
 * @author Torkild U. Resheim
 */
public class ChannelColourSchemeTemplate extends DAWNTemplateSection {

	private static final String EXTENSION_POINT = "org.dawnsci.plotting.histogram.channelColourScheme";

	@Override
	protected String getClassName() {
		return "SingleChannelColourScheme";
	}

	public IPluginReference[] getDependencies(String schemaVersion) {
		// add _all_ required dependencies, no particular version
		return new IPluginReference[] { 
				new PluginReference("org.dawnsci.plotting.histogram", null, 0),
				new PluginReference("org.eclipse.dawnsci.plotting.api", null, 0) };
	}

	@Override
	public String[] getNewFiles() {
		return new String[0];
	}

	protected String getPageDescription() {
		return "Please specify parameters for the new colour scheme extension.";
	}

	protected String getPageTitle() {
		return "Single Channel Colour Scheme Extension";
	}

	@Override
	public String getSectionId() {
		return "channelColourScheme";
	}

	@Override
	public String getUsedExtensionPoint() {
		return EXTENSION_POINT;
	}

	protected void setOptions() {
		// add all the options we need and set default values
		addOption(KEY_EXTENSION_ID, "Identifier", (String) null, 0);
		addOption(KEY_EXTENSION_NAME, "Name", (String) null, 0);
		addOption(KEY_PACKAGE_NAME, "Java package name", (String) null, 0);
		addOption(KEY_CLASS_NAME, "Java class name", (String) null, 0);
	}

	@Override
	protected void updateModel(IProgressMonitor monitor) throws CoreException {
		IPluginBase plugin = model.getPluginBase();
		IPluginExtension extension = createExtension(EXTENSION_POINT, true);
		IPluginModelFactory factory = model.getPluginFactory();

		IPluginElement setElement = factory.createElement(extension);
		setElement.setName("transfer_function");
		setElement.setAttribute("array_provider_class", getStringOption(KEY_PACKAGE_NAME) + "." + getStringOption(KEY_CLASS_NAME));
		setElement.setAttribute("id", getStringOption(KEY_EXTENSION_ID));
		setElement.setAttribute("name", getStringOption(KEY_EXTENSION_NAME));

		extension.add(setElement);
		if (!extension.isInTheModel()) {
			plugin.add(extension);
		}
	}

}
