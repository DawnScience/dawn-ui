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

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.templates.OptionTemplateSection;
import org.osgi.framework.Bundle;
/**
 * This type is the abstract superclass for the DAWN extension templates. 
 */
public abstract class DAWNTemplateSection extends OptionTemplateSection {

	protected static final String BUNDLE_ID = "org.dawnsci.dde.templates";
	protected static final String KEY_CLASS_NAME = "className";
	protected static final String KEY_EXTENSION_NAME = "extensionName";
	protected static final String KEY_EXTENSION_ID = "extensionId";

	public DAWNTemplateSection(){
		setPageCount(1);
		setOptions();
	}
	@Override

	public void addPages(Wizard wizard) {
		// create one wizard page for the options
		WizardPage p1 = createPage(0);
		p1.setTitle(getPageTitle());
		p1.setDescription(getPageDescription());
		wizard.addPage(p1);
		markPagesAdded();
	}

	protected abstract String getPageDescription();
	
	protected abstract String getPageTitle();
		
	/**
	 * Implement to initially create all options and set their default values.
	 */
	protected abstract void setOptions();

	protected ResourceBundle getPluginResourceBundle() {
		Bundle bundle = Platform.getBundle(BUNDLE_ID);
		return Platform.getResourceBundle(bundle);
	}

	protected URL getInstallURL() {
		Bundle bundle = Platform.getBundle(BUNDLE_ID);
		return bundle.getEntry("/");
	}

	protected String getFormattedPackageName(String id) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < id.length(); i++) {
			char ch = id.charAt(i);
			if (buffer.length() == 0) {
				if (Character.isJavaIdentifierStart(ch))
					buffer.append(Character.toLowerCase(ch));
			} else {
				if (Character.isJavaIdentifierPart(ch) || ch == '.')
					buffer.append(ch);
			}
		}
		return buffer.toString().toLowerCase(Locale.ENGLISH);
	}

	protected void initializeFields(IFieldData data) {
		// in a new project wizard, we don't know this yet - the
		// model has not been created
		String id = data.getId();
		String packageName = getFormattedPackageName(id);
		initializeOption(KEY_PACKAGE_NAME, packageName);
		initializeOption(KEY_CLASS_NAME, getClassName());
	}

	public void initializeFields(IPluginModelBase model) {
		String packageName = getFormattedPackageName(model.toString());
		initializeOption(KEY_PACKAGE_NAME, packageName);
		initializeOption(KEY_CLASS_NAME, getClassName());
	}

	@Override
	public boolean isDependentOnParentWizard() {
		return true;
	}
	
	protected abstract String getClassName();
	/**
	 * Returns an array of key values suitable for use in a template option.
	 * 
	 * @param extensionPoint
	 *            the extension point to look up
	 * @param name
	 *            the name of the element within the extension point
	 * @param id
	 *            the name of the identifier attribute
	 * @param label
	 *            the name of the label attribute
	 * @return an array of key/values
	 */
	protected String[][] getLookupList(String extensionPoint, String name, String id, String label) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(extensionPoint);
		Map<String, String> map = new HashMap<>();
		IConfigurationElement[] configurationElements = point.getConfigurationElements();
		for (IConfigurationElement e : configurationElements) {
			if (e.getName().equals(name)) {
				map.put(e.getAttribute(label), e.getAttribute(id));
			}
		}
		String[][] options = new String[map.size()][];
		int i = 0;
		for (String k : map.keySet()) {
			options[i++] = new String[] { map.get(k), k };
		}
		return options;
	}

}
