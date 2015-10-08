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
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.templates.OptionTemplateSection;
import org.osgi.framework.Bundle;

public abstract class DAWNTemplateSection extends OptionTemplateSection {

	protected static final String BUNDLE_ID = "org.dawnsci.dde.templates";
	protected static final String KEY_CLASS_NAME = "className";

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

}
