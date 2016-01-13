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

/**
 * This type is used to set parameters for the "PlottingAction" extension point. A
 * wizard page will be generated for the user to set values. Obtained values are
 * inserted into "plugin.xml" when this template's wizard is executing and also
 * used when code and other files are generated.
 * 
 * @author Torkild U. Resheim
 */
public class PlottingActionTemplate extends DAWNTemplateSection {

	private static final String EXTENSION_POINT = "org.eclipse.dawnsci.plotting.api.plottingAction";
	private static final String KEY_PLOT_NAME = "plot_name";
	private static final String KEY_COMMAND_ID= "command_id";
	private static final String KEY_ACTION_TYPE = "action_type";
	private static final String KEY_PLOT_TYPE = "plot_type";

	@Override
	protected String getClassName() {
		return "PlottingAction";
	}

	public IPluginReference[] getDependencies(String schemaVersion) {
		// add _all_ required dependencies, no particular version
		return new IPluginReference[] {};
	}

	@Override
	public String[] getNewFiles() {
		return new String[0];
	}

	protected String getPageDescription() {
		return "Please specify parameters for the new plotting action extension.";
	}

	protected String getPageTitle() {
		return "Plotting Action Extension";
	}

	@Override
	public String getSectionId() {
		return "plottingAction";
	}

	@Override
	public String getUsedExtensionPoint() {
		return EXTENSION_POINT;
	}

	protected void setOptions() {
		// add all the options we need and set default values
		addOption(KEY_EXTENSION_ID, "Action identifier", (String) null, 0);
		addOption(KEY_EXTENSION_NAME, "Label", (String) null, 0);
		addOption(KEY_COMMAND_ID, "Command identifier",
				getLookupList("org.eclipse.ui.commands", "command", "id", "name", false), (String) null, 0);
		addOption(KEY_ACTION_TYPE, "Action type", new String[][] { { "TOOLBAR", "Toolbar" }, { "MENUBAR", "Menubar" } },
				"TOOLBAR", 0);
		addOption(KEY_PLOT_TYPE, "Plot type",
				new String[][] { { "XY", "XY" }, { "IMAGE", "Image" }, { "SURFACE", "Surface" } }, (String) null, 0);
	}

	@Override
	protected void updateModel(IProgressMonitor monitor) throws CoreException {
		IPluginBase plugin = model.getPluginBase();
		IPluginExtension extension = createExtension(EXTENSION_POINT, true);
		IPluginModelFactory factory = model.getPluginFactory();

		IPluginElement setElement = factory.createElement(extension);
		setElement.setName("plotting_action");
		setElement.setAttribute("id", getStringOption(KEY_EXTENSION_ID));
		setElement.setAttribute("plot_name", getStringOption(KEY_PLOT_NAME));
		setElement.setAttribute("command_id", getStringOption(KEY_COMMAND_ID));
		setElement.setAttribute("icon", "icons/icon.png");
		setElement.setAttribute("label", getStringOption(KEY_EXTENSION_NAME));
		setElement.setAttribute("action_type", getStringOption(KEY_ACTION_TYPE));
		setElement.setAttribute("plot_type", getStringOption(KEY_PLOT_TYPE));

		extension.add(setElement);
		if (!extension.isInTheModel()) {
			plugin.add(extension);
		}
	}

}
