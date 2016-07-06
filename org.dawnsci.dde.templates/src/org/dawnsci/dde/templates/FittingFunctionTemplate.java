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
 * This type is used to set parameters for the "Function" extension point. A
 * wizard page will be generated for the user to set values. Obtained values are
 * inserted into "plugin.xml" when this template's wizard is executing and also
 * used when code and other files are generated. 
 * 
 * @author Torkild U. Resheim
 * @see uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction
 * @see org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction
 */
public class FittingFunctionTemplate extends DAWNTemplateSection {

	private static final String EXTENSION_POINT = "uk.ac.diamond.scisoft.analysis.fitting.function";
	private static final String KEY_USECASE_1 = "usecase1";
	private static final String KEY_USECASE_2 = "usecase2";
	private static final String KEY_USECASE_3 = "usecase3";
	private static final String KEY_USECASE_4 = "usecase4";
	private static final String KEY_USECASE_5 = "usecase5";
	
	@Override
	protected String getClassName() {
		return "FittingFunction";
	}
		
	public IPluginReference[] getDependencies(String schemaVersion) {
		// add _all_ required dependencies, no particular version 
		return new IPluginReference[] {
				new PluginReference("org.eclipse.january", null, 0),
				new PluginReference("org.eclipse.dawnsci.analysis.api", null, 0),
				new PluginReference("org.eclipse.dawnsci.analysis.dataset", null, 0),
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
		return "Please specify parameters for the new fitting function extension.";
	}

	protected String getPageTitle() {
		return "Fitting Function Extension";
	}

	@Override
	public String getSectionId() {
		return "fittingFunction";
	}

	@Override
	public String getUsedExtensionPoint() {
		return EXTENSION_POINT;
	}

	protected void setOptions() {
		// add all the options we need and set default values
		addOption(KEY_EXTENSION_ID, "Fitting function identifier", (String)null, 0);
		addOption(KEY_EXTENSION_NAME, "Fitting function name", (String)null, 0);
		addOption(KEY_PACKAGE_NAME, "Java package name", (String)null, 0);
		addOption(KEY_CLASS_NAME, "Java class name", (String)null, 0);
		addOption(KEY_USECASE_1, "Usecase 1",getLookupList(EXTENSION_POINT, "usecase", "id", "name", true), (String)null, 0);
		addOption(KEY_USECASE_2, "Usecase 2",getLookupList(EXTENSION_POINT, "usecase", "id", "name", true), (String)null, 0);
		addOption(KEY_USECASE_3, "Usecase 3",getLookupList(EXTENSION_POINT, "usecase", "id", "name", true), (String)null, 0);
		addOption(KEY_USECASE_4, "Usecase 4",getLookupList(EXTENSION_POINT, "usecase", "id", "name", true), (String)null, 0);
		addOption(KEY_USECASE_5, "Usecase 5",getLookupList(EXTENSION_POINT, "usecase", "id", "name", true), (String)null, 0);
	}

	@Override
	protected void updateModel(IProgressMonitor monitor) throws CoreException {
		IPluginBase plugin = model.getPluginBase();
		IPluginExtension extension = createExtension(EXTENSION_POINT, true);
		IPluginModelFactory factory = model.getPluginFactory();

		IPluginElement setElement = factory.createElement(extension);
		setElement.setName("operation");
		setElement.setAttribute("class", getStringOption(KEY_PACKAGE_NAME) + "." + getStringOption(KEY_CLASS_NAME));
		setElement.setAttribute("id", getStringOption(KEY_EXTENSION_ID));
		setElement.setAttribute("name", getStringOption(KEY_EXTENSION_NAME));
		if (getStringOption(KEY_USECASE_1)!=null){
			setElement.setAttribute("usecase1", getStringOption(KEY_USECASE_1));
		}
		if (getStringOption(KEY_USECASE_2)!=null){
			setElement.setAttribute("usecase2", getStringOption(KEY_USECASE_2));
		}
		if (getStringOption(KEY_USECASE_3)!=null){
			setElement.setAttribute("usecase3", getStringOption(KEY_USECASE_3));
		}
		if (getStringOption(KEY_USECASE_4)!=null){
			setElement.setAttribute("usecase4", getStringOption(KEY_USECASE_4));
		}
		if (getStringOption(KEY_USECASE_5)!=null){
			setElement.setAttribute("usecase5", getStringOption(KEY_USECASE_5));
		}

		extension.add(setElement);
		if (!extension.isInTheModel()) {
			plugin.add(extension);
		}
	}

}
