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
 * This type is used to set parameters for the "ToolPageAction" extension point. A
 * wizard page will be generated for the user to set values. Obtained values are
 * inserted into "plugin.xml" when this template's wizard is executing and also
 * used when code and other files are generated.
 * 
 * @author Torkild U. Resheim
 */
public class ToolPageActionTemplate extends DAWNTemplateSection {

	private static final String EXTENSION_POINT = "org.eclipse.dawnsci.plotting.api.toolPageAction";
	private static final String KEY_TOOL_IDENTIFIER = "tool_identifier";
	private static final String KEY_COMMAND_IDENTIFIER = "command_identifier";
	private static final String KEY_ACTION_TYPE = "action_type";

	@Override
	protected String getClassName() {
		return "ToolPageAction";
	}

	public IPluginReference[] getDependencies(String schemaVersion) {
		// add _all_ required dependencies, no particular version
		return new IPluginReference[] {
				new PluginReference("org.eclipse.january", null, 0),
				new PluginReference("org.eclipse.dawnsci.plotting.api", null, 0),
				new PluginReference("org.eclipse.ui", null, 0),
				new PluginReference("org.eclipse.core.runtime", null, 0),
				new PluginReference("org.eclipse.core.resources", null, 0), };
	}

	@Override
	public String[] getNewFiles() {
		return new String[0];
	}

	protected String getPageDescription() {
		return "Please specify parameters for the new tool page action extension.";
	}

	protected String getPageTitle() {
		return "Tool Page Action Extension";
	}

	@Override
	public String getSectionId() {
		return "toolPageAction";
	}

	@Override
	public String getUsedExtensionPoint() {
		return EXTENSION_POINT;
	}

	protected void setOptions() {
		// add all the options we need and set default values
		addOption(KEY_EXTENSION_ID, "Action identifier", (String) null, 0);
		addOption(KEY_TOOL_IDENTIFIER, "Tool page identifier",
				getLookupList("org.eclipse.dawnsci.plotting.api.toolPage", "plotting_tool_page", "id", "label", false),
				(String) null, 0); // lookup
		addOption(KEY_COMMAND_IDENTIFIER, "Command identifier",
				getLookupList("org.eclipse.ui.commands", "command", "id", "name", false), (String) null, 0);
		addOption(KEY_EXTENSION_NAME, "Label", (String) null, 0);
		addOption(KEY_ACTION_TYPE, "Action type", new String[][] { { "TOOLBAR", "Toolbar" }, { "MENUBAR", "Menubar" } },
				"TOOLBAR", 0);
	}

	@Override
	protected void updateModel(IProgressMonitor monitor) throws CoreException {
		IPluginBase plugin = model.getPluginBase();
		IPluginExtension extension = createExtension(EXTENSION_POINT, true);
		IPluginModelFactory factory = model.getPluginFactory();

		IPluginElement setElement = factory.createElement(extension);
		setElement.setName("tool_page_action");
		setElement.setAttribute("id", getStringOption(KEY_EXTENSION_ID));
		setElement.setAttribute("tool_id", getStringOption(KEY_TOOL_IDENTIFIER));
		setElement.setAttribute("command_id", getStringOption(KEY_COMMAND_IDENTIFIER));
		setElement.setAttribute("icon", "icons/icon.png");
		setElement.setAttribute("label", getStringOption(KEY_EXTENSION_NAME));
		setElement.setAttribute("action_type", getStringOption(KEY_ACTION_TYPE));

		extension.add(setElement);
		if (!extension.isInTheModel()) {
			plugin.add(extension);
		}
	}

}
