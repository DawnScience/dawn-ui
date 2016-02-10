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
 * This type is used to set parameters for the "ColourScheme" extension point. A
 * wizard page will be generated for the user to set values. Obtained values are
 * inserted into "plugin.xml" when this template's wizard is executing and also
 * used when code and other files are generated.
 * 
 * @author Torkild U. Resheim
 * @author Baha El Kassaby
 */
public class ColourSchemeTemplate extends DAWNTemplateSection {

	private static final String EXTENSION_POINT = "org.dawnsci.plotting.histogram.colourScheme";
	private static final String TRANSFER_FUNCTION_EXTENSION = "org.dawnsci.plotting.histogram.channelColourScheme";
	private static final String COLOUR_CATEGORY_EXTENSION = "org.dawnsci.plotting.histogram.colourCategory";
	private static final String KEY_RED_TRANSFER_FUNCTION = "red_transfer_function";
	private static final String KEY_GREEN_TRANSFER_FUNCTION = "green_transfer_function";
	private static final String KEY_BLUE_TRANSFER_FUNCTION = "blue_transfer_function";
	private static final String KEY_ALPHA_TRANSFER_FUNCTION = "alpha_transfer_function";
	private static final String KEY_RED_INVERTED = "red_inverted";
	private static final String KEY_BLUE_INVERTED = "blue_inverted";
	private static final String KEY_GREEN_INVERTED = "green_inverted";
	private static final String KEY_ALPHA_INVERTED = "alpha_inverted";
	private static final String KEY_CATEGORY = "colour_category";

	@Override
	protected String getClassName() {
		return "ColourScheme"; // not used in this extension point
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
		return "Please specify parameters for the new colour scheme extension.";
	}

	protected String getPageTitle() {
		return "Colour Scheme Extension";
	}

	@Override
	public String getSectionId() {
		return "colourScheme";
	}

	@Override
	public String getUsedExtensionPoint() {
		return EXTENSION_POINT;
	}

	protected void setOptions() {
		// add all the options we need and set default values
		addOption(KEY_EXTENSION_ID, "Identifier", (String) null, 0);
		addOption(KEY_EXTENSION_NAME, "Name", (String) null, 0);
		addOption(KEY_RED_TRANSFER_FUNCTION, "Red transfer function",
				getLookupList(TRANSFER_FUNCTION_EXTENSION, "transfer_function", "id", "name", false), 
				(String) null, 0);
		addOption(KEY_GREEN_TRANSFER_FUNCTION, "Green transfer function",
				getLookupList(TRANSFER_FUNCTION_EXTENSION, "transfer_function", "id", "name", false), 
				(String) null, 0);
		addOption(KEY_BLUE_TRANSFER_FUNCTION, "Blue transfer function",
				getLookupList(TRANSFER_FUNCTION_EXTENSION, "transfer_function", "id", "name", false), 
				(String) null, 0);
		addOption(KEY_ALPHA_TRANSFER_FUNCTION, "Alpha transfer function",
				getLookupList(TRANSFER_FUNCTION_EXTENSION, "transfer_function", "id", "name", false), 
				(String) null, 0);
		addOption(KEY_CATEGORY, "Colormap category",
				getLookupList(COLOUR_CATEGORY_EXTENSION, "colour_category", "id", "name", false), (String) null, 0);
		addOption(KEY_RED_INVERTED, "Red inverted", Boolean.FALSE, 0);
		addOption(KEY_BLUE_INVERTED, "Blue inverted", Boolean.FALSE, 0);
		addOption(KEY_GREEN_INVERTED, "Green inverted", Boolean.FALSE, 0);
		addOption(KEY_ALPHA_INVERTED, "Alpha inverted", Boolean.FALSE, 0);
	}

	@Override
	protected void updateModel(IProgressMonitor monitor) throws CoreException {
		IPluginBase plugin = model.getPluginBase();
		IPluginExtension extension = createExtension(EXTENSION_POINT, true);
		IPluginModelFactory factory = model.getPluginFactory();

		IPluginElement setElement = factory.createElement(extension);
		setElement.setName("colour_scheme");
		setElement.setAttribute("id", getStringOption(KEY_EXTENSION_ID));
		setElement.setAttribute("name", getStringOption(KEY_EXTENSION_NAME));
		setElement.setAttribute("red_transfer_function", getStringOption(KEY_RED_TRANSFER_FUNCTION));
		setElement.setAttribute("green_transfer_function", getStringOption(KEY_GREEN_TRANSFER_FUNCTION));
		setElement.setAttribute("blue_transfer_function", getStringOption(KEY_BLUE_TRANSFER_FUNCTION));
		setElement.setAttribute("alpha_transfer_function", getStringOption(KEY_ALPHA_TRANSFER_FUNCTION));
		setElement.setAttribute(KEY_CATEGORY, getStringOption(KEY_CATEGORY));
		setElement.setAttribute(KEY_RED_INVERTED, Boolean.toString(getBooleanOption(KEY_RED_INVERTED)));
		setElement.setAttribute(KEY_GREEN_INVERTED, Boolean.toString(getBooleanOption(KEY_GREEN_INVERTED)));
		setElement.setAttribute(KEY_BLUE_INVERTED, Boolean.toString(getBooleanOption(KEY_GREEN_INVERTED)));
		setElement.setAttribute(KEY_ALPHA_INVERTED, Boolean.toString(getBooleanOption(KEY_ALPHA_INVERTED)));
		extension.add(setElement);
		if (!extension.isInTheModel()) {
			plugin.add(extension);
		}
	}

}
