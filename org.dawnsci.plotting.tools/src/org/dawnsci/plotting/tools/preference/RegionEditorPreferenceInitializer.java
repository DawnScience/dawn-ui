/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.preference;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class RegionEditorPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		store.setDefault(RegionEditorConstants.MOBILE_REGION_SETTING, true);
		store.setDefault(RegionEditorConstants.POINT_FORMAT,     "##0");
		store.setDefault(RegionEditorConstants.ANGLE_FORMAT,     "##0");
		store.setDefault(RegionEditorConstants.INTENSITY_FORMAT, "0.######E0#");
		store.setDefault(RegionEditorConstants.SUM_FORMAT,       "0.######E0#");
	}
}
