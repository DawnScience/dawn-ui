/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system.preference;

import org.dawnsci.plotting.system.PlottingSystemActivator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
import org.eclipse.jface.preference.IPreferenceStore;

public class ToolbarConfigurationInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = PlottingSystemActivator.getLocalPreferenceStore();
		
		for (ToolbarConfigurationConstants constant : ToolbarConfigurationConstants.values()) {
			store.setDefault(constant.getId(),        constant.isVis());
		}

	}

}
