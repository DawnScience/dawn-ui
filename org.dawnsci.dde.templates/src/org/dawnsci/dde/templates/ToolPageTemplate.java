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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
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
 * <p>
 * This implementations allows the user to create a new
 * <i>plotting_tool_page</i> only, a category for the page is selected in the
 * wizard and must already exist.
 * </p>
 */
public class ToolPageTemplate extends DAWNTemplateSection {

	private static final String CLASS_NAME = "ToolPage";
	private static final String EXTENSION_POINT = "org.eclipse.dawnsci.plotting.api.toolPage";
	private static final String KEY_IDENTIFIER = "identifier";
	private static final String KEY_TOOLTIP = "tooltip";
	private static final String KEY_LABEL = "label";
	private static final String KEY_CHEAT_SHEET_ID = "cheat_sheet_id";
	private static final String KEY_CATEGORY = "category";

	@Override
	protected String getClassName() {
		return CLASS_NAME;
	}

	public IPluginReference[] getDependencies(String schemaVersion) {
		// add _all_ required dependencies, no particular version
		return new IPluginReference[] { 
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
		return "Please specify parameters for the new tool page extension.";
	}

	protected String getPageTitle() {
		return "Tool Page Extension";
	}

	@Override
	public String getSectionId() {
		return "toolPage";
	}

	@Override
	public String getUsedExtensionPoint() {
		return EXTENSION_POINT;
	}

	protected void setOptions() {
		// add all the options we need and set default values
		addOption(KEY_PACKAGE_NAME, "Java package name", (String) null, 0);
		addOption(KEY_CLASS_NAME, "Java class name", (String) null, 0);
		addOption(KEY_IDENTIFIER, "Page identifier", (String) null, 0);
		addOption(KEY_TOOLTIP, "Tooltip", (String) null, 0);
		addOption(KEY_LABEL, "Label", (String) null, 0);
		addOption(KEY_CHEAT_SHEET_ID, "Cheat sheet identifier", (String) null, 0);
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT);

		Map<String, String> map = new HashMap<>();

		IConfigurationElement[] configurationElements = point.getConfigurationElements();
		for (IConfigurationElement e : configurationElements) {
			if (e.getName().equals("plotting_tool_category")) {
				map.put(e.getAttribute("label"), e.getAttribute("id"));
			}
		}
		String[][] options = new String[map.size()][];
		int i = 0;
		for (String k : map.keySet()) {
			options[i++] = new String[] { map.get(k), k };
		}
		addOption(KEY_CATEGORY, "Category", options, (String) null, 0);
	}

	@Override
	protected void updateModel(IProgressMonitor monitor) throws CoreException {
		IPluginBase plugin = model.getPluginBase();
		IPluginExtension extension = createExtension(EXTENSION_POINT, true);
		IPluginModelFactory factory = model.getPluginFactory();

		IPluginElement setElement = factory.createElement(extension);
		setElement.setName("plotting_tool_page");
		setElement.setAttribute("class", getStringOption(KEY_PACKAGE_NAME) + "." + getStringOption(KEY_CLASS_NAME));
		setElement.setAttribute("id", getStringOption(KEY_IDENTIFIER));
		setElement.setAttribute("icon", "icons/default.gif");
		setElement.setAttribute("tooltip", getStringOption(KEY_TOOLTIP));
		setElement.setAttribute("label", getStringOption(KEY_LABEL));
		setElement.setAttribute("cheat_sheet_id", getStringOption(KEY_CHEAT_SHEET_ID));
		setElement.setAttribute("visible", "true");
		setElement.setAttribute("category", getStringOption(KEY_CATEGORY));

		extension.add(setElement);
		if (!extension.isInTheModel()) {
			plugin.add(extension);
		}
	}

}
